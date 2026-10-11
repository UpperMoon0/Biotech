package com.nstut.biotech.preview;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.nstut.biotech.machines.MachineStructures;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Actual placed-world screenshots, isolated development mod only; not included in published JARs. */
@EventBusSubscriber(modid = "biotech_ui_preview", value = Dist.CLIENT)
public final class MachineGalleryRunner {
    private record Capture(String file, String machine, String layout, String view, int width, int height, boolean formed) {}
    private static final List<Capture> captures = new ArrayList<>();
    private static int index;
    private static int settledTicks;
    private static boolean pending;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static volatile BlockPos controllerPos;
    private static Path output;
    private static boolean stopped;
    private MachineGalleryRunner() {}

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("biotech.machineGallery.enabled") || stopped) return;
        Minecraft mc=Minecraft.getInstance();
        if(failure!=null)throw new IllegalStateException("Machine gallery failed",failure);
        if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null||mc.screen!=null||mc.getOverlay()!=null)return;
        try {
            if(output==null){output=Path.of(System.getProperty("biotech.machineGallery.output"));Files.createDirectories(output);mc.options.hideGui=true;}
            if(!pending){
                if(index==24){finish();stopped=true;mc.stop();return;}
                pending=true;ready=false;settledTicks=0;
                int scene=index;
                mc.getSingleplayerServer().execute(()->prepare(mc,scene));
            } else if(ready&&++settledTicks>=100){
                var be=mc.level.getBlockEntity(controllerPos);
                if(!(be instanceof MachineBlockEntity machine)||!machine.checkMultiblock(mc.level,controllerPos,mc.level.getBlockState(controllerPos)))
                    throw new IllegalStateException("Rendered client structure is missing or invalid: "+controllerPos);
                String id=MachineStructures.ids()[index/4];
                String layout=index%4<2?"before-2.5":"after-redesign";
                String view=index%2==0?"front-left":"rear-right";
                String name=id+"-"+layout+"-"+view+".png";
                try(NativeImage frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                    frame.writeToFile(output.resolve(name));
                    captures.add(new Capture(name,id,layout,view,frame.getWidth(),frame.getHeight(),true));
                }
                index++;pending=false;
            }
        }catch(Exception ex){throw new IllegalStateException("Cannot capture placed-world machine gallery",ex);}
    }

    private static void prepare(Minecraft mc,int scene){
        try{
            var server=mc.getSingleplayerServer();
            var player=server.getPlayerList().getPlayer(mc.player.getUUID());
            if(player==null)throw new IllegalStateException("Preview player missing from integrated server");
            ServerLevel level=server.overworld();
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
            level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false,server);
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0,server);
            level.setDayTime(6000);level.setWeatherParameters(100000,0,false,false);
            String id=MachineStructures.ids()[scene/4];
            int[] origin=MachineStructures.origin(id);
            var modern=MachineStructures.pattern(id);
            var a=modern.getPattern();
            BlockState state=a[a.length-origin[1]-1][a[0].length-origin[2]-1][origin[0]].getBlock().defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH);
            MachineBlockEntity blueprint=(MachineBlockEntity)((net.minecraft.world.level.block.EntityBlock)state.getBlock()).newBlockEntity(BlockPos.ZERO,state);
            MultiblockPattern legacy=(MultiblockPattern)blueprint.getClass().getMethod("getLegacyMultiblockPattern").invoke(blueprint);
            var chosen=(scene%4<2?legacy:modern).getPattern();
            int h=chosen.length,d=chosen[0].length,w=chosen[0][0].length;
            int sx=16-w/2,sz=16-d/2;
            BlockPos anchor=new BlockPos(sx+origin[0],80+origin[1],sz+d-origin[2]-1);
            if(scene%2==0){
                // Blank fixed studio pad in a copied test world. No user save is touched.
                for(int x=-8;x<=40;x++)for(int z=-8;z<=40;z++){
                    level.setBlock(new BlockPos(x,79,z),Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),2);
                    for(int y=80;y<=100;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                }
                for(int y=0;y<h;y++)for(int z=0;z<d;z++)for(int x=0;x<w;x++){
                    var part=chosen[y][z][x];if(part==null)continue;
                    BlockState placed=part.getBlock().defaultBlockState();
                    for(var entry:part.getStates().entrySet())placed=property(placed,entry.getKey(),entry.getValue());
                    level.setBlock(new BlockPos(sx+x,80+h-y-1,sz+z),placed,2);
                }
            }
            var machine=(MachineBlockEntity)level.getBlockEntity(anchor);
            if(machine==null||!machine.checkMultiblock(level,anchor,level.getBlockState(anchor)))throw new IllegalStateException("Gallery fixture does not form: "+id);
            controllerPos=anchor;
            // Same lens, distance, lighting and target for each before/after pair.
            int maxH=Math.max(legacy.getPattern().length,a.length);
            int maxW=Math.max(legacy.getPattern()[0][0].length,a[0][0].length);
            int maxD=Math.max(legacy.getPattern()[0].length,a[0].length);
            double radius=Math.max(11,Math.max(maxW,maxD)*1.35);
            double dx=scene%2==0?-radius:radius,dz=-dx;
            double targetY=80+maxH/2.0,eyeY=80+maxH+5;
            float yaw=(float)Math.toDegrees(Math.atan2(dx,-dz));
            float pitch=(float)Math.toDegrees(Math.atan2(eyeY-targetY,Math.sqrt(dx*dx+dz*dz)));
            player.setGameMode(GameType.SPECTATOR);
            player.teleportTo(level,16+dx,eyeY-player.getEyeHeight(),16+dz,yaw,pitch);
            ready=true;
        }catch(Throwable ex){failure=ex;}
    }
    private static <T extends Comparable<T>> BlockState property(BlockState state,String name,String value){
        @SuppressWarnings("unchecked") Property<T> p=(Property<T>)state.getBlock().getStateDefinition().getProperty(name);
        return state.setValue(p,p.getValue(value).orElseThrow());
    }
    private static void finish()throws Exception{
        StringBuilder html=new StringBuilder("<!doctype html><meta charset=utf-8><title>Biotech placed-world comparison</title><style>body{background:#182024;color:#eee;font:16px sans-serif}main{display:grid;grid-template-columns:1fr 1fr;gap:20px}img{width:100%}figure{margin:0}</style><h1>Actual in-game multiblocks</h1><p>NeoForge 1.21.1, noon, fixed lens and matching camera distance per machine; front-left and rear-right show controller and automation faces. Both old and new layouts were placed and validated in the integrated server and client world.</p><main>");
        for(var capture:captures)html.append("<figure><img src=\"").append(capture.file()).append("\"><figcaption>").append(capture.file()).append("</figcaption></figure>");
        Files.writeString(output.resolve("index.html"),html.append("</main>").toString());
        Files.writeString(output.resolve("manifest.json"),new GsonBuilder().setPrettyPrinting().create().toJson(captures));
    }
}
