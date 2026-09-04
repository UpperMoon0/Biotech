# Biotech 2.1 hardening notes

Biotech 2.1 requires NsTut Lib 0.8.1 or newer within the 0.8 line. Machine recipes now use the shared persisted transaction engine, hatch capabilities enforce external IO direction, network synchronization is scoped to tracking chunks, and captured animals preserve their gameplay state.

The CI workflow is the merge gate for compile/test validation on Java 17 / Minecraft 1.20.1 Forge 47.3.12.
