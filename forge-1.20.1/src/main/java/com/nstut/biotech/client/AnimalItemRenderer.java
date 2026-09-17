package com.nstut.biotech.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.MobItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class AnimalItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final EntityRenderDispatcher entityRenderer;

    private AnimalItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    public static IClientItemExtensions clientExtensions() {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return Holder.INSTANCE;
            }
        };
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Entity entity = createEntity(level, stack);
        if (entity == null) {
            return;
        }

        float scale = 0.53125f;
        float maxSize = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (maxSize > 1.0f) {
            scale /= maxSize;
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.2f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(210.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-15.0f));
        poseStack.scale(scale, scale, scale);
        this.entityRenderer.setRenderShadow(false);
        try {
            this.entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, poseStack, buffer, packedLight);
        } finally {
            this.entityRenderer.setRenderShadow(true);
            poseStack.popPose();
        }
    }

    private static Entity createEntity(Level level, ItemStack stack) {
        if (stack.getItem() instanceof MobItem mobItem) {
            return mobItem.createMob(level, stack);
        }
        if (stack.getItem() instanceof CapturedAnimalItem capturedAnimalItem) {
            return capturedAnimalItem.createCapturedEntity(level, stack);
        }
        return null;
    }

    private static final class Holder {
        private static final AnimalItemRenderer INSTANCE = new AnimalItemRenderer();
    }
}
