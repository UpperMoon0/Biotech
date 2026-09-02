package com.nstut.biotech.views.io_hatches.fluid;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public abstract class FluidHatchScreen<T extends FluidHatchMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/fluid_hatch.png");
    private BiotechFluidTankRenderer renderer;
    public FluidHatchScreen(T menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }
    @Override
    protected void init() {
        super.init();
        renderer = new BiotechFluidTankRenderer(32000, 16, 52);
    }
    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY);
        if (isHovering(62, 17, 16, 52, pMouseX, pMouseY)) {
            FluidStack storedFluid = menu.getFluidStack();
            String fluidName = storedFluid.isEmpty()? "Empty" : storedFluid.getDisplayName().getString();
            int fluidCapacity = menu.getFluidHatchBlockEntity().TANK_CAPACITY;
            pGuiGraphics.renderTooltip(font, List.of(Component.literal("Stored Fluid:"), Component.literal(fluidName), Component.literal( storedFluid.getAmount() + " / " + fluidCapacity + " mB")), Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
        }
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack itemstack = hoveredSlot.getItem();
            pGuiGraphics.renderTooltip(font, getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, pMouseX - leftPos, pMouseY - topPos);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderer.renderFluid(graphics.pose() , leftPos + 62, topPos + 17, menu.getFluidStack());
    }
}
