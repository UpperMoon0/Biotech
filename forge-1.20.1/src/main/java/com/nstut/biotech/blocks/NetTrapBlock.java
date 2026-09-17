package com.nstut.biotech.blocks;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.ItemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
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
import org.jetbrains.annotations.NotNull;

public class NetTrapBlock extends Block {
    public static final String CAPTURED_ENTITY_TAG = "CapturedEntity";
    private static final TagKey<EntityType<?>> CAPTURABLE = TagKey.create(
            Registries.ENTITY_TYPE,
            new ResourceLocation(Biotech.MOD_ID, "capturable"));

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
        if (level.isClientSide || !isCaptureTypeSupported(entity.getType(), entity.getType().is(CAPTURABLE), level)) {
            return;
        }

        ItemStack captured = createCapturedStack(entity);
        if (captured.isEmpty()) {
            return;
        }

        CompoundTag entityData = entity.saveWithoutId(new CompoundTag());
        int sheepColor = entity instanceof Sheep sheep ? sheep.getColor().getId() : -1;
        CapturedAnimalStackState.writeCapture(
                captured,
                entityData,
                EntityType.getKey(entity.getType()).toString(),
                sheepColor);

        // The trap and animal are only consumed once the captured-item entity is accepted.
        if (!level.destroyBlock(pos, false)) {
            return;
        }

        ItemEntity drop = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.15,
                pos.getZ() + 0.5,
                captured);
        if (!level.addFreshEntity(drop)) {
            // Restore the trap if the world rejected the drop; the animal remains untouched.
            level.setBlock(pos, state, 3);
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
        if (!tagged) {
            return false;
        }
        Entity probe = entityType.create(level);
        if (probe == null) {
            return false;
        }
        probe.discard();
        return true;
    }

    private static ItemStack createCapturedStack(Entity entity) {
        // Preserve legacy item identities for the original five species so existing recipes/worlds remain compatible.
        if (entity.getType() == EntityType.COW && entity instanceof Cow cow) {
            return new ItemStack(cow.isBaby() ? ItemRegistries.BABY_COW.get() : ItemRegistries.COW.get());
        }
        if (entity.getType() == EntityType.CHICKEN && entity instanceof Chicken chicken) {
            return new ItemStack(chicken.isBaby() ? ItemRegistries.BABY_CHICKEN.get() : ItemRegistries.CHICKEN.get());
        }
        if (entity.getType() == EntityType.PIG && entity instanceof Pig pig) {
            return new ItemStack(pig.isBaby() ? ItemRegistries.BABY_PIG.get() : ItemRegistries.PIG.get());
        }
        if (entity.getType() == EntityType.SHEEP && entity instanceof Sheep sheep) {
            return new ItemStack(sheep.isBaby() ? ItemRegistries.BABY_SHEEP.get() : ItemRegistries.SHEEP.get());
        }
        if (entity.getType() == EntityType.RABBIT && entity instanceof Rabbit rabbit) {
            return new ItemStack(rabbit.isBaby() ? ItemRegistries.BABY_RABBIT.get() : ItemRegistries.RABBIT.get());
        }
        return new ItemStack(ItemRegistries.CAPTURED_ANIMAL.get());
    }
}
