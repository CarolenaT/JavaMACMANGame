package org.latinschool;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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


public class MyPacManGame implements ApplicationListener {
    Texture backgroundTexture;
    Texture pacmanTexture;
    Texture ghostTexture;
    FitViewport viewport;
    Sprite ghostSprite;
    Sprite pacmanSprite;
    SpriteBatch spriteBatch;
    Rectangle safeZone;
    Rectangle pacmanZone;
    MyPacManGame game;
    String difficulty;
    int score;
    int newScore;
    int speed;
    int numGhosts;

    ShapeRenderer shapeRenderer;
    BitmapFont font;

    private TiledMap tiledMap;
    private TiledMapTileLayer wallLayer;
    private TiledMapTileLayer pelletLayer;// Track pellet timers


    Pixmap mapPixmap;
    Color wallColor;

    private OrthogonalTiledMapRenderer tiledMapRenderer;

    @Override
    public void create() {
        backgroundTexture = new Texture("Maze.png");
        pacmanTexture = new Texture("PacmanSmall.png");
        ghostTexture = new Texture("ghost.png");

        viewport = new FitViewport(280, 280);
        viewport.getCamera().position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        viewport.getCamera().update();

        ghostSprite = new Sprite(ghostTexture);
        pacmanSprite = new Sprite(pacmanTexture);

        ghostSprite.setSize(8, 8);
        pacmanSprite.setSize(8, 8);


        ghostSprite.setPosition(20, 20);
        pacmanSprite.setPosition(16, 16);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        safeZone = new Rectangle(4, 5, 2, 1);
        pacmanZone = new Rectangle();

        tiledMap = new TmxMapLoader().load("clasic.tmx");
        // Get the layer named "PacmanMap" from the TiledMap
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        wallLayer = (TiledMapTileLayer) tiledMap.getLayers().get("PacmanMap");
        pelletLayer = (TiledMapTileLayer) tiledMap.getLayers().get("FoodMap");


        mapPixmap = new Pixmap(Gdx.files.internal("meta-tiles.png"));
        wallColor = new Color(0, 0, 0, 1);

        int newScore = 0;


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

        int speed = 40;

        // Pac-Man's current position
        float pacmanX = pacmanSprite.getX();
        float pacmanY = pacmanSprite.getY();

        // Get the current tile coordinates (based on Pac-Man's current position)
        int tileX = (int) (pacmanX / wallLayer.getTileWidth());
        int tileY = (int) (pacmanY / wallLayer.getTileHeight());

        System.out.println(tileX + ", " + tileY);

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (!isBarrierTile(tileX + 1, tileY)) {
                pacmanSprite.setPosition(tileX + 1, tileY);  // Move right
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            // Check for collision with the tile to the left
            if (!isBarrierTile(tileX - 1, tileY)) {
                pacmanSprite.setPosition(tileX - 1, tileY);  // Move left
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            if (!isBarrierTile(tileX, tileY + 1)) {
                pacmanSprite.setPosition(tileX, tileY + 1);
                System.out.println("You can move up!");
            }
            else{
                System.out.println("You can't move up!");
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (!isBarrierTile(tileX, tileY - 1)) {
                pacmanSprite.setPosition(tileX, tileY - 1);
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
        score = 1;
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
    public boolean isBarrierTile(int tileX, int tileY) {
        // Get the tile at the given coordinates
        TiledMapTileLayer.Cell cell = wallLayer.getCell(tileX, tileY);

        if (cell == null) {
            return false;
        }

        TiledMapTile tile = cell.getTile();
        return tile.getId() != 1;
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

        ghostSprite.draw(spriteBatch);
        pacmanSprite.draw(spriteBatch);


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
