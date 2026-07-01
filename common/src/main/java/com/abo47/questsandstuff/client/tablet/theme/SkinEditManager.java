package com.abo47.questsandstuff.client.tablet.theme;

import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuSystem;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SkinEditManager {
    private SkinEditManager() {
    }

    public static boolean handleClick(TabletUiState state, TabletRootWidget root, Runnable refresher, int mouseX, int mouseY, int button) {
        if (state == null || !state.root.skinEditMode) return false;
        if (ModalStateQueries.anyOpen(state)) return false;

        if (root.isContextMenuOpen()) {
            if (root.isContextMenuAt(mouseX, mouseY)) {
                root.clickContextMenu(mouseX, mouseY, button);
                return true;
            }
            root.closeContextMenu();
        }

        String hitKey = SkinEditTargetResolver.findTargetKeyAt(root, mouseX, mouseY);

        if (button == 0) {
            if (hitKey != null) {
                state.root.skinEditSelectedTarget = hitKey;
                return true;
            }
            state.root.skinEditSelectedTarget = "";
            return true;
        }

        if (button == 1) {
            String currentSelection = state.root.skinEditSelectedTarget;
            if (hitKey != null && hitKey.equals(currentSelection)) {
                buildContextMenu(state, root, refresher, mouseX, mouseY);
                return true;
            }
            state.root.skinEditSelectedTarget = "";
            return true;
        }

        return true;
    }

    public static void reapplyOverrides(TabletUiState state, WidgetGroup root) {
        if (state == null || root == null) return;
        if (state.root.skinFillOverrides.isEmpty()) return;
        String appPrefix = state.root.currentApp.isBlank() ? "" : state.root.currentApp + ":";
        for (Map.Entry<String, String> entry : state.root.skinFillOverrides.entrySet()) {
            String key = entry.getKey();

            if (key.contains(":") && !key.startsWith(appPrefix)) continue;

            String targetKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;

            if ("root".equals(targetKey)) continue;

            Widget w = SkinEditTargetResolver.widgetForKey(root, targetKey);
            if (w == null) continue;
            SkinFillOverride override = SkinFillOverride.parse(entry.getValue());
            if (override == null) continue;
            IGuiTexture tex = override.createTexture();
            if (tex != null) {
                w.setBackground(tex);
            }
        }
    }

    public static Widget findWidgetByKey(WidgetGroup root, String targetKey) {
        return SkinEditTargetResolver.widgetForKey(root, targetKey);
    }

    private static void buildContextMenu(TabletUiState state, TabletRootWidget root, Runnable refresher, int mouseX, int mouseY) {
        String targetKey = state.root.skinEditSelectedTarget;
        if (targetKey == null || targetKey.isBlank()) return;

        String app = state.root.currentApp;
        String overrideKey = app.isBlank() ? targetKey : app + ":" + targetKey;
        String rawOverride = state.root.skinFillOverrides.get(overrideKey);
        if (rawOverride == null) {
            rawOverride = state.root.skinFillOverrides.get(targetKey);
        }
        SkinFillOverride currentOverride = SkinFillOverride.parse(rawOverride);
        String currentMode = currentOverride != null ? currentOverride.mode() : "stretch";
        String currentAsset = currentOverride != null ? currentOverride.path() : "";

        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActions.action(
                TabletVocabulary.text("ui.questsandstuff.skin.change_texture"),
                "image",
                ModColors.INTERACTIVE,
                () -> {
                    root.closeContextMenu();
                    state.modal.skinEditFillTarget = targetKey;
                    ModalOpenActions.openAssetPicker(state, targetKey, currentAsset);
                    if (refresher != null) refresher.run();
                }));

        List<ContextAction> modeActions = new ArrayList<>();
        modeActions.add(ContextActions.action(
                TabletVocabulary.text("ui.questsandstuff.skin.mode_stretch"),
                "size",
                currentMode.equals("stretch") ? ModColors.SUCCESS : ModColors.TEXT_SECONDARY,
                () -> {
                    root.closeContextMenu();
                    setFillMode(state, targetKey, "stretch", currentAsset, root, refresher);
                }));
        modeActions.add(ContextActions.action(
                TabletVocabulary.text("ui.questsandstuff.skin.mode_tile"),
                "grid",
                currentMode.equals("tile") ? ModColors.SUCCESS : ModColors.TEXT_SECONDARY,
                () -> {
                    root.closeContextMenu();
                    setFillMode(state, targetKey, "tile", currentAsset, root, refresher);
                }));
        actions.add(ContextActions.submenu(
                TabletVocabulary.text("ui.questsandstuff.skin.change_mode"),
                "style",
                ModColors.TEXT_PRIMARY,
                modeActions));

        String removeKey = overrideKey;
        if (rawOverride == null || rawOverride.isBlank()) {
            removeKey = targetKey;
        }
        if (rawOverride != null && !rawOverride.isBlank()) {
            final String removalTarget = removeKey;
            actions.add(ContextActions.action(
                    TabletVocabulary.text("ui.questsandstuff.skin.remove_texture"),
                    "delete",
                    ModColors.ERROR,
                    () -> {
                        root.closeContextMenu();
                        state.root.skinFillOverrides.remove(removalTarget);
                        state.root.skinFillOverrides.remove(targetKey);
                        reapplyOverrides(state, root);
                        if (refresher != null) refresher.run();
                        TabletUiFactory.persistSkinState(state);
                    }));
        }

        List<String> labels = new ArrayList<>();
        for (ContextAction a : actions) labels.add(a.label());
        int menuW = ContextMenuSystem.preferredMenuWidth(labels, 90, 120);
        int menuH = ContextMenuPanel.heightFor(actions, actions.size());

        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int px = ContextMenuPlacement.fitRightOrLeft(mouseX, screenW, menuW);
        int py = ContextMenuPlacement.fitBelowOrAbove(mouseY, screenH, menuH);

        root.setContextMenu(
                ContextMenuPanel.build(px, py, menuW, actions, 0, actions.size(), ModColors.BORDER_BASE, state, a -> {}),
                px, py, menuW, menuH
        );
    }

    private static void setFillMode(TabletUiState state, String targetKey, String mode, String asset, WidgetGroup root, Runnable refresher) {
        if (asset == null || asset.isBlank()) return;
        String app = state.root.currentApp;
        String entryKey = app.isBlank() ? targetKey : app + ":" + targetKey;
        SkinFillOverride override = new SkinFillOverride(mode, asset);
        state.root.skinFillOverrides.put(entryKey, override.encode());
        reapplyOverrides(state, root);
        if (refresher != null) refresher.run();
        TabletUiFactory.persistSkinState(state);
    }
}
