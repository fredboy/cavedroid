package ru.deadsoftware.cavedroid.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ru.deadsoftware.cavedroid.game.objects.TouchButton;

import java.util.HashMap;

public class Assets {

    public static final JsonReader jsonReader = new JsonReader();

    private static final GlyphLayout glyphLayout = new GlyphLayout();
    static BitmapFont minecraftFont;

    public static final Sprite[][] playerSprite = new Sprite[2][4];
    public static final Sprite[][] pigSprite = new Sprite[2][2];
    public static final HashMap<String, TextureRegion> textureRegions = new HashMap<>();
    public static final ArrayMap<String, TouchButton> guiMap = new ArrayMap<>();
    public static Sprite sandSprite;
    public static Sprite gravelSprite;

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

    private static void loadPlayer() {
        Texture plTex = new Texture(Gdx.files.internal("mobs/char.png"));
        //LOOK TO LEFT
        //head
        playerSprite[0][0] = flippedSprite(new TextureRegion(plTex, 0, 0, 12, 12));
        //body
        playerSprite[0][1] = flippedSprite(new TextureRegion(plTex, 0, 13, 12, 12));
        //hand
        playerSprite[0][2] = flippedSprite(new TextureRegion(plTex, 25, 5, 20, 20));
        //leg
        playerSprite[0][3] = flippedSprite(new TextureRegion(plTex, 25, 27, 20, 20));
        //LOOK TO RIGHT
        //head
        playerSprite[1][0] = flippedSprite(new TextureRegion(plTex, 13, 0, 12, 12));
        //body
        playerSprite[1][1] = flippedSprite(new TextureRegion(plTex, 13, 13, 12, 12));
        //hand
        playerSprite[1][2] = flippedSprite(new TextureRegion(plTex, 37, 5, 20, 20));
        //leg
        playerSprite[1][3] = flippedSprite(new TextureRegion(plTex, 37, 27, 20, 20));
    }

    private static void loadPig() {
        Texture pigTex = new Texture(Gdx.files.internal("mobs/pig.png"));
        pigSprite[0][0] = flippedSprite(new TextureRegion(pigTex, 0, 0, 25, 12));
        pigSprite[1][0] = flippedSprite(new TextureRegion(pigTex, 0, 12, 25, 12));
        pigSprite[0][1] = flippedSprite(new TextureRegion(pigTex, 4, 26, 12, 12));
        pigSprite[1][1] = flippedSprite(new TextureRegion(pigTex, 16, 26, 12, 12));
    }

    /**
     * Loads texture names and sizes from <b>json/texture_regions.json</b>, cuts them to TextureRegions
     * and puts to {@link #textureRegions} HashMap
     */
    private static void loadJSON() {
        JsonValue json = jsonReader.parse(Gdx.files.internal("json/texture_regions.json"));
        for (JsonValue file = json.child(); file != null; file = file.next()) {
            Texture texture = new Texture(Gdx.files.internal(file.name() + ".png"));
            if (file.size == 0) {
                textureRegions.put(file.name(), flippedRegion(texture, 0, 0, texture.getWidth(), texture.getHeight()));
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

    public static void load() {
        loadPlayer();
        loadPig();
        loadJSON();
        minecraftFont = new BitmapFont(Gdx.files.internal("font.fnt"), true);
        minecraftFont.getData().setScale(.375f);
        sandSprite = flippedSprite(new Texture((Gdx.files.internal("textures/blocks/sand.png"))));
        gravelSprite = flippedSprite(new Texture((Gdx.files.internal("textures/blocks/gravel.png"))));
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

    public static String getStringFromJson(JsonValue json, String name, String defaultValue) {
        return json.has(name) ? json.getString(name) : defaultValue;
    }

    public static boolean getBooleanFromJson(JsonValue json, String name, boolean defaultValue) {
        return json.has(name) ? json.getBoolean(name) : defaultValue;
    }

}
