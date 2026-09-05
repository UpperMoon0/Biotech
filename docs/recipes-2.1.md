# Recipe processing in 2.1

All six Biotech machines use NsTut Lib 0.8.1's persisted transaction helper. Inputs are consumed once, progress survives reloads, invalid structures pause processing, exact remaining energy can finish a recipe, and outputs are committed only when they fit.

Biotech's internal hatches expose restorable `IItemHandlerModifiable`/`FluidTank` storage to the machine transaction path. NsTut Lib snapshots those handlers before input or output mutation and restores the complete state if a capability rejects or diverges during execution, preventing partial commits from becoming duplication or loss on retry.

## Recipe data generation

The repetitive machine recipes are generated data and have one authoritative definition in `RecipeGenerator`. Hand-authored crafting recipes remain in `src/main/resources`. Do not edit a generated machine recipe JSON directly; regenerate it instead.

Each supported target exposes a `generateRecipes` Gradle task. Normal tests/builds also regenerate and verify the generated data before packaging, so stale checked-in output cannot silently become the shipped recipe set.

The format is intentionally version-specific:

- Forge 1.20.1 keeps the legacy datapack directory and serializer shape: `data/biotech/recipes/`, item-stack `Count`, and fluid `FluidName`/`Amount`.
- NeoForge 1.21.1 and NeoForge 26.1.2 use the modern singular `data/biotech/recipe/` directory, lowercase item-stack `count`, and fluid `id`/`amount`. Generated loot tables likewise use `loot_table/` on these targets.

The build requires exactly 59 generated machine recipes on every target. Modern builds additionally reject legacy recipe keys and obsolete plural generated recipe/loot-table directories. This makes regeneration part of the build contract rather than a manual maintenance step.
