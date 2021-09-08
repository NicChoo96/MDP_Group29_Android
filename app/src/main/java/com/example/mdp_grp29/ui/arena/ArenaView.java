package com.example.mdp_grp29.ui.arena;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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
    private Paint transparentPaint;
    private Paint bluePaint;
    private Paint facePaint;
    private Paint grayPaint;

    private float zoomScale = 30f;
    private float originalScale;
    private final float touchMargin = 10f;


    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public Obstacles obsArray;
    public final int obstacleCount = 5;
    private Vector2D[] initialObstacleCanvasPos = new Vector2D[obstacleCount];

    private ArenaGrid arenaGrid;
    private final Vector2D arenaGridOffSet = new Vector2D(30f, 30f);

    private boolean isDirectionChoosing;
    private boolean directionUI_BufferLock = false;
    private ObstacleDirectionButtons obstacleDirectionButtons;

    private RobotCar robotCar;

    public static enum MoveArrow{
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    ArenaFragment arenaFragment = ArenaFragment.getInstance();

    // Constructor
    public ArenaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Create Objects
        mapPaint= new Paint();
        whitePaint= new Paint();
        blackPaint= new Paint();
        facePaint = new Paint();
        grayPaint = new Paint();
        transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluePaint = new Paint();
//        facePaint = new Paint();
//        startPaint= new Paint();
//        robotPaint= new Paint();
//        exploredPaint= new Paint();
//
        // Set Display Colors

        mapPaint.setColor(Color.LTGRAY);
//        robotPaint.setColor(Color.DKGRAY);
        whitePaint.setColor(Color.WHITE);
        grayPaint.setColor(Color.GRAY);
        blackPaint.setColor(Color.BLACK);
        bluePaint.setColor(Color.BLUE);
        facePaint.setColor(Color.RED);
        transparentPaint.setStyle(Paint.Style.FILL);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setAlpha(120);

//        facePaint.setColor(Color.RED);
//        startPaint.setColor(Color.parseColor("#A4FEFF"));    // Aqua
//        exploredPaint.setColor(Color.parseColor("#2B6EFE")); // Indigo-Blue
//
//        // Set Display Layout
//        blackPaint.setStrokeWidth(WALL_THICKNESS);
        whitePaint.setTextSize(15);
//
//        // Starting shown obstacle on screen
//        for(int i = 0; i < 5;i++){
//            obstacleViewArray.add(new ObstacleView(i+6,5, (i+1)));
//        }
//
//        // Display Maze
//        createMaze();


        CELL_SIZE = Math.abs(getWidth() / NUM_COLUMNS - 10);
        originalScale = getWidth() / NUM_COLUMNS - 5f;

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        arenaGrid = new ArenaGrid(NUM_ROWS, NUM_COLUMNS, arenaGridOffSet);

        for(int i = 0; i < obstacleCount; i++){
            initialObstacleCanvasPos[i] = new Vector2D(60f*CELL_SIZE, (i*5+6f)*CELL_SIZE);
        }

        if(arenaFragment.arenaPersistentData.getObstaclesData() != null){
            obsArray = arenaFragment.arenaPersistentData.getObstaclesData();
        }else{
            obsArray = new Obstacles(initialObstacleCanvasPos);
            savePersistentData();
        }

        if(arenaFragment.arenaPersistentData.getRobotCarData() != null){
            robotCar = arenaFragment.arenaPersistentData.getRobotCarData();
        }else{
            robotCar = new RobotCar(new Vector2D(1, 1), RobotCar.Direction.NORTH);
        }

        obstacleDirectionButtons = new ObstacleDirectionButtons();

        invalidate();
    }

    private void savePersistentData(){
        arenaFragment.arenaPersistentData.saveData(obsArray, robotCar);
    }

    public void ResetArenaView()
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

        drawGrid(canvas);
        drawObstacles(canvas);
        drawObstacleDirectionFaces(canvas);
        DisplayRobot(canvas);

        if(isDirectionChoosing)
            drawObstacleDirectionButtons(canvas);


        canvas.restore();

//
//        // Create Start Zone of Robot Position
//        // Note: Position Bottom Left = [0][0] and Robot Foot Print= [3][3]
//        drawStartZone(canvas);
//
//        // Display Route Path
//        drawRoutePath(canvas);


//
//        // Display Explored Maze
//        //drawExploredObstacles(canvas);
//
//        // Display Recognized Image from Image Recognition
//        displayImageIdentified(canvas);
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
    public void MoveRobot(MoveArrow move){
        boolean robotMoved = false;
        switch(move){
            case UP:
                if(robotCar.robotPosition.y + 2 < NUM_ROWS){
                    robotCar.robotPosition.y += 1;
                    robotMoved = true;
                }
                break;
            case DOWN:
                if(robotCar.robotPosition.y - 1 > 0){
                    robotCar.robotPosition.y -= 1;
                    robotMoved = true;
                }
                break;
            case LEFT:
                if(robotCar.robotPosition.x - 1 > 0){
                    robotCar.robotPosition.x -= 1;
                    robotMoved = true;
                }
                break;
            case RIGHT:
                if(robotCar.robotPosition.x + 2 < NUM_COLUMNS){
                    robotCar.robotPosition.x += 1;
                    robotMoved = true;
                }
                break;
        }
        if(robotMoved){
            arenaFragment.sendRobotMovement(move);
        }
        invalidate();
    }

    private void DisplayRobot(Canvas canvas){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.driverless_car5);
        Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, (int)CELL_SIZE * 3, (int)CELL_SIZE * 3, false);
        Matrix m = new Matrix();
        m.setRotate(robotCar.robotOrientationAngle, resizeBitmap.getWidth(), resizeBitmap.getHeight());
        switch((int) robotCar.robotOrientationAngle){
            case 0:
                m.postTranslate((robotCar.robotPosition.x) * CELL_SIZE,
                        (NUM_ROWS - robotCar.robotPosition.y - 1) * CELL_SIZE);
                break;
            case 90:
                m.postTranslate((robotCar.robotPosition.x - 4) * CELL_SIZE,
                        (NUM_ROWS - robotCar.robotPosition.y - 2) * CELL_SIZE);
                break;
            case 180:
                m.postTranslate((robotCar.robotPosition.x - 4) * CELL_SIZE,
                        (NUM_ROWS - robotCar.robotPosition.y - 5) * CELL_SIZE);
                break;
            case 270:
                m.postTranslate((robotCar.robotPosition.x - 1) * CELL_SIZE,
                        (NUM_ROWS - robotCar.robotPosition.y - 5) * CELL_SIZE);
                break;
        }
        canvas.drawBitmap(resizeBitmap, m, whitePaint);
    }

    private void ViewMovement(MotionEvent event){
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

    private void DragObstacle(MotionEvent event){

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

                Log.d(TAG, "Finger: " + x + ", " + y);

                // Drag Obstacles Around
                if(obstacleIndexSelected >= 0 && obstacleIndexSelected < obsArray.getObstacleCount()){
                    obsArray.setObstacleCanvasPos(obstacleIndexSelected, new Vector2D(x, y));
                }
                invalidate();

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
                    }else{
                        // Obstacle New Position according to the top left of the cell
                        Vector2D obsNewPos = new Vector2D(arenaGrid.cells[(int) newObsPos.x][(int) newObsPos.y].left,
                                arenaGrid.cells[(int) newObsPos.x][(int) newObsPos.y].top);
                        // Move Obstacle Canvas Position to latest
                        obsArray.setObstacleCanvasPos(obstacleIndexSelected, new Vector2D(
                                obsNewPos.x,
                                obsNewPos.y
                        ));
                        obsArray.setObstaclePos(obstacleIndexSelected, arenaGrid.cells[(int) newObsPos.x][(int) newObsPos.y].position);
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

    private void ChooseObstacleDirection(MotionEvent event){
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
            ChooseObstacleDirection(event);
        if(!isDirectionChoosing && directionUI_BufferLock){
            directionUI_BufferLock = false;
            return true;
        }
        if(!isDirectionChoosing){
            ViewMovement(event);
            DragObstacle(event);
        }
        return true;
//        // Place Robot Position
//        if (!mapFragment.getEnablePlotRobotPosition()) {
//            int x = 0;
//            int y = 0;
//
//            // Able to drag obstacles around
//            if(event.getAction() == MotionEvent.ACTION_MOVE){
//                x = (int) (event.getX() / cellWidth);
//                y = NUM_ROWS - 1 - (int) (event.getY() / cellHeight);
//                obstacleViewArray.get(currentSelectedObstacle).x = x;
//                obstacleViewArray.get(currentSelectedObstacle).y = y;
//                mapFragment.updateObstacleTextView(currentSelectedObstacle);
//                invalidate();
//            }
//            // On release to finalize obstacle position
//            if(event.getAction() == MotionEvent.ACTION_UP){
//                x = obstacleViewArray.get(currentSelectedObstacle).x;
//                y = obstacleViewArray.get(currentSelectedObstacle).y;
//                mapFragment.updateObstacleTextView(currentSelectedObstacle);
//            }
//        } else if (mapFragment.getEnablePlotRobotPosition()) {
//            if(event.getAction() == MotionEvent.ACTION_DOWN){
//                int x = (int) (event.getX() / cellWidth);
//                int y = NUM_ROWS - 1 - (int) (event.getY() / cellHeight);
//
//                if (x == NUM_COLUMNS - 1)
//                    x = NUM_COLUMNS - 2;
//                else if (y == NUM_ROWS - 1)
//                    y = NUM_ROWS - 2;
//
//                if (x == robotCenter[0] && y == robotCenter[1])
//                    updateRobotCoords(-1, -1, angle);
//                else
//                    updateRobotCoords(x, y, angle);
//
//                mapFragment.setRobotPosition(robotCenter, angle);
//                invalidate();
//            }
//        }
        //return true;
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
            canvas.drawText((i+1)+"", i * CELL_SIZE + arenaGridOffSet.x + 5f,
                    arenaGridOffSet.y - 4f, blackPaint);
        }

        for(int i = 0; i < NUM_ROWS; i++){
            canvas.drawText((i+1)+"", arenaGridOffSet.x - 20f,
                    i * CELL_SIZE + arenaGridOffSet.y + 15f, blackPaint);
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
