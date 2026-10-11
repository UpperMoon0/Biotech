package com.nstut.biotech.gametest;

import com.nstut.biotech.machines.MachineStructures;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Native, shared across all three targets: real registered blocks, provider validator and hatch discovery. */
public final class MachineStructureChecks {
    private MachineStructureChecks() {}

    public static void allLayoutsAndRotations(GameTestHelper helper) {
        Level level = helper.getLevel();
        // One synchronous fixture at a time; clear every touched block after each case.
        BlockPos anchor = helper.absolutePos(new BlockPos(16, 2, 16));
        for (String id : MachineStructures.ids()) {
            for (Direction facing : new Direction[] {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST}) {
                for (boolean legacy : new boolean[] {false, true}) {
                    List<BlockPos> touched = new ArrayList<>();
                    try {
                        int[] origin = MachineStructures.origin(id);
                        MultiblockPattern design = MachineStructures.pattern(id);
                        MultiblockBlock controller = design.getPattern()[design.getPattern().length-origin[1]-1]
                                [design.getPattern()[0].length-origin[2]-1][origin[0]];
                        BlockState controllerState = controller.getBlock().defaultBlockState().setValue(MachineBlock.FACING, facing);
                        level.setBlock(anchor, controllerState, 2);
                        touched.add(anchor);
                        MachineBlockEntity machine = (MachineBlockEntity) level.getBlockEntity(anchor);
                        helper.assertTrue(machine != null, id + " controller exists");
                        if (legacy) design = (MultiblockPattern) machine.getClass().getMethod("getLegacyMultiblockPattern").invoke(machine);
                        List<BlockPos> ports = new ArrayList<>();
                        BlockPos requiredPart = null;
                        var array = design.getPattern();
                        for (int y=0; y<array.length; y++) for (int z=0; z<array[y].length; z++) for (int x=0; x<array[y][z].length; x++) {
                            MultiblockBlock part = array[y][z][x];
                            if (part == null) continue;
                            BlockPos pos = MultiblockPattern.rotateBlockPos(anchor, origin[0], origin[1], origin[2],
                                    array.length, array[y].length, x, y, z, controllerState);
                            BlockState state = part.getBlock().defaultBlockState();
                            for (var entry : part.getStates().entrySet()) {
                                String value = entry.getValue();
                                if (entry.getKey().equals("facing")) value = MultiblockPattern.rotateHorizontalDirection(facing, Direction.byName(value)).getName();
                                state = withProperty(state, entry.getKey(), value);
                            }
                            level.setBlock(pos, state, 2);
                            touched.add(pos);
                            if (!pos.equals(anchor) && level.getBlockEntity(pos) != null) ports.add(pos);
                            if (requiredPart == null && !pos.equals(anchor) && !state.isAir() && level.getBlockEntity(pos) == null) requiredPart = pos;
                        }
                        String context = id + "/" + facing + "/" + (legacy ? "2.5" : "redesigned");
                        helper.assertTrue(machine.checkMultiblock(level, anchor, controllerState), context + " must form");
                        var discover = machine.getClass().getDeclaredMethod("setHatches", BlockPos.class, Level.class);
                        discover.setAccessible(true);
                        discover.invoke(machine, anchor, level);
                        int discovered = 0;
                        for (Field field : machine.getClass().getDeclaredFields()) {
                            if (!BlockEntity.class.isAssignableFrom(field.getType())) continue;
                            field.setAccessible(true);
                            Object hatch = field.get(machine);
                            helper.assertTrue(hatch instanceof BlockEntity && ports.contains(((BlockEntity) hatch).getBlockPos()), context + " hatch field " + field.getName());
                            discovered++;
                        }
                        helper.assertTrue(discovered == ports.size(), context + " all hatch instances discovered");
                        helper.assertTrue(requiredPart != null, context + " has a structural part");
                        BlockState old = level.getBlockState(requiredPart);
                        level.setBlock(requiredPart, Blocks.AIR.defaultBlockState(), 2);
                        helper.assertTrue(!machine.checkMultiblock(level, anchor, controllerState), context + " missing part must fail");
                        level.setBlock(requiredPart, Blocks.DIRT.defaultBlockState(), 2);
                        helper.assertTrue(!machine.checkMultiblock(level, anchor, controllerState), context + " wrong part must fail");
                        level.setBlock(requiredPart, old, 2);
                        helper.assertTrue(machine.checkMultiblock(level, anchor, controllerState), context + " must reform");
                        for (BlockPos port : ports) {
                            BlockState valid = level.getBlockState(port);
                            BlockState wrong = valid.setValue(MachineBlock.FACING, valid.getValue(MachineBlock.FACING).getOpposite());
                            level.setBlock(port, wrong, 2);
                            helper.assertTrue(!machine.checkMultiblock(level, anchor, controllerState), context + " wrong hatch facing must fail");
                            level.setBlock(port, valid, 2);
                        }
                        level.setBlock(anchor, controllerState.setValue(MachineBlock.OPERATING, true), 2);
                        helper.assertTrue(machine.checkMultiblock(level, anchor, controllerState), context + " operating controller still forms");
                    } catch (ReflectiveOperationException exception) {
                        throw new AssertionError("Native structure fixture failed for " + id, exception);
                    } finally {
                        for (BlockPos pos : touched) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
        helper.succeed();
    }

    private static <T extends Comparable<T>> BlockState withProperty(BlockState state, String name, String value) {
        @SuppressWarnings("unchecked") Property<T> property = (Property<T>) state.getBlock().getStateDefinition().getProperty(name);
        if (property == null) throw new AssertionError("Missing property " + name);
        return state.setValue(property, property.getValue(value).orElseThrow());
    }
}
