package com.hotlinecornella;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Logger;

public class Player {
    private static final Logger logger = new Logger(Player.class.getName(), Logger.DEBUG);
    private Texture idleTexture;
    private Texture runTexture;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private float stateTime;
    private float x, y;
    private boolean isRunning;
    private boolean flipX;
    private float scale;
    private ShapeRenderer shapeRenderer;

    public Player(String idleTextureFilePath, String runTextureFilePath, float initialX, float initialY, float scale) {
        try {
            idleTexture = new Texture(Gdx.files.internal(idleTextureFilePath));
            runTexture = new Texture(Gdx.files.internal(runTextureFilePath));
            this.x = initialX;
            this.y = initialY;
            this.scale = scale;
            this.shapeRenderer = new ShapeRenderer();

            // Assuming each frame is 120x44 pixels
            idleAnimation = createAnimation(idleTexture);
            runAnimation = createAnimation(runTexture);

            stateTime = 0f;
            isRunning = false;
            flipX = false;
        } catch (Exception e) {
            logger.error("Error loading player textures", e);
        }
    }

    private Animation<TextureRegion> createAnimation(Texture texture) {
        TextureRegion[][] tmp = TextureRegion.split(texture, 120, 44);
        TextureRegion[] frames = new TextureRegion[tmp.length * tmp[0].length];
        int index = 0;
        for (TextureRegion[] textureRegions : tmp) {
            for (TextureRegion textureRegion : textureRegions) {
                frames[index++] = textureRegion;
            }
        }
        return new Animation<>((float) 0.1, frames);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    public void render(SpriteBatch batch) {
        try {
            TextureRegion currentFrame = isRunning ? runAnimation.getKeyFrame(stateTime, true) : idleAnimation.getKeyFrame(stateTime, true);
            if (flipX && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            } else if (!flipX && currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
            batch.draw(currentFrame, x, y, currentFrame.getRegionWidth() * scale, currentFrame.getRegionHeight() * scale);

            // Render the bounds
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 0, 0, 1); // Red color
            Rectangle bounds = getBounds();
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            shapeRenderer.end();
        } catch (Exception e) {
            logger.error("Error rendering player", e);
        }
    }

    public void move(float deltaX, float deltaY) {
        x += deltaX;
        y += deltaY;
        isRunning = deltaX != 0 || deltaY != 0;
        if (deltaX < 0) {
            flipX = true;
        } else if (deltaX > 0) {
            flipX = false;
        }
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void dispose() {
        idleTexture.dispose();
        runTexture.dispose();
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, idleAnimation.getKeyFrame(0).getRegionWidth() * scale, idleAnimation.getKeyFrame(0).getRegionHeight() * scale);
    }
}
