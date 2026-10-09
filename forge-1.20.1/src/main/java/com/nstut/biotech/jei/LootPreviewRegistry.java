package com.nstut.biotech.jei;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "biotech", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
final class LootPreviewRegistry {
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void disconnect(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        SlaughterhouseLootPreview.clear();
    }
    static Item item(String id) { return BuiltInRegistries.ITEM.getOptional(new ResourceLocation(id)).orElse(null); }
}
