package com.nstut.biotech.compat.jade;

import com.nstut.nstutlib.blocks.MachineBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class BiotechJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE, MachineBlock.class);
    }
}
