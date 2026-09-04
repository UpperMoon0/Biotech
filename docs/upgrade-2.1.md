# Upgrade to Biotech 2.1

- Install NsTut Lib 0.8.1 or newer within the 0.8 line. NeoForge 26.1.2 must not use the older 0.8 build because its machine block constructor discards the keyed block properties supplied during registration.
- Existing machine recipes keep their data format; running recipes are now persisted transactionally.
- Input/output hatches enforce their direction for external automation.
- Captured animal items from older versions remain supported; newly captured animals preserve full gameplay state.
