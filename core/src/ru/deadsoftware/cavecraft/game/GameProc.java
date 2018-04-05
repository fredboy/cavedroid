package ru.deadsoftware.cavecraft.game;

import com.badlogic.gdx.utils.TimeUtils;

public class GameProc {

    public static double RUN_TIME = 0;

    public GameWorld world;
    public GameRenderer renderer;

    public int cursorX, cursorY;

    public boolean isTouchDown = false;
    public int touchDownX, touchDownY;
    public long touchDownTime;

    public GameProc() {
        world = new GameWorld(512,16);
        renderer = new GameRenderer(this);
    }

    public void update(float delta) {
        RUN_TIME += delta;
        if (isTouchDown && TimeUtils.timeSinceMillis(touchDownTime) > 500) {
            world.placeToBackground(cursorX,cursorY,1);
            isTouchDown = false;
        }
    }

}
