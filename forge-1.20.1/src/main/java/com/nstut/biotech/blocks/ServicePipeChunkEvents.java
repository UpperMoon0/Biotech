package com.nstut.biotech.blocks;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "biotech")
public final class ServicePipeChunkEvents {
    private ServicePipeChunkEvents() {}
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onLoad(net.minecraftforge.event.level.ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
                || !(event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk chunk)) return;
        ServicePipeChunkRepair.enqueue(level, chunk);
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void afterTick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && event.level instanceof net.minecraft.server.level.ServerLevel level)
            ServicePipeChunkRepair.afterTick(level);
    }
}
