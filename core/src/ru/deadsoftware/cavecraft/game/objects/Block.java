package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Block {

    private int x,y,w,h;
    private TextureRegion texture;

    public boolean collision, foreground;

    public Block(int x, int y, int w, int h, TextureRegion texture) {
        this(x,y,w,h,texture, true, false);
    }

    public Block(int x, int y, int w, int h, TextureRegion texture, boolean collision, boolean foreground) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.texture = texture;
        this.collision = collision;
        this.foreground = foreground;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public Rectangle getRect(int x, int y) {
        x*=16;
        y*=16;
        return new Rectangle(x+this.x, y+this.y, w, h);
    }

}
