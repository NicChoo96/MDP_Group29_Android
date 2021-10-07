package com.example.mdp_grp29.ui.arena;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.mdp_grp29.R;
import com.example.mdp_grp29.Vector2D;
import com.example.mdp_grp29.arena_objects.ArenaGrid;
import com.example.mdp_grp29.arena_objects.ObstacleDirectionButtons;
import com.example.mdp_grp29.arena_objects.Obstacles;
import com.example.mdp_grp29.arena_objects.RobotCar;

public class ArenaView extends View {

    private final String TAG = "ArenaView";

    private final int NUM_COLUMNS = 20;
    private final int NUM_ROWS = 20;
    private float CELL_SIZE;

    private Paint mapPaint;
    private Paint whitePaint;
    private Paint blackPaint;
    private Paint obstacleTextPaint;
    private Paint transparentPaint;
    private Paint bluePaint;
    private Paint facePaint;
    private Paint grayPaint;
    private Paint startPaint;

    private float zoomScale = 0f;
    private float originalScale;
    private final float touchMargin = 10f;


    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public Obstacles obsArray;
    public final int obstacleCount = 8;
    private final int obstacleColumns = 2;
    private final int obstacleRows = 4;

    private Vector2D[] initialObstacleCanvasPos = new Vector2D[obstacleCount];

    private ArenaGrid arenaGrid;
    private final Vector2D arenaGridOffSet = new Vector2D(30f, 5f);

    private boolean isDirectionChoosing;
    private boolean directionUI_BufferLock = false;
    private ObstacleDirectionButtons obstacleDirectionButtons;

    private RobotCar robotCar;
    private final Vector2D robotSize = new Vector2D(3,3);
    private final Vector2D initialRobotPos = new Vector2D(1,1);

    ArenaFragment arenaFragment = ArenaFragment.getInstance();

    // All target character images
    int[] images = new int[]{
            // Bullseye
            R.drawable.bulleyes,     // Image ID =0
            // Arrow
            R.drawable.white_up,           // Image ID =1
            R.drawable.red_down,            // Image ID =2
            R.drawable.green_right,         // Image ID =3
            R.drawable.blue_left,            // Image ID =4
            // Stop
            R.drawable.yellow_circle,       // Image ID =5
            // Number
            R.drawable.blue_1,              // Image ID =6
            R.drawable.green_2,             // Image ID =7
            R.drawable.red_3,               // Image ID =8
            R.drawable.white_4,             // Image ID =9
            R.drawable.yellow_5,            // Image ID =10
            R.drawable.blue_6,              // Image ID =11
            R.drawable.green_7,              // Image ID =12
            R.drawable.red_8,               // Image ID = 13
            R.drawable.white_9,               // Image ID = 14
            // Alphabet
            R.drawable.red_a,             // Image ID =15
            R.drawable.green_b,           // Image ID =16
            R.drawable.white_c,           // Image ID =17
            R.drawable.blue_d,            // Image ID =18
            R.drawable.yellow_e,           // Image ID =19
            R.drawable.red_f,           // Image ID =20
            R.drawable.green_g,           // Image ID =21
            R.drawable.white_h,           // Image ID =22
            R.drawable.blue_s,           // Image ID =23
            R.drawable.yellow_t,           // Image ID =24
            R.drawable.red_u,           // Image ID =25
            R.drawable.green_v,           // Image ID =26
            R.drawable.white_w,           // Image ID =27
            R.drawable.blue_x,           // Image ID =28
            R.drawable.yellow_y,           // Image ID =29
            R.drawable.red_z,           // Image ID =30
    };

    // Constructor
    public ArenaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Create Objects
        mapPaint= new Paint();
        whitePaint= new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
        obstacleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        facePaint = new Paint();
        grayPaint = new Paint();
        transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluePaint = new Paint();
        startPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        facePaint = new Paint();
//        startPaint= new Paint();
//        exploredPaint= new Paint();
//
        // Set Display Colors

        mapPaint.setColor(Color.LTGRAY);
//        robotPaint.setColor(Color.DKGRAY);
        whitePaint.setColor(Color.WHITE);
        grayPaint.setColor(Color.GRAY);
        blackPaint.setColor(Color.BLACK);
        obstacleTextPaint.setColor(Color.BLACK);
        bluePaint.setColor(Color.BLUE);
        facePaint.setColor(Color.RED);
        transparentPaint.setStyle(Paint.Style.FILL);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setAlpha(120);

        startPaint.setColor(Color.parseColor("#A4FEFF"));    // Aqua
        startPaint.setAlpha(120);
//        exploredPaint.setColor(Color.parseColor("#2B6EFE")); // Indigo-Blue
//
//        // Set Display Layout
//        blackPaint.setStrokeWidth(WALL_THICKNESS);
        whitePaint.setTextSize(15);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        arenaGrid = new ArenaGrid(NUM_ROWS, NUM_COLUMNS, arenaGridOffSet);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        CELL_SIZE = Math.abs(getWidth() / NUM_COLUMNS - 10) -3f;

        originalScale = CELL_SIZE;

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = 480f * CELL_SIZE/getWidth();
        Log.d(TAG, "Desired Text: " + desiredTextSize);
        // Set the paint for that size.
        obstacleTextPaint.setTextSize(desiredTextSize);

        for(int i = 0; i < obstacleColumns; i++){
            for(int j = 0; j < obstacleRows; j++){
                initialObstacleCanvasPos[i*obstacleRows + j] = new Vector2D((NUM_COLUMNS+2f + (i*2))*CELL_SIZE, (j+1)*2f*CELL_SIZE);
            }
        }

        if(isInEditMode()){
            obsArray = new Obstacles(initialObstacleCanvasPos);
            robotCar = new RobotCar(new Vector2D(1, 1), RobotCar.NORTH);
        }else{
            if(arenaFragment.arenaPersistentData.getObstaclesData() != null){
                obsArray = arenaFragment.arenaPersistentData.getObstaclesData();
            }else{
                obsArray = new Obstacles(initialObstacleCanvasPos);
                savePersistentData();
            }

            if(arenaFragment.arenaPersistentData.getRobotCarData() != null){
                robotCar = arenaFragment.arenaPersistentData.getRobotCarData();
            }else{
                robotCar = new RobotCar(new Vector2D(initialRobotPos.x, initialRobotPos.y), RobotCar.NORTH);
                savePersistentData();
            }
        }

        obstacleDirectionButtons = new ObstacleDirectionButtons();
        invalidate();
    }

    private void savePersistentData(){
        arenaFragment.arenaPersistentData.saveData(obsArray, robotCar);
    }

    public void resetArenaView()
    {
        mScaleFactor = 1.f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        canvas.scale(mScaleFactor, mScaleFactor, mScaleDetector.getFocusX() * 2f, mScaleDetector.getFocusY() * 2f);

        canvas.drawColor(Color.WHITE);
        drawObstacleTextView(canvas);
        drawGrid(canvas);
        drawStartZone(canvas);
        drawObstacles(canvas);
        drawObstacleDirectionFaces(canvas);
        displayRobot(canvas);

        if(isDirectionChoosing)
            drawObstacleDirectionButtons(canvas);

        canvas.restore();

//
//        // Display Route Path
//        drawRoutePath(canvas);


//
//        // Display Explored Maze
//        //drawExploredObstacles(canvas);
//
        // Display Recognized Target Images from Image Recognition
        displayImageOnObstacles(canvas);
    }

    // Create Start Zone
    private void drawStartZone(Canvas canvas) {
        for (int i = 0; i <= 3; i++)
            for (int j = 0; j <= 3; j++)
                canvas.drawRect(i * CELL_SIZE + arenaGridOffSet.x, (NUM_ROWS - 1 - j) * CELL_SIZE + 5f,
                        (i + 1) * CELL_SIZE + arenaGridOffSet.x, (NUM_ROWS - j) * CELL_SIZE + 5f, startPaint);
    }


    public void setRobotPos(Vector2D newPosition, String direction){
        robotCar.robotPosition = newPosition;
        switch(direction){
            case "N":
                robotCar.robotOrientationAngle = 0;
                break;
            case "S":
                robotCar.robotOrientationAngle = 180;
                break;
            case "W":
                robotCar.robotOrientationAngle = 270;
                break;
            case "E":
                robotCar.robotOrientationAngle = 90;
                break;
        }
        invalidate();
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= Math.round((detector.getScaleFactor()*100f))/100f;

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }
    //
    public void moveRobot(RobotCar.MoveArrow move){

        Vector2D robotNewMove = new Vector2D(0,0);

        switch(move){
            case UP:
                robotNewMove = calculateRobotMovementDirection(RobotCar.MoveArrow.UP);
                break;
            case DOWN:
                robotNewMove = calculateRobotMovementDirection(RobotCar.MoveArrow.DOWN);
                break;
            case LEFT:
                robotCar.robotOrientationAngle -= 90;
                if(robotCar.robotOrientationAngle < 0)
                    robotCar.robotOrientationAngle = 270;
                break;
            case RIGHT:
                robotCar.robotOrientationAngle += 90;
                if(robotCar.robotOrientationAngle > 270)
                    robotCar.robotOrientationAngle = 0;
                break;
        }

        if(robotCar.robotPosition.x + robotNewMove.x - 1 >= 0 &&
                robotCar.robotPosition.x + robotNewMove.x < NUM_COLUMNS-1)
            robotCar.robotPosition.x += robotNewMove.x;

        if(robotCar.robotPosition.y + robotNewMove.y - 1 >= 0 &&
                robotCar.robotPosition.y + robotNewMove.y < NUM_ROWS-1)
            robotCar.robotPosition.y += robotNewMove.y;

        arenaFragment.sendRobotMovement(move);
        arenaFragment.updateRobotInfoTextView(robotCar.robotPosition, robotCar.robotOrientationAngle);

        invalidate();
    }

    public void resetAllArenaObjects()
    {
        obsArray.resetObstacles(initialObstacleCanvasPos);
        robotCar.robotPosition.x = initialRobotPos.x;
        robotCar.robotPosition.y = initialRobotPos.y;
        robotCar.robotOrientationAngle = RobotCar.NORTH;
        invalidate();
        arenaFragment.updateRobotInfoTextView(robotCar.robotPosition, robotCar.robotOrientationAngle);
        arenaFragment.updateObstacleInfoTextView(0, new Vector2D(-1, -1));
    }

    public void updateObstacleTargetImage(int obstacleIndex, int imageID){
        if(obstacleIndex < 0 || obstacleIndex >= obsArray.getObstacleCount()){
            Log.d(TAG, "Invalid obstacle index given!");
            return;
        }
        obsArray.setObstacleImage(obstacleIndex, imageID);
        invalidate();
    }

    // Display Recognised Image
    private void displayImageOnObstacles(Canvas canvas) {
        for (int i = 0; i < obsArray.getObstacleCount(); i++) {
            if(obsArray.getObstacleImage(i) != -1)
            {
                if(obsArray.getObstacleImage(i) >= images.length){
                    Log.d(TAG, "Image ID is invalid at Obstacle " + i);
                    continue;
                }
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        images[obsArray.getObstacleImage(i)]);
                Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, (int)CELL_SIZE,
                        (int)CELL_SIZE, false);
                int x = (int)(obsArray.getObstaclePos(i).x * CELL_SIZE + 1);
                int y = (int)((NUM_ROWS - obsArray.getObstaclePos(i).y - 1) * CELL_SIZE);
                canvas.drawBitmap(resizeBitmap, obsArray.getObstacleCanvasPos(i).x, obsArray.getObstacleCanvasPos(i).y, whitePaint);
            }
        }
    }

    private Vector2D calculateRobotMovementDirection(RobotCar.MoveArrow moveArrow){
        Vector2D robotMovement = new Vector2D(0,0);

        int isUp = 1;

        // When the direction is DOWN instead of UP, the robot movement are the opposite
        if(moveArrow == RobotCar.MoveArrow.DOWN)
            isUp = -1;

        switch(robotCar.robotOrientationAngle){
            case RobotCar.NORTH:
                robotMovement.y = 1 * isUp;
                break;
            case RobotCar.SOUTH:
                robotMovement.y = -1 * isUp;
                break;
            case RobotCar.WEST:
                robotMovement.x = -1 * isUp;
                break;
            case RobotCar.EAST:
                robotMovement.x = 1 * isUp;
                break;
        }


        return robotMovement;
    }

    private void displayRobot(Canvas canvas){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.driverless_car5);
        Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, (int)(CELL_SIZE * robotSize.x), (int)(CELL_SIZE * robotSize.y), false);
        Matrix m = new Matrix();
        int rotation = 0;
        Vector2D robotPostTranslate = new Vector2D(0,0);
        switch(robotCar.robotOrientationAngle){
            case RobotCar.NORTH:
                robotPostTranslate = new Vector2D((robotCar.robotPosition.x) * CELL_SIZE + 3f,
                        (NUM_ROWS - robotCar.robotPosition.y - 2) * CELL_SIZE + 3f);
                rotation = 0;
                break;
            case RobotCar.EAST:
                robotPostTranslate = new Vector2D((robotCar.robotPosition.x - 3) * CELL_SIZE + 2f,
                        (NUM_ROWS - robotCar.robotPosition.y - 2) * CELL_SIZE + 3f);
                rotation = 90;
                break;
            case RobotCar.SOUTH:
                robotPostTranslate = new Vector2D((robotCar.robotPosition.x - 3) * CELL_SIZE + 3f,
                        (NUM_ROWS - robotCar.robotPosition.y - 5) * CELL_SIZE + 3f);
                rotation = 180;
                break;
            case RobotCar.WEST:
                robotPostTranslate = new Vector2D((robotCar.robotPosition.x) * CELL_SIZE + 3f,
                        (NUM_ROWS - robotCar.robotPosition.y - 5) * CELL_SIZE + 3f);
                rotation = 270;
                break;
        }
        m.setRotate(rotation, resizeBitmap.getWidth(), resizeBitmap.getHeight());
        m.postTranslate(robotPostTranslate.x, robotPostTranslate.y);
        canvas.drawBitmap(resizeBitmap, m, whitePaint);
    }

    private void viewMovement(MotionEvent event){
        if(event.getPointerCount() ==  2)
            mScaleDetector.onTouchEvent(event);
    }


    private int checkObstacleCollision(float x, float y, float touchMargin){

        float left, top, right, bottom;

        for(int i = 0; i < obsArray.getObstacleCount(); i++){
            left = obsArray.getObstacleCanvasPos(i).x;
            top = obsArray.getObstacleCanvasPos(i).y;
            right = obsArray.getObstacleCanvasPos(i).x + CELL_SIZE;
            bottom = obsArray.getObstacleCanvasPos(i).y + CELL_SIZE;

            if(x >= left - touchMargin && x <= right + touchMargin && y >= top - touchMargin && y <= bottom + touchMargin){
                return i;
            }
        }
        return -1;
    }

    private int mActivePointedId = MotionEvent.INVALID_POINTER_ID;
    private int obstacleIndexSelected = -1;

    private void dragObstacle(MotionEvent event){

        final int action = event.getActionMasked();

        if(event.getPointerCount() == 1){
            if(action == MotionEvent.ACTION_DOWN){
                final int pointerIndex = event.getActionIndex();
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                mActivePointedId = event.getPointerId(0);

                obstacleIndexSelected = checkObstacleCollision(x, y, touchMargin);

            }else if(action == MotionEvent.ACTION_MOVE){
                final int pointerIndex = event.findPointerIndex(mActivePointedId);

                if(pointerIndex == MotionEvent.INVALID_POINTER_ID)
                    return;

                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                // Drag Obstacles Around
                if(obstacleIndexSelected >= 0 && obstacleIndexSelected < obsArray.getObstacleCount()){
                    obsArray.setObstacleCanvasPos(obstacleIndexSelected, new Vector2D(x, y));
                    Vector2D obstaclePos = new Vector2D((x+CELL_SIZE/2)/CELL_SIZE,(NUM_ROWS-(y+CELL_SIZE/2)/CELL_SIZE));
                    arenaFragment.updateObstacleInfoTextView(obstacleIndexSelected, obstaclePos);
                    obsArray.setObstaclePos(obstacleIndexSelected, obstaclePos);
                    invalidate();
                }

                return;
            }else if(action == MotionEvent.ACTION_UP){
                // Check for obstacles being dragged out of screen
                if(obstacleIndexSelected != -1){
                    // Record collided grid cell position with obstacle
                    Vector2D newObsPos = arenaGrid.checkGridCollision(obsArray.getObstacleCanvasPos(obstacleIndexSelected), CELL_SIZE);
                    // Invalid grid position resets obstacle to default
                    if(newObsPos.x  == -1 || newObsPos.y == -1){
                        obsArray.setObstacleCanvasPos(obstacleIndexSelected, initialObstacleCanvasPos[obstacleIndexSelected]);
                        obsArray.setObstaclePos(obstacleIndexSelected, new Vector2D(-1, -1));
                        obsArray.setObstacleDir(obstacleIndexSelected, Obstacles.Direction.NONE);
                        arenaFragment.updateObstacleInfoTextView(obstacleIndexSelected, new Vector2D(-1, -1));
                    }else{
                        // Obstacle New Position according to the top left of the cell
                        Vector2D obsNewPos = new Vector2D(arenaGrid.cells[(int) newObsPos.x][(int) newObsPos.y].left,
                                arenaGrid.cells[(int) newObsPos.x][(int) newObsPos.y].top);
                        // Move Obstacle Canvas Position to latest
                        obsArray.setObstacleCanvasPos(obstacleIndexSelected, obsNewPos);
//                        obsArray.setObstaclePos(obstacleIndexSelected, arenaGrid.cells[(int) newObsPos.x][(int) newObsPos.y].position);
//                        arenaFragment.updateObstacleInfoTextView(obstacleIndexSelected, arenaGrid.cells[(int) newObsPos.x][(int) newObsPos.y].position);
                        // Setup for direction choosing buttons
                        obstacleDirectionButtons.setButtonConfig(new Vector2D(getWidth(), getHeight()));
                        isDirectionChoosing = true;
                        directionUI_BufferLock = true;
                    }
                    invalidate();
                }
                mActivePointedId = MotionEvent.INVALID_POINTER_ID;

            }
        }
    }

    private void drawObstacleTextView(Canvas canvas){

        int obstaclePosX, obstaclePosY;
        for(int i = 0; i < obsArray.getObstacleCount(); i++){
            obstaclePosX = (int)obsArray.getObstaclePos(i).x;
            obstaclePosY = (int)obsArray.getObstaclePos(i).y;
            canvas.drawText((i+1) + ") X: " + obstaclePosX+
                    " Y: " + obstaclePosY + " Dir: " + obsArray.getObstacleDir(i),
                    (NUM_COLUMNS+2f) * CELL_SIZE,
                    (i+12f)*CELL_SIZE, obstacleTextPaint);
        }
    }

    private void chooseObstacleDirection(MotionEvent event){
        final int action = event.getActionMasked();
        Obstacles.Direction chosenDirection = Obstacles.Direction.NONE;

        if(event.getPointerCount() == 1){
            if(action == MotionEvent.ACTION_DOWN) {
                final int pointerIndex = event.getActionIndex();
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);


                chosenDirection = obstacleDirectionButtons.checkButtonCollision(new Vector2D(x, y));
                if(chosenDirection != Obstacles.Direction.EMPTY){
                    obsArray.setObstacleDir(obstacleIndexSelected, chosenDirection);
                    arenaFragment.showToast(String.format("Obstacle Index[%d] assigned to %s", obstacleIndexSelected+1, chosenDirection.toString()));
                    arenaFragment.vibrateDevice(200);
                    obstacleIndexSelected = -1;
                    isDirectionChoosing = false;
                    savePersistentData();
                    invalidate();
                }
            }
        }
    }

    // C6- Touch Gesture
    public boolean onTouchEvent(MotionEvent event) {

        // *** The order of the if statements matters because of the buffer lock

        if(isDirectionChoosing)
            chooseObstacleDirection(event);
        if(!isDirectionChoosing && directionUI_BufferLock){
            directionUI_BufferLock = false;
            return true;
        }
        if(!isDirectionChoosing){
            viewMovement(event);
            dragObstacle(event);
        }
        return true;
    }

    private void drawGrid(Canvas canvas){
        boolean isGray = false;
        float left, top, right, bottom;
        CELL_SIZE = originalScale + zoomScale;

        // Draw Grid Squares
        arenaGrid.calculateGrid(CELL_SIZE);
        for(int i = 0; i < NUM_COLUMNS; i++){
            for(int j = 0; j < NUM_ROWS; j++){
                if(isGray){
                    canvas.drawRect(
                            arenaGrid.cells[i][j].left, arenaGrid.cells[i][j].top,
                            arenaGrid.cells[i][j].right, arenaGrid.cells[i][j].bottom, grayPaint);
                    isGray = !isGray;
                }
                else{
                    canvas.drawRect(
                            arenaGrid.cells[i][j].left, arenaGrid.cells[i][j].top,
                            arenaGrid.cells[i][j].right, arenaGrid.cells[i][j].bottom, mapPaint);
                    isGray = !isGray;
                }
            }
            isGray = !isGray;
        }

        for(int i = 0; i < NUM_COLUMNS; i++){
            canvas.drawText(i+"", i * CELL_SIZE + arenaGridOffSet.x + 13f,
                    NUM_COLUMNS * CELL_SIZE + arenaGridOffSet.y + CELL_SIZE - 8f, blackPaint);
        }

        for(int i = 0; i < NUM_ROWS; i++){
            if(NUM_ROWS - i - 1 > 9)
                canvas.drawText((NUM_ROWS - i - 1)+"", arenaGridOffSet.x - 20f,
                        i * CELL_SIZE + arenaGridOffSet.y + 22f, blackPaint);
            else
                canvas.drawText((NUM_ROWS - i - 1)+"", arenaGridOffSet.x - 15f,
                        i * CELL_SIZE + arenaGridOffSet.y + 22f, blackPaint);
        }
    }

    private void drawObstacles(Canvas canvas){
        float left, top, right, bottom;

        for(int i = 0; i < obsArray.getObstacleCount(); i++){
            left = obsArray.getObstacleCanvasPos(i).x;
            top = obsArray.getObstacleCanvasPos(i).y;
            right = obsArray.getObstacleCanvasPos(i).x + CELL_SIZE;
            bottom = obsArray.getObstacleCanvasPos(i).y + CELL_SIZE;

            canvas.drawRect(left , top, right, bottom, blackPaint);
            canvas.drawText(i+1+"", (left + right)/2f - 5f, (top + bottom)/2f + 5f, whitePaint);
        }
    }

    private void drawObstacleDirectionButtons(Canvas canvas){
        String[] directionText = {"N", "W", "X", "E", "S"};
        float left, top, right, bottom;

        // Grey-out background
        canvas.drawRect(0, 0, getWidth(), getHeight(), transparentPaint);

        for(int i = 0; i < obstacleDirectionButtons.directionsBtn.length; i++){
            left = obstacleDirectionButtons.directionsBtn[i].left;
            top = obstacleDirectionButtons.directionsBtn[i].top;
            right = obstacleDirectionButtons.directionsBtn[i].right;
            bottom = obstacleDirectionButtons.directionsBtn[i].bottom;
            canvas.drawRect(
                    left,
                    top,
                    right,
                    bottom,
                    bluePaint);
            canvas.drawText(directionText[i], (left + right)/2f - 5f, (top + bottom)/2f + 5f, whitePaint);
        }
    }

    private void drawObstacleDirectionFaces (Canvas canvas){
        float faceGap = 1.2f * CELL_SIZE;
        float left, top, right, bottom;
        for(int i = 0; i < obsArray.getObstacleCount(); i++){
            left = obsArray.getObstacleCanvasPos(i).x;
            top = obsArray.getObstacleCanvasPos(i).y;
            right = obsArray.getObstacleCanvasPos(i).x + CELL_SIZE;
            bottom = obsArray.getObstacleCanvasPos(i).y + CELL_SIZE;

            switch(obsArray.getObstacleDir(i)){
                case NORTH:
                    bottom -= faceGap;
                    break;
                case EAST:
                    left += faceGap;
                    break;
                case SOUTH:
                    top += faceGap;
                    break;
                case WEST:
                    right -= faceGap;
                    break;
                case NONE:
                case EMPTY:
                    continue;
            }
            canvas.drawRect(left, top, right, bottom,
                    facePaint);
        }
    }


}
