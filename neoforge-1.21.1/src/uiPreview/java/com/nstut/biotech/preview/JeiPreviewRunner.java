package com.nstut.biotech.preview;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.jei.*;
import com.nstut.biotech.recipes.*;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Real JEI layouts, renderer, tooltips and input handlers. Loaded only in the preview source set. */
@JeiPlugin
@EventBusSubscriber(modid = "biotech_ui_preview", value = Dist.CLIENT)
public final class JeiPreviewRunner implements IModPlugin {
    private enum Mode { CARD, SCROLLED, TINY, EXACT, NEAR_CERTAIN, CATALYST, MILK }
    private record Case(String name, IRecipeLayoutDrawable<?> layout, Mode mode) { }
    private record ImageEntry(String file, int width, int height, String mode) { }
    private static IJeiRuntime runtime;
    private static List<Case> cases;
    private static final List<ImageEntry> images = new ArrayList<>();
    private static Path output;
    private static int index;
    private static boolean advance;
    private static boolean stopping;

    @Override public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("biotech", "jei_preview");
    }
    @Override public void onRuntimeAvailable(IJeiRuntime value) { runtime = value; }
    @Override public void onRuntimeUnavailable() { runtime = null; }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("biotech.jeiPreview.enabled") || stopping) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (cases == null && runtime != null && mc.level != null && mc.player != null
                    && mc.screen == null && mc.getOverlay() == null) {
                output = Path.of(System.getProperty("biotech.jeiPreview.output"));
                Files.createDirectories(output);
                cases = createCases();
                require(cases.size() == 14, "Expected 14 distinct JEI cases");
                mc.setScreen(new PreviewScreen(cases.get(0)));
            } else if (advance) {
                advance = false;
                if (++index < cases.size()) mc.setScreen(new PreviewScreen(cases.get(index)));
                else {
                    require(images.size() == cases.size(), "Cannot finish an incomplete JEI preview run");
                    Files.writeString(output.resolve("manifest.json"), new GsonBuilder().setPrettyPrinting().create().toJson(images));
                    // Minecraft can render one final frame after stop() tears down JEI. Detach
                    // its screen first, and guard an already-captured screen reference as well.
                    stopping = true;
                    mc.setScreen(null);
                    mc.stop();
                }
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Unable to save JEI previews", failure);
        }
    }

    private static List<Case> createCases() {
        var gui = runtime.getJeiHelpers().getGuiHelper();
        List<Case> result = new ArrayList<>();
        result.add(make("breeding-multiple-outputs", new BreedingChamberCategory(gui), d -> new BreedingChamberRecipe(id("breeding"), d), false, Mode.CARD));
        result.add(make("habitat-multiple-outputs", new TerrestrialHabitatCategory(gui), d -> new TerrestrialHabitatRecipe(id("habitat"), d), false, Mode.CARD));
        result.add(make("slaughterhouse-static", new SlaughterhouseCategory(gui), d -> new SlaughterhouseRecipe(id("slaughter"), d), false, Mode.CARD));
        result.add(make("greenhouse-multiple-outputs", new GreenhouseCategory(gui), d -> new GreenhouseRecipe(id("greenhouse"), d), false, Mode.CARD));
        result.add(make("fermenter-multiple-outputs", new FermenterCategory(gui), d -> new FermenterRecipe(id("fermenter"), d), false, Mode.CARD));
        result.add(make("mixer-multiple-outputs", new MixerCategory(gui), d -> new MixerRecipe(id("mixer"), d), false, Mode.CARD));
        result.add(make("mixer-scrolled-items-and-fluids", new MixerCategory(gui), d -> new MixerRecipe(id("scrolled"), d), false, Mode.SCROLLED));
        result.add(make("mixer-tiny-chance", new MixerCategory(gui), d -> new MixerRecipe(id("tiny"), d), false, Mode.TINY));
        result.add(make("mixer-exact-chance", new MixerCategory(gui), d -> new MixerRecipe(id("exact"), d), false, Mode.EXACT));
        result.add(make("mixer-near-certain-chance", new MixerCategory(gui), d -> new MixerRecipe(id("near_certain"), d), false, Mode.NEAR_CERTAIN));
        result.add(make("habitat-adult-catalyst", new TerrestrialHabitatCategory(gui), d -> new TerrestrialHabitatRecipe(id("adult"), d), true, Mode.CATALYST));
        result.add(make("habitat-item-fluid-layout", new TerrestrialHabitatCategory(gui), d -> new TerrestrialHabitatRecipe(id("milk"), d), true, Mode.CARD));
        result.add(make("habitat-milk-rate", new TerrestrialHabitatCategory(gui), d -> new TerrestrialHabitatRecipe(id("milk_rate"), d), true, Mode.MILK));
        SlaughterhouseRecipe dynamic = new SlaughterhouseRecipe(id("dynamic"), new ModRecipeData(
                new IngredientItem[]{new IngredientItem(new ItemStack(ItemRegistries.COW.get()), true)},
                new OutputItem[0], new FluidStack[0], new FluidStack[0], 32000));
        require(dynamic.usesEntityLoot(), "Empty authored item outputs must advertise dynamic loot");
        result.add(new Case("slaughterhouse-dynamic", create(new SlaughterhouseCategory(gui), dynamic), Mode.CARD));
        return result;
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("biotech", "jei_preview/" + path); }

    private static <T extends ModRecipe<T>> Case make(String name, IRecipeCategory<T> category,
                                                      Function<ModRecipeData, T> factory, boolean milk, Mode mode) {
        ModRecipeData data = fixture(milk);
        T recipe = factory.apply(data);
        IRecipeLayoutDrawable<T> layout = create(category, recipe);
        int expected = recipe.getItemIngredients().size() + recipe.getFluidIngredients().size()
                + recipe.getItemOutputs().size() + recipe.getFluidOutputs().size();
        require(layout.getRecipeSlotsView().getSlotViews().size() == expected, name + ": missing actual JEI slots");
        require(slot(layout, "input-item-0").getRole() == JeiIngredientRoles.input(false), name + ": lost catalyst role");
        require(tooltip(layout, "input-item-0").contains("Requires an adult"), name + ": missing adult requirement");
        require(tooltip(layout, "input-item-0").contains("not consumed"), name + ": missing reusable disclosure");
        if (!milk) {
            require(tooltip(layout, "output-item-1").contains("0.001%"), name + ": tiny chance rounded away");
            require(tooltip(layout, "output-item-2").contains("12.3456%"), name + ": exact tooltip rounded");
            require(tooltip(layout, "output-item-3").contains("99.9999%"), name + ": uncertain chance shown as guaranteed");
            require(!tooltip(layout, "output-item-4").contains("Chance:"), name + ": guaranteed output cluttered");
        } else {
            require(tooltip(layout, "output-fluid-0").contains("1000 mB per cycle"), "Milk amount missing");
            require(tooltip(layout, "output-fluid-0").contains("19047.619"), "Milk nominal production rate is incorrect");
        }
        return new Case(name, layout, mode);
    }

    private static ModRecipeData fixture(boolean milk) {
        List<IngredientItem> inputs = new ArrayList<>();
        inputs.add(new IngredientItem(new ItemStack(ItemRegistries.COW.get()), false));
        for (int i = 0; i < (milk ? 1 : 10); i++) inputs.add(new IngredientItem(new ItemStack(Items.WHEAT, i + 1), true));
        List<OutputItem> outputs = new ArrayList<>();
        float[] chances = {0, 0.00001f, 0.123456f, 0.999999f, 1};
        for (int i = 0; i < (milk ? 3 : 12); i++) outputs.add(new OutputItem(
                new ItemStack(i % 2 == 0 ? Items.EGG : Items.BONE_MEAL, i + 1), milk ? 1 : chances[Math.min(i, 4)]));
        return new ModRecipeData(inputs.toArray(IngredientItem[]::new), outputs.toArray(OutputItem[]::new),
                new FluidStack[]{new FluidStack(Fluids.WATER, 250), new FluidStack(Fluids.LAVA, 10)},
                new FluidStack[]{new FluidStack(NeoForgeMod.MILK.get(), 1000), new FluidStack(Fluids.WATER, 500)}, 32000);
    }

    private static <T> IRecipeLayoutDrawable<T> create(IRecipeCategory<T> category, T recipe) {
        return runtime.getRecipeManager().createRecipeLayoutDrawable(category, recipe,
                runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup())
                .orElseThrow(() -> new IllegalStateException("JEI failed to create " + category.getTitle().getString()));
    }
    private static IRecipeSlotView slot(IRecipeLayoutDrawable<?> layout, String name) {
        return layout.getRecipeSlotsView().findSlotByName(name).orElseThrow();
    }
    @SuppressWarnings("deprecation")
    private static String tooltip(IRecipeLayoutDrawable<?> layout, String name) {
        return ((IRecipeSlotDrawable) slot(layout, name)).getTooltip().stream().map(Component::getString)
                .reduce("", (first, next) -> first + "\n" + next);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static final class PreviewScreen extends Screen {
        private final Case test;
        private int frames;
        private int left;
        private int top;
        private boolean captured;

        PreviewScreen(Case test) { super(Component.literal(test.name())); this.test = test; }
        @Override protected void init() {
            left = (width - JeiRecipeLayout.WIDTH) / 2;
            top = (height - JeiRecipeLayout.HEIGHT) / 2;
            test.layout().setPosition(left, top);
        }
        @Override public void renderBackground(GuiGraphics graphics, int x, int y, float partialTick) { }
        @Override public void render(GuiGraphics graphics, int x, int y, float partialTick) {
            if (stopping) return;
            require(runtime != null, "JEI runtime disappeared before all preview assertions completed");
            graphics.fill(0, 0, width, height, 0xFF202820);
            graphics.drawCenteredString(font, test.name(), width / 2, top - 20, 0xFFFFFFFF);
            int mouseX = -1, mouseY = -1;
            int hoverIndex = switch (test.mode()) {
                case TINY -> 1;
                case EXACT -> 2;
                case NEAR_CERTAIN -> 3;
                case MILK -> 3;
                default -> -1;
            };
            if (hoverIndex >= 0) {
                mouseX = left + JeiRecipeLayout.OUTPUT_X + hoverIndex % 3 * 18 + 8;
                mouseY = top + JeiRecipeLayout.GRID_Y + hoverIndex / 3 * 18 + 8;
            } else if (test.mode() == Mode.CATALYST) {
                mouseX = left + 8;
                mouseY = top + JeiRecipeLayout.GRID_Y + 8;
            }
            test.layout().drawRecipe(graphics, mouseX, mouseY);
            test.layout().drawOverlays(graphics, mouseX, mouseY);
            frames++;
            if (test.mode() == Mode.SCROLLED && frames == 2) {
                // The layout input handler receives screen coordinates and dispatches to its grids.
                require(test.layout().getInputHandler().handleMouseScrolled(left + 118, top + 20, 0, -100), "Output scroll not handled");
                require(test.layout().getInputHandler().handleMouseScrolled(left + 8, top + 20, 0, -100), "Input scroll not handled");
            }
            if (!captured && frames >= 8) {
                if (test.mode() == Mode.SCROLLED) {
                    String outputName = test.layout().getSlotUnderMouse(left + 136, top + 56).orElseThrow().slot().getSlotName().orElseThrow();
                    require(outputName.equals("output-fluid-1"), "Final fluid output unreachable after scrolling: " + outputName);
                    String inputName = test.layout().getSlotUnderMouse(left + 8, top + 56).orElseThrow().slot().getSlotName().orElseThrow();
                    require(inputName.equals("input-fluid-1"), "Final fluid input unreachable after scrolling: " + inputName);
                }
                if (hoverIndex >= 0 || test.mode() == Mode.CATALYST) {
                    require(test.layout().getSlotUnderMouse(mouseX, mouseY).isPresent(), "Tooltip pointer did not hit a real slot");
                }
                graphics.flush();
                capture();
                captured = true;
                advance = true;
            }
        }
        private void capture() {
            Minecraft mc = Minecraft.getInstance();
            require((int) mc.getWindow().getGuiScale() == 2, "JEI preview requires GUI scale 2");
            try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                String filename = test.name() + ".png";
                image.writeToFile(output.resolve(filename));
                images.add(new ImageEntry(filename, image.getWidth(), image.getHeight(), test.mode().name()));
            } catch (IOException failure) { throw new IllegalStateException("Unable to capture " + test.name(), failure); }
        }
    }
}
