# Biotech 2.7-alpha.3

- Allow NsTut Lib 0.8.2+ without an arbitrary upper-version ceiling and require OpenUI MC **0.0.16+** (no upper cap) on clients, retaining all loader dependency semantics.
- Compile against NsTut Lib 0.8.2 and the exact OpenUI MC 0.0.16 release-source revision containing the clipping/hover-preview fix. Preserve current machine layouts and saved-state behavior.
- Validate Forge 1.20.1, NeoForge 1.21.1 and NeoForge 26.1.2, including machine GameTests and UI/JEI preview coverage.

**Release gate:** OpenUI MC 0.0.16 must be publicly released and its published binary independently verified before Biotech 2.7-alpha.3 is published. The source build pins the OpenUI release commit, and loader metadata rejects older known-broken OpenUI versions. Accepting newer versions in metadata does not guarantee future API compatibility.
