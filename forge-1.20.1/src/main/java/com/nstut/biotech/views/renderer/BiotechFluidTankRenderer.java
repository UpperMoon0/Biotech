package com.nstut.biotech.views.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class BiotechFluidTankRenderer extends BaseFluidRenderer {
    private final long capacity;
    private final int width;
    private final int height;

    public BiotechFluidTankRenderer(long capacity, int width, int height) {
        this.capacity = capacity;
        this.width = width;
        this.height = height;
    }

    public void renderFluid(PoseStack poseStack, int x, int y, FluidStack fluidStack) {
        RenderSystem.enableBlend();
        poseStack.pushPose();
        {
            poseStack.translate(x, y, 0);
            drawFluid(poseStack, width, height, fluidStack);
        }
        poseStack.popPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    @Override
    protected void drawFluid(PoseStack poseStack, int width, int height, FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        if (fluid.isSame(Fluids.EMPTY)) {
            return;
        }

        TextureAtlasSprite fluidStillSprite = getStillFluidSprite(fluidStack);
        int fluidColor = getColorTint(fluidStack);

        long amount = fluidStack.getAmount();
        long scaledAmount = (amount * height) / capacity;

        if (amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
            scaledAmount = MIN_FLUID_HEIGHT;
        }
        if (scaledAmount > height) {
            scaledAmount = height;
        }

        drawTiledSprite(poseStack, width, height, fluidColor, scaledAmount, fluidStillSprite);
    }
}