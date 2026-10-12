# Multiblock redesign: existing-world policy

## Current layouts required from 2.7-alpha.2

Only the current construction preview is accepted for each machine. Complete 2.5 layouts and the old 2.6 Terrestrial Habitat are no longer validation fallbacks. This is a deliberate breaking structure change; there is no configuration option to select an old layout.

On loading a retired structure, validation fails and the controller stops processing. Installing this update does not replace blocks, move hatches, clear items/fluids/energy, or reset persisted active-cycle snapshots and saved output decisions. A stopped old structure must be rebuilt before production can resume. Mixed partial layouts also fail validation.

The controller remains at its existing origin. Use the guide for the current hatch coordinates and facings; those are always used for discovery after successful validation.

Published 2.6 and 2.7-alpha.1 still accept the old layouts. This PR changes the next version, 2.7-alpha.2, and does not rewrite those release binaries.

## Rebuilding and stored contents

Before updating, stop new input, clear blocked outputs, let active cycles finish, then empty all item/fluid hatches and recover stored FE where possible. After updating, old structures cannot finish their cycles while invalid.

If already updated, first recover accessible hatch contents with automation. Do not assume that breaking a controller preserves its active transaction, or that breaking a hatch preserves fluids/FE. Keep a world backup before dismantling a machine whose inputs were already consumed. This update adds no automatic content or transaction transfer.

Clear the old shell and obstructing blocks, rebuild the current guide layout, and reconnect automation to the marked hatch faces. Old blocks outside the new footprint are not removed automatically.

## Smart Hammer

With enough matching materials in the player's inventory, shift-use NsTut Lib's Smart Hammer on the retained controller to fill missing positions of the current pattern. It skips matching blocks and consumes inventory only for missing placements.

The pinned NsTut Lib Smart Hammer refuses any mismatching block whose state is not replaceable. It cannot exchange a solid old shell for a new shell, dismantle surplus old blocks, move loaded hatches or refund replaced shell blocks to inventory. Players must clear those obstructions themselves before building. Water placements consume water buckets and return empty buckets.

## Verification scope

Current native checks test every machine and all four horizontal rotations, rejection of retired 2.5 layouts and the old 2.6 Habitat, missing/wrong parts and hatch facings, current-layout processing, blocked outputs, invalidation/reformation, exact saved outputs and populated save/reload. The real disk-backed active Fermenter fixture now uses the current layout.

The separate released-binary harness remains historical evidence for the unmodified 2.5-to-2.6 upgrade. It must not be cited as proof that current 2.7-alpha.2 accepts retired layouts. Its original six-machine save, exact controller/hatch NBT, active snapshots, chunk unload/reload and exact completion were verified under those two published binaries.

Actual client galleries retain retired before images for comparison. Retired shells are explicitly expected to be invalid; current after images must form in both the integrated server and client. Only NeoForge 1.21.1 is photographed; native checks cover all three supported targets.

See [material counts and crafting budgets](machine-materials.md). Historical 2.5 pattern bodies and original offset tables remain frozen solely for rejection fixtures, before-image galleries and baseline material comparisons. They are never returned by a controller's live pattern or selected for hatch discovery.
