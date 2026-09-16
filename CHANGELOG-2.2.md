# Biotech 2.2

## Changed

- Migrate all six machine screens and all five hatch screens to OpenUI across Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2.
- Require OpenUI MC 0.0.10 or newer on clients and pin source/CI builds to OpenUI main commit `90c515f643483f1690ea73c54745b06af9f21f61`.
- Replace the old screen textures with shared layered green glass surfaces while preserving vanilla inventory-slot interaction and tooltips.
- Normalize machine recipe layouts onto shared input, progress, output, secondary-requirement, energy, and fluid baselines so equivalent content no longer drifts between machines or states.
- Tighten item-count, output-chance, recipe-fluid, tank-value, and muted-text placement for consistent spacing and readability.

## Verification

- Add an isolated OpenUI preview client that renders 56 deterministic machine/hatch PNGs across plain and textured backgrounds.
- Render hatch inventory slots from production-created `Slot` objects instead of duplicated preview coordinates, and fail preview CI when real hatch or player-inventory slot geometry drifts from the screen contract.
- Clean stale NeoForge preview-run state before launch so old FML configuration cannot make preview verification nondeterministic.
- Build pinned OpenUI providers under Java 21 in CI while retaining each Minecraft target's declared runtime/toolchain level.
- Keep cross-target unit tests, generated-recipe parity, guide validation, and packaging checks green.
