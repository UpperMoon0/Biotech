package com.nstut.biotech.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.nstut.biotech.blocks.NetTrapBlock;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.CapturedEntityState;
import com.nstut.biotech.items.MobItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueInput;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/** Renders captured-animal item stacks using the same entity renderer and model used in-world. */
public final class AnimalSpecialRenderer implements SpecialModelRenderer<AnimalSpecialRenderer.RenderData> {
    @Override
    public void submit(@Nullable RenderData data, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
        if (data == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Entity entity = data.entityType().create(level, EntitySpawnReason.LOAD);
        if (entity == null) {
            return;
        }

        if (data.capturedState() != null) {
            entity.load(TagValueInput.create(
                    ProblemReporter.DISCARDING,
                    entity.registryAccess(),
                    CapturedEntityState.sanitize(data.capturedState())));
        } else {
            if (entity instanceof Mob mob && data.baby()) {
                mob.setBaby(true);
            }
            if (entity instanceof Sheep sheep && data.sheepColor() >= 0) {
                sheep.setColor(DyeColor.byId(data.sheepColor()));
            }
        }

        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        EntityRenderState renderState = dispatcher.extractEntity(entity, 1.0f);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        renderState.nameTag = null;
        renderState.scoreText = null;
        renderState.displayFireAnimation = false;
        renderState.lightCoords = packedLight;

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
        dispatcher.submit(renderState, new CameraRenderState(), 0.0, 0.0, 0.0, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.0f, 0.0f, 0.0f));
        consumer.accept(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    @Override
    public @Nullable RenderData extractArgument(ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag captured = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG).orElse(null);

        if (stack.getItem() instanceof MobItem mobItem) {
            EntityType<? extends Mob> entityType = mobItem.entityType();
            if (entityType == null) {
                return null;
            }
            return new RenderData(
                    entityType,
                    mobItem.isBabyVariant(),
                    captured == null ? null : captured.copy(),
                    root.getInt("SheepColor").orElse(-1));
        }

        if (stack.getItem() instanceof CapturedAnimalItem) {
            String id = root.getString(CapturedAnimalItem.ENTITY_TYPE_TAG).orElse("");
            EntityType<?> entityType = EntityType.byString(id).orElse(null);
            if (entityType == null) {
                return null;
            }
            return new RenderData(entityType, false, captured == null ? null : captured.copy(), -1);
        }

        return null;
    }

    public record RenderData(EntityType<?> entityType, boolean baby, @Nullable CompoundTag capturedState, int sheepColor) {
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<RenderData> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<RenderData> bake(BakingContext context) {
            return new AnimalSpecialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<RenderData>> type() {
            return MAP_CODEC;
        }
    }
}
