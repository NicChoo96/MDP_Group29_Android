package com.example.mdp_grp29.arena_objects;

import com.example.mdp_grp29.Vector2D;

public class RobotCar {
    public Vector2D robotPosition;
    public float robotOrientationAngle;

    public static enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NONE
    }

    public RobotCar(Vector2D robotPosition, float robotOrientationAngle){
        this.robotPosition = robotPosition;
        this.robotOrientationAngle = robotOrientationAngle;
    }
}
