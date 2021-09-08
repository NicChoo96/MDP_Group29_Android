package com.example.mdp_grp29.arena_objects;

import android.util.Log;

import com.example.mdp_grp29.Vector2D;

public class ObstacleDirectionButtons {
    public Btn directionsBtn[];
    private float[][] directionButtonOffSet;
    private final float BUTTON_SIZE = 60f;

    public Obstacles.Direction[] directionsMapping = {
        Obstacles.Direction.NORTH,
        Obstacles.Direction.WEST,
        Obstacles.Direction.NONE,
        Obstacles.Direction.EAST,
        Obstacles.Direction.SOUTH,
    };

    public ObstacleDirectionButtons(){
        directionButtonOffSet = new float[][]{{0, -3}, {-3, 0}, {0, 0}, {3, 0}, {0, 3}};
        directionsBtn = new Btn[5];
    }

    public void setButtonConfig(Vector2D canvasSize){
        Vector2D screenCenter = new Vector2D(canvasSize.x/2-BUTTON_SIZE, canvasSize.y/2-BUTTON_SIZE);
        for(int i = 0; i < directionButtonOffSet.length; i++){
            directionsBtn[i] = new Btn();
            directionsBtn[i].left = screenCenter.x + directionButtonOffSet[i][0] * BUTTON_SIZE;
            directionsBtn[i].top = screenCenter.y + directionButtonOffSet[i][1] * BUTTON_SIZE;
            directionsBtn[i].right = screenCenter.x + directionButtonOffSet[i][0] * BUTTON_SIZE + BUTTON_SIZE;
            directionsBtn[i].bottom = screenCenter.y + directionButtonOffSet[i][1] * BUTTON_SIZE + BUTTON_SIZE;
        }
    }

    public Obstacles.Direction checkButtonCollision(Vector2D fingerPos){
        for(int i = 0; i < directionsBtn.length; i++){
                if(fingerPos.x >= directionsBtn[i].left - 10f && fingerPos.x <= directionsBtn[i].right + 10f &&
                        fingerPos.y >= directionsBtn[i].top - 10f && fingerPos.y <= directionsBtn[i].bottom + 10f){
                    return directionsMapping[i];
                }
        }
        return Obstacles.Direction.EMPTY;
    }

    public class Btn{
        public float left, top, right, bottom;

        public Btn(){

        }
    }
}
