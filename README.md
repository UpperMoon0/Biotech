# Biotech

## Overview
Biotech is a Minecraft mod that allows players to biologically engineer and manipulate various elements within the game.

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

---

## How to Add Custom Recipes with KubeJS

Biotech supports dynamic recipe customization through KubeJS. This allows modpack makers and server admins to add or modify machine recipes without altering the mod’s core files. All recipes use the unified JSON format described above—only the `"type"` field changes to target different machines.

### Step 1: Set Up the KubeJS Folder Structure

Ensure your Minecraft directory includes a `kubejs` folder with the following structure:

.minecraft/kubejs/server_scripts/biotech_recipes.js

*If these folders do not exist, create them manually.*

### Step 2: Create the KubeJS Script

Inside the `server_scripts` folder, create a file named `biotech_recipes.js` (or any name you want).

### Step 3: Add Your Custom Recipe Script

Paste the following script into `biotech_recipes.js`. This example adds a custom mixer recipe using the unified recipe format:

```js
ServerEvents.recipes(event => {
    // Adds a custom mixer recipe for Biotech (machine type "biotech:mixer")
    event.custom({
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
                    "id": "minecraft:diamond",
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
    });
    
    // To add recipes for other machines, simply change the "type" field.
    // For example, to add a breeding chamber recipe, you might use:
    /*
    event.custom({
        "type": "biotech:breeding_chamber",
        "itemInputs": [ ... ],
        "itemOutputs": [ ... ],
        "fluidInputs": [ ... ],
        "fluidOutputs": [ ... ],
        "energy": 18000
    });
    */
});
