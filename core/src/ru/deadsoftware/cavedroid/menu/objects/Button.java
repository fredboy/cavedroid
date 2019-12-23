package ru.deadsoftware.cavedroid.menu.objects;

import com.badlogic.gdx.math.Rectangle;

public class Button {

    public static final int WIDTH = 200;
    public static final int HEIGHT = 20;

    public static final int
            DISABLED = 0,
            NORMAL = 1,
            SELECTED = 2;
    private final Rectangle rect;
    private final String label;
    private ButtonEventListener listener;
    private int type;

    /**
     * @param label Label to be shown on button
     * @param type  Type of button where 0 - disabled,  1 - normal, 2 - selected.
     *              You should use these constants
     *              {@link #DISABLED} {@link #NORMAL} {@link #SELECTED}
     */
    public Button(String label, int x, int y, int type, ButtonEventListener listener) {
        this.label = label;
        rect = new Rectangle(x, y, WIDTH, HEIGHT);
        this.type = type;
        this.listener = listener;
    }

    public Rectangle getRect() {
        return rect;
    }

    public String getLabel() {
        return label;
    }

    public float getX() {
        return rect.x;
    }

    public float getY() {
        return rect.y;
    }

    public float getWidth() {
        return rect.width;
    }

    public float getHeight() {
        return rect.height;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void draw(ButtonRenderer drawer) {
        drawer.draw(this);
    }

    public void clicked() {
        listener.buttonClicked();
    }

}
