# Biotech 2.6

- Redesign the Greenhouse, Breeding Chamber, Terrestrial Habitat, Slaughterhouse, Mixer and Fermenter into distinct structures using a coherent graphite, ivory and teal material palette.
- Add eight craftable structural and functional parts: frame, panel, vent, vessel wall, drive, grow light, framed glass and service pipe. Their 32-pixel textures derive from actual image-generation assets.
- Keep complete original 2.5 structures valid with their exact controller origins and hatch locations. No world blocks or stored inventories, fluids, FE, active recipes or chance decisions are automatically moved or converted.
- Update guide previews and construction budgets together from one validated structure definition. Keep machine recipes, processing balance and directional automation behavior unchanged.
- Add native tests for new/legacy formation, rotations, interrupted active cycles, blocked item/fluid outputs, all Mixer fluid paths, facing-side extraction and real disk chunk persistence.
- Add an actual client before/after gallery for all six machines with front and rear views.

See [safe rebuilding, evidence and verification limits](docs/machine-upgrade.md). The disk-persistence test uses a legacy-layout fixture created under the new binary; an actual 2.5-binary saved-world upgrade has not been separately tested. Back up existing worlds before updating.

Targets: Forge 1.20.1, NeoForge 1.21.1 and NeoForge 26.1.2. Existing NsTut Lib and OpenUI dependency requirements are unchanged.
