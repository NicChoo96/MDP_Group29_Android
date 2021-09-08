package com.example.mdp_grp29.arena_objects;

import com.example.mdp_grp29.bluetooth.BluetoothComponent;

public class ArenaPersistentData {

    private Obstacles obstaclesData = null;
    private RobotCar robotCarData = null;

    private static ArenaPersistentData instance = null;

    public static ArenaPersistentData getInstance(){
        if(instance == null)
            instance = new ArenaPersistentData();

        return instance;
    }

    public ArenaPersistentData(){

    }

    public void saveData(Obstacles obstacles, RobotCar robotCar){
        obstaclesData = obstacles;
        robotCarData = robotCar;
    }

    public Obstacles getObstaclesData(){
        return obstaclesData;
    }

    public RobotCar getRobotCarData(){
        return robotCarData;
    }

}
