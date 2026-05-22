package com.abo47.questsandstuff.quest.model.canvas;

public record CanvasImageLayer(String id, String asset, int x, int y, int w, int h, int rotation, int entityYaw, int entitySpinSpeed) {
    public static final int DEFAULT_ENTITY_YAW = 0;
    public static final int DEFAULT_ENTITY_SPIN_SPEED = 60;
    public static final int MIN_ENTITY_SPIN_SPEED = 0;
    public static final int MAX_ENTITY_SPIN_SPEED = 360;

    public CanvasImageLayer {
        id = id == null ? "" : id.trim();
        asset = asset == null ? "" : asset;
        rotation = normalizeDegrees(rotation);
        entityYaw = normalizeDegrees(entityYaw);
        entitySpinSpeed = clamp(entitySpinSpeed, MIN_ENTITY_SPIN_SPEED, MAX_ENTITY_SPIN_SPEED);
    }

    public CanvasImageLayer(String id, String asset, int x, int y, int w, int h, int rotation) {
        this(id, asset, x, y, w, h, rotation, DEFAULT_ENTITY_YAW, DEFAULT_ENTITY_SPIN_SPEED);
    }

    public CanvasImageLayer moveTo(int nextX, int nextY) {
        return new CanvasImageLayer(id, asset, nextX, nextY, w, h, rotation, entityYaw, entitySpinSpeed);
    }

    public CanvasImageLayer resizeTo(int nextW, int nextH) {
        return new CanvasImageLayer(id, asset, x, y, Math.max(8, nextW), Math.max(8, nextH), rotation, entityYaw, entitySpinSpeed);
    }

    public CanvasImageLayer rotateTo(int nextRotation) {
        return new CanvasImageLayer(id, asset, x, y, w, h, nextRotation, entityYaw, entitySpinSpeed);
    }

    public CanvasImageLayer withAsset(String nextAsset) {
        return new CanvasImageLayer(id, nextAsset, x, y, w, h, rotation, entityYaw, entitySpinSpeed);
    }

    public CanvasImageLayer withEntityMotion(int nextYaw, int nextSpinSpeed) {
        return new CanvasImageLayer(id, asset, x, y, w, h, rotation, nextYaw, nextSpinSpeed);
    }

    public static int normalizeDegrees(int degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    public static int clampEntitySpinSpeed(int spinSpeed) {
        return clamp(spinSpeed, MIN_ENTITY_SPIN_SPEED, MAX_ENTITY_SPIN_SPEED);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
