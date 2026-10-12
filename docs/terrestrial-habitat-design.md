# Terrestrial Habitat: shelter, run, service pod

The habitat is a small animal-care facility rather than one cubic enclosure.

- **Rear shelter:** the back four rows have a stepped roof. Lower eaves flank a higher three-block-wide ridge with ventilation, a skylight and a grow light. Corner posts carry the canopy; the interior remains at least three blocks clear.
- **Exercise run:** the front area is open to the sky. Low framed-glass walls bound it without continuing the rear shelter into a tall box. There are no high beams joining the front arch to the shelter.
- **Offset service pod:** the front-left feeding and controls cabinet has its own lower vented roof and inspection window. Its smaller footprint and height create a second distinct mass; it does not pretend to be another walk-in room.
- **Entrance:** a separate right-hand arch sits beside the controller. Its clear opening is two blocks wide and three high, with a level path into the run and shelter. The support beside the controller is independent of the low enclosure.

Overall footprint is unchanged at 7 × 7; the highest shelter ridge is six blocks above the base layer including that base. The arch reaches five blocks and the service pod four, creating three visibly different heights. The seven automation ports and controller keep their exact 2.6 positions and facings.

The initial PR iteration was visually too cubic. This revision removes the continuous upper perimeter, lowers the run walls, introduces an offset service mass and raises only the rear roof ridge. Texture is not used as a substitute for changed geometry.

From 2.7-alpha.2, only this current layout forms. The old 2.5 Habitat and the original 2.6 Habitat are retired: their blocks remain in the world, but their controllers stop processing until players rebuild. There is no old-layout configuration or automatic conversion. Historical patterns remain solely as rejection-test and before-image fixtures. See [the rebuilding and stored-content policy](machine-upgrade.md); Smart Hammer fills cleared positions but cannot replace solid old shell blocks.
