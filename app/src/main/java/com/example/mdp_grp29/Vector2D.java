package com.example.mdp_grp29;

public class Vector2D {
    public float x, y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void Reset() {
        x = y = 0;
    }

    public String print() {
        return "X: " + x + ", Y: " + y;
    }
}
