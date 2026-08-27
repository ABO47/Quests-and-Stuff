package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.controls.TwoFieldEditor;
import com.abo47.questsandstuff.client.tablet.theme.BackgroundModes;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.addQuestAt;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.runChapterAction;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.snapToGrid;

final class CanvasContextCanvasActions {
    private CanvasContextCanvasActions() {
    }

    static void addCanvasEmptyActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedChapter) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.CANVAS || selectedChapter.isBlank()) {
            return;
        }
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.quick_add_quest"), "add", TabletColors.SUCCESS, () -> {
            int logicalX = snapToGrid(state, state.contextMenu.contextLogicalX);
            int logicalY = snapToGrid(state, state.contextMenu.contextLogicalY);
            CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                    state,
                    logicalX,
                    logicalY,
                    CanvasGeometry.slotLogicalWidth(state, 1.0f),
                    CanvasGeometry.slotLogicalHeight(state, 1.0f)
            );
            logicalX = clamped.x;
            logicalY = clamped.y;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add logicalX={} logicalY={} target={}", logicalX, logicalY, state.contextMenu.contextMenuTarget);
            addQuestAt(player, state, logicalX, logicalY, "");
            canvasViewport.refresh();
        }));
        List<ContextAction> addActions = new ArrayList<>();
        addActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_image"), "image", TabletColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasImagePicker(state, selectedChapter, state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
            ContextMenuController.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_image chapter={} logical={},{}", selectedChapter, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_text_box"), "text", TabletColors.SUCCESS, () -> {
            String id = StableIdAllocator.nextId("txt", canvasTextIds(state, selectedChapter));
            int textW = 96;
            int textH = 32;
            int x = snapToGrid(state, state.contextMenu.contextPointerLogicalX - textW / 2);
            int y = snapToGrid(state, state.contextMenu.contextPointerLogicalY - textH / 2);
            CanvasTextLayer text = new CanvasTextLayer(id, "Text", x, y, textW, textH, 0, "left", "normal", TabletColors.TEXT_PRIMARY);
            if (state.canvas.gridSnapLocked) {
                text = CanvasGridFitController.fittedText(state, text);
            }
            CanvasLayerMutations.putCanvasText(state, selectedChapter, text);
            state.canvas.canvasSelection.setPrimaryTextId(id);
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.questIds().clear();
            TextEditSession.beginMainCanvas(state, id, text.text());
            ContextMenuController.close(state);
            canvasViewport.setFocus(true);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_text_box chapter={} id={} logical={},{}", selectedChapter, id, x, y);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_entity"), "entity", TabletColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityNew(selectedChapter), state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
            ContextMenuController.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_entity chapter={} logical={},{}", selectedChapter, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_exclusive_choice"), "split", TabletColors.SUCCESS, () -> {
            String id = StableIdAllocator.nextId("ec", canvasExclusiveChoiceIds(state, selectedChapter));
            int defaultW = TabletUiFactory.CARD_W;
            int defaultH = TabletUiFactory.CARD_H;
            int x = snapToGrid(state, state.contextMenu.contextPointerLogicalX - defaultW / 2);
            int y = snapToGrid(state, state.contextMenu.contextPointerLogicalY - defaultH / 2);
            CanvasExclusiveChoice ec = new CanvasExclusiveChoice(id, x, y, defaultW, defaultH, 0, List.of());
            if (state.canvas.gridSnapLocked) {
                ec = CanvasGridFitController.fittedExclusiveChoice(state, ec);
            }
            CanvasLayerMutations.putCanvasExclusiveChoice(state, selectedChapter, ec);
            state.canvas.canvasSelection.setPrimaryEcId(id);
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.questIds().clear();
            ContextMenuController.close(state);
            canvasViewport.setFocus(true);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_exclusive_choice chapter={} id={} logical={},{}", selectedChapter, id, x, y);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_item"), "icon", TabletColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemNew(selectedChapter), state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
            ContextMenuController.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_item_model chapter={} logical={},{}", selectedChapter, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_block"), "add_block", TabletColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockNew(selectedChapter), state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
            ContextMenuController.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_block_model chapter={} logical={},{}", selectedChapter, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
            canvasViewport.refresh();
        }));
        if (RecipeViewerIntegrations.hasAvailableViewer()) {
            addActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_recipe_card"), "recipe", TabletColors.SUCCESS, () -> {
                ModalOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeNew(selectedChapter), state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
                ContextMenuController.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_recipe_card chapter={} logical={},{}", selectedChapter, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
                canvasViewport.refresh();
            }));
        }
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ADD), "add", TabletColors.SUCCESS, addActions));

        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.change_canvas_bg"), "background", TabletColors.INTERACTIVE, () -> {
            ModalOpenActions.openCanvasBackgroundPicker(state, selectedChapter, ClientQuestStateFacade.chapterCanvasBackground(selectedChapter));
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_canvas_bg chapter={}", selectedChapter);
            canvasViewport.refresh();
        }));
        String currentCanvasBg = ClientQuestStateFacade.chapterCanvasBackground(selectedChapter);
        if (!currentCanvasBg.isBlank() && !"default".equals(currentCanvasBg)) {
            SkinFillOverride parsed = BackgroundModes.decode(currentCanvasBg);
            String currentMode = parsed != null ? parsed.mode() : "stretch";
            String path = parsed != null ? parsed.path() : currentCanvasBg;
            List<ContextAction> modeActions = new ArrayList<>();
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_stretch"),
                    "size",
                    currentMode.equals("stretch") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        String encoded = BackgroundModes.encode("stretch", path);
                        runChapterAction(player, state, "set_canvas_background", selectedChapter, encoded, 0);
                        canvasViewport.refresh();
                    }));
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_tile"),
                    "brick-wall",
                    currentMode.equals("tile") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        int curW = parsed != null && "tile".equals(parsed.mode()) ? parsed.leftEdge() : 0;
                        int curH = parsed != null && "tile".equals(parsed.mode()) ? parsed.rightEdge() : 0;
                        ContextMenuController.close(state);
                        openCanvasModeEditor(state, canvasViewport, selectedChapter, "tile", path, curW, curH);
                    }));
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_original_size"),
                    "original_size",
                    currentMode.equals("center") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        String encoded = BackgroundModes.encode("center", path);
                        runChapterAction(player, state, "set_canvas_background", selectedChapter, encoded, 0);
                        canvasViewport.refresh();
                    }));
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_dynamic"),
                    "dynamic",
                    currentMode.equals("dynamic") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        String encoded = BackgroundModes.encode("dynamic", path);
                        runChapterAction(player, state, "set_canvas_background", selectedChapter, encoded, 0);
                        canvasViewport.refresh();
                    }));
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_hrstretch"),
                    "repeat",
                    currentMode.equals("hrstretch") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        int curL = parsed != null && "hrstretch".equals(parsed.mode()) ? parsed.leftEdge() : 0;
                        int curR = parsed != null && "hrstretch".equals(parsed.mode()) ? parsed.rightEdge() : 0;
                        ContextMenuController.close(state);
                        openCanvasModeEditor(state, canvasViewport, selectedChapter, "hrstretch", path, curL, curR);
                    }));
            sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.submenu(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.change_mode"),
                    "layout-dashboard",
                    TabletColors.TEXT_PRIMARY,
                    modeActions));
        }
        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_GRID_COLOR), "style_color", TabletColors.INTERACTIVE, () -> {
            int color = TabletGridControls.defaultGridColor(state);
            ModalOpenActions.openColorPicker(state, ModalTargets.gridColor(), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_grid_color color={}", color);
            canvasViewport.refresh();
        }));
        if (!ClientQuestStateFacade.chapterCanvasBackground(selectedChapter).isBlank()
                && !"default".equals(ClientQuestStateFacade.chapterCanvasBackground(selectedChapter))) {
            String canvasBgKey = "canvas_remove_bg:" + selectedChapter;
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, canvasBgKey, CanvasContextMenuController.tr("ui.questsandstuff.context.remove_canvas_bg"), () -> {
                runChapterAction(player, state, "set_canvas_background", selectedChapter, "default", 0);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_canvas_bg chapter={}", selectedChapter);
                canvasViewport.refresh();
            }));
        }

        List<ContextAction> debugActions = new ArrayList<>();
        debugActions.add(ContextActionFactory.action(
                CanvasContextMenuController.tr("ui.questsandstuff.context.debug_spawn_all_entities"),
                "entity", TabletColors.INTERACTIVE, () -> spawnAllEntities(state, selectedChapter, canvasViewport)));
        sections.add(ContextMenuSection.DEBUG, ContextActionFactory.submenu(
                CanvasContextMenuController.tr("ui.questsandstuff.context.debug"),
                "debug", TabletColors.INTERACTIVE, debugActions));
    }

    private static void openCanvasModeEditor(TabletUiState state, CanvasViewport canvasViewport, String selectedChapter, String mode, String path, int left, int right) {
        int w = 240;
        int h = 116;
        var mc = net.minecraft.client.Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int x = Math.max(4, (screenW - w) / 2);
        int y = Math.max(4, (screenH - h) / 2);
        boolean tile = "tile".equals(mode);
        String title = tile ? "ui.questsandstuff.skin.mode_tile" : "ui.questsandstuff.skin.mode_hrstretch";
        String leftKey = tile ? "ui.questsandstuff.skin.tile_size_w" : "ui.questsandstuff.skin.hrstretch_left";
        String rightKey = tile ? "ui.questsandstuff.skin.tile_size_h" : "ui.questsandstuff.skin.hrstretch_right";
        var popup = TwoFieldEditor.build(state, x, y, w, h, title, leftKey, rightKey, left, right,
                (l, r) -> {
                    String encoded = new SkinFillOverride(mode, l, r, path).encode();
                    runChapterAction(mc.player, state, "set_canvas_background", selectedChapter, encoded, 0);
                    state.root.editorPopup = null;
                    state.root.editorPopupOpen = false;
                    canvasViewport.refresh();
                },
                () -> {
                    state.root.editorPopup = null;
                    state.root.editorPopupOpen = false;
                    canvasViewport.refresh();
                });
        state.root.editorPopup = popup;
        state.root.editorPopupOpen = true;
        canvasViewport.refresh();
    }

    private static void spawnAllEntities(TabletUiState state, String chapter, CanvasViewport canvasViewport) {
        List<String> eggs = EntityPreviewRenderer.searchableSpawnEggEntries("");
        if (eggs.isEmpty()) return;
        int size = Math.max(48, CanvasGeometry.gridSize(state) * 4);
        int gap = 8;
        int perRow = 8;
        int startCenterX = state.canvas.canvasImageLogicalX;
        int startCenterY = state.canvas.canvasImageLogicalY;
        List<String> existingIds = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            existingIds.add(image.id());
        }
        for (int i = 0; i < eggs.size(); i++) {
            String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(eggs.get(i));
            if (entityId.isBlank()) continue;
            String id = StableIdAllocator.nextId("ent", existingIds);
            existingIds.add(id);
            int col = i % perRow;
            int row = i / perRow;
            int x = startCenterX + col * (size + gap) - size / 2;
            int y = startCenterY + row * (size + gap) - size / 2;
            String asset = EntityPreviewRenderer.entityAsset(entityId);
            CanvasImageLayer image = new CanvasImageLayer(id, asset, x, y, size, size, 0, 205, 1);
            if (state.canvas.gridSnapLocked) {
                image = CanvasGridFitController.fittedImage(state, image);
            }
            CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, image.x(), image.y(), image.w(), image.h());
            image = new CanvasImageLayer(id, asset, clamped.x, clamped.y, image.w(), image.h(), 0, 205, 1);
            CanvasLayerMutations.putCanvasImage(state, chapter, image);
        }
        canvasViewport.refresh();
        QuestsAndStuffMod.debugLog("[QnS:UI] debug spawned {} entities chapter={}", eggs.size(), chapter);
    }

    private static List<String> canvasExclusiveChoiceIds(TabletUiState state, String chapter) {
        List<String> ids = new ArrayList<>();
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            ids.add(ec.id());
        }
        return ids;
    }

    private static List<String> canvasTextIds(TabletUiState state, String chapter) {
        List<String> ids = new ArrayList<>();
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            ids.add(text.id());
        }
        return ids;
    }
}
