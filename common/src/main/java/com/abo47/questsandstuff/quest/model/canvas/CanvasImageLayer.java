package com.abo47.questsandstuff.quest.model.canvas;







import static com.abo47.questsandstuff.util.MathUtils.clamp;

public record CanvasImageLayer(String id, String asset, int x, int y, int w, int h, int rotation, int entityYaw, int entitySpinSpeed, int modelPitch, int pivotX, int pivotY) {
    public static final int DEFAULT_ENTITY_YAW = 0;
    public static final int DEFAULT_ENTITY_SPIN_SPEED = 60;
    public static final int DEFAULT_MODEL_PITCH = 0;
    public static final int MIN_ENTITY_SPIN_SPEED = 0;
    public static final int MAX_ENTITY_SPIN_SPEED = 360;

    public CanvasImageLayer {
        id = id == null ? "" : id.trim();
        asset = asset == null ? "" : asset;
        rotation = normalizeDegrees(rotation);
        entityYaw = normalizeDegrees(entityYaw);
        entitySpinSpeed = clamp(entitySpinSpeed, MIN_ENTITY_SPIN_SPEED, MAX_ENTITY_SPIN_SPEED);
        modelPitch = normalizeDegrees(modelPitch);
        pivotX = clamp(pivotX, 0, Math.max(1, w));
        pivotY = clamp(pivotY, 0, Math.max(1, h));
    }

    public CanvasImageLayer(String id, String asset, int x, int y, int w, int h, int rotation, int entityYaw, int entitySpinSpeed, int modelPitch) {
        this(id, asset, x, y, w, h, rotation, entityYaw, entitySpinSpeed, modelPitch, centerPivot(w), centerPivot(h));
    }

    public CanvasImageLayer(String id, String asset, int x, int y, int w, int h, int rotation, int entityYaw, int entitySpinSpeed) {
        this(id, asset, x, y, w, h, rotation, entityYaw, entitySpinSpeed, DEFAULT_MODEL_PITCH);
    }

    public CanvasImageLayer(String id, String asset, int x, int y, int w, int h, int rotation) {
        this(id, asset, x, y, w, h, rotation, DEFAULT_ENTITY_YAW, DEFAULT_ENTITY_SPIN_SPEED, DEFAULT_MODEL_PITCH);
    }

    public CanvasImageLayer moveTo(int nextX, int nextY) {
        return new CanvasImageLayer(id, asset, nextX, nextY, w, h, rotation, entityYaw, entitySpinSpeed, modelPitch, pivotX, pivotY);
    }

    public CanvasImageLayer resizeTo(int nextW, int nextH) {
        int safeW = Math.max(8, nextW);
        int safeH = Math.max(8, nextH);
        return new CanvasImageLayer(id, asset, x, y, safeW, safeH, rotation, entityYaw, entitySpinSpeed, modelPitch, scalePivot(pivotX, w, safeW), scalePivot(pivotY, h, safeH));
    }

    public CanvasImageLayer withBounds(int nextX, int nextY, int nextW, int nextH) {
        int safeW = Math.max(8, nextW);
        int safeH = Math.max(8, nextH);
        return new CanvasImageLayer(id, asset, nextX, nextY, safeW, safeH, rotation, entityYaw, entitySpinSpeed, modelPitch, scalePivot(pivotX, w, safeW), scalePivot(pivotY, h, safeH));
    }

    public CanvasImageLayer rotateTo(int nextRotation) {
        return new CanvasImageLayer(id, asset, x, y, w, h, nextRotation, entityYaw, entitySpinSpeed, modelPitch, pivotX, pivotY);
    }

    public CanvasImageLayer withAsset(String nextAsset) {
        return new CanvasImageLayer(id, nextAsset, x, y, w, h, rotation, entityYaw, entitySpinSpeed, modelPitch, pivotX, pivotY);
    }

    public CanvasImageLayer withEntityMotion(int nextYaw, int nextSpinSpeed) {
        return new CanvasImageLayer(id, asset, x, y, w, h, rotation, nextYaw, nextSpinSpeed, modelPitch, pivotX, pivotY);
    }

    public CanvasImageLayer withModelRotation(int nextYaw, int nextPitch) {
        return new CanvasImageLayer(id, asset, x, y, w, h, rotation, nextYaw, entitySpinSpeed, nextPitch, pivotX, pivotY);
    }

    public CanvasImageLayer withCenteredPivot() {
        return new CanvasImageLayer(id, asset, x, y, w, h, rotation, entityYaw, entitySpinSpeed, modelPitch, centerPivot(w), centerPivot(h));
    }

    public boolean hasCenteredPivot() {
        return pivotX == centerPivot(w) && pivotY == centerPivot(h);
    }

    public static int normalizeDegrees(int degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    public static int clampEntitySpinSpeed(int spinSpeed) {
        return clamp(spinSpeed, MIN_ENTITY_SPIN_SPEED, MAX_ENTITY_SPIN_SPEED);
    }

    private static int centerPivot(int span) {
        return Math.max(1, span) / 2;
    }

    private static int scalePivot(int pivot, int oldSpan, int newSpan) {
        if (oldSpan <= 0) {
            return centerPivot(newSpan);
        }
        if (pivot == centerPivot(oldSpan)) {
            return centerPivot(newSpan);
        }
        return Math.round((float) pivot * (float) Math.max(1, newSpan) / (float) oldSpan);
    }
}
