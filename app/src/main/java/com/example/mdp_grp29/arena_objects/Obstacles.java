package com.example.mdp_grp29.arena_objects;

import com.example.mdp_grp29.Vector2D;

public class Obstacles
{
    private Obstacle[] obstacles;

    public static enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NONE,
        EMPTY
    }

    public Obstacles(Vector2D[] initialCanvasPos){
        obstacles = new Obstacle[initialCanvasPos.length];
        for(int i = 0; i < initialCanvasPos.length; i++){
            obstacles[i] = new Obstacle(initialCanvasPos[i], Direction.NONE);
        }
    }

    public void setObstacleDir(int index, Direction newDirection){
        obstacles[index].obsDirection  = newDirection;
    }

    public Direction getObstacleDir(int index){
        return obstacles[index].obsDirection;
    }

    public Vector2D getObstaclePos(int index){
        return obstacles[index].position;
    }

    public void setObstaclePos(int index, Vector2D newPosition){
        obstacles[index].position = newPosition;
    }

    public Vector2D getObstacleCanvasPos(int index){
        return obstacles[index].topLeftCanvasPosition;
    }

    public void setObstacleCanvasPos(int index, Vector2D newPosition){
        obstacles[index].topLeftCanvasPosition = newPosition;
    }

    public int getObstacleCount(){
        return obstacles.length;
    }

    public class Obstacle
    {
        public Vector2D position;

        public Direction obsDirection;

        public Vector2D topLeftCanvasPosition;

        public Obstacle(Vector2D topLeftCanvasPosition, Direction obsDirection){
            this.topLeftCanvasPosition = topLeftCanvasPosition;
            this.obsDirection = obsDirection;
            position = new Vector2D(-1,-1);
        }

        public String printPos(){
            return String.format("X: %f, Y: %f", position.x, position.y);
        }
    }
}
