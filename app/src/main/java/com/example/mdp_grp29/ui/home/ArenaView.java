package com.example.mdp_grp29.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

public class ArenaView extends View {

    private final String TAG = "ArenaView";

    private final int NUM_COLUMNS = 20;
    private final int NUM_ROWS = 20;
    private float CELL_SIZE;

    private Paint mapPaint;
    private Paint whitePaint;
    private Paint blackPaint;

    private int centerViewX, centerViewY;
    private int viewScale;

    private float zoomScale = 1f;
    private float originalScale;
    private float previousZoomDist = 0f;
    private float currentZoomDist = 0f;
    private Vector2D currentCenter = new Vector2D(0,0);
    private Vector2D previousCenter = new Vector2D(0,0);

    // Constructor
    public ArenaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Create Objects
        mapPaint= new Paint();
        whitePaint= new Paint();
        blackPaint= new Paint();
//        facePaint = new Paint();
//        startPaint= new Paint();
//        robotPaint= new Paint();
//        exploredPaint= new Paint();
//
        // Set Display Colors

        mapPaint.setColor(Color.MAGENTA);
//        robotPaint.setColor(Color.DKGRAY);
        whitePaint.setColor(Color.WHITE);
        blackPaint.setColor(Color.BLACK);
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
        invalidate();
    }


    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        // Calculate the Cell Size based on the canvas
        CELL_SIZE = getWidth() / NUM_COLUMNS;
        originalScale = getWidth() / NUM_COLUMNS;

//        if (cellWidth > cellHeight)
//            cellWidth = cellHeight;
//        else
//            cellHeight = cellWidth;
//
//        cellSize = cellHeight;

        this.setLayoutParams(new RelativeLayout.LayoutParams((int)(CELL_SIZE * NUM_COLUMNS),
                (int)(CELL_SIZE * NUM_ROWS)));
        invalidate();
    }

    // Create Canvas for Maze
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

//        // Create Grid and Grid Cell Border
        drawGrid(canvas);
//
//        // Create Start Zone of Robot Position
//        // Note: Position Bottom Left = [0][0] and Robot Foot Print= [3][3]
//        drawStartZone(canvas);
//
//        // Display Route Path
//        drawRoutePath(canvas);
//
//        // Display Robot Position
//        displayRobot(canvas);
//
//        // Draw Obstacle
//        drawObstacleCoor(canvas);
//
//        // Draw Face Direction on Obstacle
//        drawObstacleAnnotCoor(canvas);
//
//        // Display Explored Maze
//        //drawExploredObstacles(canvas);
//
//        // Display Recognized Image from Image Recognition
//        displayImageIdentified(canvas);
    }

    private class Vector2D{
        public float x,y;
        public Vector2D(float x, float y){
            this.x = x;
            this.y = y;
        }
    }

    private float measureDistanceBetweenTwoPoints(Vector2D point1, Vector2D point2){
        return (float)Math.sqrt(Math.pow((point1.x - point2.x), 2) + Math.pow((point1.y - point2.y), 2));
    }

    private Vector2D measureCenterBetweenTwoPoints(Vector2D point1, Vector2D point2){
        Vector2D centerDist = new Vector2D((point1.x-point2.x)/2, (point1.y-point2.y)/2);
        return new Vector2D(point1.x+centerDist.x, point1.y+centerDist.y);
    }

    private void ViewMovement(MotionEvent event){
        Vector2D previousFirst, previousSecond;
        Vector2D currentFirst, currentSecond;
        int mActivePointerOne;
        int mActivePointerTwo;
        final float distThreshold = 5f;
        final float scaleStep = 2f;

        if(event.getPointerCount() == 2){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mActivePointerOne = event.getPointerId(0);
                    mActivePointerTwo = event.getPointerId(1);

                    previousFirst = new Vector2D(event.getX(mActivePointerOne), event.getY(mActivePointerOne));
                    previousSecond = new Vector2D(event.getX(mActivePointerTwo), event.getY(mActivePointerTwo));
                    // Measure Distance for zooming
                    previousZoomDist = measureDistanceBetweenTwoPoints(previousFirst, previousSecond);
                    // Measure Distance for panning
                    previousCenter = measureCenterBetweenTwoPoints(previousFirst, previousSecond);

                    break;
                case MotionEvent.ACTION_MOVE:

                    mActivePointerOne = event.getPointerId(0);
                    mActivePointerTwo = event.getPointerId(1);

                    //
                    currentFirst = new Vector2D(event.getX(mActivePointerOne), event.getY(mActivePointerOne));
                    currentSecond = new Vector2D(event.getX(mActivePointerTwo), event.getY(mActivePointerTwo));

                    // Perform functionality here

                    currentZoomDist = measureDistanceBetweenTwoPoints(currentFirst, currentSecond);
                    currentCenter = measureCenterBetweenTwoPoints(currentFirst, currentSecond);

                    
                    if(currentZoomDist > previousZoomDist + distThreshold){
                        zoomScale += scaleStep;
                    }else if(currentZoomDist < previousZoomDist - distThreshold){
                        zoomScale -= scaleStep;
                    }else{
                        Log.d(TAG, "Same positions");
                    }

                    


                    // Replace old index coordinates with new ones
                    previousFirst = currentFirst;
                    previousSecond = currentSecond;
                    previousZoomDist = measureDistanceBetweenTwoPoints(previousFirst, previousSecond);

                    invalidate();

                    break;
                case MotionEvent.ACTION_UP:
                    previousZoomDist = 0;
                    currentZoomDist = 0;
                    break;
            }
        }
    }

    private void DragObstacle(MotionEvent event){
        if(event.getPointerCount() == 1){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:

                    break;
                case MotionEvent.ACTION_MOVE:

                    break;
            }
        }
    }

    // C6- Touch Gesture
    public boolean onTouchEvent(MotionEvent event) {
        ViewMovement(event);
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
        return true;
    }

    private void drawGrid(Canvas canvas){
        boolean isWhite = true;
        float left, right, top, bottom;

        CELL_SIZE = originalScale + zoomScale;

        // Draw Grid Squares
        for (int i = 0; i <= NUM_COLUMNS - 1; i++){
            for (int j = 0; j <= NUM_ROWS - 1; j++){
                if(isWhite){
                    canvas.drawRect(i * CELL_SIZE, (NUM_ROWS - 1 - j) * CELL_SIZE,
                            (i + 1) * CELL_SIZE, (NUM_ROWS - j) * CELL_SIZE, whitePaint);
                    isWhite = false;
                }
                else{
                    canvas.drawRect(i * CELL_SIZE, (NUM_ROWS - 1 - j) * CELL_SIZE,
                            (i + 1) * CELL_SIZE, (NUM_ROWS - j) * CELL_SIZE, mapPaint);
                    isWhite = true;
                }
            }
            isWhite = !isWhite;
        }

        // Draw Grid Lines
        for (int c = 0; c < NUM_COLUMNS + 1; c++)
            canvas.drawLine(c * CELL_SIZE, 0, c * CELL_SIZE, NUM_ROWS *
                    CELL_SIZE, blackPaint);
        for (int r = 0; r < NUM_ROWS; r++)
            canvas.drawLine(0, r * CELL_SIZE, NUM_COLUMNS * CELL_SIZE,
                    r * CELL_SIZE, blackPaint);
    }
}
