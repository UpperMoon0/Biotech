# Biotech 2.8-alpha.1

- Allow NsTut Lib 0.8.2+ and OpenUI MC 0.0.10+ without arbitrary upper-version ceilings, retaining each loader's required dependency semantics.
- Compile against NsTut Lib 0.8.2 and the tested OpenUI source revision containing the clipping/hover-preview fix. Preserve the current machine layouts and saved-state behavior.
- Validate Forge 1.20.1, NeoForge 1.21.1 and NeoForge 26.1.2, including machine GameTests and UI/JEI preview coverage.

**Release gate:** This alpha has not been published. OpenUI-MC #16 must be merged, versioned and released before Biotech claims compatibility with a public OpenUI binary; reverify against that release. Accepting newer versions in metadata does not guarantee future API compatibility.
