package ru.deadsoftware.cavecraft.game.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Block {

    private int x,y,w,h;
    private int texture;

    public boolean collision, background, transparent;

    public Block(int texture) {
        this(0,0,16,16,texture, true, false, false);
    }

    public Block(int texture, boolean collision, boolean background, boolean transparent) {
        this(0,0,16,16,texture, collision, background, transparent);
    }

    public Block(int x, int y, int w, int h, int texture, boolean collision, boolean background, boolean transparent) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.texture = texture;
        this.collision = collision;
        this.background = background;
        this.transparent = transparent;
    }

    public int getTexture() {
        return texture;
    }

    public Rectangle getRect(int x, int y) {
        x*=16;
        y*=16;
        return new Rectangle(x+this.x, y+this.y, w, h);
    }

    public boolean toJump() {
        return (y<8 && collision);
    }

}
