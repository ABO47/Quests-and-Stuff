package com.abo47.questsandstuff.client.tablet.modal.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.hud.QuestHudLayoutManager;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetState;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasOverlayController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.BackgroundModes;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinOverrideKey;
import com.abo47.questsandstuff.client.tablet.ui.IntegratedServerActions;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;

public final class AssetPickerApplyActions {
    private AssetPickerApplyActions() {
    }

    public static void run(Player player, TabletUiState state, String background) {
        state.pickers.saveBrowseDirForMode();
        String blueprintTarget = ModalTargetState.target(state, ModalSession.TargetSlot.BLUEPRINT, state.modal.modalBlueprintTarget);
        if (!blueprintTarget.isBlank()) {
            CanvasBlueprintController.beginPlacement(state, background);
            state.modal.modalBlueprintTarget = "";
            state.pickers.assetBrowseDir = "";
            QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] picked blueprint target={} asset={}", blueprintTarget, background);
            return;
        }
        String soundTarget = ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTarget);
        Set<String> soundTargets = ModalTargetState.targetSet(state, ModalSession.TargetSetSlot.QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTargets);
        if (!soundTargets.isEmpty()) {
            int count = soundTargets.size();
            EditorQuestCommandClient.setQuestCompletionSound(player, soundTargets, background);
            state.modal.modalQuestCompletionSoundTargets.clear();
            state.pickers.assetBrowseDir = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest batch completion sound picked quests={} asset={}", count, background);
            return;
        }
        if (!soundTarget.isBlank()) {
            EditorQuestCommandClient.setQuestCompletionSound(player, soundTarget, background);
            state.modal.modalQuestCompletionSoundTarget = "";
            state.pickers.assetBrowseDir = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest change completion sound picked quest={} asset={}", soundTarget, background);
            return;
        }
        String questBackgroundTarget = ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_BACKGROUND, state.modal.modalQuestBackgroundTarget);
        Set<String> questBackgroundTargets = ModalTargetState.targetSet(state, ModalSession.TargetSetSlot.QUEST_BACKGROUND, state.modal.modalQuestBackgroundTargets);
        if (!questBackgroundTargets.isEmpty()) {
            EditorQuestCommandClient.setQuestBackground(player, questBackgroundTargets, background, state.modal.modalQuestBackgroundGrayscale);
            int count = questBackgroundTargets.size();
            state.modal.modalQuestBackgroundTargets.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] quest batch background picked quests={} asset={} grayscale={}", count, background, state.modal.modalQuestBackgroundGrayscale);
            return;
        }
        if (!questBackgroundTarget.isBlank()) {
            EditorQuestCommandClient.setQuestBackground(player, questBackgroundTarget, background, state.modal.modalQuestBackgroundGrayscale);
            state.modal.modalQuestBackgroundTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest background picked quest={} asset={} grayscale={}", questBackgroundTarget, background, state.modal.modalQuestBackgroundGrayscale);
            return;
        }
        String completionHudTarget = ModalTargetState.target(state, ModalSession.TargetSlot.QUEST_COMPLETION_HUD_BACKGROUND, state.modal.modalQuestCompletionHudBackgroundTarget);
        Set<String> completionHudTargets = ModalTargetState.targetSet(state, ModalSession.TargetSetSlot.QUEST_COMPLETION_HUD_BACKGROUND, state.modal.modalQuestCompletionHudBackgroundTargets);
        if (!completionHudTargets.isEmpty()) {
            EditorQuestCommandClient.setQuestCompletionHudBackground(player, completionHudTargets, background);
            int count = completionHudTargets.size();
            state.modal.modalQuestCompletionHudBackgroundTargets.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] quest batch completion hud background picked quests={} asset={}", count, background);
            return;
        }
        if (!completionHudTarget.isBlank()) {
            EditorQuestCommandClient.setQuestCompletionHudBackground(player, completionHudTarget, background);
            state.modal.modalQuestCompletionHudBackgroundTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest completion hud background picked quest={} asset={}", completionHudTarget, background);
            return;
        }
        String hudTarget = ModalTargetState.target(state, ModalSession.TargetSlot.HUD_BACKGROUND, state.modal.modalHudBackgroundTarget);
        QuestHudLayoutManager.Element hudElement = hudElement(hudTarget);
        if (hudElement != null) {
            QuestHudLayoutManager.setBackground(hudElement, background);
            state.modal.modalHudBackgroundTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] hud background picked target={} asset={}", hudTarget, background);
            return;
        }
        String skinFillTarget = state.modal.skinEditFillTarget;
        if (!skinFillTarget.isBlank()) {
            String entryKey = SkinOverrideKey.overrideKey(state, skinFillTarget);
            String existing = SkinOverrideKey.resolveOverride(state, skinFillTarget);
            SkinFillOverride existingOverride = SkinFillOverride.parse(existing);
            String mode = (existingOverride != null) ? existingOverride.mode() : "stretch";
            SkinFillOverride newOverride = new SkinFillOverride(mode, background);
            state.root.skinFillOverrides.put(entryKey, newOverride.encode());
            state.root.activeSkinTargets.add(skinFillTarget);
            state.modal.skinEditFillTarget = "";
            state.pickers.assetBrowseDir = "";
            SkinFillOverride.clearCache();
            TabletUiFactory.persistSkinState(state);
            TabletUiFactory.refreshActiveTablet();
            QuestsAndStuffMod.debugLog("[QnS:UI] skin fill override target={} asset={} mode={}", skinFillTarget, background, mode);
            return;
        }

        ModalTargetParser.Target detailsTarget = ModalTargetState.parsedTarget(state, ModalSession.TargetSlot.QUEST_DETAILS_ASSET_PICK, state.questDetails.questDetailsAssetPickTarget);
        if (!detailsTarget.kind().isBlank()) {
            QuestDetailsWindow.applyAssetPick(player, state, detailsTarget, background);
            return;
        }
        String imageTarget = ModalTargetState.target(state, ModalSession.TargetSlot.CANVAS_IMAGE, state.modal.modalCanvasImageTarget);
        if (!imageTarget.isBlank()) {
            addCanvasImage(state, imageTarget, background);
            return;
        }
        String ecTarget = state.modal.modalEcBackgroundTarget;
        if (!ecTarget.isBlank()) {
            String[] parts = ecTarget.split(":", 2);
            if (parts.length == 2) {
                String chapter = parts[0];
                String ecId = parts[1];
                CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, ecId);
                if (ec != null) {
                    CanvasExclusiveChoice updated = ec.withBackground(background);
                    CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, updated);
                    CanvasLayerMutations.persistCanvasExclusiveChoice(state, chapter, ecId);
                    QuestsAndStuffMod.debugLog("[QnS:UI] exclusive choice background picked chapter={} ec={} background={}", chapter, ecId, background);
                }
            }
            state.modal.modalEcBackgroundTarget = "";
            return;
        }
        String canvasTarget = ModalTargetState.target(state, ModalSession.TargetSlot.CANVAS_BACKGROUND, state.modal.modalCanvasBackgroundTarget);
        if (!canvasTarget.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas background picked chapter={} background={}", canvasTarget, background);
            String currentBg = com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade.chapterCanvasBackground(canvasTarget);
            SkinFillOverride o = SkinFillOverride.parse(currentBg);
            String mode = o != null ? o.mode() : "stretch";
            TabletUiFactory.runChapterAction(player, state, "set_canvas_background", canvasTarget, BackgroundModes.encode(mode, background), 0);
            return;
        }
        String connectionTextureTarget = state.modal.modalConnectionTextureTarget;
        java.util.Set<String> connectionTextureChapterTargets = state.modal.modalConnectionTextureChapterTargets;
        if (!connectionTextureChapterTargets.isEmpty()) {
            String chapter = !connectionTextureTarget.isBlank() && connectionTextureTarget.startsWith("connection|")
                    ? connectionTextureTarget.split("\\|")[1] : "";
            java.util.Map<String, java.util.Map<String, String>> questTextures = new java.util.HashMap<>();
            for (String chapterQuestId : connectionTextureChapterTargets) {
                net.minecraft.nbt.CompoundTag questTag = com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade.quest(chapterQuestId);
                if (questTag == null) continue;
                java.util.Map<String, String> prereqTextures = new java.util.HashMap<>();
                net.minecraft.nbt.ListTag prereqs = questTag.getList("prerequisites", net.minecraft.nbt.Tag.TAG_STRING);
                for (int i = 0; i < prereqs.size(); i++) {
                    String prerequisiteId = prereqs.getString(i);
                    prereqTextures.put(prerequisiteId, background);
                    com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade.setConnectionTextureLocal(chapterQuestId, prerequisiteId, background);
                    if (!chapter.isBlank()) {
                        ConnectionRenderer.setConnectionTexture(state, chapter, prerequisiteId, chapterQuestId, background);
                    }
                }
                if (!prereqTextures.isEmpty()) {
                    questTextures.put(chapterQuestId, prereqTextures);
                }
            }
            if (!questTextures.isEmpty()) {
                QuestsAndStuffMod.debugLog("[QnS:UI] chapter batch connection texture quests={} chapter={} bg={}", questTextures.size(), chapter, background);
                net.minecraft.nbt.CompoundTag batchPayload = EditorCommandPayloads.connectionTextures(questTextures);
                IntegratedServerActions.run(
                        player,
                        serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setConnectionTextures(serverPlayer, questTextures),
                        () -> com.abo47.questsandstuff.network.ModNetwork.sendToServer(new com.abo47.questsandstuff.network.quest.editor.C2SEditorCommandPacket(EditorCommandType.CONNECTION_TEXTURE_MANY, batchPayload)));
            }
            if (!chapter.isBlank()) {
                java.util.List<com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice> changedEcs = new java.util.ArrayList<>();
                for (com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, java.util.List.of())) {
                    java.util.Map<String, String> textures = new java.util.HashMap<>(ec.connectionTextures());
                    boolean changed = false;
                    for (String connectedId : ec.connectionQuestIds()) {
                        if (connectionTextureChapterTargets.contains(connectedId) || connectionTextureChapterTargets.contains(ec.id())) {
                            textures.put(connectedId, background);
                            changed = true;
                        }
                    }
                    for (String prerequisiteId : ec.prerequisiteQuestIds()) {
                        if (connectionTextureChapterTargets.contains(prerequisiteId) || connectionTextureChapterTargets.contains(ec.id())) {
                            textures.put(prerequisiteId, background);
                            changed = true;
                        }
                    }
                    if (changed) {
                        changedEcs.add(ec.withConnectionTextures(textures));
                    }
                }
                if (!changedEcs.isEmpty()) {
                    com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations.putCanvasExclusiveChoices(state, chapter, changedEcs, true);
                }
            }
            state.modal.modalConnectionTextureChapterTargets.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] connection texture chapter batch applied count={} asset={}", connectionTextureChapterTargets.size(), background);
            return;
        }
        if (connectionTextureTarget.startsWith("connection_selection|")) {
            String[] parts = connectionTextureTarget.split("\\|");
            if (parts.length >= 2) {
                String chapter = parts[1];
                for (var connection : CanvasOverlayController.selectedConnections(state, chapter)) {
                    String prereq = connection.prerequisiteId();
                    String quest = connection.questId();
                    boolean isEc = ConnectionRenderer.isEcId(state, chapter, prereq) || ConnectionRenderer.isEcId(state, chapter, quest);
                    if (isEc) {
                        EditorCanvasCommandClient.runEcConnectionTextureAction(state, prereq, quest, background);
                    } else {
                        EditorCanvasCommandClient.runConnectionTextureAction(player, quest, prereq, background);
                        ConnectionRenderer.setConnectionTexture(state, chapter, prereq, quest, background);
                    }
                }
            }
            state.modal.modalConnectionTextureTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] connection texture selection applied target={} asset={}", connectionTextureTarget, background);
            return;
        }
        if (!connectionTextureTarget.isBlank()) {
            String[] parts = connectionTextureTarget.split("\\|");
            if (parts.length >= 4) {
                String chapter = parts[1];
                String prerequisiteId = parts[2];
                String questId = parts[3];
                boolean isEc = com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer.isEcId(state, chapter, prerequisiteId)
                        || com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer.isEcId(state, chapter, questId);
                if (isEc) {
                    EditorCanvasCommandClient.runEcConnectionTextureAction(state, prerequisiteId, questId, background);
                } else {
                    EditorCanvasCommandClient.runConnectionTextureAction(player, questId, prerequisiteId, background);
                    ConnectionRenderer.setConnectionTexture(state, chapter, prerequisiteId, questId, background);
                }
            }
            state.modal.modalConnectionTextureTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] connection texture applied target={} asset={}", connectionTextureTarget, background);
            return;
        }
        String chapterTarget = ModalTargetState.target(state, ModalSession.TargetSlot.CHAPTER, state.modal.modalChapterTarget);
        if (!chapterTarget.isBlank()) {
            String currentBg = com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade.chapterBackground(chapterTarget);
            SkinFillOverride o = SkinFillOverride.parse(currentBg);
            String mode = o != null ? o.mode() : "stretch";
            TabletUiFactory.runChapterAction(player, state, "set_background", chapterTarget, BackgroundModes.encode(mode, background), 0);
        }
    }

    private static void addCanvasImage(TabletUiState state, String chapter, String asset) {
        String id = StableIdAllocator.nextId("img", canvasImageIds(state, chapter));
        int[] imageSize = canvasImageSpawnSize(state, asset);
        int imageW = imageSize[0];
        int imageH = imageSize[1];
        int x = state.canvas.canvasImageLogicalX - imageW / 2;
        int y = state.canvas.canvasImageLogicalY - imageH / 2;
        if (!state.canvas.gridSnapLocked) {
            x = TabletUiFactory.snapToGrid(state, x);
            y = TabletUiFactory.snapToGrid(state, y);
        }
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, x, y, imageW, imageH);
        CanvasImageLayer image = new CanvasImageLayer(id, asset, clamped.x, clamped.y, imageW, imageH, 0);
        if (state.canvas.gridSnapLocked) {
            image = CanvasGridFitController.fittedImage(state, image);
        }
        CanvasLayerMutations.putCanvasImage(state, chapter, image);
        state.canvas.canvasSelection.setPrimaryImageId(id);
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.draggingCanvasImage = false;
        state.canvas.resizingCanvasImage = false;
        state.canvas.rotatingCanvasImage = false;
        state.canvas.mouseMode = CanvasMouseMode.SELECT_MOVE;
        ContextMenuController.close(state);
        ContextMenuController.clearDeleteConfirm(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas image added chapter={} id={} asset={} pos={},{} size={}x{}", chapter, id, asset, clamped.x, clamped.y, imageW, imageH);
    }

    private static List<String> canvasImageIds(TabletUiState state, String chapter) {
        List<String> ids = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }

    private static int[] canvasImageSpawnSize(TabletUiState state, String asset) {
        var dimensions = TabletUiFactory.assetDimensions(asset);
        if (dimensions == null || dimensions.width() <= 0 || dimensions.height() <= 0) {
            return new int[]{96, 64};
        }
        int maxSize = Math.max(CanvasGeometry.gridSize(state), CanvasGeometry.gridSize(state) * 6);
        double scale = Math.min(1.0, maxSize / (double) Math.max(dimensions.width(), dimensions.height()));
        int width = Math.max(8, (int) Math.round(dimensions.width() * scale));
        int height = Math.max(8, (int) Math.round(dimensions.height() * scale));
        return new int[]{width, height};
    }

    private static QuestHudLayoutManager.Element hudElement(String target) {
        if ("completion".equalsIgnoreCase(target)) {
            return QuestHudLayoutManager.Element.COMPLETION;
        }
        if ("pinned".equalsIgnoreCase(target)) {
            return QuestHudLayoutManager.Element.PINNED;
        }
        return null;
    }
}
