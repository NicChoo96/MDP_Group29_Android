package com.example.mdp_grp29.arena_objects;

import com.example.mdp_grp29.Vector2D;

public class Obstacles
{
    public Obstacle[] obstacles;

    public static enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NONE
    }

    public Obstacles(Vector2D[] obstaclePos){
        obstacles = new Obstacle[obstaclePos.length];
        for(int i = 0; i < obstaclePos.length; i++){
            obstacles[i] = new Obstacle(obstaclePos[i], Direction.NONE);
        }
    }

    public void setObstacleDirection(int index, Direction newDirection){
        obstacles[index].obsDirection  = newDirection;
    }

    public Direction getObstacleDirection(int index){
        return obstacles[index].obsDirection;
    }

    public class Obstacle
    {
        public Vector2D position;

        public Direction obsDirection;

        public Obstacle(Vector2D position, Direction obsDirection){
            this.position = position;
            this.obsDirection = obsDirection;
        }
    }
}
