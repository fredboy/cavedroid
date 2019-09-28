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

import java.util.HashMap;

public class Assets {

    public static JsonReader jsonReader = new JsonReader();
    public static Sprite[][] playerSprite = new Sprite[2][4];
    public static HashMap<String, TextureRegion> textureRegions = new HashMap<>();
    public static ArrayMap<String, Rectangle> guiMap = new ArrayMap<>();
    public static Sprite[][] pigSprite = new Sprite[2][2];
    static BitmapFont minecraftFont;
    private static GlyphLayout glyphLayout = new GlyphLayout();

    private static TextureRegion flippedRegion(Texture texture, int x, int y, int width, int height) {
        return new TextureRegion(texture, x, y + height, width, -height);
    }

    private static void loadPlayer() {
        Texture plTex = new Texture(Gdx.files.internal("mobs/char.png"));
        //LOOK TO LEFT
        //head
        playerSprite[0][0] = new Sprite(new TextureRegion(plTex, 0, 0, 12, 12));
        playerSprite[0][0].flip(false, true);
        //body
        playerSprite[0][1] = new Sprite(new TextureRegion(plTex, 0, 13, 12, 12));
        playerSprite[0][1].flip(false, true);
        //hand
        playerSprite[0][2] = new Sprite(new TextureRegion(plTex, 25, 5, 20, 20));
        playerSprite[0][2].flip(false, true);
        //leg
        playerSprite[0][3] = new Sprite(new TextureRegion(plTex, 25, 27, 20, 20));
        playerSprite[0][3].flip(false, true);
        //LOOK TO RIGHT
        //head
        playerSprite[1][0] = new Sprite(new TextureRegion(plTex, 13, 0, 12, 12));
        playerSprite[1][0].flip(false, true);
        //body
        playerSprite[1][1] = new Sprite(new TextureRegion(plTex, 13, 13, 12, 12));
        playerSprite[1][1].flip(false, true);
        //hand
        playerSprite[1][2] = new Sprite(new TextureRegion(plTex, 37, 5, 20, 20));
        playerSprite[1][2].flip(false, true);
        //leg
        playerSprite[1][3] = new Sprite(new TextureRegion(plTex, 37, 27, 20, 20));
        playerSprite[1][3].flip(false, true);
    }

    private static void loadPig() {
        Texture pigTex = new Texture(Gdx.files.internal("mobs/pig.png"));
        pigSprite[0][0] = new Sprite(new TextureRegion(pigTex, 0, 0, 25, 12));
        pigSprite[0][0].flip(false, true);
        pigSprite[1][0] = new Sprite(new TextureRegion(pigTex, 0, 12, 25, 12));
        pigSprite[1][0].flip(false, true);
        pigSprite[0][1] = new Sprite(new TextureRegion(pigTex, 4, 26, 12, 12));
        pigSprite[0][1].flip(false, true);
        pigSprite[1][1] = new Sprite(new TextureRegion(pigTex, 16, 26, 12, 12));
        pigSprite[1][1].flip(false, true);
    }

    /**
     * Loads texture names and sizes from <b>json/texture_regions.json</b>, cuts them to TextureRegions
     * and puts to {@link #textureRegions} HashMap
     */
    private static void loadJSON() {
        JsonValue json = Assets.jsonReader.parse(Gdx.files.internal("json/texture_regions.json"));
        for (JsonValue file = json.child(); file != null; file = file.next()) {
            Texture texture = new Texture(Gdx.files.internal(file.name() + ".png"));
            for (JsonValue key = file.child(); key != null; key = key.next()) {
                int x = key.has("x") ? key.getInt("x") : 0;
                int y = key.has("y") ? key.getInt("y") : 0;
                int w = key.has("w") ? key.getInt("w") : texture.getWidth();
                int h = key.has("h") ? key.getInt("h") : texture.getHeight();
                textureRegions.put(key.name(), flippedRegion(texture, x, y, w, h));
            }
        }
    }

    public static void load() {
        minecraftFont = new BitmapFont(Gdx.files.internal("font.fnt"), true);
        minecraftFont.getData().setScale(.375f);
        loadPlayer();
        loadPig();
        loadJSON();
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

}
