# Upgrade to Biotech 2.1

- Install NsTut Lib 0.8.1 or newer within the 0.8.x compatibility line on every supported target. Biotech's loader metadata rejects plain 0.8; NeoForge 26.1.2 specifically needs the corrected machine block constructor in 0.8.1.
- Keep client and server Biotech versions aligned. Network protocol version 2 requires exact protocol agreement.
- Existing machine recipe JSON keeps its data format; running recipes are now persisted transactionally and probabilistic output decisions survive reloads.
- All six machine recipe types and serializers are registry-backed under `biotech:*` before recipe synchronization.
- Input/output hatches enforce their direction for external automation, and energy input hatches are externally receive-only.
- Captured animal items from older versions remain supported; newly captured animals preserve full gameplay state.
