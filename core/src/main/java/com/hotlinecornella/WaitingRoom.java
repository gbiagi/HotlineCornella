package com.hotlinecornella;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class WaitingRoom extends ScreenAdapter {
    private final SpriteBatch batch;
    private final Texture landingScreen;

    public WaitingRoom() {
        batch = new SpriteBatch();
        landingScreen = new Texture(Gdx.files.internal("images/landing_screen.png"));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(landingScreen, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        landingScreen.dispose();
    }
}
