package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsRootWidget;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

public final class SkinEditManager {
    private SkinEditManager() {
    }

    private record CapturedOriginal(String targetKey, IGuiTexture original) {
    }

    private static final IdentityHashMap<Widget, CapturedOriginal> ORIGINAL_BACKGROUNDS = new IdentityHashMap<>();

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
        if (state.root.skinFillOverrides.isEmpty()) {
            resetAllCaptured();
            return;
        }
        Set<String> activeTargets = new HashSet<>();
        for (var entry : state.root.skinFillOverrides.entrySet()) {
            String targetKey = bareTargetFor(state, entry.getKey());
            if (targetKey != null) activeTargets.add(targetKey);
        }
        resetRemovedTargets(activeTargets);
        for (var entry : state.root.skinFillOverrides.entrySet()) {
            String entryKey = entry.getKey();

            String targetKey = bareTargetFor(state, entryKey);
            if (targetKey == null) continue;
            if ("root".equals(targetKey)) continue;
            if (SkinOverrideKey.isSharedKey(targetKey) && !SkinOverrideKey.isSharedKey(entryKey)) continue;

            Widget w = SkinEditTargetResolver.widgetForKey(root, targetKey);
            if (w == null) {
                QuestsAndStuffMod.debugLog("[QnS:Skin] reapply SKIP widget not found: target={}, entryKey={}", targetKey, entryKey);
                continue;
            }
            SkinFillOverride override = SkinFillOverride.parse(entry.getValue());
            if (override == null) continue;
            IGuiTexture tex = override.createTexture();
            if (tex == null) continue;

            QuestsAndStuffMod.debugLog("[QnS:Skin] reapply target={}, mode={}, texClass={}, shared={}, card={}, root={}",
                    targetKey, override.mode(), tex.getClass().getSimpleName(),
                    SkinOverrideKey.isSharedKey(targetKey), SkinOverrideKey.isCardKey(targetKey), SkinOverrideKey.isRootKey(targetKey));

            if (SkinOverrideKey.isSharedKey(targetKey) && w instanceof WidgetGroup wg) {
                if (SkinOverrideKey.isRootKey(targetKey)) {
                    applyToWidget(w, targetKey, tex);
                } else if (!SkinOverrideKey.isCardKey(targetKey)) {
                    for (Widget child : wg.widgets) {
                        if (!SkinEditTargetResolver.hasCustomChrome(child)) {
                            if ("settings_tab_layer".equals(targetKey) && child instanceof WidgetGroup tabContainer && !tabContainer.widgets.isEmpty()) {
                                applyToWidget(tabContainer.widgets.get(0), targetKey, tex);
                            } else {
                                applyToWidget(child, targetKey, tex);
                            }
                        }
                    }
                }
            } else {
                applyToWidget(w, targetKey, tex);
            }
            state.root.activeSkinTargets.add(targetKey);
        }
    }

    private static String bareTargetFor(TabletUiState state, String entryKey) {
        if (entryKey.contains(":")) {
            String appPrefix = state.root.currentApp.isBlank() ? "" : state.root.currentApp + ":";
            if (!entryKey.startsWith(appPrefix)) return null;
            return entryKey.substring(entryKey.indexOf(':') + 1);
        }
        return entryKey;
    }

    private static void applyToWidget(Widget w, String targetKey, IGuiTexture tex) {
        CapturedOriginal cap = ORIGINAL_BACKGROUNDS.computeIfAbsent(w, k -> new CapturedOriginal(targetKey, w.getBackgroundTexture()));
        int[] offsets = skinExtendOffsets(w, targetKey, w.getBackgroundTexture());
        if (offsets != null) {
            IGuiTexture inner = tex;
            int dx = offsets[0];
            int dy = offsets[1];
            int dw = offsets[2];
            int dh = offsets[3];
            tex = new TransformTexture() {
                {
                    xOffset = dx;
                    yOffset = dy;
                }
                @Override
                protected void drawInternal(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
                    inner.draw(graphics, mouseX, mouseY, x, y, width + dw, height + dh);
                }
            };
        }

        if (tex instanceof DynamicClippingTexture dct) {
            dct.setReferenceSize(w.getSizeWidth(), w.getSizeHeight());
        }

        w.setBackground(tex);
        w.setDrawBackgroundWhenHover(true);
    }

    private static int[] skinExtendOffsets(Widget w, String targetKey, IGuiTexture original) {
        if (original instanceof GuiTextureGroup) {
            return new int[]{-1, -1, 2, 2};
        }
        if (original == null || original.equals(IGuiTexture.EMPTY)) {
            if ("quests_task_cards".equals(targetKey) || "quests_reward_cards".equals(targetKey)
                    || "settings_option_cards".equals(targetKey)
                    || "teams_member_cards".equals(targetKey)) {
                return new int[]{-1, -1, 2, 2};
            }
            Class<?> cls = w.getClass();
            if (cls == TabletIconTextButton.class || cls == QuestDetailsRootWidget.class) {
                return new int[]{-1, -1, 2, 2};
            }
        }
        return null;
    }

    private static void resetRemovedTargets(Set<String> activeTargets) {
        ORIGINAL_BACKGROUNDS.entrySet().removeIf(entry -> {
            if (!activeTargets.contains(entry.getValue().targetKey())) {
                Widget w = entry.getKey();
                if (w != null) {
                    w.setBackground(entry.getValue().original());
                }
                return true;
            }
            return false;
        });
    }

    private static void resetAllCaptured() {
        for (var entry : ORIGINAL_BACKGROUNDS.entrySet()) {
            Widget w = entry.getKey();
            if (w != null) {
                w.setBackground(entry.getValue().original());
            }
        }
        ORIGINAL_BACKGROUNDS.clear();
    }

    public static void restoreOverride(TabletUiState state, WidgetGroup root, String targetKey) {
        if (targetKey == null) return;
        String bare = SkinOverrideKey.resolveTargetKey(state, targetKey);
        ORIGINAL_BACKGROUNDS.entrySet().removeIf(entry -> {
            if (entry.getValue().targetKey().equals(bare)) {
                Widget w = entry.getKey();
                if (w != null) {
                    w.setBackground(entry.getValue().original());
                }
                return true;
            }
            return false;
        });
        reapplyOverrides(state, root);
    }

    public static Widget findWidgetByKey(WidgetGroup root, String targetKey) {
        return SkinEditTargetResolver.widgetForKey(root, targetKey);
    }

    private static String resolveSkinTarget(TabletUiState state, TabletRootWidget root, String targetKey) {
        String resolved = SkinOverrideKey.resolveTargetKey(state, targetKey);
        if (!SkinOverrideKey.isSharedKey(resolved)) {
            Widget targetWidget = SkinEditTargetResolver.widgetForKey(root, targetKey);
            if (targetWidget != null) {
                String selfKey = SkinAnchorRegistry.keyFor(targetWidget);
                if (selfKey == null) {
                    String containerKey = SkinEditTargetResolver.resolveSharedKey(targetWidget);
                    if (containerKey != null) resolved = containerKey;
                }
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
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: stretch, asset={}", currentAsset);
                    root.closeContextMenu();
                    setFillMode(state, resolvedTarget, "stretch", currentAsset, root, refresher);
                }));
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_tile"),
                "grid",
                currentMode.equals("tile") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: tile, asset={}", currentAsset);
                    root.closeContextMenu();
                    setFillMode(state, resolvedTarget, "tile", currentAsset, root, refresher);
                }));
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_center"),
                "center_focus",
                currentMode.equals("center") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: center, asset={}", currentAsset);
                    root.closeContextMenu();
                    setFillMode(state, resolvedTarget, "center", currentAsset, root, refresher);
                }));
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_dynamic"),
                "dynamic",
                currentMode.equals("dynamic") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: dynamic, asset={}", currentAsset);
                    root.closeContextMenu();
                    setFillMode(state, resolvedTarget, "dynamic", currentAsset, root, refresher);
                }));
        actions.add(ContextActionFactory.submenu(
                TabletTranslationKeys.text("ui.questsandstuff.skin.change_mode"),
                "style",
                TabletColors.TEXT_PRIMARY,
                modeActions));

        if (rawOverride != null && !rawOverride.isBlank()) {
            String skinTexKey = "skin_remove_tex:" + resolvedTarget;
            actions.add(ContextActionFactory.warningDelete(state, skinTexKey,
                    TabletTranslationKeys.text("ui.questsandstuff.skin.remove_texture"),
                    () -> {
                        root.closeContextMenu();
                        String qualified = SkinOverrideKey.overrideKey(state, resolvedTarget);
                        state.root.skinFillOverrides.remove(qualified);
                        String bare = SkinOverrideKey.resolveTargetKey(state, resolvedTarget);
                        if (!bare.equals(qualified)) {
                            state.root.skinFillOverrides.remove(bare);
                        }
                        restoreOverride(state, root, resolvedTarget);
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
                ContextMenuPanel.build(px, py, menuW, actions, 0, actions.size(), TabletColors.BORDER_BASE, state, a -> {
                    if (root.isContextMenuOpen()) {
                        buildContextMenu(state, root, refresher, mouseX, mouseY);
                    }
                }),
                px, py, menuW, menuH
        );
    }

    private static void setFillMode(TabletUiState state, String targetKey, String mode, String asset, WidgetGroup root, Runnable refresher) {
        if (asset == null || asset.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] setFillMode ABORTED: asset is blank, target={}, mode={}", targetKey, mode);
            return;
        }
        String entryKey = SkinOverrideKey.isSharedKey(targetKey) ? targetKey : (state.root.currentApp.isBlank() ? targetKey : state.root.currentApp + ":" + targetKey);
        SkinFillOverride override = new SkinFillOverride(mode, asset);
        String encoded = override.encode();
        state.root.skinFillOverrides.put(entryKey, encoded);
        state.root.activeSkinTargets.add(targetKey);
        SkinFillOverride.clearCache();
        QuestsAndStuffMod.debugLog("[QnS:Skin] setFillMode: target={}, mode={}, asset={}, entryKey={}, encoded={}", targetKey, mode, asset, entryKey, encoded);
        reapplyOverrides(state, root);
        if (refresher != null) refresher.run();
        TabletUiFactory.persistSkinState(state);
    }
}
