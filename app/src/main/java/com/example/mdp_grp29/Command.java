package com.example.mdp_grp29;

public class Command {

    // Display Recognised Image at specific obstacle from Image Recognition Module
    // TT:<obstacle no> (1,2,3,4,5):<image ID> (0-15)
    public static String TARGET = "TT";

    // Return Robot Status to Android
    public static String STATUS ="ST";

    public static String[][] REMOTE_STATUS = {
            {"RTS", "Ready to Start"},
            {"RS", "Robot Started"},
            {"LFT","Looking for Target"},
            {"F", "Moving Forward"},
            {"B", "Moving Backward"},
            {"TL", "Turning Left"},
            {"TR", "Turning Right"},
            {"STCI","Stop to Capture Image"},
            {"C", "Robot has Completed"}
    };

    // Update Robot Position and Direction
    // COMMAND:x:y:D
    public static String ROBO ="RP";

    // Update Obstacle Position and Direction
    // COMMAND:x:y:D:x:y:D.....
    public static String OBSTACLE= "OBS";

    // Send Message between Arduino (AR) to Android (AN)
    public static String STM32 = "AR,AN,";

    // Robot Movements
    public static String LEFT = "SL";
    public static String RIGHT = "SR";
    public static String FORWARD ="F";
    public static String BACK ="R";
    public static String ROTATE_LEFT ="TL";
    public static String ROTATE_RIGHT ="TR";

    public static String START_EXPLORATION = "START";

    // Terminate Session
    public static String TERMINATE = "TERMINATE";

    // Capture Image
    public static String CAPTURE ="PIC";

}
