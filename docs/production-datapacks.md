# Livestock production datapacks

Slaughterhouse recipes with empty `itemOutputs` resolve each consumed animal's concrete loot table (including per-entity overrides and variant state) once. Retained catalysts never grant death loot. A dynamic recipe must consume at least one reconstructible living animal. Nonempty `itemOutputs` instead opt into static products: their authored counts and chances are kept, and the server multiplier does not apply.

`slaughterhouseYieldMultiplier` in the world/server Biotech server config is an integer from 1 to 64, default 2. It synchronizes to clients. It multiplies post-roll item counts, not probability; it provides no player or looting context. A captured death-loot seed is respected. In-flight results and donor identity are snapshotted, so config changes, retries and reloads affect only new transactions. Counts are split at the item's stack limit (also capped at 99 for persistence); tables producing more than 256 output entries are rejected before consumption, with a server warning. Reduce those tables or the multiplier to resume.

## Renewable extension example

Copy the appropriate `examples/production/<target>/goat_milk.json` to your datapack as `data/<your_namespace>/recipes/goat_milk.json` on Forge 1.20.1, or `data/<your_namespace>/recipe/goat_milk.json` on NeoForge. These examples are not installed as recipes by Biotech. Identical copies are packaged as inert `biotech_examples/goat_milk.json` resources for the codec/transaction regression tests.

The example keeps an adult `minecraft:goat`, consumes two wheat and 250 mB water per 32,000 FE cycle, and produces 1,000 mB `minecraft:milk` plus one manure. A steady 512 FE/t requires 63 processing ticks. For a modded species, replace `minecraft:goat` with its registered entity ID, add it to `#biotech:capturable`, and replace the products with registered item/fluid IDs. The mod providing those IDs must be installed. `EntityType` and `BiotechRecipeLifecycle` belong in the required input's tag (Forge) or `minecraft:custom_data` component (NeoForge); `isConsumable: false` keeps the selected adult. Capture eligibility alone does not create a production recipe.

The ordinary defaults use two/four food, 250/500 mB water and 32,000/64,000 FE, yielding one/three eggs or color-preserving wool, or 1,000/3,000 mB milk, with one/three manure. Power outages and full item or fluid outputs delay completion without rerolling products or consuming a retained animal. Both outputs must fit before resources are consumed.

Test-only loot tables under `biotech:gametest/` are unreferenced by ordinary recipes. They permit deterministic seeded probability, player-only exclusion and oversize safety tests without replacing vanilla loot tables.
