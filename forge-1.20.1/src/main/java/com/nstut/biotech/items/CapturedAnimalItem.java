package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import com.nstut.biotech.client.AnimalItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Generic captured-entity carrier used for data-driven species that do not have a legacy Biotech item.
 * Existing cow/chicken/pig/sheep/rabbit items remain valid so old recipes and worlds keep working.
 */
public class CapturedAnimalItem extends Item {
    public static final String ENTITY_TYPE_TAG = "EntityType";
    public static final String RECIPE_LIFECYCLE_TAG = "BiotechRecipeLifecycle";
    public static final String LIFECYCLE_ANY = "any";
    public static final String LIFECYCLE_ADULT = "adult";
    public static final String LIFECYCLE_BABY = "baby";

    public CapturedAnimalItem() {
        super(new Item.Properties().stacksTo(1));
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

        CompoundTag requiredRoot = required.getTag();
        CompoundTag presentRoot = present.getTag();
        if (requiredRoot == null || presentRoot == null
                || !requiredRoot.contains(ENTITY_TYPE_TAG) || !presentRoot.contains(ENTITY_TYPE_TAG)) {
            return false;
        }

        EntityType<?> requiredType = EntityType.byString(requiredRoot.getString(ENTITY_TYPE_TAG)).orElse(null);
        EntityType<?> presentType = EntityType.byString(presentRoot.getString(ENTITY_TYPE_TAG)).orElse(null);
        if (requiredType == null || requiredType != presentType) {
            return false;
        }

        String lifecycle = requiredRoot.contains(RECIPE_LIFECYCLE_TAG)
                ? requiredRoot.getString(RECIPE_LIFECYCLE_TAG)
                : LIFECYCLE_ANY;
        if (LIFECYCLE_ANY.equals(lifecycle)) {
            return true;
        }
        if (!presentRoot.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            return false;
        }

        CompoundTag captured = presentRoot.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG);
        if (!captured.contains("Age")) {
            return false;
        }
        boolean baby = captured.getInt("Age") < 0;
        return LIFECYCLE_BABY.equals(lifecycle)
                ? baby
                : LIFECYCLE_ADULT.equals(lifecycle) && !baby;
    }

    public static boolean matchesLegacyVariant(ItemStack stack, EntityType<?> expectedType, boolean expectedBaby) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(ENTITY_TYPE_TAG) || !root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            return false;
        }

        EntityType<?> actualType = EntityType.byString(root.getString(ENTITY_TYPE_TAG)).orElse(null);
        if (actualType != expectedType) {
            return false;
        }

        CompoundTag captured = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG);
        boolean actualBaby = captured.contains("Age") && captured.getInt("Age") < 0;
        return actualBaby == expectedBaby;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(ENTITY_TYPE_TAG)) {
            return InteractionResult.FAIL;
        }

        Entity entity = createCapturedEntity(level, stack);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), 0.0f);
        if (!level.noCollision(entity, entity.getBoundingBox()) || !level.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(ENTITY_TYPE_TAG)) {
            return;
        }

        EntityType.byString(root.getString(ENTITY_TYPE_TAG)).ifPresent(type ->
                tooltip.add(Component.translatable(
                        "tooltip.biotech.captured_animal",
                        Component.translatable(type.getDescriptionId()))));
    }
    @Nullable
    public Entity createCapturedEntity(Level level, ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(ENTITY_TYPE_TAG)) {
            return null;
        }
        EntityType<?> entityType = EntityType.byString(root.getString(ENTITY_TYPE_TAG)).orElse(null);
        if (entityType == null) {
            return null;
        }
        Entity entity = entityType.create(level);
        if (entity == null) {
            return null;
        }
        if (root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            entity.load(CapturedEntityState.sanitize(root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG)));
        }
        return entity;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(AnimalItemRenderer.clientExtensions());
    }

}
