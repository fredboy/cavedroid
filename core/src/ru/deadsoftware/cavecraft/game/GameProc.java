package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavecraft.Assets;
import ru.deadsoftware.cavecraft.CaveGame;
import ru.deadsoftware.cavecraft.GameState;
import ru.deadsoftware.cavecraft.Items;
import ru.deadsoftware.cavecraft.game.mobs.Human;
import ru.deadsoftware.cavecraft.game.mobs.Mob;
import ru.deadsoftware.cavecraft.game.objects.Player;

public class GameProc {

    public static double RUN_TIME = 0;

    public Player player;

    public Array<Mob> mobs;

    public GameWorld world;
    public GameRenderer renderer;
    public GamePhysics physics;

    public int cursorX, cursorY;
    public int invSlot;
    public int ctrlMode;

    public boolean isTouchDown = false;
    public int touchDownX, touchDownY;
    public int touchDownButton;
    public long touchDownTime;

    public GameProc() {
        world = new GameWorld(1024,256);
        renderer = new GameRenderer(this);
        physics = new GamePhysics(this);
        player = new Player(world.getSpawnPoint());
        mobs = new Array<Mob>();

    }

    public void resetRenderer() {
        renderer = new GameRenderer(this);
    }

    private boolean isAutoselectable(int x, int y) {
        return (world.getForeMap(x,y)>0 &&
                Items.BLOCKS.getValueAt(world.getForeMap(x,y)).collision);
    }

    private void moveCursor() {
        if (player.canJump) {
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
        } else {
            cursorX = (int) (player.position.x + player.texWidth / 2) / 16;
            cursorY = (int) (player.position.y + player.height+8)/16;
        }
    }

    private void checkCursorBounds() {
        if (cursorX < 0) cursorX = 0;
        if (cursorX >= world.getWidth()) cursorX = world.getWidth()-1;
        if (cursorY < 0) cursorY = 0;
        if (cursorY >= world.getHeight()) cursorY = world.getHeight()-1;
    }

    public void update(float delta) {
        RUN_TIME += delta;

        physics.update(delta);
        if (ctrlMode==0) moveCursor();
        checkCursorBounds();

        if (isTouchDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) {
            if (touchDownButton== Input.Buttons.RIGHT) {
                world.placeToBackground(cursorX, cursorY,
                        player.inventory[invSlot]);
            } else if (touchDownButton==Input.Buttons.LEFT &&
                    touchDownY< Assets.invBar.getRegionHeight() &&
                    touchDownX>renderer.camera.viewportWidth/2-Assets.invBar.getRegionWidth()/2 &&
                    touchDownX<renderer.camera.viewportWidth/2+Assets.invBar.getRegionWidth()/2) {
                CaveGame.STATE = GameState.GAME_CREATIVE_INV;
            }
            isTouchDown = false;
        }
    }

}
