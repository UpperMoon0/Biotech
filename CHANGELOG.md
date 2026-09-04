# Changelog

## 2.1

Biotech 2.1 is a coordinated hardening release with NsTut Lib 0.8.1.

- Migrates all six machines to persisted transactional recipe processing, including stable chance rolls, rollback safety, reload persistence, and exact final-tick energy handling.
- Fixes Mixer fluid output, sided item/fluid hatch automation, receive-only energy input, and hatch energy persistence.
- Restricts network compatibility, scopes synchronization to tracking players, and verifies all six custom machine recipe types remain registry-backed before recipe synchronization.
- Preserves captured-animal gameplay state and makes capture/release collision and item-consumption behavior safe.
- Fixes NeoForge 26.1.2 keyed block construction compatibility through NsTut Lib 0.8.1.
- Adds cross-target CI and regression coverage for Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2.

See [`CHANGELOG-2.1.md`](CHANGELOG-2.1.md) for the complete change list and [`docs/upgrade-2.1.md`](docs/upgrade-2.1.md) for upgrade notes.
