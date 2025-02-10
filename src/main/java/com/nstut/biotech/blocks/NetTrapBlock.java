package com.nstut.biotech.blocks;

import com.nstut.biotech.items.ItemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Rabbit;
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

    public NetTrapBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion());
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        return Block.box(0, 0, 0, 16, 1, 16);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void entityInside(@NotNull BlockState blockState, Level level, @NotNull BlockPos blockPos, @NotNull Entity entity) {
        if (!level.isClientSide) {
            // Check if the entity has already been processed by a net trap
            CompoundTag persistentData = entity.getPersistentData();
            if (persistentData.getBoolean("netTrapTriggered")) {
                return;
            }

            // Mark the entity as processed so no trap triggers it again
            persistentData.putBoolean("netTrapTriggered", true);

            if (entity instanceof Cow) {
                level.destroyBlock(blockPos, false);
                entity.remove(Entity.RemovalReason.KILLED);

                ItemStack cowStack;
                if (!((Cow) entity).isBaby()) {
                    cowStack = new ItemStack(ItemRegistries.COW.get());
                } else {
                    cowStack = new ItemStack(ItemRegistries.BABY_COW.get());
                }
                level.addFreshEntity(new ItemEntity(
                        level,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        cowStack
                ));
            } else if (entity instanceof Chicken) {
                level.destroyBlock(blockPos, false);
                entity.remove(Entity.RemovalReason.KILLED);

                ItemStack chickenStack;
                if (!((Chicken) entity).isBaby()) {
                    chickenStack = new ItemStack(ItemRegistries.CHICKEN.get());
                } else {
                    chickenStack = new ItemStack(ItemRegistries.BABY_CHICKEN.get());
                }
                level.addFreshEntity(new ItemEntity(
                        level,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        chickenStack
                ));
            } else if (entity instanceof Pig) {
                level.destroyBlock(blockPos, false);
                entity.remove(Entity.RemovalReason.KILLED);

                ItemStack pigStack;
                if (!((Pig) entity).isBaby()) {
                    pigStack = new ItemStack(ItemRegistries.PIG.get());
                } else {
                    pigStack = new ItemStack(ItemRegistries.BABY_PIG.get());
                }
                level.addFreshEntity(new ItemEntity(
                        level,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        pigStack
                ));
            } else if (entity instanceof Sheep) {
                level.destroyBlock(blockPos, false);
                entity.remove(Entity.RemovalReason.KILLED);

                ItemStack sheepStack;
                if (!((Sheep) entity).isBaby()) {
                    sheepStack = new ItemStack(ItemRegistries.SHEEP.get());
                } else {
                    sheepStack = new ItemStack(ItemRegistries.BABY_SHEEP.get());
                }
                // Store the sheep's color in the item NBT
                DyeColor color = ((Sheep) entity).getColor();
                sheepStack.getOrCreateTag().putInt("SheepColor", color.getId());

                level.addFreshEntity(new ItemEntity(
                        level,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        sheepStack
                ));
            } else if (entity instanceof Rabbit) {
                level.destroyBlock(blockPos, false);
                entity.remove(Entity.RemovalReason.KILLED);

                ItemStack rabbitStack;
                if (!((Rabbit) entity).isBaby()) {
                    rabbitStack = new ItemStack(ItemRegistries.RABBIT.get());
                } else {
                    rabbitStack = new ItemStack(ItemRegistries.BABY_RABBIT.get());
                }
                level.addFreshEntity(new ItemEntity(
                        level,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        rabbitStack
                ));
            }
        }
    }
}