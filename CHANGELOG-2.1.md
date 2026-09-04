# Biotech 2.1

## Fixed
- Migrate all machine processing to NsTut Lib's persisted transactional recipe engine.
- Prevent duplicate/chance-overrolled/overstacked outputs and NBT-insensitive recipe matching through NsTut Lib 0.8.1.
- Persist probabilistic item-output decisions with the active recipe so reloads and safe rollback retries cannot reroll results.
- Roll back partial machine item/fluid input and output commits when a capability diverges during execution.
- Preserve safely rolled-back active recipes and retry capability divergence with bounded backoff instead of failing the server tick.
- Treat failed transaction rollback as non-retriable corruption and cancel the active recipe rather than risking duplicate output or repeated consumption.
- Preserve active recipes across chunk/world reloads and pause safely through invalid multiblocks.
- Recover cleanly when a validated hatch capability becomes unavailable during a machine tick.
- Consume only the exact remaining FE on the final processing tick.
- Correct Mixer fluid-output commits.
- Make input/output item and fluid hatches directional for external automation.
- Make energy input hatches externally receive-only and synchronize absolute energy correctly.
- Mark successful FE receive/extract mutations dirty so hatch energy persists across chunk unloads.
- Restrict network protocol compatibility and scope machine/hatch packets to tracking chunks.
- Send hatch state only when changed and machine UI state at a reduced cadence.
- Make fluid packet handling safe during client world transitions.
- Keep all six machine recipe types and serializers registry-backed before datapack recipe synchronization, with regression coverage for their `biotech:*` registry IDs.
- Remove client screen classes and reflection from common machine registration.
- Preserve captured animal gameplay NBT and only consume captured-mob items after a valid successful spawn.
- Make net-trap capture compile correctly on 1.20.1 and restore the trap if the captured-item entity cannot be spawned.
- Validate spawn collision for released animals and retain compatibility with legacy captured-animal items.
- Align Forge/build metadata on NsTut Lib 0.8.1 and require 0.8.1+ for NeoForge 26.1.2 so the stale unkeyed MachineBlock constructor cannot load.
- Make CI build against the exact coordinated NsTut Lib source revision through local Maven instead of depending on JitPack availability.
- Fix NeoForge 1.21.1 GameTests by generating a deterministic `biotech:empty` structure at build time instead of relying on loader-owned test templates that are not shipped by that target.
- Add GameTests for sided hatch IO, active transaction persistence through structure invalidation/reload, and registry-backed machine recipe types alongside unit/build regression coverage.
- Remove obsolete development dependencies, local Maven/runtime artifacts, and old ForgeGradle/Mixin configuration.
