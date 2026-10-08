package com.nstut.biotech.preview;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.nstut.openui.api.UIComponent;
import com.nstut.openui.api.UiRender;
import com.nstut.openui.minecraft.UiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
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
    private record ImageEntry(String file, int width, int height, String background, int guiScale, String effect) { }
    private static final int PADDING = 12;
    private static final List<ImageEntry> images = new ArrayList<>();
    private static List<PreviewFixtures.Preview> previews;
    private static int index;
    private static boolean advance;
    private static Path output;

    public UiPreviewRunner() { }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (Boolean.getBoolean("biotech.jeiPreview.enabled")) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (previews == null && mc.level != null && mc.player != null && mc.screen == null && mc.getOverlay() == null) {
                output = Path.of(System.getProperty("biotech.uiPreview.output"));
                Files.createDirectories(output);
                previews = PreviewFixtures.all();
                if (previews.size() != 87 || previews.stream().map(PreviewFixtures.Preview::name).distinct().count() != 87) {
                    throw new IllegalStateException("Expected 87 unique preview cases");
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
        private long firstRender;

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
            if (preview.textured()) {
                for (int yy = 0; yy < height; yy += 16) {
                    for (int xx = 0; xx < width; xx += 16) {
                        int color = ((xx / 16 + yy / 16) % 2 == 0) ? 0xFF59715A : 0xFF304B3F;
                        g.fill(xx, yy, xx + 15, yy + 15, color);
                    }
                }
            }
            com.nstut.biotech.views.openui.BiotechBackdrop.paint(
                    new com.nstut.openui.graphics.UiCanvas(g, font), uiLeft(), uiTop(), uiWidth(), uiHeight());
            // Chance tooltip cases use a fixed pointer; other previews keep neutral hover state.
            if (firstRender == 0) firstRender = System.nanoTime();
            int pointerX = -1, pointerY = -1;
            if (preview.name().equals("controller-chances-tiny") || preview.name().equals("controller-chances-fractional")) {
                pointerX = uiLeft() + com.nstut.biotech.views.openui.ControllerOutputLayout.VIEWPORT_X
                        + (preview.name().endsWith("-tiny") ? 8 : 36);
                pointerY = uiTop() + com.nstut.biotech.views.openui.ControllerOutputLayout.VIEWPORT_Y + 8;
            }
            super.render(g, pointerX, pointerY, 0);
            frames++;
            if (preview.name().startsWith("controller-") && preview.name().endsWith("-scrolled") && frames == 2) {
                if (!mouseScrolled(uiLeft() + 156, uiTop() + 114, 0, -100))
                    throw new IllegalStateException("Mixed controller products must accept native scrolling");
            }
            boolean hoveredChance = pointerX >= 0;
            if (!captured && frames >= 8 && (!hoveredChance || System.nanoTime() - firstRender >= 500_000_000L)) {
                if (hoveredChance && uiRuntime().overlays().components().stream()
                        .noneMatch(component -> component instanceof com.nstut.openui.controls.Tooltip))
                    throw new IllegalStateException("Chance screenshot must contain the native controller tooltip");
                if (preview.name().startsWith("controller-chances")) verifyChances();
                else if (preview.name().startsWith("controller-")) verifyMixedProducts();
                g.flush();
                capture();
                captured = true;
                advance = true;
            }
        }

        private void verifyChances() {
            var products = findProducts(uiRuntime().root());
            if (products == null) throw new IllegalStateException("Chance preview must use production controller outputs");
            String[] exact = {"0.001%", "12.3456%", "0%", "99.9999%"};
            for (int i = 0; i < exact.length; i++) {
                String tooltip = products.productAt(products.getX()
                        + com.nstut.biotech.views.openui.ControllerOutputLayout.itemX(i) + 8,
                        products.getY() + com.nstut.biotech.views.openui.ControllerOutputLayout.itemY(i) + 8);
                if (tooltip == null || !tooltip.endsWith(" (" + exact[i] + ")"))
                    throw new IllegalStateException("Controller chance tooltip lost precision: " + tooltip);
            }
        }

        private void verifyMixedProducts() {
            var products = findProducts(uiRuntime().root());
            if (products == null) throw new IllegalStateException("Controller preview must use the production combined output component");
            var viewport = products.parent();
            if (viewport.getY() != uiTop() + com.nstut.biotech.views.openui.ControllerOutputLayout.VIEWPORT_Y
                    || viewport.getHeight() != com.nstut.biotech.views.openui.ControllerOutputLayout.VIEWPORT_HEIGHT)
                throw new IllegalStateException("Controller output viewport drifted into the footer");
            if (products.productAt(viewport.getX() + 8, viewport.getY() + viewport.getHeight() + 2) != null)
                throw new IllegalStateException("Clipped controller outputs must not expose off-viewport hover targets");
            if (preview.name().endsWith("-scrolled")) {
                String product = products.productAt(viewport.getX() + 8, viewport.getY() + viewport.getHeight() - 20);
                String expected = preview.name().contains("long") ? "500 mB per cycle" : "1000 mB per cycle";
                if (product == null || !product.contains(expected))
                    throw new IllegalStateException("Final fluid product must be reachable after native scrolling: " + product);
            } else {
                String product = products.productAt(viewport.getX() + 8, viewport.getY() + 40);
                if (product == null || !product.contains("13%"))
                    throw new IllegalStateException("Third item and its chance must stay in the item lane: " + product);
            }
        }

        private com.nstut.biotech.views.openui.RecipeOutputs findProducts(UIComponent root) {
            if (root instanceof com.nstut.biotech.views.openui.RecipeOutputs products) return products;
            for (int i = 0; i < root.childCount(); i++) {
                var found = findProducts(root.child(i));
                if (found != null) return found;
            }
            return null;
        }

        private void capture() {
            Minecraft mc = Minecraft.getInstance();
            int scale = (int) mc.getWindow().getGuiScale();
            if (scale != 2) throw new IllegalStateException("Preview requires GUI scale 2, got " + scale);
            boolean hatch = uiWidth() == 176;
            int cropWidth = (uiWidth() + PADDING * 2 + (hatch ? 16 : 0)) * scale;
            int cropHeight = (uiHeight() + PADDING * 2 + (hatch ? 40 : 0)) * scale;
            int left = (uiLeft() - PADDING - (hatch ? 8 : 0)) * scale;
            int top = (uiTop() - PADDING - (hatch ? 32 : 0)) * scale;
            try (NativeImage frame = Screenshot.takeScreenshot(mc.getMainRenderTarget());
                 NativeImage cropped = new NativeImage(cropWidth, cropHeight, false)) {
                for (int y = 0; y < cropHeight; y++) {
                    for (int x = 0; x < cropWidth; x++) cropped.setPixelRGBA(x, y, frame.getPixelRGBA(left + x, top + y));
                }
                String name = preview.name() + ".png";
                cropped.writeToFile(output.resolve(name));
                images.add(new ImageEntry(name, cropWidth, cropHeight,
                        preview.textured() ? "textured" : "plain", scale, "tinted-gradient"));
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot capture " + preview.name(), exception);
            }
        }
    }
}
