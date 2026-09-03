package com.nstut.biotech.views.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BiotechItemRenderer {
    private final Minecraft minecraft;
    private final MultiBufferSource.BufferSource bufferSource;
    private final float width;
    private final float height;

    public BiotechItemRenderer(float width, float height) {
        this.width = width;
        this.height = height;
        minecraft = Minecraft.getInstance();
        bufferSource = minecraft.renderBuffers().bufferSource();
    }

    public void render(PoseStack poseStack, int x, int y, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            Level level = minecraft.level;
            LivingEntity entity = minecraft.player;
            BakedModel bakedmodel = minecraft.getItemRenderer().getModel(itemStack, level, entity, 0);
            poseStack.pushPose();
            poseStack.translate((float)(x + 8), (float)(y + 8), 150.0F);

            try {
                poseStack.scale(1.0F, -1.0F, 1.0F);
                poseStack.scale(width, height, 16.0F);
                boolean flatLighting = !bakedmodel.usesBlockLight();
                if (flatLighting) {
                    Lighting.setupForFlatItems();
                }

                minecraft.getItemRenderer().render(itemStack, ItemDisplayContext.GUI, false, poseStack, bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
                flush();
                if (flatLighting) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory category = crashreport.addCategory("Item being rendered");
                category.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
                category.setDetail("Registry Name", () -> String.valueOf(BuiltInRegistries.ITEM.getKey(itemStack.getItem())));
                category.setDetail("Item Damage", () -> String.valueOf(itemStack.getDamageValue()));
                category.setDetail("Item Components", () -> String.valueOf(itemStack.getComponentsPatch()));
                category.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
                throw new ReportedException(crashreport);
            } finally {
                poseStack.popPose();
            }
        }
    }

    public void flush() {
        RenderSystem.disableDepthTest();
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }
}
