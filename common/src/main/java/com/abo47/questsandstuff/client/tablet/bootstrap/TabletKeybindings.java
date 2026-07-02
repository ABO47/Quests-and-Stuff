package com.abo47.questsandstuff.client.tablet.bootstrap;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public final class TabletKeybindings {
    private static final String CATEGORY = "key.categories.questsandstuff";

    public static final KeyMapping OPEN_UI = new KeyMapping(
            "key.questsandstuff.open_ui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    public static final KeyMapping OPEN_QUESTS_UI = new KeyMapping(
            "key.questsandstuff.open_quests_ui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping QUICK_CONNECT = new KeyMapping(
            "key.questsandstuff.quick_connect",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    private static final KeyMapping RENAME_SELECTED = new KeyMapping(
            "key.questsandstuff.rename_selected",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F3,
            CATEGORY
    );
    public static final KeyMapping EDIT_HUD = new KeyMapping(
            "key.questsandstuff.edit_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping GIZMO_MOVE = new KeyMapping(
            "key.questsandstuff.gizmo_move",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_W,
            CATEGORY
    );
    private static final KeyMapping GIZMO_RESIZE = new KeyMapping(
            "key.questsandstuff.gizmo_resize",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY
    );
    private static final KeyMapping GIZMO_ROTATE = new KeyMapping(
            "key.questsandstuff.gizmo_rotate",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    private static final KeyMapping TOGGLE_SKIN_EDIT = new KeyMapping(
            "key.questsandstuff.toggle_skin_edit",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );

    private TabletKeybindings() {
    }

    public static void registerKeyMappings(Consumer<KeyMapping> registrar) {
        registrar.accept(OPEN_UI);
        registrar.accept(OPEN_QUESTS_UI);
        registrar.accept(QUICK_CONNECT);
        registrar.accept(RENAME_SELECTED);
        registrar.accept(EDIT_HUD);
        registrar.accept(GIZMO_MOVE);
        registrar.accept(GIZMO_RESIZE);
        registrar.accept(GIZMO_ROTATE);
        registrar.accept(TOGGLE_SKIN_EDIT);
    }

    public static boolean quickConnectDown() {
        return QUICK_CONNECT.isDown();
    }

    public static boolean quickConnectMatches(int keyCode, int scanCode) {
        return QUICK_CONNECT.matches(keyCode, scanCode);
    }

    public static boolean renameSelectedMatches(int keyCode, int scanCode) {
        return RENAME_SELECTED.matches(keyCode, scanCode);
    }

    public static boolean openUiMatches(int keyCode, int scanCode) {
        return OPEN_UI.matches(keyCode, scanCode);
    }

    public static boolean openQuestsUiMatches(int keyCode, int scanCode) {
        return OPEN_QUESTS_UI.matches(keyCode, scanCode);
    }

    public static boolean gizmoMoveMatches(int keyCode, int scanCode) {
        return GIZMO_MOVE.matches(keyCode, scanCode);
    }

    public static boolean gizmoResizeMatches(int keyCode, int scanCode) {
        return GIZMO_RESIZE.matches(keyCode, scanCode);
    }

    public static boolean gizmoRotateMatches(int keyCode, int scanCode) {
        return GIZMO_ROTATE.matches(keyCode, scanCode);
    }

    public static boolean toggleSkinEditMatches(int keyCode, int scanCode) {
        return TOGGLE_SKIN_EDIT.matches(keyCode, scanCode);
    }
}
