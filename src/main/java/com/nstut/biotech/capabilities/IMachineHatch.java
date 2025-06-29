package com.nstut.biotech.capabilities;

public interface IMachineHatch {
    HatchType getType();
    HatchDirection getDirection();

    enum HatchType {
        ENERGY,
        FLUID,
        ITEM
    }

    enum HatchDirection {
        INPUT,
        OUTPUT,
        BOTH
    }
}