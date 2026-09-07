package com.nstut.biotech.views.openui;

import com.nstut.openui.graphics.SurfaceStyle;

/** Biotech's shared visual tokens. Both production screens and previews use these surfaces. */
public final class BiotechStyle {
    private BiotechStyle() { }
    public static final int TEXT = 0xFFEAF3EC, MUTED = 0xFFADC2B5, MINT = 0xFF78D5AB;
    public static final SurfaceStyle SHELL = new SurfaceStyle(0xED304B3E, 0xF014241E, 0xAA91B5A1, 10, 6, 0x68000000);
    public static final SurfaceStyle CARD = new SurfaceStyle(0xB8446051, 0xC0253C31, 0x506E9480, 7, 0, 0);
    public static final SurfaceStyle WELL = new SurfaceStyle(0xD014281F, 0xD9223B2E, 0x60557764, 4, 0, 0);
    public static final SurfaceStyle BADGE = new SurfaceStyle(0xB03C6852, 0xC02C4F3C, 0x7078D5AB, 6, 0, 0);
    public static final int MACHINE_WIDTH = 320, MACHINE_HEIGHT = 224;
}
