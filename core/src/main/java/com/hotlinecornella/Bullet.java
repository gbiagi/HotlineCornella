package com.hotlinecornella;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {
    private final Texture texture;
    private float x, y;
    private final float speed;
    private final Direction direction;
    private final Rectangle bounds;

    public Bullet(Texture texture, float x, float y, float speed, Direction direction) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
        this.bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public void update(float deltaTime) {
        switch (direction) {
            case UP:
                y += speed * deltaTime;
                break;
            case DOWN:
                y -= speed * deltaTime;
                break;
            case LEFT:
                x -= speed * deltaTime;
                break;
            case RIGHT:
                x += speed * deltaTime;
                break;
        }
        bounds.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }

    public Rectangle getBounds() {
        return bounds;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
}
