package ru.deadsoftware.cavecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {

    public static Texture charTexture;
    public static Sprite[] playerSprite = new Sprite[2];

    public static void load() {
        charTexture = new Texture(Gdx.files.internal("char.png"));
        playerSprite[0] = new Sprite(new TextureRegion(charTexture, 0,0,8,30));
        playerSprite[0].flip(false,true);
        playerSprite[1] = new Sprite(new TextureRegion(charTexture, 8,0,8,30));
        playerSprite[1].flip(false,true);
    }

}
