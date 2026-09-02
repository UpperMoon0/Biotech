# Biotech 2.1

## Fixed
- Migrate all machine processing to NsTut Lib's persisted transactional recipe engine.
- Prevent duplicate/chance-overrolled/overstacked outputs and NBT-insensitive recipe matching through NsTut Lib 0.8.
- Preserve active recipes across chunk/world reloads and pause safely through invalid multiblocks.
- Consume only the exact remaining FE on the final processing tick.
- Correct Mixer fluid-output commits.
- Make input/output item and fluid hatches directional for external automation.
- Make energy input hatches externally receive-only and synchronize absolute energy correctly.
- Restrict network protocol compatibility and scope machine/hatch packets to tracking chunks.
- Send hatch state only when changed and machine UI state at a reduced cadence.
- Make fluid packet handling safe during client world transitions.
- Remove client screen classes and reflection from common machine registration.
- Preserve captured animal gameplay NBT and only consume captured-mob items after a valid successful spawn.
- Validate spawn collision for released animals and retain compatibility with legacy captured-animal items.
- Align Forge/build metadata and require NsTut Lib 0.8.x.
- Remove obsolete development dependencies, local Maven/runtime artifacts, and old ForgeGradle/Mixin configuration.
- Add CI build/test coverage.
