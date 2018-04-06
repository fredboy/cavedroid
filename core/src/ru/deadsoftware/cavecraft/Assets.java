package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {

    public static final int BLOCK_TEXTURES = 3;

    public static Texture charTexture;
    public static Sprite[] playerSprite = new Sprite[2];

    public static Sprite shade;

    public static Texture terrain;
    public static TextureRegion[] blockTextures = new TextureRegion[BLOCK_TEXTURES];

    public static Texture gui;
    public static TextureRegion invBar;
    public static TextureRegion invCur;

    public static void load() {
        charTexture = new Texture(Gdx.files.internal("char.png"));
        playerSprite[0] = new Sprite(new TextureRegion(charTexture, 0,0,8,30));
        playerSprite[0].flip(false,true);
        playerSprite[1] = new Sprite(new TextureRegion(charTexture, 8,0,8,30));
        playerSprite[1].flip(false,true);

        shade = new Sprite(new Texture(Gdx.files.internal("shade.png")));

        gui = new Texture(Gdx.files.internal("gui.png"));
        invBar = new TextureRegion(gui,0,0,182,22);
        invCur = new TextureRegion(gui,0,22,24,24);

        terrain = new Texture(Gdx.files.internal("terrain.png"));
        for (int i=0; i<BLOCK_TEXTURES; i++) {
            blockTextures[i] = new TextureRegion(terrain,
                    (i%16)*16, (i/16)*16, 16,16);
            blockTextures[i].flip(false,true);
        }
    }

}
