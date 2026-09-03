package com.nstut.biotech.views.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public abstract class BaseFluidRenderer {
    protected static final int TEXTURE_SIZE = 16;
    protected static final int MIN_FLUID_HEIGHT = 1;

    protected abstract void drawFluid(GuiGraphicsExtractor graphics, int x, int y, int width, int height, FluidStack fluidStack);

    protected TextureAtlasSprite getStillFluidSprite(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
        Identifier fluidStill = renderProperties.getStillTexture(fluidStack);
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
    }

    protected int getColorTint(FluidStack ingredient) {
        return IClientFluidTypeExtensions.of(ingredient.getFluid()).getTintColor(ingredient);
    }

    protected static void drawTiledSprite(GuiGraphicsExtractor graphics, int x, int y, int tiledWidth, int tiledHeight,
                                          int color, long scaledAmount, TextureAtlasSprite sprite) {
        long remainingHeight = Math.min(scaledAmount, tiledHeight);
        int bottom = y + tiledHeight;

        while (remainingHeight > 0) {
            int tileHeight = (int) Math.min(TEXTURE_SIZE, remainingHeight);
            bottom -= tileHeight;
            for (int tileX = 0; tileX < tiledWidth; tileX += TEXTURE_SIZE) {
                int tileWidth = Math.min(TEXTURE_SIZE, tiledWidth - tileX);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + tileX, bottom, tileWidth, tileHeight, color);
            }
            remainingHeight -= tileHeight;
        }
    }
}
