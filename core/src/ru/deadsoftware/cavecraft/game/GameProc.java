package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.utils.TimeUtils;
import ru.deadsoftware.cavecraft.game.objects.Player;

public class GameProc {

    public static double RUN_TIME = 0;

    public Player player;

    public GameWorld world;
    public GameRenderer renderer;
    public GamePhysics physics;

    public int cursorX, cursorY;
    public int invSlot;
    public int ctrlMode;

    public boolean isTouchDown = false;
    public int touchDownX, touchDownY;
    public long touchDownTime;

    public GameProc() {
        world = new GameWorld(512,256);
        renderer = new GameRenderer(this);
        physics = new GamePhysics(this);
        player = new Player();
    }

    public void resetRenderer() {
        renderer = new GameRenderer(this);
    }

    public void update(float delta) {
        RUN_TIME += delta;

        physics.update(delta);

        if (isTouchDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) {
            world.placeToBackground(cursorX,cursorY,
                    player.inventory[invSlot]);
            isTouchDown = false;
        }
    }

}
