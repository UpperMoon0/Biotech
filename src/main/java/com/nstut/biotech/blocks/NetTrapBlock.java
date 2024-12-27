package com.nstut.biotech.blocks;

import com.nstut.biotech.items.ItemRegistries;
import com.nstut.nstutlib.items.SmartHammer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetTrapBlock extends Block {
    public NetTrapBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Block.box(0,0,0,16,1,16);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!level.isClientSide) {
            if (entity instanceof Cow) {
                level.destroyBlock(blockPos, false);

                entity.remove(Entity.RemovalReason.KILLED);

                if (!((Cow) entity).isBaby()) {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.COW.get()));
                    level.addFreshEntity(itemEntity);
                } else {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.BABY_COW.get()));
                    level.addFreshEntity(itemEntity);
                }
            }
            if (entity instanceof Chicken) {
                level.destroyBlock(blockPos, false);

                entity.remove(Entity.RemovalReason.KILLED);

                if (!((Chicken) entity).isBaby()) {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.CHICKEN.get()));
                    level.addFreshEntity(itemEntity);
                } else {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.BABY_CHICKEN.get()));
                    level.addFreshEntity(itemEntity);
                }
            }
            if (entity instanceof Pig) {
                level.destroyBlock(blockPos, false);

                entity.remove(Entity.RemovalReason.KILLED);

                if (!((Pig) entity).isBaby()) {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.PIG.get()));
                    level.addFreshEntity(itemEntity);
                } else {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.BABY_PIG.get()));
                    level.addFreshEntity(itemEntity);
                }
            }
            if (entity instanceof Sheep) {
                level.destroyBlock(blockPos, false);

                entity.remove(Entity.RemovalReason.KILLED);

                if (!((Sheep) entity).isBaby()) {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.SHEEP.get()));
                    level.addFreshEntity(itemEntity);
                } else {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.BABY_SHEEP.get()));
                    level.addFreshEntity(itemEntity);
                }
            }
            if (entity instanceof Rabbit) {
                level.destroyBlock(blockPos, false);

                entity.remove(Entity.RemovalReason.KILLED);

                if (!((Rabbit) entity).isBaby()) {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.RABBIT.get()));
                    level.addFreshEntity(itemEntity);
                } else {
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), new ItemStack(ItemRegistries.BABY_RABBIT.get()));
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }
}
