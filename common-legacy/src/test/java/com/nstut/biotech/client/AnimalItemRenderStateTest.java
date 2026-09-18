package com.nstut.biotech.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalItemRenderStateTest {
    @Test
    void restoresDisabledShadowStateAfterRender() {
        AtomicBoolean shadow = new AtomicBoolean(false);
        AnimalItemRenderState.withoutShadow(shadow.get(), shadow::set, () -> assertFalse(shadow.get()));
        assertFalse(shadow.get(), "item rendering must not enable shadows that were disabled before it ran");
    }

    @Test
    void restoresEnabledShadowStateAfterRender() {
        AtomicBoolean shadow = new AtomicBoolean(true);
        AnimalItemRenderState.withoutShadow(shadow.get(), shadow::set, () -> assertFalse(shadow.get()));
        assertTrue(shadow.get(), "item rendering must restore enabled shadows after it finishes");
    }

    @Test
    void restoresShadowStateWhenEntityRenderingThrows() {
        AtomicBoolean shadow = new AtomicBoolean(false);
        assertThrows(IllegalStateException.class, () ->
                AnimalItemRenderState.withoutShadow(shadow.get(), shadow::set, () -> {
                    assertFalse(shadow.get());
                    throw new IllegalStateException("synthetic render failure");
                }));
        assertFalse(shadow.get(), "shadow state must be restored from a failing render path too");
    }
}