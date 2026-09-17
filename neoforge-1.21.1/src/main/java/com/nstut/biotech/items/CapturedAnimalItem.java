package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Generic fallback for capturable entity types that do not use one of Biotech's legacy per-species items. */
public class CapturedAnimalItem extends Item {
    public static final String ENTITY_TYPE_TAG = "EntityType";

    public CapturedAnimalItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static boolean matchesLegacyVariant(ItemStack stack, EntityType<?> expectedType, boolean expectedBaby) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(ENTITY_TYPE_TAG) || !root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
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
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(ENTITY_TYPE_TAG)) {
            return InteractionResult.FAIL;
        }

        EntityType<?> entityType = EntityType.byString(root.getString(ENTITY_TYPE_TAG)).orElse(null);
        if (entityType == null) {
            return InteractionResult.FAIL;
        }

        Entity entity = entityType.create(level);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        if (root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            entity.load(CapturedEntityState.sanitize(root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG)));
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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(ENTITY_TYPE_TAG)) {
            return;
        }

        EntityType.byString(root.getString(ENTITY_TYPE_TAG)).ifPresent(type ->
                tooltip.add(Component.translatable(
                        "tooltip.biotech.captured_animal",
                        Component.translatable(type.getDescriptionId()))));
    }
}
