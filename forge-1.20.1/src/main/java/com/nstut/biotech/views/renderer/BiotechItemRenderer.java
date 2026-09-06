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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

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
            poseStack.translate((float)(x + 8), (float)(y + 8), (float)(150));

            try {
                poseStack.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
                poseStack.scale(width, height, 16.0F);
                boolean flag = !bakedmodel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }

                minecraft.getItemRenderer().render(itemStack, ItemDisplayContext.GUI, false, poseStack, bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
                flush();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(itemStack.getItem())));
                crashreportcategory.setDetail("Item Damage", () -> String.valueOf(itemStack.getDamageValue()));
                crashreportcategory.setDetail("Item NBT", () -> String.valueOf(itemStack.getTag()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
                throw new ReportedException(crashreport);
            }

            poseStack.popPose();
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