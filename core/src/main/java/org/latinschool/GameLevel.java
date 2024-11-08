package org.latinschool;

import com.badlogic.gdx.ScreenAdapter;

public class GameLevel extends ScreenAdapter {
    private final String difficulty;
    private MyPacManGame game;

    public GameLevel(MyPacManGame game, String difficulty) {
        this.game = game;
        this.difficulty = difficulty;
        initializeLevel();
    }

    public void initializeLevel() {
        int ghostSpeed;
        int numDots;
        int lives;

        switch (difficulty) {
            case "Easy":
                ghostSpeed = 2;
                numDots = 50;
                lives = 5;
                break;
            case "Medium":
                ghostSpeed = 3;
                numDots = 40;
                lives = 3;
                break;
            case "Hard":
                ghostSpeed = 4;
                numDots = 30;
                lives = 1;
                break;
            default:
                ghostSpeed = 2;
                numDots = 50;
                lives = 5;
                break;
        }
    }
}

