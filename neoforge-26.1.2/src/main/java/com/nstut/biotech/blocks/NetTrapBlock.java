package com.nstut.biotech.blocks;

import com.nstut.biotech.items.ItemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(0, 0, 0, 16, 1, 16);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void entityInside(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide) return;
        ItemStack captured = createCapturedStack(entity);
        if (captured.isEmpty()) return;

        CompoundTag entityData = entity.saveWithoutId(new CompoundTag());
        CustomData.update(DataComponents.CUSTOM_DATA, captured, root -> {
            root.put(CAPTURED_ENTITY_TAG, entityData);
            if (entity instanceof Sheep sheep) {
                DyeColor color = sheep.getColor();
                root.putInt("SheepColor", color.getId());
            }
        });

        if (!level.destroyBlock(pos, false)) return;
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.15, pos.getZ() + 0.5, captured);
        if (!level.addFreshEntity(drop)) {
            level.setBlock(pos, state, 3);
            return;
        }
        entity.remove(Entity.RemovalReason.DISCARDED);
    }

    private static ItemStack createCapturedStack(Entity entity) {
        if (entity instanceof Cow cow) return new ItemStack(cow.isBaby() ? ItemRegistries.BABY_COW.get() : ItemRegistries.COW.get());
        if (entity instanceof Chicken chicken) return new ItemStack(chicken.isBaby() ? ItemRegistries.BABY_CHICKEN.get() : ItemRegistries.CHICKEN.get());
        if (entity instanceof Pig pig) return new ItemStack(pig.isBaby() ? ItemRegistries.BABY_PIG.get() : ItemRegistries.PIG.get());
        if (entity instanceof Sheep sheep) return new ItemStack(sheep.isBaby() ? ItemRegistries.BABY_SHEEP.get() : ItemRegistries.SHEEP.get());
        if (entity instanceof Rabbit rabbit) return new ItemStack(rabbit.isBaby() ? ItemRegistries.BABY_RABBIT.get() : ItemRegistries.RABBIT.get());
        return ItemStack.EMPTY;
    }
}
