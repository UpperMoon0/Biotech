package com.nstut.biotech.blocks;

@net.neoforged.fml.common.EventBusSubscriber(modid = "biotech")
public final class ServicePipeChunkEvents {
    private ServicePipeChunkEvents() {}
    @net.neoforged.bus.api.SubscribeEvent
    public static void onLoad(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
                || !(event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk chunk)) return;
        ServicePipeChunkRepair.enqueue(level, chunk);
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void afterTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
            ServicePipeChunkRepair.afterTick(level);
    }
}
