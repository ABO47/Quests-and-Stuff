package com.abo47.questsandstuff.client.tablet.quest.canvas;

final class CanvasRotationMath {
    private CanvasRotationMath() {
    }

    static int[] rotatedBounds(int x, int y, int width, int height, int rotationDegrees) {
        return rotatedBoundsAtPivot(x, y, width, height, Math.max(1, width) / 2, Math.max(1, height) / 2, rotationDegrees);
    }

    static int[] rotatedBoundsAtPivot(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safePivotX = Math.max(0, Math.min(safeWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeHeight, pivotY));
        double effectivePivotX = effectivePivot(safePivotX, safeWidth);
        double effectivePivotY = effectivePivot(safePivotY, safeHeight);
        int rotation = normalizeDegrees(rotationDegrees);
        if (isCardinalTurn(rotation)) {
            return cardinalRotatedBoundsAtPivot(x, y, safeWidth, safeHeight, effectivePivotX, effectivePivotY, rotation);
        }
        double centerX = x + effectivePivotX;
        double centerY = y + effectivePivotY;
        double radians = Math.toRadians(rotation);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double[][] corners = {
                {-effectivePivotX, -effectivePivotY},
                {safeWidth - effectivePivotX, -effectivePivotY},
                {safeWidth - effectivePivotX, safeHeight - effectivePivotY},
                {-effectivePivotX, safeHeight - effectivePivotY}
        };
        for (double[] corner : corners) {
            double sx = centerX + corner[0] * cos - corner[1] * sin;
            double sy = centerY + corner[0] * sin + corner[1] * cos;
            minX = Math.min(minX, sx);
            minY = Math.min(minY, sy);
            maxX = Math.max(maxX, sx);
            maxY = Math.max(maxY, sy);
        }
        return new int[]{
                floorClean(minX),
                floorClean(minY),
                ceilClean(maxX),
                ceilClean(maxY)
        };
    }

    static boolean isQuarterTurn(int rotationDegrees) {
        int normalized = normalizeDegrees(rotationDegrees);
        return normalized == 90 || normalized == 270;
    }

    static boolean isCardinalTurn(int rotationDegrees) {
        int normalized = normalizeDegrees(rotationDegrees);
        return normalized == 0 || normalized == 90 || normalized == 180 || normalized == 270;
    }

    static int normalizeDegrees(int degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    static int scaledPivot(int pivot, int oldSpan, int newSpan) {
        int safeOld = Math.max(1, oldSpan);
        int safeNew = Math.max(1, newSpan);
        int safePivot = Math.max(0, Math.min(safeOld, pivot));
        if (safePivot == safeOld / 2) {
            return safeNew / 2;
        }
        return Math.max(0, Math.min(safeNew, Math.round(safePivot * (float) safeNew / (float) safeOld)));
    }

    static double effectivePivot(int pivot, int span) {
        int safeSpan = Math.max(1, span);
        int safePivot = Math.max(0, Math.min(safeSpan, pivot));
        return safePivot == safeSpan / 2 ? safeSpan / 2.0D : safePivot;
    }

    static double[] rotatedCorner(
            int x,
            int y,
            int width,
            int height,
            int pivotX,
            int pivotY,
            int rotationDegrees,
            int cornerX,
            int cornerY
    ) {
        int sx = cornerX < 0 ? -1 : 1;
        int sy = cornerY < 0 ? -1 : 1;
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safePivotX = Math.max(0, Math.min(safeWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeHeight, pivotY));
        double effectivePivotX = effectivePivot(safePivotX, safeWidth);
        double effectivePivotY = effectivePivot(safePivotY, safeHeight);
        double localX = sx >= 0 ? safeWidth - effectivePivotX : -effectivePivotX;
        double localY = sy >= 0 ? safeHeight - effectivePivotY : -effectivePivotY;
        int rotation = normalizeDegrees(rotationDegrees);
        if (isCardinalTurn(rotation)) {
            double[] rotated = rotateCardinalLocal(localX, localY, rotation);
            return new double[]{
                    x + effectivePivotX + rotated[0],
                    y + effectivePivotY + rotated[1]
            };
        }
        double radians = Math.toRadians(rotation);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double pivotWorldX = x + effectivePivotX;
        double pivotWorldY = y + effectivePivotY;
        return new double[]{
                pivotWorldX + localX * cos - localY * sin,
                pivotWorldY + localX * sin + localY * cos
        };
    }

    private static int[] cardinalRotatedBoundsAtPivot(
            int x,
            int y,
            int width,
            int height,
            double effectivePivotX,
            double effectivePivotY,
            int rotation
    ) {
        double centerX = x + effectivePivotX;
        double centerY = y + effectivePivotY;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double[][] corners = {
                {-effectivePivotX, -effectivePivotY},
                {width - effectivePivotX, -effectivePivotY},
                {width - effectivePivotX, height - effectivePivotY},
                {-effectivePivotX, height - effectivePivotY}
        };
        for (double[] corner : corners) {
            double[] rotated = rotateCardinalLocal(corner[0], corner[1], rotation);
            double sx = centerX + rotated[0];
            double sy = centerY + rotated[1];
            minX = Math.min(minX, sx);
            minY = Math.min(minY, sy);
            maxX = Math.max(maxX, sx);
            maxY = Math.max(maxY, sy);
        }
        return new int[]{
                floorClean(minX),
                floorClean(minY),
                ceilClean(maxX),
                ceilClean(maxY)
        };
    }

    private static double[] rotateCardinalLocal(double x, double y, int rotation) {
        return switch (rotation) {
            case 90 -> new double[]{-y, x};
            case 180 -> new double[]{-x, -y};
            case 270 -> new double[]{y, -x};
            default -> new double[]{x, y};
        };
    }

    private static int floorClean(double value) {
        double nearest = Math.rint(value);
        if (Math.abs(value - nearest) < 1.0E-7D) {
            return (int) nearest;
        }
        return (int) Math.floor(value);
    }

    private static int ceilClean(double value) {
        double nearest = Math.rint(value);
        if (Math.abs(value - nearest) < 1.0E-7D) {
            return (int) nearest;
        }
        return (int) Math.ceil(value);
    }
}
