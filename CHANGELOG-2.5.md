# Biotech 2.5

- Add persisted Ignore signal / Needs signal / No signal controls to all six controllers. Paused transactions retain their inputs, energy progress, recipe snapshot, and output rolls.
- Show server-authoritative stall diagnostics in controller menus and Jade, including missing fluid, energy, blocked outputs, redstone pause, and transaction rejection.
- Add bounded server settings for new-cycle energy cost and controller processing rate; preserve default balance and active saved costs.
- Apply server balance settings to JEI energy and timing estimates.
- Localize controller diagnostics, controls, energy/fluid tooltips, and fluid product descriptions; document Patchouli translation and upgrade behavior.
- Add native regressions for pause/resume, exact output preservation across reload, output diagnostics, and active cost preservation on every supported target.
