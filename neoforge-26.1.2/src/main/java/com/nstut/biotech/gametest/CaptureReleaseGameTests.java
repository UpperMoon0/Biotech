package com.nstut.biotech.gametest;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.NetTrapBlock;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.items.MobItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/** Exercises the real trap/drop/item-use pipeline, rather than reconstruction helpers alone. */
public final class CaptureReleaseGameTests {
    private static final BlockPos TRAP = new BlockPos(1, 1, 1);
    private static final BlockPos RELEASE_SUPPORT = new BlockPos(3, 0, 1);
    private static final BlockPos HOLDER = new BlockPos(1, 1, 3);
    private static final String LEASH_KEY = "leash";
    private static final String FALL_DISTANCE_KEY = "fall_distance";

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, Biotech.MOD_ID);
    private static final List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> TESTS = List.of(
            register("net_trap_releases_named_colored_sheep", CaptureReleaseGameTests::netTrapReleasesNamedColoredSheep),
            register("net_trap_releases_named_variant_rabbit", CaptureReleaseGameTests::netTrapReleasesNamedVariantRabbit),
            register("net_trap_releases_generic_horse_with_equipment", CaptureReleaseGameTests::netTrapReleasesGenericHorseWithEquipment),
            register("net_trap_strips_serialized_entity_holder_leash", CaptureReleaseGameTests::netTrapStripsSerializedEntityHolderLeash),
            register("net_trap_strips_serialized_fence_leash", CaptureReleaseGameTests::netTrapStripsSerializedFenceLeash));

    private static DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> register(
            String name, Consumer<GameTestHelper> test) {
        return TEST_FUNCTIONS.register(name, () -> test);
    }

    public static void register(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "capture_release"));
        for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test : TESTS) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.getKey(),
                    new TestData<>(environment, Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "livestock"), 40, 0, true)));
        }
    }

    private CaptureReleaseGameTests() {
    }

    public static void netTrapReleasesNamedColoredSheep(GameTestHelper helper) {
        Sheep original = helper.spawn(EntityType.SHEEP, TRAP);
        prepare(original, "Rosie", -7200);
        original.setColor(DyeColor.MAGENTA);
        original.setSheared(true);
        original.setHealth(5.0F);

        Sheep released = (Sheep) captureAndRelease(helper, original, ItemRegistries.BABY_SHEEP.get());
        helper.assertTrue(released.getColor() == DyeColor.MAGENTA, "Capture/release must preserve sheep color");
        helper.assertTrue(released.isSheared(), "Capture/release must preserve sheared state");
        helper.succeed();
    }

    public static void netTrapReleasesNamedVariantRabbit(GameTestHelper helper) {
        Rabbit original = helper.spawn(EntityType.RABBIT, TRAP);
        // Load through vanilla's own format: modern rabbit variant setters are not public.
        CompoundTag state = save(original);
        state.putInt("RabbitType", 5);
        state.putInt("MoreCarrotTicks", 37);
        load(original, state);
        prepare(original, "Pepper", -3600);
        helper.assertTrue(original.getVariant() == Rabbit.Variant.SALT, "Fixture must be a salt rabbit");

        Rabbit released = (Rabbit) captureAndRelease(helper, original, ItemRegistries.BABY_RABBIT.get());
        helper.assertTrue(released.getVariant() == Rabbit.Variant.SALT, "Capture/release must preserve rabbit variant");
        helper.assertTrue(intValue(save(released), "MoreCarrotTicks") == 37,
                "Capture/release must preserve rabbit-specific persistent state");
        helper.succeed();
    }

    public static void netTrapReleasesGenericHorseWithEquipment(GameTestHelper helper) {
        Horse original = helper.spawn(EntityType.HORSE, TRAP);
        CompoundTag state = save(original);
        state.putInt("Variant", 3 | (2 << 8));
        load(original, state);
        prepare(original, "Copper", 1200);
        original.tameWithName(mockPlayer(helper));
        original.setTemper(73);
        equipHorse(helper, original);
        assertHorseEquipment(helper, original);
        CompoundTag originalState = save(original);
        helper.assertTrue(originalState.contains("Owner"), "Fixture must serialize horse ownership");

        Horse released = (Horse) captureAndRelease(helper, original, ItemRegistries.CAPTURED_ANIMAL.get());
        helper.assertTrue(released.getVariant() == original.getVariant()
                        && released.getMarkings() == original.getMarkings(),
                "Generic carrier must preserve horse variant and markings");
        helper.assertTrue(released.isTamed() && released.getTemper() == 73,
                "Generic carrier must preserve taming and temper");
        helper.assertTrue(originalState.get("Owner").equals(save(released).get("Owner")),
                "A new world identity must retain the horse's existing owner");
        assertHorseEquipment(helper, released);
        helper.succeed();
    }

    public static void netTrapStripsSerializedEntityHolderLeash(GameTestHelper helper) {
        Sheep original = helper.spawn(EntityType.SHEEP, TRAP);
        prepare(original, "Entity leash", 0);
        Entity holder = helper.spawn(EntityType.COW, HOLDER);
        original.setLeashedTo(holder, false);
        CompoundTag saved = save(original);
        helper.assertTrue(saved.get(LEASH_KEY) instanceof CompoundTag,
                "Vanilla must serialize the entity-holder form before capture");
        CompoundTag leash = (CompoundTag) saved.get(LEASH_KEY);
        helper.assertTrue(save(holder).get("UUID").equals(leash.get("UUID")),
                "Fixture leash must refer to the actual holder's UUID");

        Sheep released = (Sheep) captureAndRelease(helper, original, ItemRegistries.SHEEP.get());
        assertNoLeash(helper, released);
        helper.succeed();
    }

    public static void netTrapStripsSerializedFenceLeash(GameTestHelper helper) {
        Sheep original = helper.spawn(EntityType.SHEEP, TRAP);
        prepare(original, "Fence leash", 0);
        helper.setBlock(HOLDER, Blocks.OAK_FENCE);
        BlockPos fencePos = helper.absolutePos(HOLDER);
        LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(helper.getLevel(), fencePos);
        original.setLeashedTo(knot, false);
        CompoundTag saved = save(original);
        assertFenceLeash(helper, saved, fencePos);

        Sheep released = (Sheep) captureAndRelease(helper, original, ItemRegistries.SHEEP.get());
        assertNoLeash(helper, released);
        helper.succeed();
    }

    private static void prepare(Animal animal, String name, int age) {
        animal.setAge(age);
        animal.setCustomName(Component.literal(name));
        animal.setCustomNameVisible(true);
        animal.setNoAi(true);
        animal.setPersistenceRequired();
        animal.setDeltaMovement(0.75, 0.25, -0.5);
        animal.fallDistance = 19.5F;
    }

    private static Animal captureAndRelease(GameTestHelper helper, Animal original, Item expectedItem) {
        UUID originalId = original.getUUID();
        int originalAge = original.getAge();
        float originalHealth = original.getHealth();
        CompoundTag originalState = save(original);
        helper.assertTrue(originalState.contains("UUID") && originalState.contains("Pos")
                        && originalState.contains("Motion") && originalState.contains(FALL_DISTANCE_KEY),
                "Fixture must use actual vanilla serialization, including the target's fall-distance key");
        helper.assertTrue(original.fallDistance > 0, "Fixture must carry nonzero fall distance");

        helper.setBlock(TRAP, BlockRegistries.NET_TRAP.get());
        BlockPos trapPos = helper.absolutePos(TRAP);
        BlockState trapState = helper.getLevel().getBlockState(trapPos);
        helper.assertTrue(trapState.getBlock() instanceof NetTrapBlock, "Fixture must use the registered net trap");
        // Public block-state dispatch invokes NetTrapBlock.entityInside, including real tag eligibility.
        trapState.entityInside(helper.getLevel(), trapPos, original, InsideBlockEffectApplier.NOOP, true);
        helper.assertTrue(original.isRemoved(), "Successful capture must remove the original world entity");
        helper.assertTrue(helper.getLevel().getBlockState(trapPos).isAir(), "Successful capture must consume the trap");
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(trapPos).inflate(0.75), Entity::isAlive);
        helper.assertTrue(drops.size() == 1, "Capture must produce exactly one item entity");
        ItemStack captured = drops.get(0).getItem().copy();
        drops.get(0).discard();
        helper.assertTrue(captured.is(expectedItem) && captured.getCount() == 1,
                "Capture must preserve legacy item identity or select the generic carrier, with count one");
        helper.assertTrue(captured.getItem() instanceof MobItem || captured.getItem() instanceof CapturedAnimalItem,
                "Release must exercise a real captured-animal item");
        CompoundTag root = itemData(captured);
        helper.assertTrue(EntityType.getKey(original.getType()).toString().equals(stringValue(root, "EntityType")),
                "Captured item must record the original entity type");
        CompoundTag stored = capturedData(root);
        assertNoWorldState(helper, stored);
        helper.assertTrue(intValue(stored, "Age") == originalAge, "Captured item must preserve exact age");
        for (String key : List.of("CustomName", "CustomNameVisible", "Health", "NoAI", "PersistenceRequired")) {
            helper.assertTrue(originalState.get(key) != null && originalState.get(key).equals(stored.get(key)),
                    "Captured item must preserve persistent field " + key);
        }

        helper.setBlock(RELEASE_SUPPORT, Blocks.STONE);
        BlockPos clicked = helper.absolutePos(RELEASE_SUPPORT);
        Player player = mockPlayer(helper);
        helper.assertTrue(!player.isCreative() && !player.isSpectator(), "Release fixture must use a survival player");
        player.setItemInHand(InteractionHand.MAIN_HAND, captured);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false));
        InteractionResult result = captured.getItem().useOn(context);
        helper.assertTrue(result.consumesAction(), "The captured item's useOn must release into the server world");
        helper.assertTrue(captured.isEmpty(), "Survival release must consume exactly the captured item");
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),
                "Survival release must remove the captured item from the player's actual hand");
        BlockPos releasePos = clicked.above();
        List<Animal> animals = helper.getLevel().getEntitiesOfClass(Animal.class,
                new AABB(releasePos).inflate(0.5), animal -> animal.isAlive() && animal.getType() == original.getType());
        helper.assertTrue(animals.size() == 1, "Release must add exactly one entity of the captured type");
        Animal released = animals.get(0);
        helper.assertTrue(!originalId.equals(released.getUUID()), "Release must allocate a fresh world UUID");
        helper.assertTrue(released.getAge() == originalAge && released.isBaby() == (originalAge < 0),
                "Release must preserve the exact captured age and lifecycle");
        helper.assertTrue(original.getCustomName().equals(released.getCustomName()) && released.isCustomNameVisible(),
                "Release must preserve the custom name and visibility");
        helper.assertTrue(released.getHealth() == originalHealth && released.isNoAi() && released.isPersistenceRequired(),
                "Release must preserve health, AI, and persistence state");
        helper.assertTrue(released.position().distanceToSqr(Vec3.atBottomCenterOf(releasePos)) < 0.000001,
                "Release must use the clicked destination instead of the captured coordinates");
        helper.assertTrue(released.getDeltaMovement().lengthSqr() == 0 && released.fallDistance == 0,
                "Release must not inherit captured velocity or fall distance");
        assertNoLeash(helper, released);
        return released;
    }

    private static void assertNoWorldState(GameTestHelper helper, CompoundTag stored) {
        for (String key : List.of("UUID", "Pos", "Motion", "Rotation", "FallDistance", "fall_distance",
                "Leash", "leash", "Passengers", "RootVehicle", "Dimension", "PortalCooldown")) {
            helper.assertTrue(!stored.contains(key), "Captured state must not retain world-bound field " + key);
        }
    }

    private static void assertNoLeash(GameTestHelper helper, Animal released) {
        CompoundTag releasedState = save(released);
        helper.assertTrue(!released.isLeashed() && !releasedState.contains("Leash") && !releasedState.contains("leash"),
                "Release must not retain a live or pending serialized leash attachment");
    }

    private static void assertFenceLeash(GameTestHelper helper, CompoundTag saved, BlockPos fencePos) {
        helper.assertTrue(saved.get(LEASH_KEY) instanceof IntArrayTag,
                "Vanilla must serialize the modern fence-position leash form");
        int[] position = ((IntArrayTag) saved.get(LEASH_KEY)).getAsIntArray();
        helper.assertTrue(java.util.Arrays.equals(position, new int[] {fencePos.getX(), fencePos.getY(), fencePos.getZ()}),
                "Fixture leash must refer to the actual fence position");
    }

    private static void equipHorse(GameTestHelper helper, Horse horse) {
        horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.DIAMOND_HORSE_ARMOR));
    }

    private static void assertHorseEquipment(GameTestHelper helper, Horse horse) {
        helper.assertTrue(horse.getItemBySlot(EquipmentSlot.SADDLE).is(Items.SADDLE)
                        && horse.getItemBySlot(EquipmentSlot.BODY).is(Items.DIAMOND_HORSE_ARMOR),
                "Generic capture/release must preserve saddle and armor payload");
    }

    private static Player mockPlayer(GameTestHelper helper) {
        return helper.makeMockPlayer(GameType.SURVIVAL);
    }

    private static CompoundTag save(Entity entity) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(output);
        return output.buildResult();
    }

    private static void load(Entity entity, CompoundTag state) {
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), state));
    }

    private static CompoundTag itemData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static CompoundTag capturedData(CompoundTag root) {
        return root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG).orElseThrow();
    }

    private static int intValue(CompoundTag tag, String key) {
        return tag.getInt(key).orElseThrow();
    }

    private static String stringValue(CompoundTag tag, String key) {
        return tag.getString(key).orElseThrow();
    }
}
