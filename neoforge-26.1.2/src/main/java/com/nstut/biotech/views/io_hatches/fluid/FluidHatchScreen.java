package com.nstut.biotech.views.io_hatches.fluid;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public abstract class FluidHatchScreen<T extends FluidHatchMenu> extends AbstractContainerScreen<T> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/fluid_hatch.png");
    private final BiotechFluidTankRenderer renderer = new BiotechFluidTankRenderer(32000, 16, 52);

    public FluidHatchScreen(T menu, Inventory inventory, Component component) {
        super(menu, inventory, component, 176, 166);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (isHovering(62, 17, 16, 52, mouseX, mouseY)) {
            FluidStack storedFluid = menu.getFluidStack();
            String fluidName = storedFluid.isEmpty() ? "Empty" : storedFluid.getDisplayName().getString();
            int fluidCapacity = menu.getFluidHatchBlockEntity().TANK_CAPACITY;
            graphics.setTooltipForNextFrame(font,
                    List.of(Component.literal("Stored Fluid:"), Component.literal(fluidName),
                            Component.literal(storedFluid.getAmount() + " / " + fluidCapacity + " mB")),
                    Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        renderer.renderFluid(graphics, leftPos + 62, topPos + 17, menu.getFluidStack());
    }
}
