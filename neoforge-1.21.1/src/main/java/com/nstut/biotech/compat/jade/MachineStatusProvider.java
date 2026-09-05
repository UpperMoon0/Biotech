package com.nstut.biotech.compat.jade;

import com.nstut.biotech.Biotech;
import com.nstut.nstutlib.blocks.MachineBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum MachineStatusProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "machine_status");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        boolean operating = accessor.getBlockState().hasProperty(MachineBlock.OPERATING)
                && accessor.getBlockState().getValue(MachineBlock.OPERATING);
        tooltip.add(Component.translatable(
                "jade.biotech.machine.status",
                Component.translatable(operating
                        ? "jade.biotech.machine.operating"
                        : "jade.biotech.machine.idle")));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
