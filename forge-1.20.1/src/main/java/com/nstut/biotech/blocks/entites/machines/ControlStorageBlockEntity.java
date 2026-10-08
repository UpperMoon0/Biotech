package com.nstut.biotech.blocks.entites.machines;
import com.nstut.biotech.machines.RedstoneMode;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
/** Platform serialization only; the provider continues to own the transaction snapshot. */
public abstract class ControlStorageBlockEntity extends MachineBlockEntity {
    protected com.nstut.nstutlib.recipes.ModRecipeData savedDisplayRecipe;
    protected RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    protected ControlStorageBlockEntity(BlockEntityType<? extends MachineBlockEntity> type, BlockPos pos, BlockState state, int x, int y, int z) { super(type, pos, state, x, y, z); }
    @Override public void load(CompoundTag tag) { super.load(tag); redstoneMode = RedstoneMode.fromId(tag.getInt("biotechRedstoneMode"));
        savedDisplayRecipe = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("activeRecipeId")) == null
                || !tag.contains("activeRecipeSnapshot") ? null
                : com.nstut.nstutlib.recipes.ModRecipeData.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag.get("activeRecipeSnapshot")).result().orElse(null);
 }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("biotechRedstoneMode", redstoneMode.ordinal()); }
    @SuppressWarnings("unchecked")
    protected <R extends com.nstut.nstutlib.recipes.ModRecipe<R>> java.util.List<R> diagnosticRecipes(net.minecraft.world.level.Level level, net.minecraft.world.item.crafting.RecipeType<R> type) { return level.getRecipeManager().getAllRecipesFor(type); }
    protected int[] persistedOutputIndexes(net.minecraft.world.level.Level level) { return saveWithFullMetadata().getIntArray("activeItemOutputIndexes"); }
}
