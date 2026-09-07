# OpenUI migration

Targets: Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2.

The six machine screen definitions and three hatch screen bases are shared across targets. The five concrete hatch registrations remain unchanged. Rendering adapters account for Forge fluid names and the 26.1.2 graphics extraction API. The existing menus, packet synchronization, recipes, and slot interaction are unchanged.

The Mixer and Fermenter retain input grids; Breeding Chamber and Terrestrial Habitat retain animal-to-output diagrams with food and water; Greenhouse retains seed, fertilizer, and centered outputs; Slaughterhouse retains its vertical output list. Item counts and output chances remain visible, with item names available on hover. Energy, fluid, and progress tooltips read live synchronized values, including when the pointer is stationary. Unsynchronized capacities and invalid structures do not divide by zero or render stale active recipes.

OpenUI owns component layout, lifecycle, and overlays. Vanilla owns real inventory slots and item tooltips. Screen backgrounds use OpenUI drawing primitives before slot rendering. No texture-backed screen implementation remains. JEI's embedded recipe-category textures and Patchouli content remain because those integrations own their UI.

## Verification

- `./gradlew buildAll`: compile, package, unit tests, generated recipe parity, and guide validation for all targets.
- `./gradlew gameTestAll`: existing gameplay integration tests on all targets.
- Client checks: open every machine and hatch; inspect invalid, idle, and active machine states; hover energy, fluid, progress, and recipe outputs; resize and change GUI scale; close/reopen; exercise hatch pickup, drag, shift-click, carried stacks, and inventory-key dismissal.

The OpenUI source revision is pinned in `gradle.properties`; CI and release jobs publish that exact provider to Maven Local before compiling Biotech. OpenUI is required on the client only.

## PNG preview job

Run `./gradlew :neoforge-1.21.1:renderUiPreviews` (Windows: `gradlew.bat`) after publishing the pinned 1.21.1 providers. The job launches its own client, waits for resources to load at the title screen, renders each case, saves PNGs, and exits automatically. No world, account login, or manual clicks are needed. Linux without a display requires `xvfb-run -a` and an OpenGL implementation.

The output is `neoforge-1.21.1/build/ui-previews/`. Open `index.html` for a gallery, or use the individual PNGs. `manifest.json` records every file and its dimensions and is written only after successful completion. Each run clears its own previous output, and the Gradle task fails when the manifest or any PNG is missing or invalid.

The 28 cases cover all 11 screens:

- Six machines, each active, idle, and with an invalid structure (18 PNGs).
- Item input/output, fluid input/output, and energy input hatches, each empty and filled (10 PNGs).

Images use English labels, GUI scale 2, a flat background, fixed sample values, and no pointer hover. Machine PNGs are 528 × 416; hatch PNGs are 400 × 380. Machine and hatch content comes from the production `MachineUi` and `HatchUi` builders; hatch slots use deterministic display fixtures at menu coordinates. This is a visual preview job, not an inventory-interaction test or a pixel-perfect comparison against golden images. Software and hardware OpenGL may differ slightly.

The `UI PNG previews` GitHub Actions workflow runs on pull requests, pushes to main, and manual dispatch. Download the `biotech-ui-previews` artifact for the images and gallery. The canonical renderer runs on NeoForge 1.21.1, covering the shared layouts; it does not certify the rendering adapters on every Minecraft version.

The renderer is a separate `uiPreview` source set and development mod. Only `runUiPreview` loads it. Normal clients, servers, unit tests, and release JARs do not include the preview runner.
