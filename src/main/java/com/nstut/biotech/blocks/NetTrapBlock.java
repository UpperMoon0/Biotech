package com.nstut.biotech.blocks;

import com.nstut.biotech.items.ItemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class NetTrapBlock extends Block {
    public static final String CAPTURED_ENTITY_TAG = "CapturedEntity";

    public NetTrapBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion());
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state,
                                        @NotNull BlockGetter level,
                                        @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return Block.box(0, 0, 0, 16, 1, 16);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void entityInside(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide) {
            return;
        }

        ItemStack captured = createCapturedStack(entity);
        if (captured.isEmpty()) {
            return;
        }

        CompoundTag entityData = new CompoundTag();
        if (!entity.saveWithoutId(entityData)) {
            return;
        }
        captured.getOrCreateTag().put(CAPTURED_ENTITY_TAG, entityData);

        // Keep the legacy sheep-color key so old UI/tooltips and old saves remain compatible.
        if (entity instanceof Sheep sheep) {
            DyeColor color = sheep.getColor();
            captured.getOrCreateTag().putInt("SheepColor", color.getId());
        }

        // Consume the trap only after the complete capture payload is ready.
        if (!level.destroyBlock(pos, false)) {
            return;
        }

        entity.remove(Entity.RemovalReason.DISCARDED);
        level.addFreshEntity(new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.15,
                pos.getZ() + 0.5,
                captured));
    }

    private static ItemStack createCapturedStack(Entity entity) {
        if (entity instanceof Cow cow) {
            return new ItemStack(cow.isBaby() ? ItemRegistries.BABY_COW.get() : ItemRegistries.COW.get());
        }
        if (entity instanceof Chicken chicken) {
            return new ItemStack(chicken.isBaby() ? ItemRegistries.BABY_CHICKEN.get() : ItemRegistries.CHICKEN.get());
        }
        if (entity instanceof Pig pig) {
            return new ItemStack(pig.isBaby() ? ItemRegistries.BABY_PIG.get() : ItemRegistries.PIG.get());
        }
        if (entity instanceof Sheep sheep) {
            return new ItemStack(sheep.isBaby() ? ItemRegistries.BABY_SHEEP.get() : ItemRegistries.SHEEP.get());
        }
        if (entity instanceof Rabbit rabbit) {
            return new ItemStack(rabbit.isBaby() ? ItemRegistries.BABY_RABBIT.get() : ItemRegistries.RABBIT.get());
        }
        return ItemStack.EMPTY;
    }
}
