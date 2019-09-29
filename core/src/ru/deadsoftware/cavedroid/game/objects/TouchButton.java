package ru.deadsoftware.cavedroid.game.objects;

import com.badlogic.gdx.math.Rectangle;

public class TouchButton {

    private Rectangle rect;
    private int code;
    private boolean mouse;

    public TouchButton(Rectangle rect, int code, boolean mouse) {
        this.rect = rect;
        this.code = code;
        this.mouse = mouse;
    }

    public Rectangle getRect() {
        return rect;
    }

    public int getCode() {
        return code;
    }

    public boolean isMouse() {
        return mouse;
    }

}
