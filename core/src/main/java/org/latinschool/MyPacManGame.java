package org.latinschool;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import java.util.ArrayList;


public class MyPacManGame implements ApplicationListener {
    Sound deathSound;
    Sound eatingSound;
    Music music;
    Texture backgroundTexture;
    Texture pacmanTexture;
    Texture ghostTexture;
    Texture left;
    Texture up;
    Texture down;
    FitViewport viewport;
    ArrayList<Ghost> ghosts;
    Sprite pacmanSprite;
    SpriteBatch spriteBatch;
    Rectangle pacmanZone;
    String difficulty;
    int score;
    int newScore;
    int numGhosts;
    int hearts;

    ShapeRenderer shapeRenderer;
    BitmapFont font;

    private TiledMap tiledMap;
    private TiledMapTileLayer wallLayer;
    private TiledMapTileLayer pelletLayer;// Track pellet timers

    float pauseTime = 0.06f; //
    float pauseTimer = 0f;
    public float ghostPauseTime;
    public float ghostPauseTimer = 0f;
    float messageTimer = 0f;
    String currentMessage = "";

    Pixmap mapPixmap;
    Color wallColor;

    private OrthogonalTiledMapRenderer tiledMapRenderer;

    @Override
    public void create() {
        deathSound = Gdx.audio.newSound(Gdx.files.internal("pacman_death.wav"));
        eatingSound = Gdx.audio.newSound(Gdx.files.internal("pacman_chomp.wav"));
        music = Gdx.audio.newMusic(Gdx.files.internal("playingMusic.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();

        backgroundTexture = new Texture("Maze.png");
        pacmanTexture = new Texture("PacmanSmall.png");
        up = new Texture("PacmanUp.png");
        down = new Texture("PacmanDown.png");
        left = new Texture("PacmanLeft.png");
        ghostTexture = new Texture("ghost.png");

        viewport = new FitViewport(224, 288);
        viewport.getCamera().position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        viewport.getCamera().update();

        pacmanSprite = new Sprite(pacmanTexture);

        pacmanSprite.setSize(8, 8);

        pacmanSprite.setPosition(16, 24);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        pacmanZone = new Rectangle();

        tiledMap = new TmxMapLoader().load("clasic.tmx");
        // Get the layer named "PacmanMap" from the TiledMap
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        wallLayer = (TiledMapTileLayer) tiledMap.getLayers().get("PacmanMap");
        pelletLayer = (TiledMapTileLayer) tiledMap.getLayers().get("FoodMap");

        mapPixmap = new Pixmap(Gdx.files.internal("meta-tiles.png"));
        wallColor = new Color(0, 0, 0, 1);

        int newScore = 0;

        difficulty = "Easy";

        switch (difficulty) {
            case "Easy":
                numGhosts = 4;
                score = 1;
                hearts = 5;
                ghostPauseTime = 0.6f;
                break;
            case "Medium":
                numGhosts = 6;
                score = 10;
                hearts = 3;
                ghostPauseTime = 0.8f;
                break;
            case "Hard":
                numGhosts = 8;
                score = 20;
                hearts = 1;
                ghostPauseTime = 1f;
                break;
            default:
                numGhosts = 2;
                score = 1;
                hearts = 5;
                ghostPauseTime = 0.6f;
                break;
        }

        ghosts = new ArrayList<>();

        // Create multiple ghosts
        for (int i = 0; i < numGhosts; i++) {
            ghosts.add(new Ghost(ghostTexture, MathUtils.random(16, 200), MathUtils.random(16, 200)));
        }

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        lives();
        moveGhost();
        logic();
        points();
        draw();
        drawMessage();
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    public void lives(){
        for (Ghost ghost : ghosts) {
            if (ghost.sprite.getBoundingRectangle().overlaps(pacmanSprite.getBoundingRectangle())) {
                hearts -= 1;
                pacmanSprite.setPosition(112,136);
                deathSound.play();

                currentMessage = "You lost a heart!";
                messageTimer = 2f;
                break;
            }
        }
        if (hearts == 0){
            deathSound.play();
            currentMessage = "Game Over!";
            messageTimer = 5f;
            Gdx.app.exit();
        }
    }

    public void drawMessage() {
        if (messageTimer > 0) {
            // Decrease the timer
            messageTimer -= Gdx.graphics.getDeltaTime();

            GlyphLayout layout = new GlyphLayout();
            layout.setText(font, currentMessage);

            // Draw the current message on the screen
            spriteBatch.begin();
            font.setColor(Color.RED);
            font.getData().setScale(2);
            font.draw(spriteBatch, currentMessage, viewport.getWorldWidth() / 2 - layout.width / 2, viewport.getWorldHeight() / 2 + layout.height / 2);
            spriteBatch.end();
        }
    }

    public void input() {
            float delta = Gdx.graphics.getDeltaTime();

            // Only allow movement if the pauseTimer is finished
            if (pauseTimer > 0) {
                pauseTimer -= delta;
                return; // If the pauseTimer is still running, don't allow movement
            }

            // Pac-Man's current position
            float pacmanX = pacmanSprite.getX();
            float pacmanY = pacmanSprite.getY();

            // Get the current tile coordinates
            int tileX = (int) (pacmanX / wallLayer.getTileWidth());
            int tileY = (int) (pacmanY / wallLayer.getTileHeight());
            int yPix = tileY * wallLayer.getTileWidth();
            int xPix = tileX * wallLayer.getTileWidth();

            System.out.println(tileX + ", " + tileY);


            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                if (!isBarrierTile(tileX + 1, tileY)) {
                    pacmanSprite.setPosition((tileX + 1) * 8, yPix);
                    pacmanSprite.setTexture(pacmanTexture);
                    pauseTimer = pauseTime;

                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                if (!isBarrierTile(tileX - 1, tileY)) {
                    pacmanSprite.setPosition((tileX - 1) * 8, yPix);
                    pacmanSprite.setTexture(left);
                    pauseTimer = pauseTime;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                if (!isBarrierTile(tileX, tileY + 1)) {
                    pacmanSprite.setPosition(xPix, (tileY + 1) * 8);
                    pacmanSprite.setTexture(up);
                    pauseTimer = pauseTime;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                if (!isBarrierTile(tileX, tileY - 1)) {
                    pacmanSprite.setPosition(xPix, (tileY - 1) * 8);
                    pacmanSprite.setTexture(down);
                    pauseTimer = pauseTime;
                }
            }
        }

    public void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        pacmanSprite.setX(MathUtils.clamp(pacmanSprite.getX(), 0, worldWidth - pacmanSprite.getWidth()));
        pacmanSprite.setY(MathUtils.clamp(pacmanSprite.getY(), 0, worldHeight - pacmanSprite.getHeight()));

    }
    public void points() {
        Rectangle pacmanRect = pacmanSprite.getBoundingRectangle();

        int tileWidth = wallLayer.getTileWidth();
        int tileHeight = wallLayer.getTileHeight();

        float pacmanX = pacmanSprite.getX();
        float pacmanY = pacmanSprite.getY();

        int tileX = (int) (pacmanX / tileWidth);
        int tileY = (int) (pacmanY / tileHeight);

        TiledMapTileLayer.Cell cell = pelletLayer.getCell(tileX, tileY);
        // Check for collision with the pellet
        if (cell != null) {
            TiledMapTile tile = cell.getTile();
            if (tile.getId() == 27|| tile.getId() == 18) {
                newScore += score;
                eatingSound.play();
                System.out.println("Pac-Man score is: " + newScore);

                // Remove the pellet from the map
                pelletLayer.setCell(tileX, tileY, null);
                System.out.println("Removed the pellet at " + tileX + "," + tileY);
            }

        }

            if (newScore % 246 == 0) {
                TiledMap newTiledMap = new TmxMapLoader().load("clasic.tmx");
                pelletLayer = (TiledMapTileLayer) newTiledMap.getLayers().get("FoodMap");
                // After resetting the layer, you might want to call tiledMapRenderer.render() to re-render the map
                tiledMapRenderer.setMap(newTiledMap);  // Update the renderer with the new map
            }

    }

    public void moveGhost() {
        float delta = Gdx.graphics.getDeltaTime();
        // Move each ghost
        for (Ghost ghost : ghosts) {
            ghost.move(wallLayer, delta);
        }
    }

    public boolean isBarrierTile(int tileX, int tileY) {
        // Get the tile at the given coordinates
        TiledMapTileLayer.Cell cell = wallLayer.getCell(tileX, tileY);

        if (cell == null) {
            return false;
        }


        TiledMapTile tile = cell.getTile();
        if (tile.getId() == 36){
            pelletLayer.setCell(tileX, tileY, null);
            return false;
        }
        return tile.getId() != 1;
    }

    public void renderGhosts(){
        for (Ghost ghost : ghosts) {
            ghost.sprite.draw(spriteBatch); // Draw each ghost
        }
    }

    public void draw() {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply viewport transformations
        viewport.apply();

        tiledMapRenderer.setView((OrthographicCamera) viewport.getCamera()); // Set the camera view
        tiledMapRenderer.render(); // Render the tile map layers

        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        pacmanSprite.draw(spriteBatch);

        renderGhosts();


        spriteBatch.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
        backgroundTexture.dispose();
        pacmanTexture.dispose();
        ghostTexture.dispose();
        tiledMap.dispose();
        tiledMapRenderer.dispose();
    }
}
