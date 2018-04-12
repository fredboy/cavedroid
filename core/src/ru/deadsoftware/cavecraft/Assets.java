package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {

    public static final int BLOCK_TEXTURES = 3;

    public static BitmapFont minecraftFont;

    public static Texture charTexture;
    public static Sprite[][] playerSkin = new Sprite[2][4];

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
        minecraftFont = new BitmapFont(Gdx.files.internal("font.fnt"), true);
        charTexture = new Texture(Gdx.files.internal("mobs/char.png"));
        //LOOK TO LEFT
        //head
        playerSkin[0][0] = new Sprite(new TextureRegion(charTexture, 0,0,12,12));
        playerSkin[0][0].flip(false,true);
        //body
        playerSkin[0][1] = new Sprite(new TextureRegion(charTexture, 0,13,12,12));
        playerSkin[0][1].flip(false,true);
        //hand
        playerSkin[0][2] = new Sprite(new TextureRegion(charTexture, 25,5,20,20));
        playerSkin[0][2].flip(false,true);
        //leg
        playerSkin[0][3] = new Sprite(new TextureRegion(charTexture, 25,27,20,20));
        playerSkin[0][3].flip(false,true);
        //LOOK TO RIGHT
        //head
        playerSkin[1][0] = new Sprite(new TextureRegion(charTexture, 13,0,12,12));
        playerSkin[1][0].flip(false,true);
        //body
        playerSkin[1][1] = new Sprite(new TextureRegion(charTexture, 13,13,12,12));
        playerSkin[1][1].flip(false,true);
        //hand
        playerSkin[1][2] = new Sprite(new TextureRegion(charTexture, 37,5,20,20));
        playerSkin[1][2].flip(false,true);
        //leg
        playerSkin[1][3] = new Sprite(new TextureRegion(charTexture, 37,27,20,20));
        playerSkin[1][3].flip(false,true);


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
