package com.hotlinecornella;

public class PlayerMoveMessage {
    public String type = "playerMove";
    public String id;
    public float x;
    public float y;

    public PlayerMoveMessage(String id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}