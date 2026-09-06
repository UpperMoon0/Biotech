# Biotech 2.1 hardening notes

Biotech 2.1 requires NsTut Lib 0.8.1 or newer within the 0.8.x compatibility line on every supported target. Machine recipes use the shared persisted transaction engine, hatch capabilities enforce external IO direction, network synchronization is scoped to tracking chunks, and captured animals preserve their gameplay state.

The multi-version workspace now separates shared behavior from compatibility edges. `common/` is used by all three targets, `common-legacy/` contains code shared by Forge 1.20.1 and NeoForge 1.21.1, and `common-neoforge/` contains code shared by the two NeoForge targets. Platform modules should only retain code that genuinely depends on incompatible Minecraft, Forge/NeoForge, rendering, transfer, networking, or serialization APIs.

Generated machine data is a build contract: every target regenerates and verifies exactly 59 machine recipes, and the normalized gameplay content must remain semantically identical even though 1.20.1 and 1.21+ use different datapack schemas. Generation failures are fatal rather than log-only.

The CI/verification path covers Forge 1.20.1 on Java 17, NeoForge 1.21.1 on Java 21, and NeoForge 26.1.2 on Java 25. Unit/regression tests cover shared behavior and version-specific contracts; GameTests cover sided hatch IO, active transaction persistence through invalidation/reload, and registry-backed custom machine recipe types.