package com.nstut.biotech.compat.jade;
import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
public enum MachineStatusDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final Identifier UID = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "machine_diagnostics");
    @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ControlledMachineBlockEntity machine) {
            data.putInt("BiotechStatus", machine.getMachineStatus().ordinal());
            data.putInt("BiotechRedstoneMode", machine.getRedstoneMode().ordinal());
        }
    }
    @Override public Identifier getUid() { return UID; }
}
