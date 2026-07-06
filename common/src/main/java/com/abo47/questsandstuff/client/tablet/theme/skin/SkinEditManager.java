package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public final class SkinEditManager {
    private SkinEditManager() {
    }

    public static boolean handleClick(TabletUiState state, TabletRootWidget root, Runnable refresher, int mouseX, int mouseY, int button) {
        if (state == null || !state.root.skinEditMode) return false;
        if (ModalStateQueries.anyOpen(state)) {
            if (!state.root.skinEditSelectedTarget.isEmpty()) {
                state.root.skinEditSelectedTarget = "";
                root.closeContextMenu();
            }
            return false;
        }

        if (root.isContextMenuOpen()) {
            if (root.isContextMenuAt(mouseX, mouseY)) {
                root.clickContextMenu(mouseX, mouseY, button);
                return true;
            }
            root.closeContextMenu();
        }

        Widget homeBtn = root.getHomeButton();
        String hitKey;
        if (homeBtn != null && homeBtn.isVisible() && homeBtn.isMouseOverElement(mouseX, mouseY)) {
            hitKey = "home_btn";
        } else {
            hitKey = SkinEditTargetResolver.findTargetKeyAt(root, mouseX, mouseY);
        }

        if (button == 0) {
            state.root.skinEditSelectedTarget = hitKey != null ? hitKey : "";
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
        state.root.activeSkinTargets.clear();
        if (state.root.skinFillOverrides.isEmpty()) return;
        for (var entry : state.root.skinFillOverrides.entrySet()) {
            String entryKey = entry.getKey();

            String targetKey;
            if (entryKey.contains(":")) {
                String appPrefix = state.root.currentApp.isBlank() ? "" : state.root.currentApp + ":";
                if (!entryKey.startsWith(appPrefix)) continue;
                targetKey = entryKey.substring(entryKey.indexOf(':') + 1);
            } else {
                targetKey = entryKey;
            }

            if ("root".equals(targetKey)) continue;
            if (SkinOverrideKey.isSharedKey(targetKey) && !SkinOverrideKey.isSharedKey(entryKey)) continue;

            Widget w = SkinEditTargetResolver.widgetForKey(root, targetKey);
            if (w == null) continue;
            SkinFillOverride override = SkinFillOverride.parse(entry.getValue());
            if (override == null) continue;
            IGuiTexture tex = override.createTexture();
            if (tex == null) continue;

            if (SkinOverrideKey.isSharedKey(targetKey) && w instanceof WidgetGroup wg) {
                if (SkinOverrideKey.isRootKey(targetKey)) {
                    wg.setBackground(tex);
                } else if (!SkinOverrideKey.isCardKey(targetKey)) {
                    for (Widget child : wg.widgets) {
                        if (!SkinEditTargetResolver.hasCustomChrome(child)) {
                            child.setBackground(tex);
                        }
                    }
                }
            } else {
                w.setBackground(tex);
            }
            state.root.activeSkinTargets.add(targetKey);

            if (SkinOverrideKey.hasCanvasBackground(targetKey)) {
                String bgKey = SkinOverrideKey.viewportBackgroundKey(targetKey);
                if (bgKey != null) {
                    Widget bg = SkinAnchorRegistry.findByKey(bgKey);
                    if (bg != null) bg.setBackground(IGuiTexture.EMPTY);
                }
            }
        }
    }

    public static Widget findWidgetByKey(WidgetGroup root, String targetKey) {
        return SkinEditTargetResolver.widgetForKey(root, targetKey);
    }

    private static String resolveSkinTarget(TabletUiState state, TabletRootWidget root, String targetKey) {
        String resolved = SkinOverrideKey.resolveTargetKey(state, targetKey);
        if (!SkinOverrideKey.isSharedKey(resolved)) {
            Widget targetWidget = SkinEditTargetResolver.widgetForKey(root, targetKey);
            if (targetWidget != null) {
                String containerKey = SkinEditTargetResolver.resolveSharedKey(targetWidget);
                if (containerKey != null) resolved = containerKey;
            }
        }
        return resolved;
    }

    private static void buildContextMenu(TabletUiState state, TabletRootWidget root, Runnable refresher, int mouseX, int mouseY) {
        String targetKey = state.root.skinEditSelectedTarget;
        if (targetKey == null || targetKey.isBlank()) return;

        String resolvedTarget = resolveSkinTarget(state, root, targetKey);

        String rawOverride = SkinOverrideKey.resolveOverride(state, resolvedTarget);
        SkinFillOverride currentOverride = SkinFillOverride.parse(rawOverride);
        String currentMode = currentOverride != null ? currentOverride.mode() : "stretch";
        String currentAsset = currentOverride != null ? currentOverride.path() : "";

        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.change_texture"),
                "image",
                TabletColors.INTERACTIVE,
                () -> {
                    root.closeContextMenu();
                    state.modal.skinEditFillTarget = resolvedTarget;
                    ModalOpenActions.openAssetPicker(state, resolvedTarget, currentAsset);
                    if (refresher != null) refresher.run();
                }));

        List<ContextAction> modeActions = new ArrayList<>();
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_stretch"),
                "size",
                currentMode.equals("stretch") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    root.closeContextMenu();
                    setFillMode(state, resolvedTarget, "stretch", currentAsset, root, refresher);
                }));
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_tile"),
                "grid",
                currentMode.equals("tile") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    root.closeContextMenu();
                    setFillMode(state, resolvedTarget, "tile", currentAsset, root, refresher);
                }));
        actions.add(ContextActionFactory.submenu(
                TabletTranslationKeys.text("ui.questsandstuff.skin.change_mode"),
                "style",
                TabletColors.TEXT_PRIMARY,
                modeActions));

        if (rawOverride != null && !rawOverride.isBlank()) {
            actions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.remove_texture"),
                    "delete",
                    TabletColors.ERROR,
                    () -> {
                        root.closeContextMenu();
                        String qualified = SkinOverrideKey.overrideKey(state, resolvedTarget);
                        state.root.skinFillOverrides.remove(qualified);
                        String bare = SkinOverrideKey.resolveTargetKey(state, resolvedTarget);
                        if (!bare.equals(qualified)) {
                            state.root.skinFillOverrides.remove(bare);
                        }
                        if (refresher != null) refresher.run();
                        TabletUiFactory.persistSkinState(state);
                    }));
        }

        List<String> labels = new ArrayList<>();
        for (ContextAction a : actions) labels.add(a.label());
        int menuW = ContextMenuRenderer.preferredMenuWidth(labels, 90, 120);
        int menuH = ContextMenuPanel.heightFor(actions, actions.size());

        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int px = ContextMenuPlacement.fitRightOrLeft(mouseX, screenW, menuW);
        int py = ContextMenuPlacement.fitBelowOrAbove(mouseY, screenH, menuH);

        root.setContextMenu(
                ContextMenuPanel.build(px, py, menuW, actions, 0, actions.size(), TabletColors.BORDER_BASE, state, a -> {}),
                px, py, menuW, menuH
        );
    }

    private static void setFillMode(TabletUiState state, String targetKey, String mode, String asset, WidgetGroup root, Runnable refresher) {
        if (asset == null || asset.isBlank()) return;
        String entryKey = SkinOverrideKey.isSharedKey(targetKey) ? targetKey : (state.root.currentApp.isBlank() ? targetKey : state.root.currentApp + ":" + targetKey);
        SkinFillOverride override = new SkinFillOverride(mode, asset);
        state.root.skinFillOverrides.put(entryKey, override.encode());
        state.root.activeSkinTargets.add(targetKey);
        reapplyOverrides(state, root);
        if (refresher != null) refresher.run();
        TabletUiFactory.persistSkinState(state);
    }
}
