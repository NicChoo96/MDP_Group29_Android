package com.example.mdp_grp29.arena_objects;

import com.example.mdp_grp29.Vector2D;

public class RobotCar {
    public Vector2D robotPosition;
    public int robotOrientationAngle;

    public static final int NORTH = 0;
    public static final int SOUTH = 180;
    public static final int WEST = 270;
    public static final int EAST = 90;

    public static enum MoveArrow{
        UP,
        DOWN,
        LEFT,
        RIGHT,
        NONE
    }

    public void resetRobotCar(Vector2D initialPos){
        robotPosition = initialPos;
        robotOrientationAngle = 0;
    }

    public RobotCar(Vector2D robotPosition, int robotOrientationAngle){
        this.robotPosition = robotPosition;
        this.robotOrientationAngle = robotOrientationAngle;
    }
}
