# Terrestrial Habitat controller texture

Generated with the built-in image generation tool for the habitat entrance redesign. This is a production texture source, not an in-game screenshot.

## Source and exports

- `terrestrial-habitat-controller-source.png`: original 1774 × 887 opaque atlas, idle square on the left and active square on the right.
- Source crops: idle `(0, 0, 887, 887)` and active `(887, 0, 887, 887)`.
- Each crop is exported to 32 × 32 with nearest-neighbor sampling. The 16px trial lost the shelter/growth detail, so it was rejected.
- Exported to the existing habitat-only `terrestrial_habitat_off_overlay.png` and `terrestrial_habitat_on_overlay.png` in `common/src/main/resources/assets/biotech/textures/block/`.
- `MachineGenerator` composites at the larger input resolution with nearest-neighbor scaling, and emits the normal habitat `_off.png` / `_on.png` fronts on all three targets. Other controllers remain pixel-identical 16px assets.
- The shared material atlas, shared casing, machine IDs and controller models are unchanged. The front uses graphite/ivory/teal with a muted idle glyph and fresh green active glyph. The shelter roof, upward growth arrow, and grass cue distinguish the habitat from generic industrial controllers.

Export commands (from the repository root):

```sh
magick docs/art/terrestrial-habitat-controller-source.png -crop 887x887+0+0 +repage -filter point -resize 32x32 common/src/main/resources/assets/biotech/textures/block/terrestrial_habitat_off_overlay.png
magick docs/art/terrestrial-habitat-controller-source.png -crop 887x887+887+0 +repage -filter point -resize 32x32 common/src/main/resources/assets/biotech/textures/block/terrestrial_habitat_on_overlay.png
```

## Generation prompt

Use case: stylized-concept. Asset type: production Minecraft pixel-art controller texture atlas for a Terrestrial Habitat animal-care machine. Generate one flat, front-orthographic 2:1 landscape canvas containing exactly TWO seamless-adjacent equal square tiles, no gutters, left idle state, right active state. These are the same panel with identical pixel geometry; only the illuminated growth symbol and small status light change color. Each square MUST be designed on an extremely coarse exact 16 by 16 logical pixel grid with hard square cells, enlarged for output, no subpixel detail. The texture fills each square edge to edge. Frame: dark graphite steel, very thin desaturated ivory enamel top/side edging, muted dark teal trim, compact inset charcoal display. Central symbol: a chunky easily readable upward growth arrow under a small simple peaked shelter-roof glyph; a tiny grass/leaf cue at its base to communicate animal habitat. Keep symbol large, about 8 logical pixels wide, no animals, no letters or numbers. The left idle symbol is muted gray-green with a dark status light; the right active symbol is bright fresh leaf green with one pale mint highlight and a matching green status light. Top and side frames, bottom service grille, and all nonlit pixels must be identical between panels. Palette is tightly limited, flat colors, modest pixel shading, no smooth gradients. Chunky Minecraft industrial pixel art matching graphite/ivory/teal Biotech machine materials. No perspective, no 3D cube, no environment, no shadows cast outside, no labels, no watermark, no transparency. All detail must remain legible when each tile is downsampled to 16x16 pixels.

## Checks

`MachineTextureTest` checks unclipped 32px overlays, nearest-neighbor enlargement, transparent pixels, unchanged legacy 16px compositing, distinct active-state pixels, and matching generated habitat fronts across all three native targets. Visual review used enlarged nearest-neighbor previews of the actual 32px exports.
