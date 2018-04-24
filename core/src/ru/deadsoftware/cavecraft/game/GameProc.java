package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavecraft.*;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.game.objects.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class GameProc implements Serializable{

    public static double RUN_TIME = 0;

    public Player player;

    public ArrayList<Mob> mobs;

    public transient GameWorld world;
    public transient GameRenderer renderer;
    public transient GamePhysics physics;

    public int cursorX, cursorY;
    public int invSlot;
    public int ctrlMode;

    public boolean isTouchDown, isKeyDown;
    public int touchDownX, touchDownY, keyDownCode;
    public int touchDownButton;
    public long touchDownTime;

    public GameProc() {
        world = new GameWorld();
        world.generate(1024,256);
        player = new Player(world.getSpawnPoint());
        mobs = new ArrayList<Mob>();
        physics = new GamePhysics(this);
        if (!CaveGame.TOUCH) ctrlMode = 1;
        if (CaveGame.TOUCH) {
            renderer = new GameRenderer(this,320,
                    320*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        } else {
            renderer = new GameRenderer(this,480,
                    480*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        }
        GameSaver.save(this);
    }

    public void resetRenderer() {
        if (CaveGame.TOUCH) {
            renderer = new GameRenderer(this,320,
                    320*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        } else {
            renderer = new GameRenderer(this,480,
                    480*((float)GameScreen.getHeight()/GameScreen.getWidth()));
        }
    }

    private boolean isAutoselectable(int x, int y) {
        return (world.getForeMap(x,y)>0 &&
                Items.BLOCKS.getValueAt(world.getForeMap(x,y)).collision);
    }

    private void moveCursor() {
        if (ctrlMode == 0 && CaveGame.TOUCH) {
            cursorX = (int) (player.position.x + player.texWidth / 2) / 16;
            if (player.dir == 0) cursorX--;
                else cursorX++;
            cursorY = (int) (player.position.y + player.texWidth) / 16;
            if (!isAutoselectable(cursorX, cursorY)) {
                cursorY++;
            }
            if (!isAutoselectable(cursorX, cursorY)) {
                cursorY++;
            }
            if (!isAutoselectable(cursorX, cursorY)) {
                if (player.dir == 0) cursorX++;
                else cursorX--;
            }
        } else if (!CaveGame.TOUCH){
            cursorX = (int)(Gdx.input.getX()*
                    (renderer.camera.viewportWidth/GameScreen.getWidth())+renderer.camera.position.x)/16;
            cursorY = (int)(Gdx.input.getY()*
                    (renderer.camera.viewportHeight/GameScreen.getHeight())+renderer.camera.position.y)/16;
            if ((Gdx.input.getX()*
                    (renderer.camera.viewportWidth/GameScreen.getWidth())+renderer.camera.position.x)<0)
                cursorX--;
        }
    }

    private void checkCursorBounds() {
        if (cursorY < 0) cursorY = 0;
        if (cursorY >= world.getHeight()) cursorY = world.getHeight()-1;
        if (ctrlMode==1) {
            if (cursorX*16+8<player.position.x+player.texWidth/2)
                player.dir=0;
            if (cursorX*16+8>player.position.x+player.texWidth/2)
                player.dir=1;
        }
    }

    public void update(float delta) {
        RUN_TIME += delta;

        physics.update(delta);
        moveCursor();
        checkCursorBounds();

        if (isTouchDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) {
            if (touchDownButton== Input.Buttons.RIGHT) {
                world.placeToBackground(cursorX, cursorY,
                        player.inventory[invSlot]);
            } else if (touchDownButton==Input.Buttons.LEFT &&
                    touchDownY< Assets.invBar.getRegionHeight() &&
                    touchDownX>renderer.camera.viewportWidth/2-Assets.invBar.getRegionWidth()/2 &&
                    touchDownX<renderer.camera.viewportWidth/2+Assets.invBar.getRegionWidth()/2) {
                CaveGame.STATE = AppState.GAME_CREATIVE_INV;
            }
            isTouchDown = false;
        }
    }

}
