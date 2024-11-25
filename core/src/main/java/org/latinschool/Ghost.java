package org.latinschool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;

public class Ghost {
    Texture texture;
    Texture texture2;
    Sprite sprite;
    float x, y;
    int direction; // 0 = up, 1 = down, 2 = left, 3 = right
    public float ghostPauseTimer = 0f;
    public float ghostPauseTime; // Duration of the pause before next move

    public Ghost(Texture texture, float x, float y) {
        this.texture = texture;
        this.texture2 = texture2;
        this.sprite = new Sprite(texture);
        this.x = 80;
        this.y = 80;
        this.sprite.setSize(8, 8);
        this.sprite.setPosition(x, y);
        this.direction = MathUtils.random(0, 3); // Start with a random direction
        this.ghostPauseTime = 0.05f; // Example pause time before movement
    }

    public void move(TiledMapTileLayer wallLayer, float delta) {
        int tileX = (int) (x / wallLayer.getTileWidth());
        int tileY = (int) (y / wallLayer.getTileHeight());

        texture2 = new Texture("ghostRight.png");

        // Apply pause time if necessary
        if (ghostPauseTimer > 0) {
            ghostPauseTimer -= delta;
            return; // Don't move if pause time hasn't finished
        }

        boolean moved = false;

        // Check current direction and try moving
        switch (direction) {
            case 0: // Up
                if (!isBarrierTile(tileX, tileY + 1, wallLayer)) {
                    y += wallLayer.getTileHeight(); // Move up by the tile height
                    moved = true;
                }
                break;
            case 1: // Down
                if (!isBarrierTile(tileX, tileY - 1, wallLayer)) {
                    y -= wallLayer.getTileHeight(); // Move down by the tile height
                    moved = true;
                }
                break;
            case 2: // Left
                if (!isBarrierTile(tileX - 1, tileY, wallLayer)) {
                    x -= wallLayer.getTileWidth(); // Move left by the tile width
                    this.sprite.setTexture(texture);

                    moved = true;
                }
                break;
            case 3: // Right
                if (!isBarrierTile(tileX + 1, tileY, wallLayer)) {
                    x += wallLayer.getTileWidth(); // Move right by the tile width
                    this.sprite.setTexture(texture2);
                    moved = true;
                }
                break;
        }

        // If the ghost couldn't move, pick a new random direction
        if (!moved) {
            direction = MathUtils.random(0, 3);
        }

        // Reset the pause timer after a successful move
        if (moved) {
            ghostPauseTimer = ghostPauseTime;
        }

        // Update the sprite's position to reflect the new x, y
        sprite.setPosition(x, y);
    }

    // Helper method to check if the given tile is a barrier (non-movable)
    private boolean isBarrierTile(int tileX, int tileY, TiledMapTileLayer wallLayer) {
        TiledMapTileLayer.Cell cell = wallLayer.getCell(tileX, tileY);
        if (cell == null) {
            return false; // No tile here means it's not a barrier
        }
        TiledMapTile tile = cell.getTile();
        return tile.getId() != 1; // Barrier tiles have ID != 1
    }
}
