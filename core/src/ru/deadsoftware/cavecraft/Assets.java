package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {

    public static final int BLOCK_TEXTURES = 3;

    public static Texture charTexture;
    public static TextureRegion[] playerSkin = new TextureRegion[2];

    public static Sprite shade;

    public static Texture terrain;
    public static TextureRegion[] blockTextures = new TextureRegion[BLOCK_TEXTURES];

    public static Texture gui;
    public static TextureRegion invBar;
    public static TextureRegion invCur;

    public static Texture touchGui;
    public static TextureRegion[] touchArrows = new TextureRegion[4];
    public static TextureRegion touchLMB, touchRMB;
    public static TextureRegion touchToggleMode;
    public static TextureRegion touchSpace;

    public static void load() {
        charTexture = new Texture(Gdx.files.internal("char.png"));
        playerSkin[0] = new TextureRegion(charTexture, 0,0,8,30);
        playerSkin[0].flip(false,true);
        playerSkin[1] = new TextureRegion(charTexture, 8,0,8,30);
        playerSkin[1].flip(false,true);

        shade = new Sprite(new Texture(Gdx.files.internal("shade.png")));

        gui = new Texture(Gdx.files.internal("gui.png"));
        invBar = new TextureRegion(gui,0,0,182,22);
        invCur = new TextureRegion(gui,0,22,24,24);

        touchGui = new Texture(Gdx.files.internal("touch_gui.png"));
        for (int i=0; i<4; i++) {
            touchArrows[i] = new TextureRegion(touchGui, i*26, 0, 26,26);
            touchArrows[i].flip(false, true);
        }
        touchLMB = new TextureRegion(touchGui, 0, 26, 26,26);
        touchLMB.flip(false, true);
        touchRMB = new TextureRegion(touchGui, 52, 26, 26,26);
        touchRMB.flip(false, true);
        touchToggleMode = new TextureRegion(touchGui, 26, 26, 26, 26);
        touchToggleMode.flip(false, true);
        touchSpace = new TextureRegion(touchGui, 0, 52, 104, 26);
        touchSpace.flip(false, true);

        terrain = new Texture(Gdx.files.internal("terrain.png"));
        for (int i=0; i<BLOCK_TEXTURES; i++) {
            blockTextures[i] = new TextureRegion(terrain,
                    (i%16)*16, (i/16)*16, 16,16);
            blockTextures[i].flip(false,true);
        }
    }

}
