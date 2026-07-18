package com.abo47.questsandstuff.client.tablet.layout;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;

public final class TabletResizeCursor {
    private static long handle;
    private static boolean active;

    private TabletResizeCursor() {
    }

    public static void update(boolean nextActive) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return;
        }
        long window = minecraft.getWindow().getWindow();
        if (window == 0L) {
            return;
        }
        if (nextActive) {
            if (handle == 0L) {
                handle = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);
            }
            if (handle != 0L && !active) {
                GLFW.glfwSetCursor(window, handle);
                active = true;
            }
            return;
        }
        if (active) {
            GLFW.glfwSetCursor(window, 0L);
            active = false;
        }
    }
}
