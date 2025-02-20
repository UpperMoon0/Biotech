# Biotech

## Overview
Biotech is a Minecraft mod that allows players to biologically engineer and manipulate various elements within the game.

---

CurseForge link: [https://www.curseforge.com/minecraft/mc-mods/biotech](https://www.curseforge.com/minecraft/mc-mods/biotech)

---

## Recipe JSON Format
Every machine recipe follows this structure:

- **`type`**: A string representing the recipe type identifier.  
  Example: `"biotech:mixer"`

- **`itemInputs`**: An array of input item objects. Each object must include:
    - **`itemStack`**: An object containing:
        - **`id`**: The item identifier.
        - **`Count`**: The quantity required.
    - **`isConsumable`**: A Boolean indicating if the item is consumed when the recipe is processed.

- **`itemOutputs`**: An array of output item objects. Each object includes:
    - **`itemStack`**: An object containing:
        - **`id`**: The output item identifier.
        - **`Count`**: The quantity produced.
    - **`chance`**: (Optional) A float value representing the probability of the output (default is 1.0).

- **`fluidInputs`** and **`fluidOutputs`**: Arrays of fluid objects (if your recipe involves fluids). Their format should match what your custom serializer expects (for example, with `"fluid"` and `"amount"` properties).

- **`energy`**: An integer that represents the total energy cost for processing the recipe.

### Example Recipe
Below is an example JSON for a recipe:

```json
{
    "type": "biotech:mixer",
    "itemInputs": [
        {
            "itemStack": {
                "id": "biotech:paper_bag",
                "Count": 12
            },
            "isConsumable": true
        },
        {
            "itemStack": {
                "id": "minecraft:wheat",
                "Count": 3
            },
            "isConsumable": true
        },
        {
            "itemStack": {
                "id": "minecraft:carrot",
                "Count": 2
            },
            "isConsumable": true
        },
        {
            "itemStack": {
                "id": "minecraft:potato",
                "Count": 2
            },
            "isConsumable": true
        },
        {
            "itemStack": {
                "id": "minecraft:apple",
                "Count": 1
            },
            "isConsumable": true
        }
    ],
    "itemOutputs": [
        {
            "itemStack": {
                "id": "biotech:cow_feed",
                "Count": 12
            },
            "chance": 1.0
        }
    ],
    "fluidInputs": [],
    "fluidOutputs": [],
    "energy": 24000
}
