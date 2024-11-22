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
    int hearts;

    ShapeRenderer shapeRenderer;
    BitmapFont font;

    private TiledMap tiledMap;
    private TiledMapTileLayer wallLayer;
    private TiledMapTileLayer pelletLayer;// Track pellet timers

    private float pauseTime = 0.06f; // 0.5 seconds pause (adjustable)
    private float pauseTimer = 0f;
    private float ghostMoveTime = 0.06f; // 0.5 seconds pause (adjustable)
    private float ghostMoveTimer = 0f;

    int ghost1CurrentDirection = 3;

    Pixmap mapPixmap;
    Color wallColor;

    private float ghostX, ghostY;

    private OrthogonalTiledMapRenderer tiledMapRenderer;

    @Override
    public void create() {
        backgroundTexture = new Texture("Maze.png");
        pacmanTexture = new Texture("PacmanSmall.png");
        ghostTexture = new Texture("ghost.png");

        viewport = new FitViewport(224, 288);
        viewport.getCamera().position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        viewport.getCamera().update();

        ghostSprite = new Sprite(ghostTexture);
        //ghostSprite = new Ghost(ghostTexture, Direction.RIGHT, 24,16,this);
        pacmanSprite = new Sprite(pacmanTexture);

        ghostSprite.setSize(8, 8);
        pacmanSprite.setSize(8, 8);


        ghostSprite.setPosition(16, 32);
        pacmanSprite.setPosition(16, 24);

        ghostY = 24;
        ghostX = 24;

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


        hearts = 3;
        numGhosts = 4;

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
        renderGhosts();
        pause();
        moveGhost();
        logic();
        points();
        draw();
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {
        if (ghostSprite.getBoundingRectangle().overlaps(pacmanSprite.getBoundingRectangle())) {
            hearts -= 1;
            System.out.println("You lose a heart!");
        }
        if (hearts == 0){
            System.out.println("You lose! Sorry!");

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
                    pauseTimer = pauseTime;

                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                if (!isBarrierTile(tileX - 1, tileY)) {
                    pacmanSprite.setPosition((tileX - 1) * 8, yPix);
                    pauseTimer = pauseTime;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                if (!isBarrierTile(tileX, tileY + 1)) {
                    pacmanSprite.setPosition(xPix, (tileY + 1) * 8);
                    pauseTimer = pauseTime;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                if (!isBarrierTile(tileX, tileY - 1)) {
                    pacmanSprite.setPosition(xPix, (tileY - 1) * 8);
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

    public void moveGhost() {
        float delta = Gdx.graphics.getDeltaTime();

        // Only allow movement if the ghost move timer has finished
        if (ghostMoveTimer > 0) {
            ghostMoveTimer -= delta;
            return;
        }

        // Random direction
        //int currentDirection = MathUtils.random(3);

        // Get current position in tiles
        int tileX = (int) (ghostX / wallLayer.getTileWidth());
        int tileY = (int) (ghostY / wallLayer.getTileHeight());
        int randomNumber;

        // Movement logic: check each direction
        boolean moved = false;

        switch (ghost1CurrentDirection) {
            case 0: // Move Up
                if (!isBarrierTile(tileX, tileY + 1)) {
                        ghostY += wallLayer.getTileHeight(); // Move the ghost up
                        moved = true;
                }
                else {
                    do {
                        ghost1CurrentDirection = MathUtils.random.nextInt(4);
                    } while (ghost1CurrentDirection == 1 || ghost1CurrentDirection == 0);
                }
                break;


            case 1: // Move Down
                if (!isBarrierTile(tileX, tileY - 1)) {
                    ghostY -= wallLayer.getTileHeight(); // Move the ghost down
                    moved = true;
                }
                else {
                    do {
                        ghost1CurrentDirection = MathUtils.random.nextInt(4);
                    } while (ghost1CurrentDirection == 1 || ghost1CurrentDirection == 0);
                }
                break;


            case 2: // Move Left
                if (!isBarrierTile(tileX - 1, tileY)) {
                    ghostX -= wallLayer.getTileWidth(); // Move the ghost left
                    moved = true;
                }
                else {
                    do {
                        ghost1CurrentDirection = MathUtils.random.nextInt(4);
                    } while (ghost1CurrentDirection == 3 || ghost1CurrentDirection == 2);
                }
                break;


            case 3: // Move Right
                if (!isBarrierTile(tileX + 1, tileY)) {
                    ghostX += wallLayer.getTileWidth(); // Move the ghost right
                    moved = true;
                }
                else {
                    do {
                        // Generate a random number between 0 and 3
                        ghost1CurrentDirection = MathUtils.random.nextInt(4);
                    } while (ghost1CurrentDirection == 3 || ghost1CurrentDirection == 2);
                }
                break;
        }

        if (moved) {
            ghostMoveTimer = ghostMoveTime; // Reset the move timer
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

    public void renderGhosts(){
        for (int x = numGhosts; x > 0; x--){
            ghostSprite = new Sprite(ghostTexture);
            ghostSprite.setSize(8, 8);
            ghostSprite.setPosition(80, 80);
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

        renderGhosts();
        pacmanSprite.draw(spriteBatch);
        ghostSprite.setPosition(ghostX, ghostY);
        ghostSprite.draw(spriteBatch);


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
