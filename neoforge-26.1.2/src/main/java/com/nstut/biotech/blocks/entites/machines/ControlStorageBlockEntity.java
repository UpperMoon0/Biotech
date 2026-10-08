package com.nstut.biotech.blocks.entites.machines;
import com.nstut.biotech.machines.RedstoneMode;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
/** Platform serialization only; the provider continues to own the transaction snapshot. */
public abstract class ControlStorageBlockEntity extends MachineBlockEntity {
    protected RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    protected ControlStorageBlockEntity(BlockEntityType<? extends MachineBlockEntity> type, BlockPos pos, BlockState state, int x, int y, int z) { super(type, pos, state, x, y, z); }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); redstoneMode = RedstoneMode.fromId(input.getIntOr("biotechRedstoneMode", 0)); }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.putInt("biotechRedstoneMode", redstoneMode.ordinal()); }
    @SuppressWarnings("unchecked")
    protected <R extends com.nstut.nstutlib.recipes.ModRecipe<R>> java.util.List<R> diagnosticRecipes(net.minecraft.world.level.Level level, net.minecraft.world.item.crafting.RecipeType<R> type) { return ((net.minecraft.server.level.ServerLevel) level).recipeAccess().getRecipes().stream().filter(h -> h.value().getType() == type).map(h -> (R) h.value()).toList(); }
    protected int[] persistedOutputIndexes(net.minecraft.world.level.Level level) { return saveWithFullMetadata(level.registryAccess()).getIntArray("activeItemOutputIndexes").orElseGet(() -> new int[0]); }
}
