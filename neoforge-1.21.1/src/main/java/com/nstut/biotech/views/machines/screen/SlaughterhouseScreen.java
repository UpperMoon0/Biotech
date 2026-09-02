package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.views.machines.menu.SlaughterhouseMenu;
import com.nstut.biotech.views.renderer.BiotechFluidRenderer;
import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import com.nstut.biotech.views.renderer.BiotechItemRenderer;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SlaughterhouseScreen extends AbstractContainerScreen<SlaughterhouseMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/" + MachineRegistries.SLAUGHTERHOUSE.id() + ".png");
    private static final int MAX_PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 24;

    public SlaughterhouseScreen(SlaughterhouseMenu menu, Inventory inventory, Component component) { super(menu, inventory, component); }
    @Override protected void init() { super.init(); imageWidth = 212; }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        int rate = 0;
        if (menu.getIsOperating()) {
            rate = menu.getEnergyConsumeRate(); ModRecipeData recipe = menu.getRecipe();
            String raw = recipe.getIngredientItems()[0].getItemStack().getDisplayName().getString(); g.drawCenteredString(font, raw.substring(1, raw.length() - 1), 70, 76, 0xFFFFFF);
            if (isHovering(31, 100, 20, 20, mouseX, mouseY)) g.renderTooltip(font, List.of(Component.literal(recipe.getFluidIngredients()[0].getDisplayName().getString())), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            g.drawCenteredString(font, recipe.getFluidIngredients()[0].getAmount() + " mB", 95, 104, 0xFFFFFF);
            OutputItem[] outputs = menu.getRecipe().getOutputItems();
            for (int i = 0; i < outputs.length; i++) { String chance = outputs[i].getChance() < 1 ? " (" + (int)(outputs[i].getChance() * 100) + "%)" : ""; g.drawString(font, outputs[i].getItemStack().getCount() + chance, 143, 29 + 18 * i, 0xFFFFFF); }
        }
        if (isHovering(4,43,9,76,mouseX,mouseY)) {
            if (menu.getStructureValid()) g.renderTooltip(font, List.of(Component.literal("Stored Energy:"), Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"), Component.literal("Consuming: "), Component.literal(rate + " FE / t")), Optional.empty(), mouseX-leftPos, mouseY-topPos);
            else g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX-leftPos, mouseY-topPos);
        }
        if (isHovering(196,28,12,75,mouseX,mouseY)) {
            if (menu.getStructureValid()) { FluidStack stored=menu.getFluidStored(); String name=stored.isEmpty()?"Empty":stored.getDisplayName().getString(); g.renderTooltip(font,List.of(Component.literal("Stored Fluid:"),Component.literal(name),Component.literal(stored.getAmount()+" / "+menu.getFluidCapacity()+" mB")),Optional.empty(),mouseX-leftPos,mouseY-topPos); }
            else g.renderTooltip(font,Component.literal("Invalid Structure"),mouseX-leftPos,mouseY-topPos);
        }
        if (isHovering(88,28,19,19,mouseX,mouseY)) {
            if (!menu.getStructureValid()) g.renderTooltip(font,Component.literal("Invalid Structure"),mouseX-leftPos,mouseY-topPos);
            else if (!menu.getIsOperating()) g.renderTooltip(font,Component.literal("Not Operating"),mouseX-leftPos,mouseY-topPos);
            else { int cost=menu.getRecipe().getTotalEnergy(); float total=((float)cost/rate)/20,current=((float)menu.getEnergyConsumed()/rate)/20; g.renderTooltip(font,List.of(Component.literal("Progress:"),Component.literal(menu.getEnergyConsumed()+" / "+cost+" FE"),Component.literal(String.format("%.1f",current)+" / "+String.format("%.1f",total)+" s")),Optional.empty(),mouseX-leftPos,mouseY-topPos); }
        }
        String name=Component.translatable("menu.title.biotech."+MachineRegistries.SLAUGHTERHOUSE.id()).getString(); g.drawString(font,name,106-font.width(name)/2,3,0x3F3F3F,false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g,float partialTick,int mouseX,int mouseY) {
        renderBackground(g); g.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight); if(!menu.getStructureValid()) return;
        if(menu.getEnergyStored()>0){int h=getEnergyHeight();g.blit(TEXTURE,leftPos+4,topPos+119-h,212,100-h,9,h);} if(!menu.getFluidStored().isEmpty()) new BiotechFluidTankRenderer(menu.getFluidCapacity(),12,75).renderFluid(g.pose(),leftPos+196,topPos+28,menu.getFluidStored()); if(!menu.getIsOperating()) return;
        g.blit(TEXTURE,leftPos+86,topPos+26,212,0,getProgressWidth()+1,PROGRESS_HEIGHT); new BiotechItemRenderer(48,48).render(g.pose(),leftPos+46,topPos+35,menu.getRecipe().getIngredientItems()[0].getItemStack()); OutputItem[] outputs=menu.getRecipe().getOutputItems(); for(int i=0;i<outputs.length;i++) new BiotechItemRenderer(16,16).render(g.pose(),leftPos+123,topPos+24+18*i,outputs[i].getItemStack()); new BiotechFluidRenderer().renderFluid(g.pose(),leftPos+31,topPos+98,20,20,menu.getRecipe().getFluidIngredients()[0]);
    }

    public int getEnergyHeight(){int h=menu.getEnergyStored()*76/menu.getEnergyCapacity();return h==0&&menu.getEnergyStored()>0?1:h;} public int getProgressWidth(){return menu.getRecipeEnergyCost()==0?0:menu.getEnergyConsumed()*MAX_PROGRESS_WIDTH/menu.getRecipeEnergyCost();}
}
