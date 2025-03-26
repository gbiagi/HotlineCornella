package com.hotlinecornella;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameOverScreen extends ScreenAdapter {
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final boolean gameWon;
    private final Stage stage;
    private final Main game;

    public GameOverScreen(boolean gameWon) {
        this.gameWon = gameWon;
        this.game = (Main) Gdx.app.getApplicationListener();

        batch = new SpriteBatch();
        font = new BitmapFont();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        createUI();

        if (!gameWon) {
            System.out.println("Game Over! You lost!");
        } else {
            System.out.println("Game Won!");
        }
    }

    private void createUI() {
        // Create a simple skin for the button
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        if (!Gdx.files.internal("uiskin.json").exists()) {
            // Fallback if skin file doesn't exist
            skin = new Skin();
            TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
            textButtonStyle.font = font;
            skin.add("default", textButtonStyle);
        }

        // Create restart button
        TextButton restartButton = new TextButton("Restart Game", skin);
        restartButton.setSize(200, 60);
        restartButton.setPosition(
            (Gdx.graphics.getWidth() - restartButton.getWidth()) / 2,
            ((float) Gdx.graphics.getHeight() / 2) - 100
        );

        // Add button listener
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Restart the game - go back to waiting room
                game.setScreen(new WaitingRoom(game));
                WsClient.connectInstance();
            }
        });

        // Add button to the stage
        stage.addActor(restartButton);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        batch.begin();
        if (gameWon) {
            font.draw(batch, "Game Won! Congratulations!", Gdx.graphics.getWidth() / 2.5f, Gdx.graphics.getHeight() / 2f);
        } else {
            font.draw(batch, "Game Over! You lost!", Gdx.graphics.getWidth() / 2.5f, Gdx.graphics.getHeight() / 2f);
        }
        batch.end();

        // Render the stage with the button
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        stage.dispose();
    }
}
