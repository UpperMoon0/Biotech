package com.nstut.biotech.compat.jade;

import com.nstut.biotech.Biotech;
import com.nstut.nstutlib.blocks.MachineBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

import java.util.Locale;

public enum MachineStatusProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final Identifier UID = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "machine_status");
    private static final int LABEL_WIDTH = 52;
    private static final int BAR_WIDTH = 92;
    private static final int BAR_HEIGHT = 14;
    private static final int ENERGY_COLOR = 0xFFE7B84B;
    private static final int FLUID_INPUT_COLOR = 0xFF4AA3DF;
    private static final int FLUID_OUTPUT_COLOR = 0xFF43BFA3;
    private static final int PROGRESS_COLOR = 0xFF67C95C;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        boolean structureValid = data.getBooleanOr("StructureValid", true);
        boolean operating = data.getBooleanOr("Operating",
                accessor.getBlockState().hasProperty(MachineBlock.OPERATING)
                        && accessor.getBlockState().getValue(MachineBlock.OPERATING));

        if (!structureValid) {
            tooltip.add(Component.literal("\u2022 ").withStyle(ChatFormatting.RED)
                    .append(Component.translatable("jade.biotech.machine.invalid").withStyle(ChatFormatting.WHITE)));
            return;
        }

        tooltip.add(Component.literal("\u2022 ").withStyle(operating ? ChatFormatting.GREEN : ChatFormatting.YELLOW)
                .append(Component.translatable(operating
                        ? "jade.biotech.machine.operating"
                        : "jade.biotech.machine.idle").withStyle(ChatFormatting.WHITE)));

        long energyCapacity = data.getLongOr("EnergyCapacity", 0L);
        long energyStored = data.getLongOr("EnergyStored", 0L);
        if (energyCapacity > 0) {
            float ratio = Mth.clamp((float) energyStored / (float) energyCapacity, 0F, 1F);
            String value = accessor.showDetails()
                    ? compact(energyStored) + "/" + compact(energyCapacity) + " FE"
                    : compact(energyStored) + " FE";
            addLabeledBar(tooltip, "Energy", ratio, value, ENERGY_COLOR);
        }

        int fluidCount = data.getIntOr("FluidCount", 0);
        for (int i = 0; i < fluidCount; i++) {
            int capacity = data.getIntOr("FluidCapacity" + i, 0);
            int amount = data.getIntOr("FluidAmount" + i, 0);
            if (capacity <= 0) continue;

            float ratio = Mth.clamp((float) amount / (float) capacity, 0F, 1F);
            boolean output = data.getBooleanOr("FluidOutput" + i, false);
            String fluidName = data.getStringOr("FluidName" + i, "Fluid");
            String value = accessor.showDetails()
                    ? compact(amount) + "/" + compact(capacity) + " mB"
                    : compact(amount) + " mB";
            String label = output ? fluidName + " Out" : fluidName;
            addLabeledBar(tooltip, label, ratio, value,
                    output ? FLUID_OUTPUT_COLOR : FLUID_INPUT_COLOR);
        }

        int recipeCost = data.getIntOr("RecipeEnergyCost", 0);
        int consumed = data.getIntOr("EnergyConsumed", 0);
        if (recipeCost > 0) {
            float ratio = Mth.clamp((float) consumed / (float) recipeCost, 0F, 1F);
            int percent = Math.round(ratio * 100F);
            String value = accessor.showDetails()
                    ? percent + "%  " + compact(consumed) + "/" + compact(recipeCost) + " FE"
                    : percent + "%";
            addLabeledBar(tooltip, "Progress", ratio, value, PROGRESS_COLOR);
        }
    }

    private static void addLabeledBar(ITooltip tooltip, String label, float ratio, String value, int color) {
        tooltip.add(JadeUI.text(Component.literal(label + ":").withStyle(ChatFormatting.WHITE))
                .size(LABEL_WIDTH, BAR_HEIGHT)
                .offset(0, 2)
                .alignSelfCenter());
        tooltip.append(progressBar(ratio, Component.literal(value), color));
    }

    private static net.minecraft.client.gui.layouts.LayoutElement progressBar(float ratio, Component text, int color) {
        ProgressView.Part part = new ProgressView.PartBuilder()
                .progress(ratio)
                .color(color)
                .build();
        ProgressView view = new ProgressView(part, text.copy().withStyle(ChatFormatting.WHITE), JadeUI.progressStyle(), BoxStyle.nestedBox());
        return JadeUI.progress(view, BAR_WIDTH, BAR_HEIGHT);
    }

    private static String compact(long value) {
        if (value >= 1_000_000L) {
            double scaled = value / 1_000_000D;
            return scaled < 10D ? String.format(Locale.ROOT, "%.1fM", scaled) : String.format(Locale.ROOT, "%.0fM", scaled);
        }
        if (value >= 10_000L) return String.format(Locale.ROOT, "%.0fk", value / 1_000D);
        if (value >= 1_000L) return String.format(Locale.ROOT, "%.1fk", value / 1_000D);
        return Long.toString(value);
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
