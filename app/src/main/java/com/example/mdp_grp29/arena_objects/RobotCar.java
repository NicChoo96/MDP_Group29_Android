package com.example.mdp_grp29.arena_objects;

import com.example.mdp_grp29.Vector2D;

public class RobotCar {
    public Vector2D robotPosition;
    public float robotOrientationAngle;
    public Direction robotDirection;

    public static enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NONE
    }

    public RobotCar(Vector2D robotPosition, Direction robotDirection){
        this.robotPosition = robotPosition;
        this.robotDirection = robotDirection;
    }
}
