# Biotech 2.4: stateful livestock processing

Supported targets remain Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2. Existing captured cow, chicken, pig, sheep, and rabbit item IDs and ordinary machine recipe schemas remain supported. The generic capture/tag foundation is documented in [the 2.3 upgrade guide](upgrade-2.3.md).

## Breeding and growth

- Breeding parents remain non-consumable. The first matching input-slot parent is the deterministic donor for variant/genetic inheritance.
- Newborns inherit the explicit variant/genetic allowlist, not personal names, inventory, owner relationships, or world identity. They begin at age -24000.
- Habitat growth preserves each consumed baby's sanitized gameplay state and finishes ageing it. Batch recipes preserve each individual independently; they do not copy one baby's name/color over an entire batch.
- Legacy sheep items storing only `SheepColor` are normalized before processing.
- Captures strip transient world identity, transform, vehicle/passenger, and leash data. Both legacy and modern serialized field names are sanitized.
- Generic captured-animal recipe tooltips distinguish adult and baby requirements; datapack requirements can select `adult`, `baby`, or `any` lifecycle semantics.

Prepared livestock recipes carry exact animal-input bindings in their transaction snapshot. These markers exist only in recipe ingredient copies, never on the player's captured item. A same-species animal with different state cannot replace the bound donor while an output hatch is blocked. If inputs no longer match before consumption, that transaction is cancelled without consuming them; a later cycle selects the current inputs normally.

Rejected animal allocation, oversized bindings/outputs, and invalid dynamic-loot preparation leave inputs and resources unchanged and use a 20-tick retry cooldown. Correct the input or datapack recipe to resume processing after that cooldown.

Already-started transactions retain their persisted recipe snapshot. New recipes, configuration changes, and loot datapack changes apply to newly started transactions.

## Slaughterhouse: dynamic and static recipes

An empty authored `itemOutputs` list selects dynamic entity-loot processing. The captured entity is reconstructed from its safe stored state and its actual loot table is resolved from the running server, including variant/custom entity loot and datapack replacements.

The loot context uses the machine position, the reconstructed entity, and generic non-player damage. It does not invent a player kill, looting level, or enchanted weapon. Biotech's server-synced configuration key `slaughterhouseYieldMultiplier` controls post-roll count amplification (default 2, permitted range 1–64). Amplified counts are split into legal item stacks before persistence. Every consumed animal contributes its own loot; retained catalysts do not generate extra slaughter loot. Loot results are prepared once and stored in the active recipe snapshot; pause/reload does not reroll them.

An explicit nonempty authored `itemOutputs` list remains a static datapack override. Its authored counts and probabilities are used unchanged, without the dynamic loot multiplier. JEI follows the same static/dynamic policy; a dynamic label does not claim to enumerate every possible loot result.

## Renewable habitat recipes

Default production recipes keep one adult as a non-consumed inhabitant:

| Product | Tier 1 | Tier 2 |
| --- | ---: | ---: |
| Food consumed | 2 | 4 |
| Water consumed | 250 mB | 500 mB |
| Energy consumed | 32,000 FE | 64,000 FE |
| Eggs or wool | 1 | 3 |
| Milk | 1,000 mB | 3,000 mB |
| Manure | 1 | 3 |

Chicken produces eggs, sheep produces its captured color of wool, and cow produces `minecraft:milk`. Biotech enables the loader-provided milk fluid during mod construction. A Fluid Output Hatch is required to receive milk. A full or incompatible output tank pauses output delivery safely.

These are ordinary data-driven recipes. Food, water, energy, products, and the non-consumable flag can be changed by a datapack. Generic captured-animal species/lifecycle selectors support goat milk and future/modded products; adding a species to the capture tag alone still does not create machine recipes.

JEI identifies non-consumable inputs, displays production timing based on the machine's processing rate, and separates scrollable item/fluid contents without overlapping the energy/timing area. Chance labels are compact; hover the output for its precise recipe probability.

Machine controllers keep item products, chance labels, and fluid products in one clipped output panel. Scroll over the products to reach longer datapack output lists; fluid rows follow the item rows.

## Verification

Run `./gradlew testAllVersions buildAll gameTestAll` after publishing the pinned NsTut Lib and OpenUI dependencies to Maven Local. `full-validation` opts a draft pull request into the existing cross-target CI matrix. The fast draft lane alone is insufficient to establish all-loader gameplay coverage.
