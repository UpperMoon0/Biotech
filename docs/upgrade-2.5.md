# Biotech 2.5: factory control

Supported targets remain Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2. Existing worlds retain their inventories and saved transactions. No provider dependency upgrade is required. Update Biotech on both clients and servers: network protocol 4 rejects clients that lack the controller menu data or synchronized Slaughterhouse loot catalog.

## Redstone control

The button at the top right of each controller cycles **Ignore signal**, **Needs signal**, and **No signal**. Ignore signal is the default, including for old worlds and unknown saved mode IDs. The other modes read the controller's vanilla neighbor signal. Modes are saved per controller. A player must have the controller's valid menu open to change its mode.

Pausing stops transaction processing before preparation, consumption, energy extraction, or completion. The active recipe definition, exact chance decisions, consumed-input phase, and energy progress remain saved. A paused controller also exposes its saved recipe and progress to the menu after reload, before processing resumes. Resuming continues that cycle; it does not refund consumed inputs or reroll products. Structure validation continues while paused. A broken structure takes precedence over a redstone pause in diagnostics.

The controller's existing operating block state represents an active transaction, including a paused transaction. Use the menu or Jade status to distinguish a stall from processing. Comparator output is not added in this release.

## Stall diagnostics

All six controllers synchronize diagnostics through their vanilla menu data. Jade requests the same cached server status. Clients do not scan inventories or choose recipes. Idle input diagnoses refresh at most once every 20 world ticks (one second at 20 TPS), so a changed input may briefly retain its previous message. This informational cache does not delay transaction processing or redstone controls. A machine with no available recipes reports No matching recipe even when its input inventory is empty.

| Status | Action |
| --- | --- |
| Invalid structure | Repair the multiblock and check hatch orientation. |
| Missing items | Supply recipe inputs to the item input hatch. |
| Missing fluid | Item requirements match a recipe; supply its fluid inputs. |
| No matching recipe | Check quantities, animal lifecycle/state, and datapack recipe requirements in JEI. |
| Insufficient energy | Supply energy at the energy hatch. |
| Item output blocked | Extract products or remove incompatible stacks. |
| Fluid output blocked | Empty or drain the correct output tank. |
| Paused by redstone | Change the signal or controller mode. |
| Transaction failed; retrying | Inspect the server log for the transaction rejection; correct the recipe or storage condition. |

Item output has priority over fluid output when both are blocked. Before input consumption, diagnostics use the transaction's exact persisted output rolls. Once inputs are committed, output capacity is checked again at completion; an intervening full hatch does not undo consumed inputs. A completed cycle can show Processing for its completion tick before returning to its next input status.

## Server balance settings

Edit `<world>/serverconfig/biotech-server.toml`. Use `defaultconfigs/biotech-server.toml` for defaults in newly created worlds. Loader server configuration synchronization keeps connected clients on the server's settings.

| Setting | Default | Bounds | Effect |
| --- | ---: | --- | --- |
| `machineEnergyMultiplier` | 1.0 | 0.01–100.0 | Scales datapack `totalEnergy` when a new cycle starts. Positive costs round up and saturate at the integer limit; free recipes stay free. |
| `machineEnergyPerTick` | 512 | 1–65536 | Maximum processing energy per controller tick. Applies to active cycles too. |
| `slaughterhouseYieldMultiplier` | 2 | 1–64 | Existing post-roll multiplier for new dynamic entity-loot transactions. Explicit datapack item outputs remain unchanged. |

Active saved cycles retain their energy cost across configuration and datapack reloads. The processing rate does not change hatch capacity or the hatch's existing 512 FE/t external acceptance limit. Higher rates require stored energy and cannot sustain consumption beyond the actual supply. JEI shows costs and ideal production timing for new cycles; the controller displays its actual saved cycle cost. Reconnect after changing settings while connected to refresh JEI's cached recipe cards.

For machine-specific costs, products, fluid amounts, chances, or renewable/manure yields, override the recipes in a datapack. This release preserves recipe authority and default output balance rather than introducing global output multipliers. See [production datapacks](production-datapacks.md).

## Translation workflow

Player-facing controller diagnostics, redstone labels, energy/fluid tooltips, and product descriptions use keys in `assets/biotech/lang/en_us.json`. Add another locale JSON alongside it, retain each key's `%s` placeholders in order, and translate values. Minecraft uses English fallback for untranslated keys. Automated checks verify literal translation references and dynamically generated status/mode keys against the English catalog.

Patchouli prose is a separate translation surface: copy the guide's `en_us/categories` and `en_us/entries` trees to your locale directory. Preserve entry IDs, category IDs, page types, recipe references, and formatting codes such as `$(br)`. Translate names and prose. Translating the language JSON alone does not translate the guide's literal prose.

Greenhouse defaults now follow mature crop loot rather than fixed item lists; melon output becomes slices and seed counts can vary. Explicit nonempty outputs preserve pack-authored balance. Breeding now derives each newborn from both parents using vanilla rules, replacing 2.4 first-parent inheritance.

The default capture tag and machine recipes cover all supported vanilla non-aquatic Animal types in each version (28/29/31 species). New creative entries identify each additional species; captured-item tooltips expose appearance and saved attributes. Generic breeding uses two separate parent entries because captured animals have a stack limit of one.
