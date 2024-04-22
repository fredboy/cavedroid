package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.game.objects.TouchButton;
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Assets {

    private static final int BLOCK_DAMAGE_STAGES = 10;

    public static final JsonReader jsonReader = new JsonReader();

    private static final List<Texture> loadedTextures = new LinkedList<>();

    public static final Sprite[][] playerSprite = new Sprite[2][4];
    public static final Sprite[][] pigSprite = new Sprite[2][2];

    public static final Sprite[] blockDamageSprites = new Sprite[10];

    public static final HashMap<String, TextureRegion> textureRegions = new HashMap<>();
    public static final ArrayMap<String, TouchButton> guiMap = new ArrayMap<>();
    private static final GlyphLayout glyphLayout = new GlyphLayout();
    public static BitmapFont minecraftFont;

    public static Map<String, Texture> blockTextures = new HashMap<>();
    public static Map<String, Texture> itemTextures = new HashMap<>();

    public static void dispose() {
        minecraftFont.dispose();
        loadedTextures.forEach(Texture::dispose);
        loadedTextures.clear();
    }
    
    private static Texture loadTexture(FileHandle fileHandle) {
        Texture texture = new Texture(fileHandle);
        loadedTextures.add(texture);
        return texture;
    }

    private static TextureRegion flippedRegion(Texture texture, int x, int y, int width, int height) {
        return new TextureRegion(texture, x, y + height, width, -height);
    }

    private static Sprite flippedSprite(Texture texture) {
        Sprite sprite = new Sprite(texture);
        sprite.flip(false, true);
        return sprite;
    }

    private static Sprite flippedSprite(TextureRegion texture) {
        Sprite sprite = new Sprite(texture);
        sprite.flip(false, true);
        return sprite;
    }

    private static void loadMob(AssetLoader assetLoader, Sprite[][] sprite, String mob) {
        for (int i = 0; i < sprite.length; i++) {
            for (int j = 0; j < sprite[i].length; j++) {
                sprite[i][j] = flippedSprite(loadTexture(
                        assetLoader.getAssetHandle("mobs/" + mob + "/" + i + "_" + j + ".png")));
                sprite[i][j].setOrigin(sprite[i][j].getWidth() / 2, 0);
            }
        }
    }

    private static void loadBlockDamage(AssetLoader assetLoader) {
        final Texture blockDamageTexture = loadTexture(assetLoader.getAssetHandle("break.png"));
        for (int i = 0; i < BLOCK_DAMAGE_STAGES; i++) {
            blockDamageSprites[i] = new Sprite(flippedRegion(blockDamageTexture, i * 16, 0, 16, 16));
        }
    }

    private static void setPlayerHeadOrigin() {
        for (Sprite[] sprites : playerSprite) {
            sprites[0].setOrigin(sprites[0].getWidth() / 2, sprites[0].getHeight());
        }
    }

    /**
     * Loads texture names and sizes from <b>json/texture_regions.json</b>, cuts them to TextureRegions
     * and puts to {@link #textureRegions} HashMap
     */
    private static void loadJSON(AssetLoader assetLoader) {
        JsonValue json = jsonReader.parse(assetLoader.getAssetHandle("json/texture_regions.json"));
        for (JsonValue file = json.child(); file != null; file = file.next()) {
            Texture texture = loadTexture(assetLoader.getAssetHandle(file.name() + ".png"));
            if (file.size == 0) {
                textureRegions.put(file.name(),
                        flippedRegion(texture, 0, 0, texture.getWidth(), texture.getHeight()));
            } else {
                for (JsonValue key = file.child(); key != null; key = key.next()) {
                    int x = getIntFromJson(key, "x", 0);
                    int y = getIntFromJson(key, "y", 0);
                    int w = getIntFromJson(key, "w", texture.getWidth());
                    int h = getIntFromJson(key, "h", texture.getHeight());
                    textureRegions.put(key.name(), flippedRegion(texture, x, y, w, h));
                }
            }
        }
    }

    private static Texture resolveTexture(AssetLoader assetLoader, String textureName, String lookUpPath, Map<String, Texture> cache) {
        if (cache.containsKey(textureName)) {
            return cache.get(textureName);
        }

        final Texture texture = loadTexture(assetLoader.getAssetHandle(lookUpPath + File.separator + textureName + ".png"));
        cache.put(textureName, texture);

        return texture;
    }

    public static Texture resolveItemTexture(AssetLoader assetLoader, String textureName) {
        return resolveTexture(assetLoader, textureName, "textures/items", itemTextures);
    }

    public static Texture resolveBlockTexture(AssetLoader assetLoader, String textureName) {
        return resolveTexture(assetLoader, textureName, "textures/blocks", blockTextures);
    }

    private static void loadAllPngsFromDirInto(FileHandle dir, Map<String, Texture> loadInto) {
        for (FileHandle handle : dir.list((d, name) -> name.endsWith(".png"))) {
            loadInto.put(handle.nameWithoutExtension(), loadTexture(handle));
        }
    }

    private static void loadItems(AssetLoader assetLoader) {
        final FileHandle itemsDir = assetLoader.getAssetHandle("textures/items");
        loadAllPngsFromDirInto(itemsDir, itemTextures);
    }

    private static void loadBlocks(AssetLoader assetLoader) {
        final FileHandle blocksDir = assetLoader.getAssetHandle("textures/blocks");
        loadAllPngsFromDirInto(blocksDir, blockTextures);
    }

    public static void load(final AssetLoader assetLoader) {
        loadMob(assetLoader, playerSprite, "char");
        loadMob(assetLoader, pigSprite, "pig");
        loadBlockDamage(assetLoader);
        loadJSON(assetLoader);
        loadBlocks(assetLoader);
        loadItems(assetLoader);
        setPlayerHeadOrigin();
        minecraftFont = new BitmapFont(assetLoader.getAssetHandle("font.fnt"), true);
        minecraftFont.getData().setScale(.375f);
    }

    /**
     * @param s string whose width you want to know
     * @return A width of string written in {@link #minecraftFont} in pixels
     */
    public static int getStringWidth(String s) {
        glyphLayout.setText(minecraftFont, s);
        return (int) glyphLayout.width;
    }

    /**
     * @param s string whose height you want to know
     * @return A height of string written in {@link #minecraftFont} in pixels
     */
    public static int getStringHeight(String s) {
        glyphLayout.setText(minecraftFont, s);
        return (int) glyphLayout.height;
    }

    public static int getIntFromJson(JsonValue json, String name, int defaultValue) {
        return json.has(name) ? json.getInt(name) : defaultValue;
    }

    public static float getFloatFromJson(JsonValue json, String name, float defaultValue) {
        return json.has(name) ? json.getFloat(name) : defaultValue;
    }

    public static String getStringFromJson(JsonValue json, String name, String defaultValue) {
        return json.has(name) ? json.getString(name) : defaultValue;
    }

    public static boolean getBooleanFromJson(JsonValue json, String name, boolean defaultValue) {
        return json.has(name) ? json.getBoolean(name) : defaultValue;
    }

}
