package com.nstut.biotech.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/** Rehydrates derived pipe state when old palette entries or neighboring chunks load. */
public final class ServicePipeChunkRepair {
    private ServicePipeChunkRepair() {}

    private static final java.util.Map<net.minecraft.server.level.ServerLevel,
            java.util.Set<LevelChunk>> PENDING = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    public static void enqueue(net.minecraft.server.level.ServerLevel level, LevelChunk chunk) {
        synchronized (PENDING) { PENDING.computeIfAbsent(level, ignored -> new java.util.HashSet<>()).add(chunk); }
    }

    /** Called only at the end of a server world tick, after chunk integration. */
    public static void afterTick(net.minecraft.server.level.ServerLevel level) {
        java.util.Set<LevelChunk> pending;
        synchronized (PENDING) { pending = PENDING.remove(level); }
        if (pending == null) return;
        for (var chunk : pending)
            if (level.getChunkSource().getChunkNow(chunk.getPos().x, chunk.getPos().z) == chunk) repair(level, chunk);
    }

    public static void repair(Level level, LevelChunk chunk) {
        var sections = chunk.getSections();
        int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            var section = sections[sectionIndex];
            if (!section.maybeHas(state -> state.getBlock() instanceof ServicePipeBlockBase)) continue;
            int minY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
            for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                if (!(section.getBlockState(x, y, z).getBlock() instanceof ServicePipeBlockBase)) continue;
                var pos = new BlockPos(minX+x, minY+y, minZ+z);
                repairOne(level, pos);
                // The other side may have loaded before this chunk. Do not force-load it.
                if (x == 0 || x == 15 || z == 0 || z == 15)
                    for (Direction direction : Direction.Plane.HORIZONTAL)
                        repairOne(level, pos.relative(direction));
            }
        }
    }

    private static void repairOne(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return;
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ServicePipeBlockBase)) return;
        var connected = state;
        for (Direction direction : Direction.values()) {
            var neighbor = pos.relative(direction);
            // Unknown neighbors retain their saved flag until their own load event repairs both sides.
            if (level.hasChunkAt(neighbor)) connected = connected.setValue(ServicePipeBlockBase.connection(direction),
                    level.getBlockState(neighbor).getBlock() instanceof ServicePipeBlockBase);
        }
        // Notify clients and persist, without traversing neighbor shapes into unloaded chunks.
        if (connected != state) level.setBlock(pos, connected, 2 | 16);
    }
}
