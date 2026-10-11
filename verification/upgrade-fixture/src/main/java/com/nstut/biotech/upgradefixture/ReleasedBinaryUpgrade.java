package com.nstut.biotech.upgradefixture;

import com.google.gson.GsonBuilder;
import com.nstut.biotech.Config;
import com.nstut.biotech.blocks.entites.hatches.*;
import com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.machines.RedstoneMode;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Separate mod: never compiled into or substituted for either published Biotech binary. */
@Mod("upgrade_fixture")
@EventBusSubscriber(modid="upgrade_fixture")
public final class ReleasedBinaryUpgrade {
    private static final String[] IDS={"greenhouse","breeding_chamber","terrestrial_habitat","slaughterhouse","mixer","fermenter"};
    private static final String[] RECIPES={"greenhouse_wheat","breeding_chamber_cow_t1_wheat","terrestrial_habitat_cow_milk_t1_wheat","slaughterhouse_cow","chicken_feed","fertilizer"};
    private static CompoundTag saved;
    private static final List<MachineBlockEntity> loaded=new ArrayList<>();
    private static boolean waiting;
    private static int ticks;
    private static int unloadedTicks;
    private static Path evidence;
    private static String phase;
    private static String binaryHash;
    public ReleasedBinaryUpgrade() {}

    @SubscribeEvent public static void started(ServerStartedEvent event) {
        var server=event.getServer();
        try {
            phase=System.getProperty("biotech.upgrade.phase");
            evidence=Path.of(System.getProperty("biotech.upgrade.evidence"));Files.createDirectories(evidence);
            Path expected=Path.of(System.getProperty("biotech.upgrade.jar"));
            Path actual=ModList.get().getModFileById("biotech").getFile().getFilePath();
            binaryHash=hash(actual);
            require(binaryHash.equals(hash(expected)),"Runtime Biotech must be the exact downloaded release JAR");
            String version=ModList.get().getModContainerById("biotech").orElseThrow().getModInfo().getVersion().toString();
            require(version.equals(phase.equals("seed")?"2.5":"2.6"),"Wrong runtime version: "+version);
            Config.machineEnergyPerTick=128;Config.machineEnergyMultiplier=1.0;
            if(phase.equals("seed"))seed(server);
            else beginVerify(server);
        } catch(Throwable failure) { fail(server,failure); }
    }

    private static void seed(MinecraftServer server)throws Exception {
        var level=server.overworld();saved=new CompoundTag();saved.putString("sourceVersion","2.5");saved.putString("sourceJarSha256",binaryHash);
        ListTag fixtures=new ListTag();
        for(int index=0;index<IDS.length;index++) {
            String id=IDS[index];int cx=4096+index*4,cz=4096;
            BlockPos pos=new BlockPos((cx<<4)+5,90,(cz<<4)+11);
            level.setChunkForced(cx,cz,true);
            var block=BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("biotech",id));
            var controller=block.defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH);
            level.setBlock(pos,controller,2);
            var machine=(MachineBlockEntity)level.getBlockEntity(pos);
            // This is the actual 2.5 implementation, not a copied or newly generated legacy pattern.
            var array=machine.getMultiblockPattern().getPattern();List<BlockPos> positions=new ArrayList<>();
            for(int y=0;y<array.length;y++)for(int z=0;z<array[y].length;z++)for(int x=0;x<array[y][z].length;x++) {
                var part=array[y][z][x];if(part==null)continue;
                var at=MultiblockPattern.rotateBlockPos(pos,machine.getSouthOffsetX(),machine.getSouthOffsetY(),machine.getSouthOffsetZ(),array.length,array[y].length,x,y,z,controller);
                require((at.getX()>>4)==cx&&(at.getZ()>>4)==cz,id+" fits one unloadable chunk");
                BlockState state=part.getBlock().defaultBlockState();for(var entry:part.getStates().entrySet())state=property(state,entry.getKey(),entry.getValue());
                level.setBlock(at,state,2);positions.add(at);
            }
            require(machine.checkMultiblock(level,pos,controller),id+" forms under 2.5");
            var recipe=(ModRecipe<?>)level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("biotech",RECIPES[index])).orElseThrow().value();
            var data=recipe.getRecipe();var inputs=hatches(level,positions,ItemInputHatchBlockEntity.class);
            int slot=0;
            for(var ingredient:data.getIngredientItems()) {
                var stack=ingredient.getItemStack().copy();
                if(CapturedAnimalStackState.entityTypeId(stack).equals("minecraft:cow")) {
                    var animal=new CompoundTag();animal.putInt("Age",0);animal.putString("UpgradeFixtureMarker",id);animal.putString("CustomName","\"Saved 2.5 animal\"");
                    CapturedAnimalStackState.writeCapture(stack,animal,"minecraft:cow",-1);
                }
                inputs.get(slot/9).getInternalItemStorage().setStackInSlot(slot%9,stack);slot++;
            }
            inputs.get(0).getInternalItemStorage().setStackInSlot(8,new ItemStack(Items.GOLD_INGOT,7));
            var fluidInputs=hatches(level,positions,FluidInputHatchBlockEntity.class);
            for(int i=0;i<data.getFluidIngredients().length;i++) {var fluid=data.getFluidIngredients()[i].copy();fluid.grow(250);fluidInputs.get(i).setFluid(fluid);}
            var output=hatches(level,positions,ItemOutputHatchBlockEntity.class).getFirst();output.getInternalItemStorage().setStackInSlot(8,new ItemStack(Items.DIAMOND,2));
            var energy=hatches(level,positions,EnergyInputHatchBlockEntity.class).getFirst();energy.setEnergy(128);
            tick(level,machine,1);
            require((boolean)field(machine,"ingredientsConsumed")&&(int)field(machine,"energyConsumed")==128,id+" has consumed inputs and real partial progress");
            ((ControlledMachineBlockEntity)machine).setRedstoneMode(RedstoneMode.HIGH);energy.setEnergy(32);
            var snapshot=(ModRecipeData)field(machine,"activeRecipeSnapshot");require(snapshot!=null,id+" saved snapshot exists");
            var fixture=new CompoundTag();fixture.putString("id",id);fixture.putLong("controller",pos.asLong());fixture.putInt("energy",snapshot.getTotalEnergy());
            ListTag expectedItems=new ListTag();expectedItems.add(new ItemStack(Items.DIAMOND,2).save(level.registryAccess()));
            for(int selected:(int[])field(machine,"activeItemOutputIndexes"))expectedItems.add(snapshot.getOutputItems()[selected].getItemStack().save(level.registryAccess()));
            fixture.put("expectedItems",expectedItems);
            ListTag expectedFluids=new ListTag();for(var fluid:snapshot.getFluidOutputs()) {var entry=new CompoundTag();entry.putString("id",BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());entry.putInt("amount",fluid.getAmount());expectedFluids.add(entry);}fixture.put("expectedFluids",expectedFluids);
            ListTag cells=new ListTag();
            for(var at:positions) {var cell=new CompoundTag();cell.putLong("pos",at.asLong());cell.putString("state",level.getBlockState(at).toString());var be=level.getBlockEntity(at);if(be!=null)cell.put("nbt",be.saveWithFullMetadata(level.registryAccess()));cells.add(cell);}fixture.put("cells",cells);fixtures.add(fixture);
            level.setChunkForced(cx,cz,false);
        }
        saved.put("fixtures",fixtures);NbtIo.writeCompressed(saved,evidence.resolve("v2.5-saved-state.nbt"));
        level.getChunkSource().save(true);finish(server,"Saved six active legacy machines with the unmodified 2.5 binary");
    }

    private static void beginVerify(MinecraftServer server)throws Exception {
        saved=NbtIo.readCompressed(evidence.resolve("v2.5-saved-state.nbt"),NbtAccounter.unlimitedHeap());
        require(saved.getString("sourceVersion").equals("2.5"),"Fixture source must be 2.5");
        var fixtures=saved.getList("fixtures",Tag.TAG_COMPOUND);require(fixtures.size()==6,"Exactly six old-binary fixtures required");
        var level=server.overworld();
        for(int i=0;i<fixtures.size();i++) {
            var fixture=fixtures.getCompound(i);var pos=BlockPos.of(fixture.getLong("controller"));
            level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);
            assertSaved(level,fixture);var machine=(MachineBlockEntity)level.getBlockEntity(pos);
            require(machine.checkMultiblock(level,pos,level.getBlockState(pos)),fixture.getString("id")+" original build still forms in 2.6");loaded.add(machine);
        }
        level.getChunkSource().save(true);
        for(var machine:loaded)level.setChunkForced(machine.getBlockPos().getX()>>4,machine.getBlockPos().getZ()>>4,false);
        waiting=true;
    }

    @SubscribeEvent public static void tickServer(ServerTickEvent.Post event) {
        if(!waiting)return;var server=event.getServer();var level=server.overworld();
        try {
            require(++ticks<1800,"All six chunks must naturally unload before timeout");
            for(var machine:loaded)if(level.getChunkSource().hasChunk(machine.getBlockPos().getX()>>4,machine.getBlockPos().getZ()>>4)){unloadedTicks=0;return;}
            if(++unloadedTicks<20)return;
            waiting=false;level.getChunkSource().save(true);
            var fixtures=saved.getList("fixtures",Tag.TAG_COMPOUND);
            for(int i=0;i<fixtures.size();i++) {
                var fixture=fixtures.getCompound(i);var pos=BlockPos.of(fixture.getLong("controller"));
                level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);
                assertSaved(level,fixture);var machine=(MachineBlockEntity)level.getBlockEntity(pos);
                require(machine!=loaded.get(i),fixture.getString("id")+" gets a fresh block entity after real chunk reload");
                complete(level,machine,fixture);
                level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,false);
            }
            finish(server,"All six 2.5 active cycles survived 2.6 load, disk chunk unload/reload and exact one-cycle completion");
        }catch(Throwable failure){waiting=false;fail(server,failure);}
    }

    private static void assertSaved(ServerLevel level,CompoundTag fixture) {
        var cells=fixture.getList("cells",Tag.TAG_COMPOUND);
        for(int i=0;i<cells.size();i++) {var cell=cells.getCompound(i);var pos=BlockPos.of(cell.getLong("pos"));require(level.getBlockState(pos).toString().equals(cell.getString("state")),"No block replacement at "+pos);if(cell.contains("nbt")){var be=level.getBlockEntity(pos);require(be!=null&&cell.getCompound("nbt").equals(be.saveWithFullMetadata(level.registryAccess())),"Exact old-binary controller/hatch NBT at "+pos);}}
    }
    private static void complete(ServerLevel level,MachineBlockEntity machine,CompoundTag fixture) {
        List<BlockPos> positions=new ArrayList<>();var cells=fixture.getList("cells",Tag.TAG_COMPOUND);for(int i=0;i<cells.size();i++)positions.add(BlockPos.of(cells.getCompound(i).getLong("pos")));
        var energy=hatches(level,positions,EnergyInputHatchBlockEntity.class).getFirst();energy.setEnergy(fixture.getInt("energy")-128);((ControlledMachineBlockEntity)machine).setRedstoneMode(RedstoneMode.IGNORE);
        tick(level,machine,fixture.getInt("energy")/128+5);
        require(energy.getInternalEnergyStorage().getEnergyStored()==0,"Exact remaining FE only");
        for(int i=0;i<cells.size();i++){var cell=cells.getCompound(i);var be=level.getBlockEntity(BlockPos.of(cell.getLong("pos")));if(be instanceof ItemInputHatchBlockEntity||be instanceof FluidInputHatchBlockEntity)require(cell.getCompound("nbt").equals(be.saveWithFullMetadata(level.registryAccess())),"No second item/fluid consumption, no animal or stored payload changes");}
        var output=hatches(level,positions,ItemOutputHatchBlockEntity.class).getFirst();var itemCapability=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,output.getBlockPos(),output.getBlockState().getValue(MachineBlock.FACING));require(itemCapability!=null,"Exposed item output capability");
        List<ItemStack> expected=new ArrayList<>();var encoded=fixture.getList("expectedItems",Tag.TAG_COMPOUND);for(int i=0;i<encoded.size();i++)expected.add(ItemStack.parseOptional(level.registryAccess(),encoded.getCompound(i)));
        for(int slot=0;slot<itemCapability.getSlots();slot++){var stack=itemCapability.extractItem(slot,Integer.MAX_VALUE,false);int left=stack.getCount();for(var candidate:expected){if(ItemStack.isSameItemSameComponents(stack,candidate)){int used=Math.min(left,candidate.getCount());candidate.shrink(used);left-=used;}}require(left==0,"No changed or duplicated saved item result");}require(expected.stream().allMatch(ItemStack::isEmpty),"All exact saved output items emitted");
        var fluidOutputs=hatches(level,positions,FluidOutputHatchBlockEntity.class);var fluidExpected=fixture.getList("expectedFluids",Tag.TAG_COMPOUND);
        for(int i=0;i<fluidOutputs.size();i++){var hatch=fluidOutputs.get(i);var cap=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,hatch.getBlockPos(),hatch.getBlockState().getValue(MachineBlock.FACING));require(cap!=null,"Exposed fluid output capability");var fluid=cap.drain(Integer.MAX_VALUE,net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);if(i<fluidExpected.size()){var expectedFluid=fluidExpected.getCompound(i);require(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString().equals(expectedFluid.getString("id"))&&fluid.getAmount()==expectedFluid.getInt("amount"),"Exact saved fluid output");}else require(fluid.isEmpty(),"No unexpected fluid output");}
        tick(level,machine,10);
        for(int i=0;i<output.getInternalItemStorage().getSlots();i++)require(output.getInternalItemStorage().getStackInSlot(i).isEmpty(),"No item output replay");for(var hatch:fluidOutputs)require(hatch.getInternalTank().getFluidInTank(0).isEmpty(),"No fluid output replay");
    }
    private static void finish(MinecraftServer server,String result)throws Exception {Files.writeString(evidence.resolve(phase+"-success.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("phase",phase,"binarySha256",binaryHash,"machines",Arrays.asList(IDS),"result",result,"sourceSha256",saved.getString("sourceJarSha256"))));server.saveEverything(false,true,true);server.halt(false);}
    private static void fail(MinecraftServer server,Throwable failure){failure.printStackTrace();try{Files.writeString(evidence.resolve("failure.txt"),failure.toString());}catch(Exception ignored){}server.halt(false);}
    private static String hash(Path path)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static Object field(MachineBlockEntity machine,String name)throws RuntimeException{try{var f=MachineBlockEntity.class.getDeclaredField(name);f.setAccessible(true);return f.get(machine);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
    private static void tick(ServerLevel level,MachineBlockEntity machine,int count){for(int i=0;i<count;i++)MachineBlockEntity.serverTick(level,machine.getBlockPos(),level.getBlockState(machine.getBlockPos()),machine);}
    private static <T extends BlockEntity> List<T> hatches(ServerLevel level,List<BlockPos> positions,Class<T> type){List<T> result=new ArrayList<>();for(var pos:positions){var be=level.getBlockEntity(pos);if(type.isInstance(be))result.add(type.cast(be));}return result;}
    private static <T extends Comparable<T>> BlockState property(BlockState state,String name,String value){@SuppressWarnings("unchecked") Property<T> p=(Property<T>)state.getBlock().getStateDefinition().getProperty(name);return state.setValue(p,p.getValue(value).orElseThrow());}
}
