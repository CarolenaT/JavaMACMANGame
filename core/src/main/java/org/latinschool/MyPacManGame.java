package org.latinschool;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.HashMap;
import java.util.Map;


public class MyPacManGame implements ApplicationListener {
    Texture backgroundTexture;
    Texture pacmanTexture;
    Texture ghostTexture;
    Texture powerPillTexture;
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

    private final float PAC_SPEED = 4f;
    private final float PELLET_DISAPPEAR_TIME = 60f;

    private TiledMap tiledMap;
    private TiledMapTileLayer wallLayer;
    private Map<String, Float> pelletTimers;
    private TiledMapTileLayer pelletLayer;// Track pellet timers


    Pixmap mapPixmap;
    Color wallColor;

    private OrthogonalTiledMapRenderer tiledMapRenderer;

    @Override
    public void create() {
        backgroundTexture = new Texture("Maze.png");
        pacmanTexture = new Texture("Pacman.png");
        ghostTexture = new Texture("ghost.png");

        viewport = new FitViewport(280, 280);
        viewport.getCamera().position.set(0, 0, 0);
        viewport.getCamera().update();

        ghostSprite = new Sprite(ghostTexture);
        pacmanSprite = new Sprite(pacmanTexture);

        ghostSprite.setSize(8, 8);
        pacmanSprite.setSize(8, 8);


        ghostSprite.setPosition(20, 20);
        pacmanSprite.setPosition(15, 15);

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

        pelletTimers = new HashMap<>();


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

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            pacmanSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            pacmanSprite.translateX(-speed * delta);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            pacmanSprite.translateY(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            pacmanSprite.translateY(-speed * delta);
        }

        // Before actually moving, check if the next position collides with a wall
        }

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
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        pacmanSprite.setX(MathUtils.clamp(pacmanSprite.getX(), 0, worldWidth - pacmanSprite.getWidth()));
        pacmanSprite.setY(MathUtils.clamp(pacmanSprite.getY(), 0, worldHeight - pacmanSprite.getHeight()));
    }
    public void points() {
        score = 1;
        for (int x = 0; x < pelletLayer.getWidth(); x++) {
            for (int y = 0; y < pelletLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = pelletLayer.getCell(x, y);

                if (cell != null && !pelletTimers.containsKey(x + "," + y)) {
                    // Check for collision with the pellet
                    if (pacmanSprite.getBoundingRectangle().overlaps(new Rectangle(15+x, 15+y, 1f, 1f))) {

                        newScore += score;// Increment the score
                        System.out.println("Pac-Man score is: " + newScore);

                        // Mark this pellet as eaten and start the timer
                        //pelletTimers.put(x + "," + y, PELLET_DISAPPEAR_TIME);

                        // Remove the pellet from the layer
                        pelletLayer.setCell(x, y, null);
                    }
                }

                    if (newScore == 246) {
                        // Restore the pellet after 1 minute
                        newScore = 0;
                        TiledMap newTiledMap = new TmxMapLoader().load("clasic.tmx");
                        pelletLayer = (TiledMapTileLayer) newTiledMap.getLayers().get("FoodMap");
                        // After resetting the layer, you might want to call tiledMapRenderer.render() to re-render the map
                        tiledMapRenderer.setMap(newTiledMap);  // Update the renderer with the new map


                        /*pelletTimers.remove(x + "," + y);
                        TiledMapTile tile = new StaticTiledMapTile(/*new TextureRegion(powerPillTexture) );
                        TiledMapTileLayer.Cell newCell = new TiledMapTileLayer.Cell();
                        newCell.setTile(tile);
                        pelletLayer.setCell(x, y, newCell);*/
                    }
                }
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
