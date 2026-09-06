package com.nstut.biotech.views.io_hatches.energy;

import com.nstut.biotech.Biotech;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class EnergyHatchScreen<T extends EnergyHatchMenu> extends AbstractContainerScreen<T> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/energy_hatch.png");

    public EnergyHatchScreen(T menu, Inventory inventory, Component component) {
        super(menu, inventory, component, 176, 166);
    }

    @Override
    protected void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (isHovering(80, 17, 16, 52, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font,
                    List.of(Component.literal(menu.getEnergy() + " / " + menu.getBlockEntity().ENERGY_CAPACITY + " FE")),
                    Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.getEnergy() > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 80, topPos + 69 - menu.getEnergyHeight(), 176,
                    52 - menu.getEnergyHeight(), 16, menu.getEnergyHeight(), 256, 256);
        }
    }
}
