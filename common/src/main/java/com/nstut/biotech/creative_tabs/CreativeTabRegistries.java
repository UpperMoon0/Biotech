package com.nstut.biotech.creative_tabs;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.items.ItemRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabRegistries {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Biotech.MOD_ID);
    public static final RegistryObject<CreativeModeTab> BIOTECH_TAB = CREATIVE_MODE_TABS.register("biotech", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistries.NET_TRAP_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (RegistryObject<Item> i : ItemRegistries.ITEM_SET) {
                    output.accept(i.get());
                }
            })
            .title(Component.translatable("itemGroup.biotech"))
            .build());
}
