package com.example.mdp_grp29.arena_objects;

import com.example.mdp_grp29.Vector2D;

public class ArenaGrid {
    public ArenaCell[][] cells;

    private Vector2D arenaGridOffSet;

    private int row, col;

    public void calculateGrid(float CELL_SIZE){
        for (int x = 0; x < col; x++){
            for (int y = 0; y < row; y++){
                cells[x][y].left = x * CELL_SIZE + arenaGridOffSet.x;
                cells[x][y].top = y * CELL_SIZE + arenaGridOffSet.y;
                cells[x][y].right = (x + 1) * CELL_SIZE + arenaGridOffSet.x;
                cells[x][y].bottom = (y+1) * CELL_SIZE + arenaGridOffSet.y;
            }
        }
    }

    public Vector2D checkGridCollision(Vector2D obsPos, float CELL_SIZE){
        float cellCenter = CELL_SIZE/2;
        for(int x = 0; x < col; x++){
            for(int y = 0; y < row; y++){
                if(obsPos.x + cellCenter >= cells[x][y].left && obsPos.x + cellCenter <= cells[x][y].right &&
                        obsPos.y + cellCenter >= cells[x][y].top && obsPos.y + cellCenter <= cells[x][y].bottom){
                    return new Vector2D(x, y);
                }
            }
        }
        return new Vector2D(-1, -1);
    }

    public ArenaGrid(int row, int col, Vector2D arenaGridOffSet){
        this.row = row;
        this.col = col;
        this.arenaGridOffSet = arenaGridOffSet;
        cells = new ArenaCell[col][row];
        for(int x = 0; x < col; x++){
            for(int y = 0; y < row; y++){
                cells[x][y] = new ArenaCell(new Vector2D(x, row-y-1));
            }
        }
    }

    public class ArenaCell{
        public Vector2D position;
        public float left, right, top, bottom;

        public ArenaCell(Vector2D position){
            this.position = position;
        }
    }


}
