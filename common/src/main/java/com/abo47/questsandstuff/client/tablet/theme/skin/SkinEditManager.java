package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.controls.FourFieldEditor;
import com.abo47.questsandstuff.client.tablet.controls.TwoFieldEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
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
            state.root.skinModeEditorOpen = false;
        }
            return false;
        }

        if (root.isContextMenuOpen()) {
            if (root.isContextMenuAt(mouseX, mouseY)) {
                root.clickContextMenu(mouseX, mouseY, button);
                return true;
            }
            if (!state.root.skinModeEditorOpen) {
                root.closeContextMenu();
                state.root.skinModeEditorOpen = false;
            }
            return true;
        }

        Widget homeBtn = root.getHomeButton();
        String hitKey;
        if (homeBtn != null && homeBtn.isVisible() && homeBtn.isMouseOverElement(mouseX, mouseY)) {
            hitKey = "home_btn";
        } else if (isMinimapHit(state, root, mouseX, mouseY)) {
            hitKey = minimapHitKey(state, root, mouseX, mouseY);
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

            if (isMinimapKey(targetKey)) {
                SkinFillOverride.clearCache();
                state.root.activeSkinTargets.add(targetKey);
                continue;
            }

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
                        if (!SkinEditTargetResolver.hasCustomChrome(child)
                                || child instanceof TabletIconTextButton
                                || child instanceof TextFieldWidget) {
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
                protected void drawInternal(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
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

        ContextMenuSections sections = new ContextMenuSections();
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(
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
                    setFillMode(state, resolvedTarget, "stretch", currentAsset, 0, 0, root, refresher);
                }));
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_tile"),
                "brick-wall",
                currentMode.equals("tile") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: tile, asset={}", currentAsset);
                    int curW = currentOverride != null && "tile".equals(currentOverride.mode()) ? currentOverride.leftEdge() : 0;
                    int curH = currentOverride != null && "tile".equals(currentOverride.mode()) ? currentOverride.rightEdge() : 0;
                    root.closeContextMenu();
                    openModeEditor(state, root, refresher, resolvedTarget, currentAsset, mouseX, mouseY,
                            "tile", "ui.questsandstuff.skin.mode_tile", "ui.questsandstuff.skin.tile_size_w", "ui.questsandstuff.skin.tile_size_h", curW, curH);
                }));
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_original_size"),
                "original_size",
                currentMode.equals("center") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: center, asset={}", currentAsset);
                    root.closeContextMenu();
                    setFillMode(state, resolvedTarget, "center", currentAsset, 0, 0, root, refresher);
                }));
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_dynamic"),
                "dynamic",
                currentMode.equals("dynamic") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: dynamic, asset={}", currentAsset);
                    int curL = currentOverride != null && "dynamic".equals(currentOverride.mode()) ? currentOverride.leftEdge() : 1;
                    int curR = currentOverride != null && "dynamic".equals(currentOverride.mode()) ? currentOverride.rightEdge() : 1;
                    int curT = currentOverride != null && "dynamic".equals(currentOverride.mode()) ? currentOverride.topEdge() : 1;
                    int curB = currentOverride != null && "dynamic".equals(currentOverride.mode()) ? currentOverride.bottomEdge() : 1;
                    root.closeContextMenu();
                    openDynamicModeEditor(state, root, refresher, resolvedTarget, currentAsset, mouseX, mouseY, curL, curR, curT, curB);
                }));
        modeActions.add(ContextActionFactory.action(
                TabletTranslationKeys.text("ui.questsandstuff.skin.mode_hrstretch"),
                "repeat",
                currentMode.equals("hrstretch") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                () -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode action clicked: hrstretch, asset={}", currentAsset);
                    int curLeft = currentOverride != null && "hrstretch".equals(currentOverride.mode()) ? currentOverride.leftEdge() : 0;
                    int curRight = currentOverride != null && "hrstretch".equals(currentOverride.mode()) ? currentOverride.rightEdge() : 0;
                    root.closeContextMenu();
                    openModeEditor(state, root, refresher, resolvedTarget, currentAsset, mouseX, mouseY,
                            "hrstretch", "ui.questsandstuff.skin.mode_hrstretch", "ui.questsandstuff.skin.hrstretch_left", "ui.questsandstuff.skin.hrstretch_right", curLeft, curRight);
                }));
        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.submenu(
                TabletTranslationKeys.text("ui.questsandstuff.skin.change_mode"),
                "layout-dashboard",
                TabletColors.TEXT_PRIMARY,
                modeActions));

        if (rawOverride != null && !rawOverride.isBlank()) {
            String skinTexKey = "skin_remove_tex:" + resolvedTarget;
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, skinTexKey,
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

        List<ContextAction> built = sections.build();
        List<String> labels = new ArrayList<>();
        for (ContextAction a : built) labels.add(a.label());
        int menuW = ContextMenuRenderer.preferredMenuWidth(labels, 90, 120);
        int menuH = ContextMenuPanel.heightFor(built, built.size());

        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int px = ContextMenuPlacement.fitRightOrLeft(mouseX, screenW, menuW);
        int py = ContextMenuPlacement.fitBelowOrAbove(mouseY, screenH, menuH);

        root.setContextMenu(
                ContextMenuPanel.build(px, py, menuW, built, 0, built.size(), TabletColors.BORDER_BASE, state, a -> {
                    if (state.root.skinModeEditorOpen) {
                        return;
                    }
                    if (root.isContextMenuOpen()) {
                        buildContextMenu(state, root, refresher, mouseX, mouseY);
                    }
                }),
                px, py, menuW, menuH
        );
    }

    private static void setFillMode(TabletUiState state, String targetKey, String mode, String asset, int leftEdge, int rightEdge, WidgetGroup root, Runnable refresher) {
        setFillMode(state, targetKey, mode, asset, leftEdge, rightEdge, 0, 0, root, refresher);
    }

    private static void setFillMode(TabletUiState state, String targetKey, String mode, String asset, int leftEdge, int rightEdge, int topEdge, int bottomEdge, WidgetGroup root, Runnable refresher) {
        if (asset == null || asset.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] setFillMode ABORTED: asset is blank, target={}, mode={}", targetKey, mode);
            return;
        }
        String entryKey = SkinOverrideKey.isSharedKey(targetKey) ? targetKey : (state.root.currentApp.isBlank() ? targetKey : state.root.currentApp + ":" + targetKey);
        SkinFillOverride override = new SkinFillOverride(mode, leftEdge, rightEdge, topEdge, bottomEdge, asset);
        String encoded = override.encode();
        state.root.skinFillOverrides.put(entryKey, encoded);
        state.root.activeSkinTargets.add(targetKey);
        SkinFillOverride.clearCache();
        QuestsAndStuffMod.debugLog("[QnS:Skin] setFillMode: target={}, mode={}, asset={}, entryKey={}, encoded={}", targetKey, mode, asset, entryKey, encoded);
        reapplyOverrides(state, root);
        if (refresher != null) refresher.run();
        TabletUiFactory.persistSkinState(state);
    }

    private static void openModeEditor(TabletUiState state, TabletRootWidget root, Runnable refresher, String targetKey, String asset, int mouseX, int mouseY,
            String mode, String titleKey, String leftLabelKey, String rightLabelKey, int left, int right) {
        int w = 240;
        int h = 116;
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int x = Math.max(4, Math.min(mouseX, screenW - w - 4));
        int y = Math.max(4, Math.min(mouseY, screenH - h - 4));
        Runnable cancel = () -> {
            state.root.skinModeEditorOpen = false;
            root.closeContextMenu();
        };
        WidgetGroup popup = TwoFieldEditor.build(state, x, y, w, h, titleKey, leftLabelKey, rightLabelKey, left, right,
                (l, r) -> {
                    QuestsAndStuffMod.debugLog("[QnS:Skin] mode editor apply: target={}, mode={}, asset={}, edges=({},{}), currentMode={}", targetKey, mode, asset, l, r);
                    state.root.skinModeEditorOpen = false;
                    root.closeContextMenu();
                    setFillMode(state, targetKey, mode, asset, l, r, root, refresher);
                },
                cancel);
        state.root.skinModeEditorOpen = true;
        root.setContextMenu(popup, x, y, w, h);
    }

    private static void openDynamicModeEditor(TabletUiState state, TabletRootWidget root, Runnable refresher, String targetKey, String asset, int mouseX, int mouseY, int l, int r, int t, int b) {
        int w = 240;
        int h = 116;
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int x = Math.max(4, Math.min(mouseX, screenW - w - 4));
        int y = Math.max(4, Math.min(mouseY, screenH - h - 4));
        Runnable cancel = () -> {
            state.root.skinModeEditorOpen = false;
            root.closeContextMenu();
        };
        WidgetGroup popup = FourFieldEditor.build(state, x, y, w, h, "ui.questsandstuff.skin.mode_dynamic", "ui.questsandstuff.skin.hrstretch_left", "ui.questsandstuff.skin.hrstretch_right", "ui.questsandstuff.skin.dynamic_top", "ui.questsandstuff.skin.dynamic_bottom", l, r, t, b, (a, c, d, e) -> {
            state.root.skinModeEditorOpen = false;
            root.closeContextMenu();
            setFillMode(state, targetKey, "dynamic", asset, a, c, d, e, root, refresher);
        }, cancel);
        state.root.skinModeEditorOpen = true;
        root.setContextMenu(popup, x, y, w, h);
    }

    private static boolean isMinimapKey(String key) {
        return "quests_minimap_body".equals(key) || "quests_minimap_toggle".equals(key);
    }

    static boolean isMinimapHit(TabletUiState state, TabletRootWidget root, int mx, int my) {
        int[] abs = minimapViewportOrigin(root);
        if (abs == null) return false;
        int vpAbsX = abs[0];
        int vpAbsY = abs[1];
        int bodyX = vpAbsX + state.canvas.minimapPanelX;
        int bodyY = vpAbsY + state.canvas.minimapPanelY;
        int toggleX = vpAbsX + state.canvas.minimapToggleX;
        int toggleY = vpAbsY + state.canvas.minimapToggleY;
        return (state.canvas.minimapPanelW > 0 && state.canvas.minimapPanelH > 0
                && mx >= bodyX && mx < bodyX + state.canvas.minimapPanelW
                && my >= bodyY && my < bodyY + state.canvas.minimapPanelH)
                || (state.canvas.minimapToggleW > 0 && state.canvas.minimapToggleH > 0
                && mx >= toggleX && mx < toggleX + state.canvas.minimapToggleW
                && my >= toggleY && my < toggleY + state.canvas.minimapToggleH);
    }

    static String minimapHitKey(TabletUiState state, TabletRootWidget root, int mx, int my) {
        int[] abs = minimapViewportOrigin(root);
        if (abs == null) return null;
        int vpAbsX = abs[0];
        int vpAbsY = abs[1];
        int bodyX = vpAbsX + state.canvas.minimapPanelX;
        int bodyY = vpAbsY + state.canvas.minimapPanelY;
        int toggleX = vpAbsX + state.canvas.minimapToggleX;
        int toggleY = vpAbsY + state.canvas.minimapToggleY;
        if (state.canvas.minimapToggleW > 0 && state.canvas.minimapToggleH > 0
                && mx >= toggleX && mx < toggleX + state.canvas.minimapToggleW
                && my >= toggleY && my < toggleY + state.canvas.minimapToggleH) {
            return "quests_minimap_toggle";
        }
        if (state.canvas.minimapPanelW > 0 && state.canvas.minimapPanelH > 0
                && mx >= bodyX && mx < bodyX + state.canvas.minimapPanelW
                && my >= bodyY && my < bodyY + state.canvas.minimapPanelH) {
            return "quests_minimap_body";
        }
        return null;
    }

    private static int[] minimapViewportOrigin(TabletRootWidget root) {
        CanvasViewport vp = root.getCanvasViewport();
        if (vp == null) return null;
        return new int[]{vp.getPositionX(), vp.getPositionY()};
    }
}
