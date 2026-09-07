package com.nstut.biotech.preview;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.nstut.openui.api.UIComponent;
import com.nstut.openui.api.UiRender;
import com.nstut.openui.minecraft.UiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Separate development mod, loaded exclusively by runUiPreview. Never included in Biotech's JAR. */
@Mod(value = "biotech_ui_preview", dist = Dist.CLIENT)
@EventBusSubscriber(modid = "biotech_ui_preview", value = Dist.CLIENT)
public final class UiPreviewRunner {
    private record ImageEntry(String file, int width, int height) { }
    private static final int PADDING = 12;
    private static final List<ImageEntry> images = new ArrayList<>();
    private static List<PreviewFixtures.Preview> previews;
    private static int index;
    private static boolean advance;
    private static Path output;

    public UiPreviewRunner() { }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        try {
            if (previews == null && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                output = Path.of(System.getProperty("biotech.uiPreview.output"));
                Files.createDirectories(output);
                previews = PreviewFixtures.all();
                if (previews.size() != 28 || previews.stream().map(PreviewFixtures.Preview::name).distinct().count() != 28) {
                    throw new IllegalStateException("Expected 28 unique preview cases");
                }
                mc.setScreen(new PreviewScreen(previews.get(0)));
            } else if (advance) {
                advance = false;
                if (++index < previews.size()) mc.setScreen(new PreviewScreen(previews.get(index)));
                else {
                    writeResults();
                    mc.stop();
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot write UI previews", exception);
        }
    }

    private static void writeResults() throws IOException {
        StringBuilder html = new StringBuilder("<!doctype html><html lang=\"en\"><meta charset=\"utf-8\"><title>Biotech UI previews</title>"
                + "<style>body{background:#101713;color:#e4efe8;font:16px sans-serif;margin:24px}main{display:flex;flex-wrap:wrap;gap:20px}"
                + "figure{margin:0}img{image-rendering:pixelated;max-width:100%}figcaption{margin:8px 0}</style>"
                + "<h1>Biotech UI previews</h1><p>Shared production layouts rendered with fixed sample data on NeoForge 1.21.1.</p><main>");
        for (ImageEntry image : images) html.append("<figure><img src=\"").append(image.file()).append("\" alt=\"")
                .append(image.file()).append("\"><figcaption>").append(image.file()).append("</figcaption></figure>");
        Files.writeString(output.resolve("index.html"), html.append("</main></html>").toString());
        // The manifest is the success marker and is written only after every PNG and the gallery exist.
        Files.writeString(output.resolve("manifest.json"), new GsonBuilder().setPrettyPrinting().create().toJson(images));
    }

    private static final class PreviewScreen extends UiScreen {
        private final PreviewFixtures.Preview preview;
        private int frames;
        private boolean captured;

        PreviewScreen(PreviewFixtures.Preview preview) {
            super(Component.literal(preview.name()));
            this.preview = preview;
        }
        @Override protected int uiLeft() { return (width - preview.width()) / 2; }
        @Override protected int uiTop() { return (height - preview.height()) / 2; }
        @Override protected int uiWidth() { return preview.width(); }
        @Override protected int uiHeight() { return preview.height(); }
        @Override protected UIComponent buildUI() { return preview.content().get(); }

        // The preview paints its own backdrop; vanilla's menu blur would soften the panel beneath it.
        @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) { }

        @Override public void render(GuiGraphics g, int mx, int my, float pt) {
            g.fill(0, 0, width, height, 0xFF101713);
            UiRender.roundedOutline(g, uiLeft(), uiTop(), uiWidth(), uiHeight(), 6, 0xFF192622, 0xFF517464);
            // A neutral pointer and partial tick keep hover effects and animations out of baseline previews.
            super.render(g, -1, -1, 0);
            if (!captured && ++frames >= 8) {
                g.flush();
                capture();
                captured = true;
                advance = true;
            }
        }

        private void capture() {
            Minecraft mc = Minecraft.getInstance();
            int scale = (int) mc.getWindow().getGuiScale();
            if (scale != 2) throw new IllegalStateException("Preview requires GUI scale 2, got " + scale);
            int cropWidth = (uiWidth() + PADDING * 2) * scale;
            int cropHeight = (uiHeight() + PADDING * 2) * scale;
            int left = (uiLeft() - PADDING) * scale, top = (uiTop() - PADDING) * scale;
            try (NativeImage frame = Screenshot.takeScreenshot(mc.getMainRenderTarget());
                 NativeImage cropped = new NativeImage(cropWidth, cropHeight, false)) {
                for (int y = 0; y < cropHeight; y++) {
                    for (int x = 0; x < cropWidth; x++) cropped.setPixelRGBA(x, y, frame.getPixelRGBA(left + x, top + y));
                }
                String name = preview.name() + ".png";
                cropped.writeToFile(output.resolve(name));
                images.add(new ImageEntry(name, cropWidth, cropHeight));
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot capture " + preview.name(), exception);
            }
        }
    }
}
