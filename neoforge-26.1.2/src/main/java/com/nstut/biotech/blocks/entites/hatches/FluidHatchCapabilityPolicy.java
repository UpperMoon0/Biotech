package com.nstut.biotech.blocks.entites.hatches;

final class FluidHatchCapabilityPolicy {
    private FluidHatchCapabilityPolicy() {
    }

    static long externalCapacity(boolean inputHatch, boolean emptyResource, long delegateCapacity) {
        return inputHatch || emptyResource ? delegateCapacity : 0L;
    }
}
