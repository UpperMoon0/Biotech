package com.nstut.biotech.client;

import java.util.function.Consumer;

final class AnimalItemRenderState {
    private AnimalItemRenderState() {
    }

    static void withoutShadow(boolean previousShadow, Consumer<Boolean> shadowSetter, Runnable renderAction) {
        shadowSetter.accept(false);
        try {
            renderAction.run();
        } finally {
            shadowSetter.accept(previousShadow);
        }
    }
}