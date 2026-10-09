package com.nstut.biotech.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

/** Reconstruct only an off-world preview entity to read actual saved traits and attribute values. */
public final class CapturedAnimalTraitTooltip {
    private CapturedAnimalTraitTooltip() {}
    public static void append(ItemStack stack, Level level, Consumer<Component> tooltip) {
        if (level == null || CapturedAnimalStackState.read(stack).isEmpty()) return;
        var entity = stack.getItem() instanceof MobItem mob ? mob.createMob(level, stack)
                : stack.getItem() instanceof CapturedAnimalItem captured ? captured.createCapturedEntity(level, stack) : null;
        if (entity == null) return;
        try {
            // Vanilla skips saved attributes when loading client-side preview entities.
            if (level.isClientSide() && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                var input = net.minecraft.world.level.storage.TagValueInput.create(
                        net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess(), CapturedAnimalStackState.read(stack));
                input.read("attributes", net.minecraft.world.entity.ai.attributes.AttributeInstance.Packed.LIST_CODEC)
                        .ifPresent(living.getAttributes()::apply);
                living.setHealth(input.getFloatOr("Health", living.getHealth()));
            }
            var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
            entity.saveWithoutId(output);
            var state = output.buildResult();
            String type = net.minecraft.world.entity.EntityType.getKey(entity.getType()).toString();
            var json = net.minecraft.nbt.NbtOps.INSTANCE.convertTo(com.mojang.serialization.JsonOps.INSTANCE, state).getAsJsonObject();
            CapturedAnimalTraits.describe(type, json).forEach(tooltip);
            if (entity.getCustomName() != null) tooltip.accept(Component.translatable("tooltip.biotech.trait.name", entity.getCustomName()));
            if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                for (var slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                    var equipment = living.getItemBySlot(slot);
                    if (!equipment.isEmpty()) tooltip.accept(Component.translatable("tooltip.biotech.trait.equipment", equipment.getHoverName()));
                }
                tooltip.accept(Component.translatable("tooltip.biotech.trait.health", number(living.getHealth() / 2.0), number(living.getMaxHealth() / 2.0)));
                var speed = living.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
                if (speed != null) tooltip.accept(Component.translatable("tooltip.biotech.trait.speed", number(speed.getValue())));
                var jump = living.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH);
                if (jump != null) tooltip.accept(Component.translatable("tooltip.biotech.trait.jump", number(jump.getValue())));
            }
        } finally { entity.discard(); }
    }
    private static String number(double value) { return String.format(java.util.Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", ""); }
}
