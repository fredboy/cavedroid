package ru.deadsoftware.cavedroid.menu.objects;

import com.badlogic.gdx.math.Rectangle;

public class Button {

    private final Rectangle rect;
    private final String label;
    private int type;

    public Button(String label, float x, float y, float width, float height, int type) {
        this.label = label;
        rect = new Rectangle(x, y, width, height);
        this.type = type;
    }

    public Button(String label, float x, float y, float width, float height) {
        this(label, x, y, width, height, 1);
    }

    public Button(String label, float x, float y, int type) {
        this(label, x, y, 200, 20, type);
    }

    public Button(String label, float x, float y) {
        this(label, x, y, 200, 20, 1);
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

}
