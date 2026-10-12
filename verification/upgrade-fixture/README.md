# Actual released-binary world upgrade

This isolated test-only mod records historical upgrade behavior of the immutable 2.5 and 2.6 releases. Current 2.7-alpha.2 deliberately requires rebuilding retired layouts; this fixture does not test the current binary or prove legacy acceptance by it. It is a standalone Gradle project: it does not compile Biotech source, modify either release JAR, or enter the production JARs.

The workflow downloads the published NeoForge 1.21.1 Biotech 2.5 and 2.6 JARs, verifies pinned SHA-256 digests, release asset IDs and tag commit IDs, records release metadata and resolved provider commits, and starts two separate server processes with pinned dependencies. At startup the harness verifies the runtime Biotech mod file matches the supplied released JAR byte-for-byte and has the expected version.

1. The real 2.5 runtime builds all six original structures and starts representative real recipes. It pauses each after 128 FE with consumed inputs, an active snapshot and saved output choices, captured-animal payloads, unused stored items, preexisting outputs, residual fluid and stored FE. It records exact controller/hatch NBT and blockstates, saves the world, and shuts down normally.
2. The workflow archives that original world before the second process starts. The 2.6 runtime loads the same world without conversion. The harness compares every saved block and block entity, validates all old builds, releases all six chunk tickets, waits for real disk unload, reloads fresh controllers, and compares again. It then completes each cycle with exactly the remaining FE, checks unchanged consumed-input/catalyst payloads and saved outputs, extracts through output-facing capabilities, and checks for replay.

Only a newly generated, loopback-only CI server world is used. No user world, server or credentials are accessed. Failure produces no success marker and fails the workflow even if the server shuts down with status zero. A 30-day artifact retains the original 2.5 world archive, NBT expectations, runtime binary digests and phase logs/results.

Scope: actual two-binary upgrade on NeoForge 1.21.1; the existing main suite independently covers runtime/layout/persistence behavior on all three targets. This adds no gameplay changes or version bump.
