package com.hotlinecornella;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class WaitingRoom extends ScreenAdapter {
    private final SpriteBatch batch;
    private final BitmapFont font;
    private boolean gameReady = false;

    public WaitingRoom(Main game) {
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        font.draw(batch, "Waiting for players...", Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        batch.end();
    }



    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    public boolean isGameReady() {
        return gameReady;
    }

    public void setGameReady(boolean gameReady) {
        this.gameReady = gameReady;
    }
}
