package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/** Generic fallback for capturable entity types that do not use one of Biotech's legacy per-species items. */
public class CapturedAnimalItem extends Item {
    public static final String ENTITY_TYPE_TAG = "EntityType";
    public static final String RECIPE_LIFECYCLE_TAG = "BiotechRecipeLifecycle";
    public static final String LIFECYCLE_ANY = "any";
    public static final String LIFECYCLE_ADULT = "adult";
    public static final String LIFECYCLE_BABY = "baby";

    public CapturedAnimalItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * Matches pack-defined generic animal requirements by species plus an optional lifecycle selector.
     * Missing lifecycle metadata defaults to {@code any}; adult/baby selectors require captured age state.
     */
    public static boolean matchesGenericSpecies(ItemStack required, ItemStack present) {
        if (!(required.getItem() instanceof CapturedAnimalItem)
                || !(present.getItem() instanceof CapturedAnimalItem)) {
            return false;
        }

        CompoundTag requiredRoot = required.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag presentRoot = present.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String requiredTypeId = requiredRoot.getString(ENTITY_TYPE_TAG).orElse("");
        String presentTypeId = presentRoot.getString(ENTITY_TYPE_TAG).orElse("");
        if (requiredTypeId.isEmpty() || presentTypeId.isEmpty()) {
            return false;
        }

        EntityType<?> requiredType = EntityType.byString(requiredTypeId).orElse(null);
        EntityType<?> presentType = EntityType.byString(presentTypeId).orElse(null);
        if (requiredType == null || requiredType != presentType) {
            return false;
        }

        String lifecycle = requiredRoot.getString(RECIPE_LIFECYCLE_TAG).orElse(LIFECYCLE_ANY);
        if (LIFECYCLE_ANY.equals(lifecycle)) {
            return true;
        }

        CompoundTag captured = presentRoot.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG).orElse(null);
        if (captured == null || !captured.contains("Age")) {
            return false;
        }
        boolean baby = captured.getInt("Age").orElse(0) < 0;
        return LIFECYCLE_BABY.equals(lifecycle)
                ? baby
                : LIFECYCLE_ADULT.equals(lifecycle) && !baby;
    }

    public static boolean matchesLegacyVariant(ItemStack stack, EntityType<?> expectedType, boolean expectedBaby) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String entityTypeId = root.getString(ENTITY_TYPE_TAG).orElse("");
        if (entityTypeId.isEmpty() || !root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            return false;
        }

        EntityType<?> actualType = EntityType.byString(entityTypeId).orElse(null);
        if (actualType != expectedType) {
            return false;
        }

        CompoundTag captured = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG).orElseGet(CompoundTag::new);
        boolean actualBaby = captured.getInt("Age").orElse(0) < 0;
        return actualBaby == expectedBaby;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String entityTypeId = root.getString(ENTITY_TYPE_TAG).orElse("");
        if (entityTypeId.isEmpty()) {
            return InteractionResult.FAIL;
        }

        Entity entity = createCapturedEntity(level, stack);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        entity.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), 0.0f);
        if (!level.noCollision(entity, entity.getBoundingBox()) || !level.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    public Entity createCapturedEntity(Level level, ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String entityTypeId = root.getString(ENTITY_TYPE_TAG).orElse("");
        if (entityTypeId.isEmpty()) {
            return null;
        }
        EntityType<?> entityType = EntityType.byString(entityTypeId).orElse(null);
        if (entityType == null) {
            return null;
        }
        Entity entity = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (entity == null) {
            return null;
        }
        if (root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            CompoundTag captured = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG).orElseGet(CompoundTag::new);
            entity.load(TagValueInput.create(
                    ProblemReporter.DISCARDING,
                    entity.registryAccess(),
                    CapturedEntityState.sanitize(captured)));
        }
        return entity;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String entityTypeId = root.getString(ENTITY_TYPE_TAG).orElse("");
        if (entityTypeId.isEmpty()) {
            return;
        }

        EntityType.byString(entityTypeId).ifPresent(type ->
                tooltip.accept(Component.translatable(
                        "tooltip.biotech.captured_animal",
                        Component.translatable(type.getDescriptionId()))));
    }
}
