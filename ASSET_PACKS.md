# Creating asset packs

CaveDroid supports ZIP asset packs on desktop and Android. Import one in
**Settings → Asset packs → Import ZIP**, enable it, and press **Done** to apply it immediately.
A paused world is saved and reloaded with the selected packs, then stays paused.
The top enabled pack has priority; missing files fall back to lower packs, then
bundled assets. Packs apply to every world and are not included in saves.

## ZIP layout

ZIP the contents of your pack directory, without an enclosing folder or `assets/`
prefix. Paths are case-sensitive and use `/` separators.

```text
MyPack.zip
├── pack.json
├── icon.png
└── textures/textures/blocks/stone.png
```

The repeated `textures/textures` matches the bundled path. Use [assets/](assets/)
as the path and data-schema reference. For working checkerboard examples, run:

```bash
python3 scripts/create-example-asset-packs.py
```

The ZIPs are written to `build/asset-pack-examples/`.

## Metadata

A minimal `pack.json`:

```json
{
  "formatVersion": 1,
  "name": "My Stone Pack",
  "version": "1.0.0",
  "author": "Your name",
  "license": "CC0-1.0",
  "icon": "icon.png"
}
```

All fields above are required. Text fields must be nonempty; `icon` is a relative
PNG path inside the pack. `formatVersion` identifies the pack format, separately
from the game and pack versions; unsupported formats are rejected.
`textureScales` is an optional image-path-to-integer map described below.

Importing the same ZIP again does not duplicate it. Different ZIPs can coexist,
including different versions of the same pack.

## Overrides

| Content | Paths |
| --- | --- |
| Block and item textures | `textures/textures/blocks/`, `textures/textures/items/` |
| Mobs and equipment | `textures/mobs/`, `textures/equipment/` |
| HUD and controls | `textures/`, `touch_gui.png`, `crosshair.png`, `json/texture_regions.json` |
| Environment | `textures/environment/`, `parallax/` |
| Sounds | `sfx/`, including category `index.txt` files |
| Menu styles and fonts | `skin/` |
| Translations | `i18n/` |
| Gameplay data | `json/game_items.json`, `json/mobs.json`, `json/crafting.json`, `json/touch_buttons.json` |

Packs provide data for existing behavior; they cannot add executable code or
selectable UI languages. Non-JSON files replace whole files, including properties
bundles and sound indexes. Sound samples must be numbered contiguously from `0`;
loading stops at the first missing number across the stack.

JSON merges from bundled data through enabled packs, lowest priority first:

- Objects merge recursively. New entries are appended.
- Arrays and scalar values replace the lower value in full.
- An empty array clears an array; an empty object does not clear an object.
- `null` replaces a value but does not delete an entry. Required fields cannot be
  cleared; the merged data must satisfy the game's schemas and references.

For example, this partial `json/mobs.json` changes only the pig's health:

```json
{"pig": {"hp": 20}}
```

Each item in `json/game_items.json` can set `creative_category` to `building`,
`nature`, `utilities`, `equipment`, `food`, `materials`, or `creatures`. This
controls creative inventory filtering independently of the item's `type`.
New items without a category default to `materials`; overrides of existing items
inherit the bundled category unless they replace it explicitly.

Bundled item and block keys use family prefixes such as `armor_`, `tool_`,
`slab_`, and `wood_`. Historical save keys are accepted during repository lookup;
new saves use the canonical keys. Packs should use the canonical definitions
listed in `assets/json/game_items.json`. Related texture filenames and references
use the same family prefixes; shared textures, such as tinted leather armor,
remain explicitly referenced by their definitions. Behavior/action IDs retain
their existing names. The key rename inventory is recorded in `item-key-renames.csv`.

## Higher-resolution textures

One block remains 16 logical pixels wide. Replacing a 16×16 image with a 32×32 or
64×64 image increases resolution without changing its size in the world.
Scale is inferred from the bundled image when both dimensions increase by the
same integer factor. Each image can have its own scale.

For new images or changed layouts, add `textureScales` to `pack.json`:

```json
{
  "textureScales": {
    "textures/textures/blocks/my_stone.png": 4
  }
}
```

Each path must refer to an image supplied by that pack, with dimensions divisible
by its positive integer scale. The icon does not need a scale declaration.

Keep texture regions, sprite margins, offsets, touch rectangles, atlas descriptors,
and font metrics in **logical pixels**. A frame at logical y=16 starts at image
y=64 in a 4× sheet. When replacing only an atlas or font image, keep its descriptor
unchanged. Preserve animation layouts unless you also update their definitions.
Device texture-size limits still apply.

## Troubleshooting

Check paths and stack order, and press **Done** to apply changes first. For distorted sprites, check
uniform scaling and logical coordinates. For missing sounds or labels, check the
complete index or properties file. Test changed content with the pack alone and
with another pack above and below it.

If applying changes fails, the current assets and world remain active; the menu
shows the error so you can fix the ZIP and reimport. At startup, a failing saved
stack is disabled and bundled assets are restored. Imports and removals persist
immediately; selection and priority changes apply with **Done**. Removing a running
pack defers file deletion until the current asset generation is released.

Use ordinary unencrypted ZIPs with relative paths. Import rejects unsafe paths,
duplicates, conflicting file/directory entries, and the reserved root `.removed`
entry. Size and entry limits are defined in
[PackFormat.kt](core/data/assets/src/main/kotlin/ru/fredboy/cavedroid/data/assets/pack/PackFormat.kt)
and [AssetPackRepositoryImpl.kt](core/data/assets/src/main/kotlin/ru/fredboy/cavedroid/data/assets/pack/AssetPackRepositoryImpl.kt).

## Sharing

Share the ZIP with its format version and include required license texts and
attributions. A metadata license label does not replace them. Check bundled
`attribution.txt` files before adapting artwork. Generated checkerboard examples
are CC0.
