# Biotech

**Turn crops and livestock into a real production line.**

Biotech adds automation-focused multiblock machines for farming and animal processing. Capture livestock as items, breed and raise animals without keeping large entity herds around your factory, grow crops at industrial scale, process animal drops, and recycle manure into fertilizer.

Instead of one magic block doing everything, Biotech gives each step its own machine, structure, inputs, outputs, and automation points. Build the line you want and connect it with your existing item, fluid, and energy infrastructure.

## What you can build

### Greenhouse

Grow crops automatically with water and FE.

- Supports wheat, beetroot, carrots, potatoes, melons, pumpkins, cactus, and sugar cane.
- Fertilizer recipes consume more water and energy in exchange for larger yields.
- Outputs crops and reusable seeds where appropriate.

### Breeding Chamber

Automate animal breeding without waiting on the vanilla breeding cooldown.

- Supply two captured adult animals, matching food, water, and energy.
- The parent items are required by the recipe but are **not consumed**.
- Produces a captured baby animal ready for the next production stage.

### Terrestrial Habitat

Raise baby animals into adults automatically.

- Consumes the captured baby animal and matching food.
- Produces the adult animal plus **manure**.
- Manure feeds directly into the fertilizer production loop.

### Slaughterhouse

Turn captured adult livestock into useful drops.

- Processes cows, chickens, pigs, sheep, and rabbits.
- Produces larger batches of their normal resources than a single vanilla kill.
- Designed to slot directly into an automated animal-production chain.

### Mixer

Combine ingredients into animal feeds and other machine recipes.

- Supports item inputs and fluid inputs.
- Supports both item and fluid outputs where a recipe needs them.
- Creates the higher-tier animal feeds used by Biotech's livestock loop.

### Fermenter

Process organic materials into useful products.

- Its core progression use is turning **manure into Fertilizer**.
- Connect it to your Habitat output and Greenhouse input for a closed farming loop.

---

## Capture animals with the Net Trap

Place a **Net Trap** on the ground and let a supported animal step on it. The trap captures the animal into an item that can be transported, stored, released again, or fed into Biotech machinery.

Supported animals:

- Cows
- Chickens
- Pigs
- Sheep
- Rabbits

Adult and baby animals remain distinct, and captured gameplay state is preserved where relevant.

---

## Built for automation

Biotech machines are multiblocks with dedicated hatches rather than sealed one-block factories.

- **Item Input Hatch** — 9 slots
- **Item Output Hatch** — 9 slots
- **Fluid Input Hatch** — 32 buckets
- **Fluid Output Hatch** — 32 buckets
- **Energy Input Hatch** — stores 614,400 FE and accepts up to 512 FE/t

Connect your pipes, inventories, tanks, and cables to each hatch's outward-facing side. Input hatches accept resources; output hatches expose completed products.

That makes Biotech fit naturally into larger tech packs instead of forcing you into a separate automation system.

---

## In-game guide, JEI, and Jade

**Patchouli is required** and provides the Biotech Guide Book. The guide includes:

- A machine-basics walkthrough
- Net Trap usage
- Item, fluid, and energy hatch behavior
- Crafting recipes
- Full multiblock previews for every machine

**JEI integration** shows machine recipes with exact ingredients, fluid quantities, outputs, chances, and total FE cost.

**Jade integration** can show machine status, structure validity, energy, fluid state, and processing progress directly in-world.

---

## Dependencies

Biotech requires:

- **NsTut Lib 0.8.1+ within the 0.8.x compatibility line**
- **Patchouli**
- **OpenUI MC 0.0.9** on clients, matching the Minecraft version and loader

JEI and Jade are supported integrations and are highly useful in larger modpacks.

---

## Supported versions

Current Biotech 2.1 targets:

- **Minecraft 1.20.1 — Forge — Java 17**
- **Minecraft 1.21.1 — NeoForge — Java 21**
- **Minecraft 26.1.2 — NeoForge — Java 25**

Install the Biotech file that matches your exact Minecraft version and loader.

---

## For modpack makers

Biotech machine recipes are data-driven and can be extended with datapacks or KubeJS custom recipes. The six recipe types are:

- `biotech:breeding_chamber`
- `biotech:terrestrial_habitat`
- `biotech:slaughterhouse`
- `biotech:greenhouse`
- `biotech:fermenter`
- `biotech:mixer`

The GitHub README documents the exact JSON schema differences between 1.20.1 and 1.21.1+.

---

Biotech is built for players who want farms to feel like engineered systems: capture, breed, grow, process, recycle, automate. 🌱⚙️
