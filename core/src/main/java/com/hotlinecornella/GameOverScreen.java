package com.hotlinecornella;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen extends ScreenAdapter {
    private final SpriteBatch batch;
    private final BitmapFont font;
    private boolean gameWon;

    public GameOverScreen(boolean gameWon) {
        this.gameWon = gameWon;

        batch = new SpriteBatch();
        font = new BitmapFont();

        if (!gameWon) {
            System.out.println("Game Over! You lost!");
        } else {
            System.out.println("Game Won!");
        }
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        if (gameWon) {
            font.draw(batch, "Game Won! Congratulations!", Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        } else {
            font.draw(batch, "Game Over! You lost!", Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        }
        batch.end();
    }
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
