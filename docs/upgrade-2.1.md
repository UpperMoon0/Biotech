# Upgrade to Biotech 2.1

- Install NsTut Lib 0.8.1 or newer within the 0.8.x compatibility line on every supported target. Biotech's loader metadata rejects plain 0.8; NeoForge 26.1.2 specifically needs the corrected machine block constructor in 0.8.1.
- Keep client and server Biotech versions aligned. Network protocol version 2 requires exact protocol agreement.
- Custom machine recipe JSON must use the schema for its Minecraft target. Forge 1.20.1 keeps legacy `Count` and `FluidName`/`Amount` fields, while NeoForge 1.21.1 and 26.1.2 use lowercase `count` and fluid `id`/`amount`. When moving a pack between these Minecraft versions, migrate custom recipe JSON accordingly. Running recipes are persisted transactionally and probabilistic output decisions survive reloads.
- All six machine recipe types and serializers are registry-backed under `biotech:*` before recipe synchronization.
- Input/output hatches enforce their direction for external automation, and energy input hatches are externally receive-only.
- Captured animal items from older versions remain supported; newly captured animals preserve full gameplay state.
