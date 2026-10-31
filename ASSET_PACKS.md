# Creating asset packs

CaveDroid supports local ZIP asset packs on desktop and Android. Open **Settings →
Asset packs → Import ZIP**, enable a pack, press **Done**, and restart the app.
The top enabled pack has the highest priority. Files missing from it come from
lower packs, then the game's bundled assets. Packs are applied to every world.

## Your first pack

1. Make an empty directory named `MyPack`.
2. Create `pack.json` using the example below.
3. Add your own PNG icon as `icon.png`.
4. Create `textures/textures/blocks/stone.png`: a 16×16, 32×32, or 64×64 PNG.
   The repeated `textures/textures` is intentional: it matches the game's assets.
5. ZIP the **contents** of `MyPack`, without the enclosing directory. For example,
   run `zip -r ../MyPack.zip .` inside `MyPack`.
6. Import and enable the ZIP, press Done, and fully close and reopen CaveDroid.
7. Open a world containing stone. Its appearance changes; its size stays the same.

```text
MyPack.zip
├── pack.json
├── icon.png
└── textures
    └── textures
        └── blocks
            └── stone.png
```

Use case-sensitive paths with `/` separators. Do not put an `assets/` directory
inside the ZIP. Include only files you want to replace or add.

The repository includes a reproducible example generator:

```bash
python3 scripts/create-example-asset-packs.py
```

It writes original checkerboard example packs at 16×, 32×, and 64× resolution to
`build/asset-pack-examples/`. These examples are useful for testing import and
stack priority and do not redistribute the bundled artwork.

## Metadata

This is a complete minimal `pack.json`:

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

| Field | Meaning |
| --- | --- |
| `formatVersion` | Required integer declaring the asset-pack format this pack supports. Currently `1`; it must match the game’s supported pack format. |
| `name` | Required nonempty display name. |
| `version` | Required nonempty pack version string. This is separate from the game version. |
| `author` | Required nonempty author or team name. |
| `license` | Required nonempty license label, such as `CC0-1.0` or `CC-BY-SA-4.0`. |
| `icon` | Required relative path to a PNG in this pack, at most 1024×1024 pixels and 4 MiB. |
| `textureScales` | Optional object mapping image paths in this pack to positive integer scale factors. |

Asset-pack format versions are independent of game releases and of the pack's own
`version`. A pack declares a single `formatVersion`, with no game-version range.
Format `1` remains compatible across game releases until the asset-pack layout or
data contract changes incompatibly. Such a change increments the supported format
version; authors then update their packs and declare that version. Packs targeting
an unsupported format are rejected during import.

Importing the same ZIP twice does not create a duplicate. Different ZIPs, including
different releases of the same pack, can coexist.

## What can be overridden

Use the [bundled assets](assets/) as a path and schema reference.

| Content | Relevant paths |
| --- | --- |
| Block textures | `textures/textures/blocks/*.png` |
| Item textures | `textures/textures/items/*.png` |
| Mob parts and armor | `textures/mobs/`, `textures/equipment/` |
| HUD, inventories, touch graphics | `textures/`, `touch_gui.png`, `crosshair.png`, `json/texture_regions.json` |
| Environment | `textures/environment/`, `parallax/` |
| Sounds | `sfx/`, including category `index.txt` files |
| Menu styles, atlas and fonts | `skin/skin.json`, `skin/skin.atlas`, `skin/skin.png`, `skin/font/` |
| Localization | `i18n/CaveDroid_Menu_*.properties`, `CaveDroid_Items_*.properties`, `CaveDroid_Onboarding_*.properties` |
| Blocks and items | `json/game_items.json` |
| Mobs | `json/mobs.json` |
| Crafting | `json/crafting.json` |
| Touch controls | `json/touch_buttons.json` |

Packs supply data for existing game behavior. They cannot add executable code,
new behavior implementations, new platform features, or new selectable UI locales.
Launcher icons and other resources consumed before the game starts are not changed.

Sound samples use numbered files such as `0.wav`, `1.wav`, and so on. Loading stops
at the first missing number across the complete stack. Include a full category
index when adding sound groups. Blank index lines are ignored.

Non-JSON files replace the entire file. For example, supply a complete localization
properties file or sound index; their individual entries are not merged. Existing
locale selection and locale fallback still apply. Font pages and atlas images are
looked up independently, so a pack may replace only a referenced image.

## Stack priority and JSON merging

Suppose the enabled list is **A**, then **B**. If both contain `stone.png`, A wins.
If only B contains `dirt.png`, B wins. If neither contains `grass.png`, the bundled
grass is used. Disabling A reveals B's stone after restart.

JSON files merge from bundled data through B and then A. Objects merge recursively.
Arrays, strings, numbers, booleans, and explicit `null` replace the lower value.
Existing entry order is preserved; new entries are appended.

For example, the following **partial** `json/mobs.json` changes only the pig's
health, retaining all its other bundled fields:

```json
{
  "pig": {
    "hp": 20
  }
}
```

If the lower definition were `{"pig":{"hp":10,"speed":2}}`, the merged result
would be `{"pig":{"hp":20,"speed":2}}`. A higher pack can override `hp` again.
Adding a new top-level mob key adds a definition, but that definition must contain
all required fields from the existing mob schema and reference available textures.

An item's fields can be changed in `json/game_items.json` like this:

```json
{
  "blocks": {
    "stone": {
      "hp": 40
    }
  }
}
```

Arrays are replaced as a whole: overriding an entry's `recipes`, `sprites`, or
`drop_info` array replaces that entire array. Recipe `input` arrays are also
replaced, including any `null` empty slots. Include every array element you want
to keep. An empty array clears the array. An empty object does not clear an object.

For a nullable field, `{"tint":null}` clears the previous tint. Null does **not**
delete an entry, and assigning null to a required non-null field is invalid.
There is no definition-deletion syntax. The merged result must satisfy the game's
schemas and references.

## Higher-resolution textures

Image pixels and game coordinates are separate. One block is always 16 logical
pixels wide, regardless of the image's resolution. Larger images retain their full
resolution on the GPU; they do not make blocks, sprites, fonts, or buttons larger.

For an existing image, scale is inferred against its bundled size:

| Bundled image | Replacement | Scale |
| --- | --- | --- |
| 16×16 block | 32×32 | 2 |
| 16×16 block | 64×64 | 4 |
| 16×160 animation sheet | 64×640 | 4 |
| 176×166 inventory | 352×332 | 2 |

Both dimensions must increase by the same integer factor. Different images in a
pack may have different scales. A 64× stone texture does not change the scale of
a bundled 16× dirt texture or a 32× texture supplied by a lower pack.

For **new images** or **changed layouts**, add an explicit scale:

```json
{
  "textureScales": {
    "textures/textures/blocks/my_stone.png": 4,
    "textures/my_inventory.png": 2
  }
}
```

This fragment belongs inside the full metadata object. Each entry must refer to
an image actually supplied by that pack. Its dimensions must be divisible by the
scale. A scale of `1` explicitly treats image pixels as logical pixels. The icon
is displayed separately and does not need a scale declaration.

Keep JSON texture-region coordinates, block sprite margins, mob offsets, and
touch rectangles in logical pixels. For example, a 16-pixel frame begins at
logical y=16 even when it begins at image y=64 in a 4× sheet. Preserve the existing
frame layout and frame count unless you also change the relevant definitions.

Atlas files and bitmap-font files also use **logical coordinates**. When replacing
only an image with a larger version, leave the existing descriptor unchanged.
When repacking an atlas or generating a font, convert descriptor coordinates and
sizes back into logical units; do not feed a descriptor containing 4× source-pixel
coordinates into a texture declared as scale 4. Font metrics, kerning, padding,
atlas offsets, and nine-patch splits remain in logical units too.

Keep transparency and the game's sprite-sheet orientation. PNG replacements use
the existing nearest-neighbor filtering. Device maximum texture dimensions still
apply, particularly for large atlas pages. A pack that exceeds that limit cannot
be loaded on that device.

## Testing and troubleshooting

Test with only your pack enabled, then with another pack above and below it.
Check a world, inventories, menus, animated blocks, mobs, sound, and touch controls
for the content you changed. Test on the lowest-capability device you support.

| Symptom | Check |
| --- | --- |
| Import says metadata is missing | `pack.json` must be at the ZIP root, not under `MyPack/` or `assets/`. |
| A texture does not change | Exact case-sensitive path, enabled order, Done, and a full app restart. |
| A new image fails to load | Add its path to `textureScales`; ensure definitions reference the correct file. |
| Cropped or distorted sprites | Uniform scale, logical region coordinates, and animation-sheet layout. |
| Missing sounds | Correct category index and contiguous numbered sample files. |
| Missing translated labels | Properties files replace whole files; preserve the existing keys. |
| Unsupported asset-pack format | Use the supported `formatVersion` (currently `1`) and follow its layout and data contract. |
| Bundled assets return after startup | The merged stack failed validation/loading. Open Asset packs to read the error, correct the ZIP, and reimport. |

If loading fails, CaveDroid disables the enabled stack, restores bundled assets,
and keeps installed packs so you can fix them. Selection changes are pending until
restart. Back discards selection edits; imported or explicitly removed packs stay
imported or removed. Removing a currently running pack defers deleting its files
until restart.

Limits: 256 MiB compressed, 1 GiB extracted, 20,000 ZIP entries, 64 KiB metadata,
16 MiB per JSON file, and a 4 MiB/1024×1024 icon. Use ordinary, unencrypted ZIPs.
Absolute paths, `..`, backslashes, duplicate entries, and conflicting file/directory
paths and the reserved root entry `.removed` are rejected. Missing assets fall back; malformed overrides cause an error.

Saves do not contain packs or remember a per-world selection. Keep required packs
available when returning to worlds that use custom definitions.

## Sharing and licensing

Share the ZIP and state its asset-pack format version. Use `author` and `license` to
identify your work and its license. Keep license texts and third-party attribution
inside your pack when required; a metadata label does not replace those documents.
Consult the existing `attribution.txt` files before adapting bundled artwork.
The generated checkerboard examples are original CC0 examples.
