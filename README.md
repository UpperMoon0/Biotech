# Biotech

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.1%20%7C%2026.1.2-brightgreen)
![Loaders](https://img.shields.io/badge/loaders-Forge%20%7C%20NeoForge-orange)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-lightgrey)

**Biotech** turns crops and livestock into compact, automation-friendly production lines. Capture supported animals as items, build purpose-built multiblock machines, grow crops, breed and raise livestock, process animal drops, and recycle manure into fertilizer.

CurseForge: https://www.curseforge.com/minecraft/mc-mods/biotech

## Required dependencies

Biotech requires:

- **NsTut Lib 0.8.1+ within the 0.8.x compatibility line**
- **Patchouli** for the in-game Biotech Guide Book
- **OpenUI MC 0.0.10 or newer within the 0.0.x compatibility line** on clients, matching the Minecraft version and loader

JEI and Jade integrations are included when those mods are installed. JEI is strongly recommended for inspecting exact machine recipes and Jade can display machine status, energy, fluid, and progress information.

All six machine screens and all five hatch screens use OpenUI. Machine-specific recipe diagrams, energy and fluid gauges, progress timing, and output chances read the existing synchronized menu data. Hatch inventory slots keep vanilla click, drag, shift-click, and tooltip behavior.

For source builds, publish the target from OpenUI-MC at the `openui_ref` revision in `gradle.properties` to Maven Local before building Biotech (for example, run `./gradlew :neoforge-1.21.1:publishToMavenLocal` in OpenUI-MC). CI and release builds do this automatically, alongside the pinned NsTut Lib provider. See [UI verification](docs/openui-migration.md) for the migration checks.

## Supported targets

| Minecraft | Loader | Runtime Java | Module / artifact |
| :--- | :--- | ---: | :--- |
| 1.20.1 | Forge | 17 | `forge-1.20.1` / `biotech-forge-1.20.1` |
| 1.21.1 | NeoForge | 21 | `neoforge-1.21.1` / `biotech-neoforge-1.21.1` |
| 26.1.2 | NeoForge | 25 | `neoforge-26.1.2` / `biotech-neoforge-26.1.2` |

Install the file matching both your Minecraft version and loader. A jar built for one target is not a cross-version artifact.

---

## Key features

### Capture livestock instead of moving entities through your factory

Place a **Net Trap** and let an entity in `#biotech:capturable` step onto it. The animal becomes an item that preserves gameplay-relevant state and can later be released again or processed by Biotech machinery when a matching recipe exists.

Biotech 2.3 includes these capturable entities by default:

- Cows
- Chickens
- Pigs
- Sheep
- Rabbits
- Horses
- Goats
- Llamas
- Camels

The original five livestock species keep their legacy item IDs for world and recipe compatibility. Other tagged entities use the generic `biotech:captured_animal` carrier. Packmakers can extend `#biotech:capturable` with datapacks; capture eligibility alone does **not** create machine recipes for a new species.

Adult/baby state, variants, custom names, and other persistent gameplay state are preserved where applicable. Animal items render the actual entity model in inventory, held-item, and JEI contexts rather than a separate flat animal texture.

### Six multiblock machines

- **Greenhouse** — grows wheat, beetroot, carrots, potatoes, melons, pumpkins, cactus, and sugar cane. Supported crops have higher-yield fertilizer recipes.
- **Breeding Chamber** — combines two captured adult parents with matching food, water, and energy to produce a baby animal. Parent items are required but not consumed.
- **Terrestrial Habitat** — raises captured baby animals into adults and produces manure as a by-product.
- **Slaughterhouse** — converts captured adult animals into larger batches of their normal drops.
- **Mixer** — produces animal feeds and supports recipes with item and fluid inputs/outputs.
- **Fermenter** — processes organic materials, including turning manure into Fertilizer.

Every machine has a Patchouli multiblock preview and crafting recipe in the Biotech Guide Book.

### Automation-ready hatches

Biotech multiblocks use explicit IO blocks:

- Item Input Hatch: 9 inventory slots
- Item Output Hatch: 9 inventory slots
- Fluid Input Hatch: 32,000 mB
- Fluid Output Hatch: 32,000 mB
- Energy Input Hatch: 614,400 FE storage, up to 512 FE/t input

External automation is directional: connect item pipes, fluid pipes, or energy cables to the hatch-facing side. Input hatches accept resources and output hatches expose completed products.

### Transaction-safe machine processing

Biotech 2.3 keeps NsTut Lib's persisted transactional recipe engine. Active recipes survive safe reloads, partial commits can roll back, probabilistic output decisions do not reroll after reload, and machines pause safely when their multiblock becomes invalid.

---

## In-game guide and integrations

The **Biotech Guide Book** is the main player reference. It covers machine basics, Net Traps, hatches, crafting recipes, and every multiblock structure.

With **JEI**, each Biotech machine has a recipe category showing exact inputs, fluid quantities, outputs, chances, and energy cost. With **Jade**, machine controllers expose status information such as structure validity, progress, energy, and fluid state.

---

## Custom recipes / KubeJS

Biotech machine recipes can be added with datapacks or `event.custom(...)` in KubeJS. Use the schema for the Minecraft version you target.

Net Trap eligibility is separately data-driven through `#biotech:capturable`. Extending that entity-type tag enables capture/release through the generic carrier; add explicit machine recipes if the new species should also work in the Breeding Chamber, Terrestrial Habitat, or Slaughterhouse. See [`docs/upgrade-2.3.md`](docs/upgrade-2.3.md) for version-specific tag paths.

### Recipe types

- `biotech:breeding_chamber`
- `biotech:terrestrial_habitat`
- `biotech:slaughterhouse`
- `biotech:greenhouse`
- `biotech:fermenter`
- `biotech:mixer`

### 1.20.1 schema

Forge 1.20.1 uses the legacy serialized field names:

```json
{
  "type": "biotech:mixer",
  "itemInputs": [
    {
      "itemStack": { "id": "minecraft:wheat", "Count": 1 },
      "isConsumable": true
    }
  ],
  "itemOutputs": [
    {
      "itemStack": { "id": "biotech:cow_feed", "Count": 1 },
      "chance": 1.0
    }
  ],
  "fluidInputs": [
    { "FluidName": "minecraft:water", "Amount": 1000 }
  ],
  "fluidOutputs": [],
  "energy": 24000
}
```

### 1.21.1 and 26.1.2 schema

NeoForge 1.21.1 and 26.1.2 use lowercase item counts and modern fluid fields:

```json
{
  "type": "biotech:mixer",
  "itemInputs": [
    {
      "itemStack": { "id": "minecraft:wheat", "count": 1 },
      "isConsumable": true
    }
  ],
  "itemOutputs": [
    {
      "itemStack": { "id": "biotech:cow_feed", "count": 1 },
      "chance": 1.0
    }
  ],
  "fluidInputs": [
    { "id": "minecraft:water", "amount": 1000 }
  ],
  "fluidOutputs": [],
  "energy": 24000
}
```

`isConsumable: false` keeps an input present after a successful recipe. `chance: 1.0` means the output is guaranteed.

---

## Release and migration documentation

- [`CHANGELOG.md`](CHANGELOG.md) - canonical changelog
- [`CHANGELOG-2.3.md`](CHANGELOG-2.3.md) - complete Biotech 2.3 release notes
- [`docs/upgrade-2.3.md`](docs/upgrade-2.3.md) - Captured Animal v2 migration and datapack notes
- [`CHANGELOG-2.2.md`](CHANGELOG-2.2.md) - Biotech 2.2 OpenUI changes
- [`CHANGELOG-2.1.md`](CHANGELOG-2.1.md) - Biotech 2.1 hardening changes
- [`docs/upgrade-2.1.md`](docs/upgrade-2.1.md) - historical 2.1 upgrade notes
- [`docs/recipes-2.1.md`](docs/recipes-2.1.md) - transactional recipe behavior
- [`docs/networking-2.1.md`](docs/networking-2.1.md) - synchronization behavior
- [`README_HARDENING.md`](README_HARDENING.md) - hardening and validation notes

## Building and testing

```bash
# JVM/unit/regression suites across all supported targets
./gradlew testAllVersions

# Build every supported target
./gradlew buildAll

# Real GameTest coverage across all supported targets
./gradlew gameTestAll
```

Generated machine data is deterministic and verified during builds. Each target must generate exactly 59 machine recipes using the schema and datapack layout required by that Minecraft version.

## License

All Rights Reserved. Created by **NsTut**.
