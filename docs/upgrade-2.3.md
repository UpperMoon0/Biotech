# Upgrade to Biotech 2.3

Biotech 2.3 keeps the same three Minecraft/loader targets and is designed to be compatible with existing 2.2 worlds. The main migration surface is the new data-driven captured-animal system.

## Existing worlds and items

- Existing cow, chicken, pig, sheep, and rabbit captured items remain valid.
- Existing Breeding Chamber, Terrestrial Habitat, and Slaughterhouse recipes for those five livestock species keep their current IDs and behavior.
- The generic `biotech:captured_animal` item is an implementation carrier created by the Net Trap for entities without a legacy Biotech animal item. It is intentionally not an empty creative-tab item.
- Generic captures representing one of the original five species can satisfy the corresponding existing recipe when entity type and adult/baby state match.

No manual item conversion or world migration is required for the legacy five species.

## Extending Net Trap capture

Net Trap eligibility is controlled by the entity-type tag `#biotech:capturable`. Biotech includes these entities by default:

- `minecraft:cow`
- `minecraft:chicken`
- `minecraft:pig`
- `minecraft:sheep`
- `minecraft:rabbit`
- `minecraft:horse`
- `minecraft:goat`
- `minecraft:llama`
- `minecraft:camel`

Packmakers can extend the tag from a datapack. The tag directory differs by Minecraft family:

- Forge 1.20.1: `data/biotech/tags/entity_types/capturable.json`
- NeoForge 1.21.1 and 26.1.2: `data/biotech/tags/entity_type/capturable.json`

Use normal tag `values`/`replace` semantics. Because the tag ID is `biotech:capturable`, contributions to that tag belong under the `data/biotech/...` namespace path even when they come from another datapack.

**Capture support and machine recipe support are separate.** `#biotech:capturable` is an allow-list inside Biotech's safe capture baseline: the entity type must be constructible again through the same Minecraft `EntityType` factory used for release. Unsupported entries (for example `minecraft:player` and other create-nothing/non-constructible types) are ignored before the trap or original entity is consumed. A valid tagged animal can be captured and released through the generic carrier, but the tag does not invent Breeding Chamber, Terrestrial Habitat, or Slaughterhouse recipes. Add explicit machine recipes if the new species should participate in those production chains.

For those explicit machine recipes, a `biotech:captured_animal` input carrying an `EntityType` requirement is matched by species. Per-capture `CapturedEntity` state such as age, variant, or custom name is ignored for this generic-species match, so independently captured animals of the same species satisfy the same recipe while other species do not. A generic carrier without a valid `EntityType` is not a wildcard.

## Captured state behavior

New captures sanitize the entity payload **before it is stored in the item**. Gameplay state such as age, variant data, custom name, and other persistent entity properties can survive capture, while transient world identity/placement data such as UUID, position, motion, rotation, fall distance, portal state, and leash state is removed.

Item previews use a temporary reconstructed entity. World-only presentation effects are suppressed for the preview (for example visible nametags, fire, and glowing outlines), but that does not mutate the stored item payload or the state restored when the animal is released.

## JEI and rendering

Animal items use their entity model in inventory/held/JEI contexts rather than dedicated flat animal-item textures. Biotech's machine JEI categories now use JEI's standard slot backgrounds and recipe arrow instead of Biotech-owned recipe background sprites.

## Dependencies and targets

Dependency compatibility is unchanged from 2.2:

- NsTut Lib 0.8.1+ within the 0.8.x compatibility line
- Patchouli
- OpenUI MC 0.0.10+ within the 0.0.x compatibility line on clients

Supported targets remain:

- Minecraft 1.20.1 / Forge / Java 17
- Minecraft 1.21.1 / NeoForge / Java 21
- Minecraft 26.1.2 / NeoForge / Java 25
