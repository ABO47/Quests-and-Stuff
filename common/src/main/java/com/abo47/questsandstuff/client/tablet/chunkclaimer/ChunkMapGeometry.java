package com.abo47.questsandstuff.client.tablet.chunkclaimer;

public final class ChunkMapGeometry {
    private ChunkMapGeometry() {
    }

    public static int cellSize(int mapW, int mapH, int gridW, int gridH) {
        int cw = gridW > 0 ? mapW / gridW : mapW;
        int ch = gridH > 0 ? mapH / gridH : mapH;
        return Math.max(1, Math.min(cw, ch));
    }

    public static int gridOriginX(int mapW, int cell, int gridW) {
        return Math.max(0, (mapW - cell * gridW) / 2);
    }

    public static int gridOriginY(int mapH, int cell, int gridH) {
        return Math.max(0, (mapH - cell * gridH) / 2);
    }

    public static int cellPixelX(int originX, int cell, int gridW, int dx) {
        return originX + (dx + gridW / 2) * cell;
    }

    public static int cellPixelY(int originY, int cell, int gridH, int dz) {
        return originY + (dz + gridH / 2) * cell;
    }

    public static int deltaX(int localX, int originX, int cell, int gridW) {
        return (localX - originX) / cell - gridW / 2;
    }

    public static int deltaZ(int localY, int originY, int cell, int gridH) {
        return (localY - originY) / cell - gridH / 2;
    }

    public static int floorDeltaX(int localX, int originX, int cell, int gridW) {
        return (int) Math.floor((localX - originX) / (double) cell) - gridW / 2;
    }

    public static int floorDeltaZ(int localY, int originY, int cell, int gridH) {
        return (int) Math.floor((localY - originY) / (double) cell) - gridH / 2;
    }

    public static ChunkMapCell cellAt(int localX, int localY, int mapW, int mapH, int gridW, int gridH) {
        int cell = cellSize(mapW, mapH, gridW, gridH);
        int ox = gridOriginX(mapW, cell, gridW);
        int oy = gridOriginY(mapH, cell, gridH);
        int dx = floorDeltaX(localX, ox, cell, gridW);
        int dz = floorDeltaZ(localY, oy, cell, gridH);
        int loX = floorDeltaX(0, ox, cell, gridW);
        int hiX = floorDeltaX(mapW, ox, cell, gridW);
        int loZ = floorDeltaZ(0, oy, cell, gridH);
        int hiZ = floorDeltaZ(mapH, oy, cell, gridH);
        if (dx < loX || dx > hiX || dz < loZ || dz > hiZ) {
            return null;
        }
        return new ChunkMapCell(dx, dz);
    }

    public record ChunkMapCell(int dx, int dz) {
    }

    public static boolean inGridX(int dx, int gridW) {
        int half = gridW / 2;
        return dx >= -half && dx <= half;
    }

    public static boolean inGridZ(int dz, int gridH) {
        int half = gridH / 2;
        return dz >= -half && dz <= half;
    }
}
