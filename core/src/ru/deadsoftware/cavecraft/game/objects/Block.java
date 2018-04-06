package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Block {

    private Rectangle rect;
    private TextureRegion texture;

    public Block(int x, int y, int w, int h, TextureRegion texture) {
        rect = new Rectangle(x,y,w,h);
        this.texture = texture;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public Rectangle getRect() {
        return rect;
    }

    public int getX() {
        return (int)rect.x;
    }

    public int getY() {
        return (int)rect.y;
    }

    public int getWidth() {
        return (int)rect.width;
    }

    public int getHeight() {
        return (int)rect.height;
    }
}
