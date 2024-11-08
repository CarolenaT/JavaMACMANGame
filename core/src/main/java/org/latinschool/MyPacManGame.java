package org.latinschool;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MyPacManGame implements ApplicationListener {
    Texture backgroundTexture;
    Texture pacmanTexture;
    Texture ghostTexture;
    Texture powerPillTexture;
    FitViewport viewport;
    Sprite ghostSprite;
    Sprite pacmanSprite;
    Sprite powerPillSprite;
    SpriteBatch spriteBatch;
    Rectangle safeZone;
    Rectangle pacmanZone;
    int score;
    MyPacManGame game;
    String difficulty;


    ShapeRenderer shapeRenderer;
    BitmapFont font;

    private final float PAC_SPEED = 4f;

    private TiledMap tiledMap;
    private TiledMapTileLayer wallLayer;
    Pixmap mapPixmap;
    Color wallColor;

    private float pacmanVelocityX = 0; // Horizontal velocity of Pac-Man
    private float pacmanVelocityY = 0; // Vertical velocity of Pac-Man

    @Override
    public void create() {
        backgroundTexture = new Texture("Maze.png");
        pacmanTexture = new Texture("Pacman.png");
        ghostTexture = new Texture("ghost.png");
        powerPillTexture = new Texture("pellet.png");

        viewport = new FitViewport(10, 10);
        viewport.getCamera().position.set(0, 0, 0);
        viewport.getCamera().update();

        ghostSprite = new Sprite(ghostTexture);
        pacmanSprite = new Sprite(pacmanTexture);
        powerPillSprite = new Sprite(powerPillTexture);

        ghostSprite.setSize(0.5f, 0.5f);
        pacmanSprite.setSize(0.5f, 0.5f);
        powerPillSprite.setSize(0.5f, 0.5f);


        ghostSprite.setPosition(1, 1);
        pacmanSprite.setPosition(9, 9);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        safeZone = new Rectangle(4, 5, 2, 1);
        pacmanZone = new Rectangle();


        tiledMap = new TmxMapLoader().load("clasic.tmx");
        // Get the layer named "walls" from the TiledMap
        wallLayer = (TiledMapTileLayer) tiledMap.getLayers().get("walls");

        mapPixmap = new Pixmap(Gdx.files.internal("meta-tiles.png"));
        wallColor = new Color(0, 0, 0, 1);

        score = 0;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        points();
        draw();
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    public void input() {
        float delta = Gdx.graphics.getDeltaTime();

        float nextX = pacmanSprite.getX();
        float nextY = pacmanSprite.getY();

        pacmanVelocityX = 0; // Reset velocities to avoid continuous movement
        pacmanVelocityY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            nextX += PAC_SPEED * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            nextX -= PAC_SPEED * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            nextY += PAC_SPEED * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            nextY -= PAC_SPEED * delta;
        }

        // Before actually moving, check if the next position collides with a wall
        if (!isCollision(nextX, nextY)) {
            // No collision, so move Pac-Man to the new position
            pacmanSprite.setX(nextX);
            pacmanSprite.setY(nextY);
    }}

    public boolean isCollision(float x, float y) {
        // Convert world coordinates (x, y) to pixel coordinates in the image
        int pixelX = (int) (x); // Adjust scaling if necessary
        int pixelY = (int) (y);

        // Ensure that the coordinates are within bounds of the image
        if (pixelX < 0 || pixelX >= mapPixmap.getWidth() || pixelY < 0 || pixelY >= mapPixmap.getHeight()) {
            return false; // Outside the map, no collision
        }

        // Get the color of the pixel at the (x, y) position
        int pixelColor = mapPixmap.getPixel(pixelX, pixelY);
        Color color = new Color(pixelColor);

        // Check if the pixel color matches the wall color (black)
        return color.equals(wallColor); // true if it's a black pixel (wall)
    }


    public void logic() {
        // Calculate next position based on current position + velocity
        float nextX = pacmanSprite.getX() + pacmanVelocityX;
        float nextY = pacmanSprite.getY() + pacmanVelocityY;

        // Check if the next position collides with a wall
        if (!isCollision(nextX, nextY)) {
            // No collision, so move Pac-Man to the new position
            pacmanSprite.setX(nextX);
            pacmanSprite.setY(nextY);
        } else {
            // Collision detected, adjust Pac-Man's position or stop
            // You can add logic here to adjust Pac-Man's position to stop at the wall,
            // or handle "bouncing" off walls if you prefer.
            // For example, stop movement in the direction of the collision:
            if (pacmanVelocityX != 0) { // Horizontal movement
                pacmanSprite.setX(pacmanSprite.getX()); // Keep Pac-Man in place horizontally
            }
            if (pacmanVelocityY != 0) { // Vertical movement
                pacmanSprite.setY(pacmanSprite.getY()); // Keep Pac-Man in place vertically
            }
        }

        // Additional logic for keeping Pac-Man within bounds of the screen, if necessary
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        pacmanSprite.setX(MathUtils.clamp(pacmanSprite.getX(), 0, worldWidth - pacmanSprite.getWidth()));
        pacmanSprite.setY(MathUtils.clamp(pacmanSprite.getY(), 0, worldHeight - pacmanSprite.getHeight()));
    }
    public void points() {
        if (pacmanSprite.getBoundingRectangle().overlaps(powerPillSprite.getBoundingRectangle())) {

            System.out.println("Pac-Man position: " + pacmanSprite.getX() + ", " + pacmanSprite.getY());
            System.out.println("Power pill position: " + powerPillSprite.getX() + ", " + powerPillSprite.getY());


            if (difficulty.equals("Easy")) {
                score += 1;
                System.out.println("Pac-Man score is: " + score);

                powerPillSprite.setPosition(-10, -10);

            }
        }
    }
    public void draw() {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply viewport transformations
        viewport.apply();

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        //spriteBatch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        ghostSprite.draw(spriteBatch);
        pacmanSprite.draw(spriteBatch);

        for (int i = 0; i <= 10; i++) {
            float xPos = i * 1.0f;
            float yPos = 5.0f;

            powerPillSprite.setPosition(xPos, yPos);
            powerPillSprite.draw(spriteBatch);
        }

        spriteBatch.end();

        /*drawCoordinatePlane();*/
    }

    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
        backgroundTexture.dispose();
        pacmanTexture.dispose();
        ghostTexture.dispose();
        tiledMap.dispose();
        mapPixmap.dispose();
    }
}

    /*public void drawCoordinatePlane() {
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GRAY);

        // Draw grid lines
        for (float y = -viewport.getWorldHeight() / 2; y < viewport.getWorldHeight() / 2; y++) {
            shapeRenderer.line(-viewport.getWorldWidth() / 2, y, viewport.getWorldWidth() / 2, y);
        }
        for (float x = -viewport.getWorldWidth() / 2; x < viewport.getWorldWidth() / 2; x++) {
            shapeRenderer.line(x, -viewport.getWorldHeight() / 2, x, viewport.getWorldHeight() / 2);
        }

        // Draw axes
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(-viewport.getWorldWidth() / 2, 0, viewport.getWorldWidth() / 2, 0);
        shapeRenderer.line(0, -viewport.getWorldHeight() / 2, 0, viewport.getWorldHeight() / 2);

        shapeRenderer.end();

        // Draw axis labels
        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(0.5f);  // Scale down the font to fit better

        // Draw x-axis labels
        for (float x = -viewport.getWorldWidth() / 2 + 1; x < viewport.getWorldWidth() / 2; x++) {
            if (x != 0) {
                font.draw(spriteBatch, String.valueOf((int) x), x, 0.2f);
            }
        }

        // Draw y-axis labels
        for (float y = -viewport.getWorldHeight() / 2 + 1; y < viewport.getWorldHeight() / 2; y++) {
            if (y != 0) {
                font.draw(spriteBatch, String.valueOf((int) y), 0.2f, y);
            }
        }

        spriteBatch.end();
    }*/

