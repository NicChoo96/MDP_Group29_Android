package com.example.mdp_grp29.ui.arena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.example.mdp_grp29.Vector2D;
import com.example.mdp_grp29.arena_objects.Obstacles;

public class ArenaView extends View {

    private final String TAG = "ArenaView";

    private final int NUM_COLUMNS = 20;
    private final int NUM_ROWS = 20;
    private float CELL_SIZE;

    private Paint mapPaint;
    private Paint whitePaint;
    private Paint blackPaint;


    private float zoomScale = 30f;
    private float originalScale;
    private Vector2D minViewPort = new Vector2D(-1f, -1f);
    private Vector2D maxViewPort = new Vector2D(300f, 300f);


    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public Obstacles obsArray;
    public final int obstacleCount = 5;
    private Vector2D[] initialObstaclePos = new Vector2D[obstacleCount];


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




        this.setLayoutParams(new RelativeLayout.LayoutParams((int)(CELL_SIZE * NUM_COLUMNS),
                (int)(CELL_SIZE * NUM_ROWS)));
        CELL_SIZE = getWidth() / NUM_COLUMNS - 10;
        originalScale = getWidth() / NUM_COLUMNS - 5f;

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        for(int i = 0; i < obstacleCount; i++){
            initialObstaclePos[i] = new Vector2D(25f, (i*2) + 6f);
        }
        obsArray = new Obstacles(initialObstaclePos);

        invalidate();
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
        canvas.scale(mScaleFactor, mScaleFactor);

        if (mScaleDetector.isInProgress()) {

            canvas.scale(mScaleFactor, mScaleFactor, mScaleDetector.getFocusX() * 2f, mScaleDetector.getFocusY() * 2f);
        }
        else{
            canvas.scale(mScaleFactor, mScaleFactor);
        }

        canvas.drawColor(Color.WHITE);

//        // Create Grid and Grid Cell Border
        drawGrid(canvas);
        drawObstacles(canvas);

        canvas.restore();

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

    private void ViewMovement(MotionEvent event){
        if(event.getPointerCount() ==  2)
            mScaleDetector.onTouchEvent(event);
    }

    private void DragObstacle(MotionEvent event){
        if(event.getPointerCount() == 1){
            switch(event.getAction()){
                case MotionEvent.ACTION_POINTER_DOWN:

                    break;
                case MotionEvent.ACTION_MOVE:

                    break;
            }
        }
    }

    // C6- Touch Gesture
    public boolean onTouchEvent(MotionEvent event) {
        // Let the ScaleGestureDetector inspect all events.
        ViewMovement(event);
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
        boolean isWhite = true;
        float left, top, right, bottom;
        final Vector2D viewOffSet = new Vector2D(30f, 30f);
        CELL_SIZE = originalScale + zoomScale;

        // Draw Grid Squares
        for (int i = 0; i <= NUM_COLUMNS - 1; i++){
            for (int j = 0; j <= NUM_ROWS - 1; j++){
                left = i * CELL_SIZE + viewOffSet.x;
                top = (NUM_ROWS - 1 - j) * CELL_SIZE + viewOffSet.y;
                right = (i + 1) * CELL_SIZE + viewOffSet.x;
                bottom = (NUM_ROWS - j) * CELL_SIZE + viewOffSet.y;
                if(isWhite){
                    canvas.drawRect(left , top, right, bottom, whitePaint);
                    isWhite = false;
                }
                else{
                    canvas.drawRect(left , top, right, bottom, mapPaint);
                    isWhite = true;
                }
            }
            isWhite = !isWhite;
        }

        for(int i = 0; i < NUM_COLUMNS; i++){
            canvas.drawText((i+1)+"", i * CELL_SIZE + viewOffSet.x + 5f,
                    viewOffSet.y - 4f, blackPaint);
        }

        for(int i = 0; i < NUM_ROWS; i++){
            canvas.drawText((i+1)+"", viewOffSet.x - 20f,
                    i * CELL_SIZE + viewOffSet.y + 15f, blackPaint);
        }
    }
    
    private void drawObstacles(Canvas canvas){
        float left, top, right, bottom;

        for(int i = 0; i < obsArray.obstacles.length; i++){
            left = obsArray.obstacles[i].position.x * CELL_SIZE;
            top = obsArray.obstacles[i].position.y * CELL_SIZE;
            right = (obsArray.obstacles[i].position.x + 1) * CELL_SIZE;
            bottom = (obsArray.obstacles[i].position.y + 1) * CELL_SIZE;

            canvas.drawRect(left , top, right, bottom, blackPaint);
            canvas.drawText(i+1+"", (left + right)/2f - 5f, (top + bottom)/2f + 5f, whitePaint);
        }
    }
}
