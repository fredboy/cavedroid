package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.Gdx;
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

import java.util.HashMap;

public class Assets {

    public static final JsonReader jsonReader = new JsonReader();
    public static final Sprite[][] playerSprite = new Sprite[2][4];
    public static final Sprite[][] pigSprite = new Sprite[2][2];
    public static final HashMap<String, TextureRegion> textureRegions = new HashMap<>();
    public static final ArrayMap<String, TouchButton> guiMap = new ArrayMap<>();
    private static final GlyphLayout glyphLayout = new GlyphLayout();
    static BitmapFont minecraftFont;

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
                sprite[i][j] = flippedSprite(new Texture(
                        assetLoader.getAssetHandle("mobs/" + mob + "/" + i + "_" + j + ".png")));
                sprite[i][j].setOrigin(sprite[i][j].getWidth() / 2, 0);
            }
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
            Texture texture = new Texture(assetLoader.getAssetHandle(file.name() + ".png"));
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

    public static void load(final AssetLoader assetLoader) {
        loadMob(assetLoader, playerSprite, "char");
        loadMob(assetLoader, pigSprite, "pig");
        loadJSON(assetLoader);
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
