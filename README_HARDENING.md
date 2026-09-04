# Biotech 2.1 hardening notes

Biotech 2.1 requires NsTut Lib 0.8.1 or newer within the 0.8.x compatibility line. Machine recipes now use the shared persisted transaction engine, hatch capabilities enforce external IO direction, network synchronization is scoped to tracking chunks, and captured animals preserve their gameplay state.

The CI workflow is the merge gate across every supported target: Forge 1.20.1 on Java 17, NeoForge 1.21.1 on Java 21, and NeoForge 26.1.2 on Java 25. GameTests cover sided hatch IO, active transaction persistence through invalidation/reload, and registry-backed custom machine recipe types so recipe synchronization cannot silently regress to unregistered types.
