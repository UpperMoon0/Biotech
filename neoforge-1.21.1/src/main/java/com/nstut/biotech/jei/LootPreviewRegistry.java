package com.nstut.biotech.jei;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
@net.neoforged.fml.common.EventBusSubscriber(modid = "biotech", value = net.neoforged.api.distmarker.Dist.CLIENT)
final class LootPreviewRegistry {
    @net.neoforged.bus.api.SubscribeEvent
    public static void disconnect(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        SlaughterhouseLootPreview.clear();
    }
    static Item item(String id) { return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).orElse(null); }
}
