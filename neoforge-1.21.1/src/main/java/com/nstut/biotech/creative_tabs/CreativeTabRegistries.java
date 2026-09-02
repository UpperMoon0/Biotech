package com.nstut.biotech.creative_tabs;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.items.ItemRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CreativeTabRegistries {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Biotech.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BIOTECH_TAB = CREATIVE_MODE_TABS.register("biotech", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistries.NET_TRAP_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (Supplier<Item> item : ItemRegistries.ITEM_SET) output.accept(item.get());
            })
            .title(Component.translatable("itemGroup.biotech"))
            .build());

    private CreativeTabRegistries() {
    }
}
