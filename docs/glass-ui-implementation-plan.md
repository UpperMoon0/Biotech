# Biotech glass UI implementation plan

Status: portable tinted-surface implementation delivered on the existing PR branches. The implementation uses gradients, translucency, disjoint borders, shadows, shared production/preview backdrops, redesigned machine cards, and unchanged hatch slot coordinates. The preview job now renders 56 plain/textured images. Framebuffer blur, an expanded GUI-scale stress gallery, and full in-world manual interaction checks remain follow-up work; they are not claimed by the current automated verification.

## Objective

Redesign every Biotech machine and hatch screen using OpenUI, preserving existing gameplay and inventory behavior while introducing the visual hierarchy and depth of the supplied dashboard reference. Use Biotech's forest-green, sage, and mint palette rather than the reference's warm gray and orange palette.

The reference establishes a direction: translucent outer panels, grouped inner cards, restrained highlights, soft shadows, clear headings, and prominent status information. Adapt that direction to Minecraft's screen sizes, pixel font, item rendering, and GUI scales. Do not reproduce the reference's banking content, decorative navigation, or unrelated controls.

## Repository and branch strategy

| Repository | Existing PR | Branch | Planned responsibility |
| --- | --- | --- | --- |
| OpenUI-MC | [#9](https://github.com/UpperMoon0/OpenUI-MC/pull/9) | `feature/modern-framework-foundation` | Reusable surface rendering and styling capabilities |
| Biotech | [#34](https://github.com/UpperMoon0/Biotech/pull/34) | `codex/openui-all-versions` | Biotech theme, screen composition, fixtures, and integration |

1. Fetch both PR branches and inspect their latest changes before editing. The OpenUI branch inspected for this plan was at `d6138702e5f850ea8b7606100e672b52fda1305f`; this is a reference point, not a requirement to reset to that commit.
2. Continue OpenUI's existing PR branch in a suitable checkout/worktree. Preserve any unrelated local work and do not start the implementation from the detached main checkout.
3. Keep reusable rendering features in OpenUI and machine-specific layouts, labels, colors, and data bindings in Biotech.
4. Publish and validate the exact OpenUI revision before updating Biotech's provider pin. Ensure CI and local builds resolve the same provider revision without stale Maven Local artifacts.
5. Update the two existing PR descriptions with final scope, validation evidence, dependencies, and remaining limitations.

## Current implementation and gaps

Biotech shares its six machine layouts through `MachineUi`, hatch content through `HatchUi`, and live display data through `MachineDisplay`. Platform adapters handle legacy `GuiGraphics` and the 26.1.2 graphics extraction path. Vanilla still owns real hatch slots and inventory interaction.

Current screens use a mostly flat outer panel, small gauges, loosely grouped items, and little distinction between titles, status, and supporting information. The goal is to improve structure as well as surface styling.

OpenUI already has themes, typography roles, cards, rounded panels, basic shadows, highlights, progress tracks, and pills. PR #9 adds a shared `UiDrawContext` interface and immutable styles. Inspection did not find a general glass material, reusable rounded gradient surface, configurable soft shadow, or backdrop-blur API. Recheck the branch before introducing new equivalents.

## Visual specification

### Palette and theme roles

Use the following initial palette as implementation starting values; tune against actual game-rendered previews:

| Role | Initial color | Use |
| --- | --- | --- |
| Backdrop | `#101713` | Dark neutral-green surround |
| Shell | `#192622` | Main tinted glass panel |
| Raised surface | `#263B32` | Recipe, resource, and inventory cards |
| Primary accent | `#78D5AB` | Progress and active emphasis |
| Border/highlight | `#91B5A1` | Restrained panel edges |
| Primary text | `#EAF3EC` | Titles and important values |
| Secondary text | `#ADC2B5` | Labels and supporting information |
| Energy/warning | `#E8BC5A` | Energy indicators and warnings |
| Fluid | `#69AED7` | Fluid accents; preserve fluid textures/tints |
| Invalid/error | `#DD8383` | Invalid structure and error status |

Define opacity, spacing, radii, typography, and elevation centrally. Use more opaque inner cards when needed for readability. Keep color paired with labels or symbols so state is understandable without color alone.

### Surface hierarchy

- Outer shell: large rounded corners, subtle green gradient, thin bright edge, soft exterior shadow, and optional background blur.
- Inner cards: smaller radii, quiet borders, slightly lighter tint, and consistent padding.
- Item and inventory wells: inset surfaces that clearly communicate boundaries without competing with items.
- Status badge: compact labeled pill, with restrained state-specific color.
- Resource displays: visible values and units, readable tracks, and live detailed tooltips.

Start with a logical spacing scale of 4, 8, and 12 pixels. Use crisp pixel-aligned content and avoid shrinking item sprites or text to make a layout fit. Determine final screen dimensions using actual GUI-scale and small-window checks; do not stretch the desktop reference into the existing bounds indiscriminately.

### Motion and typography

Use a clear title, section-label, and value hierarchy through OpenUI's existing typography support. Avoid an external font dependency unless a demonstrated readability issue requires it. Any optional transition must be subtle, must not delay state updates, and must have deterministic behavior in previews. Static polish is the first milestone; decorative animation is not required.

## OpenUI implementation

### Shared surface capability

Introduce an additive, reusable surface/material description, with final naming chosen to fit the existing API. It should express tint/opacity, rounded corners, border width/color, gradient colors, highlight, and shadow settings. Expose it through the shared drawing interface and an appropriate component/style entry point so consumers do not duplicate raw platform drawing code.

Required behavior:

- Rounded gradients respect bounds and corner geometry.
- Translucent borders do not tint the entire interior through a full solid border underlay. Inspect the existing nested-rectangle outline implementation before reusing it for glass surfaces.
- Alpha blending does not accumulate unevenly where rounded-rectangle fill segments overlap.
- Shadow softness and extent are bounded and configurable; clipping and layout bounds remain predictable.
- Existing opaque panels and old method signatures keep their previous behavior.
- Additions to the shared drawing interface preserve the compatibility guarantees of PR #9, using default implementations or an optional capability interface where appropriate.
- Shared geometry and style logic are reused across platform adapters.

### Backdrop-blur feasibility milestone

Prototype blur on the Greenhouse shell before committing to a public blur contract. Determine how each supported renderer captures and samples the background, orders rendering, and restores graphics state. In particular, account for deferred extraction in 26.1.2 rather than copying an immediate-mode implementation.

The effect must blur only the content behind the shell. Text, items, gauges, tooltips, and carried stacks must remain sharp. Blur must not recursively sample already-rendered cards or require a separate full-screen pass per card. Cache or reuse work within a frame where safe, and release resources on resize, screen removal, and reload as appropriate.

Provide a low-cost translucent/gradient fallback when blur is unsupported or disabled. Compare fallback and blur screenshots on plain and textured backgrounds. If a renderer cannot support correct blur within reasonable complexity or cost, document that limitation and ship the same layout with the fallback on that renderer. Do not block the entire visual redesign on blur.

### OpenUI verification

- Test surface geometry at tiny sizes, zero dimensions, large radii, translucent colors, and clipping boundaries.
- Test style composition and explicit overrides if the style API is extended.
- Verify graphics-state restoration and effect-resource lifecycle where applicable.
- Run the existing shared-source parity, full supported-loader build, unit-test, public binary compatibility, and downstream boot gates from PR #9.
- Inspect representative rendered surfaces on Biotech's three target versions. Record rendering cost on the test environment rather than claiming a hardware-independent performance budget.

## Biotech implementation

### Shared screen structure

Create a central Biotech theme and shared shell/card composition used by both production screens and the preview runner. Candidate helpers include a shell, header, status badge, resource card, recipe section, and inventory section; keep their names and structure aligned with repository conventions.

The composition should provide:

1. A header containing the machine or hatch icon, title, and status where meaningful.
2. Clearly grouped energy, fluid, and progress information sourced from existing synchronized data.
3. A primary recipe area with input/output labels and visible directional connections.
4. A footer or supporting area only when it communicates existing information.

Do not add buttons, configuration settings, history charts, or machine capabilities that the current menus do not support. Keep the existing recipe visibility rules and truthful idle/invalid states. Display empty sections with useful labels rather than stale active recipe contents.

### Machine-specific layouts

| Screen | Composition to preserve and improve |
| --- | --- |
| Mixer | Grouped ingredient grid leading to output, with central progress and a separate energy card; no invented fluid requirement |
| Fermenter | Ingredient group, input description, fluid requirement, and output connected through progress |
| Greenhouse | Seed as the main input, fertilizer/water as supporting inputs, growth progress, and grouped harvest outputs |
| Breeding Chamber | Parent animal input leading to offspring, with food/water in a supporting requirements card |
| Terrestrial Habitat | Young animal input leading to grown animal and byproducts, with food/water grouped below |
| Slaughterhouse | Animal input leading to a vertically organized output list, preserving quantities and chance labels |

Ensure every existing recipe's input/output count fits or has a deliberate overflow treatment. Keep quantities, probabilities, item-name tooltips, energy values, fluid values, and progress available. Avoid hardcoding the design around only the sample recipes.

### Hatches and inventory safety

Cover item input, item output, fluid input, fluid output, and energy input hatches. Give storage and player inventory separate visual grouping while keeping existing menu-relative slot coordinates as the baseline. If more header space is needed, first evaluate a decorative shell extension around the content instead of moving interactive slots independently of the menus.

Retain vanilla ownership of slot hit testing, pickup, drag, shift-click, carried stacks, and item tooltips. Decorative OpenUI components must not intercept those actions. Preserve foreground layering for hover effects and carried items. Make the tank/energy presentation consistent with machine resource cards without hiding real slots or changing capacity behavior.

## PNG preview job changes

The existing job renders 28 cases on NeoForge 1.21.1: six machines in active/idle/invalid states and five hatches in empty/filled states. Retain that coverage.

1. Replace the preview's independently painted shell with the shared production shell rendering. Keep sample inventory contents explicitly identified as fixtures.
2. Render each of the 28 cases on both a plain background and a deterministic textured background: 56 baseline images. The textured scene should reveal translucency without depending on an external image or a loaded world.
3. Add explicit fallback examples if blur is implemented, and selected stress cases for long labels, dense recipes, small windows, and GUI scale. Keep the expected case inventory centralized instead of scattering hardcoded totals.
4. Update the manifest with screen, state, background, GUI scale, effect mode, and dimensions. Validate required cases, unique filenames, successful completion, readable PNGs, and expected dimensions.
5. Update crop bounds and padding for the final shell dimensions and shadow extent; the current 528 × 416 and 400 × 380 sizes may change.
6. Organize the HTML gallery so reviewers can compare states and background variants easily. Preserve deterministic sample values and neutral pointer positioning.
7. Continue uploading PNGs, manifest, gallery, and failure logs through the existing workflow. Verify release and source JARs still exclude the preview mod.

The canonical PNG job validates shared composition. It does not replace in-world interaction checks or certify each native rendering adapter. Keep those limitations explicit in documentation and PR descriptions.

## Implementation sequence and completion gates

### Phase 1: establish baselines

- Fetch and inspect both existing PR branches and applicable repository instructions.
- Build the current OpenUI PR provider and verify Biotech compatibility before visual edits.
- Preserve current gallery output for before/after comparison outside release artifacts.
- Inventory recipe sizes, screen constraints, hatch slot coordinates, and existing theme capabilities.

Completion: current behavior and provider compatibility are reproducible, and missing capabilities are confirmed against the latest branch.

### Phase 2: reusable surfaces and two representative screens

- Implement the minimum OpenUI surface additions and Biotech theme.
- Redesign Greenhouse and fluid input hatch using shared production/preview rendering.
- Evaluate backdrop blur and fallback on textured backgrounds.
- Inspect text contrast, material depth, slot alignment, and small-window behavior; iterate before rolling out.

Completion: both representative screens demonstrate the intended appearance with readable content and correct layering.

### Phase 3: complete all screens

- Apply the shared structure to the other five machines and four hatches.
- Preserve each machine's diagram and all live data bindings.
- Address dense recipes, long names, empty values, invalid structures, and unsynchronized capacities.
- Add translated labels for new presentation text through the existing localization mechanism.

Completion: all 11 screens use the new visual system, with no legacy replacement screen introduced.

### Phase 4: expand previews and verify behavior

- Generate the full plain/textured gallery and selected stress cases.
- Check Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2 builds and unit/gameplay tests.
- Inspect native rendering on each target, including tooltip and effect layering.
- Exercise hatch pickup, drag, shift-click, carried stacks, inventory-key dismissal, close/reopen, resizing, and GUI-scale changes in-world.
- Confirm live tooltips refresh while the pointer remains stationary.
- Inspect release and source JARs for preview-code exclusion.

Completion: the gallery is visually reviewed, required checks pass, and any unperformed checks are explicitly recorded.

### Phase 5: publish coordinated PR updates

- Commit reusable OpenUI changes on the existing OpenUI PR branch and run its compatibility gates.
- Pin Biotech to the validated OpenUI commit and verify a fresh provider build resolves correctly.
- Commit Biotech's theme, layouts, preview updates, and documentation to its existing PR branch.
- Update both PR descriptions with screenshots/artifact locations, test results, blur/fallback behavior, and dependency ordering.

Completion: both existing PRs contain reviewable changes, with Biotech's provider pin matching the published OpenUI revision. Merge remains a separate action from updating the PRs.

## Acceptance criteria

- All six machines and five hatches have clear grouping, readable hierarchy, layered Biotech-colored surfaces, and consistent resource/status presentation.
- Items, counts, output chances, fluid requirements, progress, and live tooltips retain existing meaning and behavior.
- Glass effects preserve sharp foreground content and offer a usable fallback.
- Hatch visuals match actual slot coordinates and do not interfere with inventory interaction.
- Representative small-window and GUI-scale checks show no clipped essential content or inaccessible slots.
- The preview job uses production shell/content code and generates validated, reviewable artifacts.
- OpenUI additions remain reusable and compatible with existing consumers.
- Both PRs are updated, all required checks are reported accurately, and release artifacts contain no preview runner.
