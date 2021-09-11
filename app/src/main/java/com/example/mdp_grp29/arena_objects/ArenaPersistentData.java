package com.example.mdp_grp29.arena_objects;

import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ArenaPersistentData {

    private Obstacles obstaclesData = null;
    private RobotCar robotCarData = null;

    private ArrayAdapter<String> statusHistory;

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

    public void saveHistory(ArrayAdapter<String> history){
        statusHistory = history;
    }

    public ArrayAdapter<String> loadHistory(){
        return statusHistory;
    }

    public Obstacles getObstaclesData(){
        return obstaclesData;
    }

    public RobotCar getRobotCarData(){
        return robotCarData;
    }

}
