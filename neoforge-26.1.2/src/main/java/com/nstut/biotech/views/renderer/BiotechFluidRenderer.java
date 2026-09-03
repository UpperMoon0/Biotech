package com.nstut.biotech.views.renderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class BiotechFluidRenderer extends BaseFluidRenderer {
    public void renderFluid(GuiGraphicsExtractor graphics, int x, int y, int width, int height, FluidStack fluidStack) {
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
        drawTiledSprite(graphics, x, y, width, height, fluidColor, height, fluidStillSprite);
    }
}
