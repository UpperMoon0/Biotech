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
