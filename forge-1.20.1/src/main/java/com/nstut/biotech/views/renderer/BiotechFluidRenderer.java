package com.nstut.biotech.views.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class BiotechFluidRenderer extends BaseFluidRenderer {

    public void renderFluid(PoseStack poseStack, int x, int y, int width, int height, FluidStack fluidStack) {
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

        drawTiledSprite(poseStack, width, height, fluidColor, height, fluidStillSprite);
    }
}