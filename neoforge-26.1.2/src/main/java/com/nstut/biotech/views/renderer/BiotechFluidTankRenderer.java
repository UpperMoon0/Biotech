package com.nstut.biotech.views.renderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class BiotechFluidTankRenderer extends BaseFluidRenderer {
    private final long capacity;
    private final int width;
    private final int height;

    public BiotechFluidTankRenderer(long capacity, int width, int height) {
        this.capacity = capacity;
        this.width = width;
        this.height = height;
    }

    public void renderFluid(GuiGraphicsExtractor graphics, int x, int y, FluidStack fluidStack) {
        drawFluid(graphics, x, y, width, height, fluidStack);
    }

    @Override
    protected void drawFluid(GuiGraphicsExtractor graphics, int x, int y, int width, int height, FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        if (fluid.isSame(Fluids.EMPTY)) {
            return;
        }

        TextureAtlasSprite fluidStillSprite = getStillFluidSprite(fluidStack);
        int fluidColor = getColorTint(fluidStack);
        long amount = fluidStack.getAmount();
        long scaledAmount = capacity <= 0 ? 0 : (amount * height) / capacity;

        if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
            scaledAmount = MIN_FLUID_HEIGHT;
        }
        if (scaledAmount > height) {
            scaledAmount = height;
        }

        drawTiledSprite(graphics, x, y, width, height, fluidColor, scaledAmount, fluidStillSprite);
    }
}
