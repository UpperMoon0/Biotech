package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MobItem extends Item {
    private final int type;

    public MobItem(int type) {
        super(new Item.Properties());
        this.type = type;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        ItemStack stack = context.getItemInHand();
        Mob mob = createMob(level);
        if (mob == null) return InteractionResult.FAIL;

        restoreCapturedState(mob, stack);
        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        mob.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), 0.0f);
        if (!level.noCollision(mob, mob.getBoundingBox())) return InteractionResult.FAIL;
        if (!level.addFreshEntity(mob)) return InteractionResult.FAIL;
        if (!player.isCreative()) stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Nullable
    private Mob createMob(Level level) {
        EntityType<? extends Mob> entityType = switch (type) {
            case 1, 2 -> EntityType.COW;
            case 3, 4 -> EntityType.CHICKEN;
            case 5, 6 -> EntityType.PIG;
            case 7, 8 -> EntityType.SHEEP;
            case 9, 10 -> EntityType.RABBIT;
            default -> null;
        };
        if (entityType == null) return null;
        Mob mob = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (mob == null) return null;
        if (type == 2 || type == 4 || type == 6 || type == 8 || type == 10) mob.setBaby(true);
        return mob;
    }

    private void restoreCapturedState(Mob mob, ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            CompoundTag captured = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG).orElseGet(CompoundTag::new);
            mob.load(TagValueInput.create(ProblemReporter.DISCARDING, mob.registryAccess(), CapturedEntityState.sanitize(captured)));
            return;
        }
        if (mob instanceof Sheep sheep) {
            root.getInt("SheepColor").ifPresent(id -> sheep.setColor(DyeColor.byId(id)));
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (type != 7 && type != 8) return;
        DyeColor color = DyeColor.WHITE;
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (root.contains("SheepColor")) {
            color = DyeColor.byId(root.getInt("SheepColor").orElse(0));
        } else if (root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            CompoundTag captured = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG).orElseGet(CompoundTag::new);
            if (captured.contains("Color")) color = DyeColor.byId(captured.getByte("Color").orElse((byte) 0));
        }
        tooltip.accept(Component.translatable("tooltip.biotech.sheep_color", color.getName().replace('_', ' ')));
    }
}
