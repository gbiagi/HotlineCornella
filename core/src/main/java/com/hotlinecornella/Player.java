package com.hotlinecornella;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {
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
    private Texture bulletTexture;
    private List<Bullet> bullets;
    private String id;
    private int health;

    public Player(String idleTextureFilePath, String runTextureFilePath, float initialX, float initialY, float scale) {
        try {
            idleTexture = new Texture(Gdx.files.internal(idleTextureFilePath));
            runTexture = new Texture(Gdx.files.internal(runTextureFilePath));
            this.x = initialX;
            this.y = initialY;
            this.scale = scale;
            this.shapeRenderer = new ShapeRenderer();
            this.id = UUID.randomUUID().toString(); // Generate a unique ID for the player

            health = 50;

            // Assuming each frame is 120x44 pixels
            idleAnimation = createAnimation(idleTexture);
            runAnimation = createAnimation(runTexture);

            stateTime = 0f;
            isRunning = false;
            flipX = false;
            bulletTexture = new Texture("images/bullet.png");
            bullets = new ArrayList<>();

        } catch (Exception e) {
            System.out.println("Error loading player textures" + e);
        }
    }

    private Animation<TextureRegion> createAnimation(Texture texture) {
        TextureRegion[][] tmp = TextureRegion.split(texture, 29, 39);
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
        for (Bullet bullet : bullets) {
            bullet.update(deltaTime);
        }
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

            for (Bullet bullet : bullets) {
                bullet.render(batch);
            }

            // Render the player hitbox --------------------------
//            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//            shapeRenderer.setColor(1, 0, 0, 1); // Red color
//            Rectangle bounds = getBounds();
//            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
//            shapeRenderer.end();

        } catch (Exception e) {
            System.out.println("Error rendering player" + e);
        }
    }
    public void renderHealthBar(ShapeRenderer shapeRenderer) {
        // Get the dimensions for positioning
        float healthBarWidth = idleAnimation.getKeyFrame(0).getRegionWidth() * scale;
        float healthBarHeight = 5;

        // Position the health bar above the player
        float healthBarX = x;
        float healthBarY = y + (idleAnimation.getKeyFrame(0).getRegionHeight() * scale) + 5;

        // Calculate the filled portion based on health percentage
        float healthPercentage = health / 50f;
        float fillWidth = healthPercentage * healthBarWidth;

        // Interpolate color from green to red based on health percentage
        float red = 1 - healthPercentage;

        // Begin shape renderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw the background (empty health bar)
        shapeRenderer.setColor(0.5f, 0, 0, 1); // Dark red for background
        shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

        // Draw the filled portion with interpolated color
        shapeRenderer.setColor(red, healthPercentage, 0, 1); // Interpolated color
        shapeRenderer.rect(healthBarX, healthBarY, fillWidth, healthBarHeight);

        shapeRenderer.end();
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
    public void shoot(Direction direction) {
        float bulletX = getX() + (idleAnimation.getKeyFrame(0).getRegionWidth() * scale) / 2 - (float) bulletTexture.getWidth() / 2;
        float bulletY = getY() + (idleAnimation.getKeyFrame(0).getRegionHeight() * scale) / 2 - (float) bulletTexture.getHeight() / 2;
        Bullet bullet = new Bullet(bulletTexture, bulletX, bulletY, 300, direction);
        bullets.add(bullet);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, idleAnimation.getKeyFrame(0).getRegionWidth() * scale, idleAnimation.getKeyFrame(0).getRegionHeight() * scale);
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public List<Bullet> getBullets() {
        return bullets;
    }
    public void setRunning(boolean running) {
        isRunning = running;
    }
    public boolean isRunning() {
        return isRunning;
    }
    public String getId() {
        return id;
    }
    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }
    public void playerHit() {
        if (health > 0) {
            health -= 10;
        }
    }
    public void dispose() {
        idleTexture.dispose();
        runTexture.dispose();
    }

}
