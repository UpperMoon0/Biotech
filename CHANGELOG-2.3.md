# Biotech 2.3

Biotech 2.3 expands captured animals from a fixed five-species implementation into a data-driven system while keeping existing worlds, item IDs, and livestock recipes compatible.

## Added

- Add **Captured Animal v2** across Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2.
- Move Net Trap eligibility to the `#biotech:capturable` entity-type tag. The default tag includes cows, chickens, pigs, sheep, rabbits, horses, goats, llamas, and camels.
- Add the generic `biotech:captured_animal` carrier for capturable entities that do not use one of Biotech's legacy livestock item IDs. The generic carrier is created through capture rather than exposed as an empty creative-tab item.
- Render animal items with their in-game entity model instead of a dedicated flat item texture on all supported targets.
- Add runtime regression coverage for captured-state storage/round trips and legacy animal-preview presentation state.

## Changed

- Keep the existing cow, chicken, pig, sheep, and rabbit item identities as a compatibility bridge. Generic captures of those same species can satisfy existing animal-machine recipe requirements when the stored entity type and adult/baby state match.
- Store sanitized captured entity state before it enters the item payload, while preserving gameplay-relevant data needed when the animal is released again.
- Replace Biotech-owned JEI recipe background sprites with JEI-native standard slot backgrounds and recipe arrows.
- Let generic captured-animal machine requirements select species plus optional dult, aby, or ny lifecycle semantics while ignoring unrelated per-individual state.

## Fixed

- Prevent transient world identity and transform data from being persisted inside newly captured animal items.
- Prevent legacy 1.20.1/1.21.1 animal item rendering from leaking the global entity-renderer shadow setting.
- Suppress world-only nametags, fire, and glowing-outline effects on temporary legacy item-preview entities without changing the stored captured state.
- Stabilize reconstructed animal previews so item rendering does not inherit constructor-randomized orientation between frames.
- Reject `#biotech:capturable` entries that are not reconstructible entity types before consuming the Net Trap or original entity, preventing datapack mistakes from creating unreleasable captures.

## Compatibility and packmakers

- Existing captured cow/chicken/pig/sheep/rabbit items and existing machine recipes remain valid; no world migration is required for them.
- `#biotech:capturable` accepts only reconstructible entity types. Non-constructible entries are ignored before capture consumes anything.
- Adding a valid animal to `#biotech:capturable` enables Net Trap capture and the generic carrier, but **does not automatically add Breeding Chamber, Terrestrial Habitat, or Slaughterhouse recipes**. Add those recipes separately when extending livestock processing.
- To extend `biotech:capturable`, datapacks use `data/biotech/tags/entity_types/capturable.json` on Forge 1.20.1 and `data/biotech/tags/entity_type/capturable.json` on NeoForge 1.21.1/26.1.2.
- Supported targets remain Forge 1.20.1 (Java 17), NeoForge 1.21.1 (Java 21), and NeoForge 26.1.2 (Java 25).

See [`docs/upgrade-2.3.md`](docs/upgrade-2.3.md) for migration and datapack details.
