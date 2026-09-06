# Changelog

## 2.1

Biotech 2.1 is a coordinated hardening release with NsTut Lib 0.8.1 and requires NsTut Lib 0.8.1 or newer within the 0.8.x compatibility line on every supported target.

- Migrates all six machines to persisted transactional recipe processing, including stable chance rolls, rollback safety, reload persistence, and exact final-tick energy handling.
- Fixes Mixer fluid output, sided item/fluid hatch automation, receive-only energy input, and hatch energy persistence.
- Restricts network compatibility, scopes synchronization to tracking players, and verifies all six custom machine recipe types remain registry-backed before recipe synchronization.
- Preserves captured-animal gameplay state and makes capture/release collision and item-consumption behavior safe.
- Fixes NeoForge 26.1.2 keyed block construction compatibility through NsTut Lib 0.8.1.
- Adds cross-target CI and regression coverage for Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2.
- Consolidates shared Java, tests, assets, Patchouli content, and generator infrastructure into common source tiers so loader/version modules keep only genuine compatibility-edge code.
- Keeps all three generated machine recipe sets semantically identical while retaining each Minecraft version's required datapack schema and pack metadata format.
- Corrects the Patchouli guide, in-game mod description, README, migration notes, and player-facing CurseForge documentation to match actual machine, hatch, capture, and recipe behavior.
- Fixes NeoForge 26.1.2 Slaughterhouse JEI integration to preserve full captured-item stacks/components instead of reducing them to bare items.
- Makes generated-resource failures fail the build instead of logging and continuing, and centralizes captured-entity sanitization plus machine-screen progress math across supported targets.
- Migrates the hand-authored NeoForge 1.21.1 machine recipes to the modern `count` / `id` / `amount` schema, updates sheep feed to `minecraft:short_grass`, and replaces Patchouli's removed `shaped_book_recipe` serializer with a normal crafting recipe carrying the `patchouli:book` data component.
- Adds permanent verification for the six hand-authored machine recipes and fixes the Forge GameTest runtime dependency set so the full Forge test suite can run in CI instead of a compatibility-test-only exception.
- Makes Forge GameTests fail closed: package a Biotech-owned empty test structure, require five discovered tests, and assert representative generated and hand-authored recipes exist at runtime.

See [`CHANGELOG-2.1.md`](CHANGELOG-2.1.md) for the complete change list and [`docs/upgrade-2.1.md`](docs/upgrade-2.1.md) for upgrade notes.
