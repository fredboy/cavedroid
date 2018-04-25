package ru.deadsoftware.cavecraft.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ru.deadsoftware.cavecraft.CaveGame;

public class Assets {

    public static final int BLOCK_TEXTURES = 47;

    private static GlyphLayout layout;

    public static BitmapFont minecraftFont;

    public static Sprite gameLogo;

    public static Texture menuButtonTexture;
    public static TextureRegion[] menuButton = new TextureRegion[3];

    public static Texture playerTexture;
    public static Sprite[][] playerSprite = new Sprite[2][4];

    public static Texture pigTexture;
    public static Sprite[][] pigSprite = new Sprite[2][2];

    public static Sprite shade;

    public static Texture terrain;
    public static TextureRegion[] blockTextures = new TextureRegion[BLOCK_TEXTURES];

    public static Texture gui;
    public static TextureRegion invBar;
    public static TextureRegion invBarCur;
    public static TextureRegion guiCur;

    public static Texture creativeTexture;
    public static TextureRegion creativeInv;
    public static TextureRegion creativeScroll;

    public static Texture touchGui;
    public static TextureRegion[] touchArrows = new TextureRegion[4];
    public static TextureRegion touchLMB, touchRMB;
    public static TextureRegion touchToggleMode;
    public static TextureRegion touchSpace;

    private static void loadPlayer() {
        playerTexture = new Texture(Gdx.files.internal("mobs/char.png"));
        //LOOK TO LEFT
        //head
        playerSprite[0][0] = new Sprite(new TextureRegion(playerTexture, 0,0,12,12));
        playerSprite[0][0].flip(false,true);
        //body
        playerSprite[0][1] = new Sprite(new TextureRegion(playerTexture, 0,13,12,12));
        playerSprite[0][1].flip(false,true);
        //hand
        playerSprite[0][2] = new Sprite(new TextureRegion(playerTexture, 25,5,20,20));
        playerSprite[0][2].flip(false,true);
        //leg
        playerSprite[0][3] = new Sprite(new TextureRegion(playerTexture, 25,27,20,20));
        playerSprite[0][3].flip(false,true);
        //LOOK TO RIGHT
        //head
        playerSprite[1][0] = new Sprite(new TextureRegion(playerTexture, 13,0,12,12));
        playerSprite[1][0].flip(false,true);
        //body
        playerSprite[1][1] = new Sprite(new TextureRegion(playerTexture, 13,13,12,12));
        playerSprite[1][1].flip(false,true);
        //hand
        playerSprite[1][2] = new Sprite(new TextureRegion(playerTexture, 37,5,20,20));
        playerSprite[1][2].flip(false,true);
        //leg
        playerSprite[1][3] = new Sprite(new TextureRegion(playerTexture, 37,27,20,20));
        playerSprite[1][3].flip(false,true);
    }

    private static void loadPig() {
        pigTexture = new Texture(Gdx.files.internal("mobs/pig.png"));
        pigSprite[0][0] = new Sprite(new TextureRegion(pigTexture, 0, 0, 25, 12));
        pigSprite[0][0].flip(false,true);
        pigSprite[1][0] = new Sprite(new TextureRegion(pigTexture, 0, 12, 25, 12));
        pigSprite[1][0].flip(false,true);
        pigSprite[0][1] = new Sprite(new TextureRegion(pigTexture, 4, 26, 12, 12));
        pigSprite[0][1].flip(false,true);
        pigSprite[1][1] = new Sprite(new TextureRegion(pigTexture, 16, 26, 12, 12));
        pigSprite[1][1].flip(false,true);
    }

    public static void load() {
        minecraftFont = new BitmapFont(Gdx.files.internal("font.fnt"), true);
        minecraftFont.getData().setScale(.375f);

        layout = new GlyphLayout();

        gameLogo = new Sprite(new Texture(Gdx.files.internal("gamelogo.png")));
        gameLogo.flip(false, true);

        menuButtonTexture = new Texture(Gdx.files.internal("buttons.png"));
        for (int i=0; i<3; i++) {
            menuButton[i] = new TextureRegion(menuButtonTexture, 0, 20*i, 200, 20);
            menuButton[i].flip(false, true);
        }

        loadPlayer();
        loadPig();

        shade = new Sprite(new Texture(Gdx.files.internal("shade.png")));

        gui = new Texture(Gdx.files.internal("gui.png"));
        guiCur = new TextureRegion(gui,0,0,16,16);
        invBar = new TextureRegion(gui,0,16,182,22);
        invBarCur = new TextureRegion(gui,0,38,24,24);

        creativeTexture = new Texture(Gdx.files.internal("allitems.png"));
        creativeInv = new TextureRegion(creativeTexture, 0, 0, 176, 136);
        creativeInv.flip(false,true);
        creativeScroll = new TextureRegion(creativeTexture, 3, 137, 12, 15);
        creativeScroll.flip(false, true);

        if (CaveGame.TOUCH) {
            touchGui = new Texture(Gdx.files.internal("touch_gui.png"));
            for (int i = 0; i < 4; i++) {
                touchArrows[i] = new TextureRegion(touchGui, i * 26, 0, 26, 26);
                touchArrows[i].flip(false, true);
            }
            touchLMB = new TextureRegion(touchGui, 0, 26, 26, 26);
            touchLMB.flip(false, true);
            touchRMB = new TextureRegion(touchGui, 52, 26, 26, 26);
            touchRMB.flip(false, true);
            touchToggleMode = new TextureRegion(touchGui, 26, 26, 26, 26);
            touchToggleMode.flip(false, true);
            touchSpace = new TextureRegion(touchGui, 0, 52, 104, 26);
            touchSpace.flip(false, true);
        }

        terrain = new Texture(Gdx.files.internal("terrain.png"));
        for (int i=0; i<BLOCK_TEXTURES; i++) {
            blockTextures[i] = new TextureRegion(terrain,
                    (i%16)*16, (i/16)*16, 16,16);
            blockTextures[i].flip(false,true);
        }
    }

    public static int getStringWidth(String s){
        layout.setText(minecraftFont,s);
        return (int)layout.width;
    }

    public static int getStringHeight(String s){
        layout.setText(minecraftFont,s);
        return (int)layout.height;
    }

}
