package com.nstut.biotech.machines;

import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.Consumer;

/**
 * New-build preview with complete, exact 2.6 habitat and 2.5 validation fallbacks.
 * Selection occurs only after a whole pattern succeeds, before hatch discovery.
 * No blocks, inventories, fluids or transaction fields are changed here.
 * All layouts use the controller's original origin, preserving NsTut Lib previews.
 */
public final class CompatibleMachinePattern extends MultiblockPattern {
    private final MultiblockPattern legacy;
    private final MultiblockPattern habitat26;
    private final Consumer<Boolean> selectRedesigned;

    public CompatibleMachinePattern(String id, MultiblockPattern legacy, Consumer<Boolean> selectRedesigned) {
        super(MachineStructures.pattern(id).getPattern());
        this.legacy = legacy;
        this.habitat26 = id.equals("terrestrial_habitat") ? MachineStructures.habitat26Pattern() : null;
        this.selectRedesigned = selectRedesigned;
    }

    @Override
    public boolean check(Level level, BlockPos controllerPos, BlockState controllerState,
                         int x, int y, int z) {
        if (super.check(level, controllerPos, controllerState, x, y, z)) {
            selectRedesigned.accept(true);
            return true;
        }
        // 2.6 uses the same controller origin and all seven hatch offsets as 2.7.
        if (habitat26 != null && habitat26.check(level, controllerPos, controllerState, x, y, z)) {
            selectRedesigned.accept(true);
            return true;
        }
        if (legacy.check(level, controllerPos, controllerState, x, y, z)) {
            selectRedesigned.accept(false);
            return true;
        }
        // Leave the last selection alone while invalid. The provider pauses processing;
        // the next successful check always refreshes selection before setHatches runs.
        return false;
    }
}
