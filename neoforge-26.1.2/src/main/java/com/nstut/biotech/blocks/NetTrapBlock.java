package com.nstut.biotech.blocks;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.ItemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class NetTrapBlock extends Block {
    public static final String CAPTURED_ENTITY_TAG = "CapturedEntity";
    private static final TagKey<EntityType<?>> CAPTURABLE = TagKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "capturable"));

    public NetTrapBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                        @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(0, 0, 0, 16, 1, 16);
    }

    @Override
    protected void entityInside(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                @NotNull Entity entity, @NotNull InsideBlockEffectApplier effects,
                                boolean canApplyEffects) {
        if (level.isClientSide() || !isCaptureTypeSupported(entity.getType(), entity.is(CAPTURABLE), level)) return;
        ItemStack captured = createCapturedStack(entity);
        if (captured.isEmpty()) return;

        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        entity.saveWithoutId(output);
        CompoundTag entityData = output.buildResult();
        int sheepColor = entity instanceof Sheep sheep ? sheep.getColor().getId() : -1;
        CapturedAnimalStackState.writeCapture(
                captured,
                entityData,
                EntityType.getKey(entity.getType()).toString(),
                sheepColor);

        if (!level.destroyBlock(pos, false)) return;
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.15, pos.getZ() + 0.5, captured);
        if (!level.addFreshEntity(drop)) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            return;
        }
        entity.remove(Entity.RemovalReason.DISCARDED);
    }

    /**
     * Datapack membership is only one half of the capture contract. The type must also be
     * reconstructible through the same EntityType factory used by release. This preflight runs
     * before the trap or original entity is consumed.
     */
    public static boolean isCaptureTypeSupported(EntityType<?> entityType, boolean tagged, Level level) {
        if (!tagged) return false;
        Entity probe = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (probe == null) return false;
        probe.discard();
        return true;
    }

    private static ItemStack createCapturedStack(Entity entity) {
        // Preserve legacy item identities for the original five species so existing recipes/worlds remain compatible.
        if (entity.getType() == EntityType.COW && entity instanceof Cow cow) return new ItemStack(cow.isBaby() ? ItemRegistries.BABY_COW.get() : ItemRegistries.COW.get());
        if (entity.getType() == EntityType.CHICKEN && entity instanceof Chicken chicken) return new ItemStack(chicken.isBaby() ? ItemRegistries.BABY_CHICKEN.get() : ItemRegistries.CHICKEN.get());
        if (entity.getType() == EntityType.PIG && entity instanceof Pig pig) return new ItemStack(pig.isBaby() ? ItemRegistries.BABY_PIG.get() : ItemRegistries.PIG.get());
        if (entity.getType() == EntityType.SHEEP && entity instanceof Sheep sheep) return new ItemStack(sheep.isBaby() ? ItemRegistries.BABY_SHEEP.get() : ItemRegistries.SHEEP.get());
        if (entity.getType() == EntityType.RABBIT && entity instanceof Rabbit rabbit) return new ItemStack(rabbit.isBaby() ? ItemRegistries.BABY_RABBIT.get() : ItemRegistries.RABBIT.get());
        return new ItemStack(ItemRegistries.CAPTURED_ANIMAL.get());
    }
}
