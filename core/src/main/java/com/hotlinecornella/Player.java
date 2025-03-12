package com.hotlinecornella;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private Texture texture;
    private Vector2 position;
    private float speed;
    private Rectangle bounds;

    public Player(String textureFile, float x, float y, float speed) {
        this.texture = new Texture(Gdx.files.internal(textureFile));
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public void update(float deltaTime) {
        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) moveY += speed * deltaTime;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) moveY -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) moveX -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) moveX += speed * deltaTime;

        position.add(moveX, moveY);
        bounds.setPosition(position.x, position.y);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }
}
