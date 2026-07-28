package com.abo47.questsandstuff.client.tablet.modal.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetState;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEditActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;

record EntityVariantPickerModel(
        String target,
        String asset,
        String entityId,
        List<EntityVariantCatalog.VariantEntry> allVariants,
        String selected,
        boolean foldered,
        String activeFolder,
        boolean browsingFolder,
        List<EntityVariantCatalog.VariantFolder> folders,
        List<EntityVariantCatalog.VariantEntry> variants,
        List<EntityVariantTile> tiles
) {
    static EntityVariantPickerModel create(TabletUiState state) {
        String target = ModalTargetState.target(state, ModalSession.TargetSlot.ENTITY_VARIANT, state.pickers.entityVariantTarget);
        String asset = currentAsset(state, target);
        String entityId = EntityPreviewRenderer.entityId(asset);
        List<EntityVariantCatalog.VariantEntry> allVariants = EntityVariantCatalog.variantsFor(entityId);
        if (target.isBlank() || entityId.isBlank() || allVariants.isEmpty()) {
            return null;
        }

        String selected = selectedVariant(state, entityId, asset, allVariants);
        boolean foldered = EntityVariantCatalog.hasVariantFolders(entityId);
        String activeFolder = foldered ? activeFolder(state, entityId) : "";
        boolean browsingFolder = foldered && !activeFolder.isBlank();
        List<EntityVariantCatalog.VariantFolder> folders = foldered && !browsingFolder
                ? EntityVariantCatalog.variantFoldersFor(entityId, state.pickers.entityVariantSearch)
                : List.of();
        List<EntityVariantCatalog.VariantEntry> variants = browsingFolder
                ? EntityVariantCatalog.variantsForFolder(entityId, activeFolder, state.pickers.entityVariantSearch)
                : foldered ? List.of() : EntityVariantCatalog.search(entityId, state.pickers.entityVariantSearch);
        return new EntityVariantPickerModel(
                target,
                asset,
                entityId,
                List.copyOf(allVariants),
                selected,
                foldered,
                activeFolder,
                browsingFolder,
                List.copyOf(folders),
                List.copyOf(variants),
                tiles(foldered, browsingFolder, folders, variants)
        );
    }

    String emptyText() {
        return TabletTranslationKeys.text(browsingFolder || !foldered ? QuestTranslationKeys.NO_VARIANTS : QuestTranslationKeys.NO_BIOME_FOLDERS);
    }

    private static List<EntityVariantTile> tiles(boolean foldered, boolean browsingFolder, List<EntityVariantCatalog.VariantFolder> folders, List<EntityVariantCatalog.VariantEntry> variants) {
        List<EntityVariantTile> tiles = new ArrayList<>();
        if (foldered && !browsingFolder) {
            for (EntityVariantCatalog.VariantFolder folder : folders) {
                tiles.add(EntityVariantTile.folder(folder));
            }
        } else {
            for (EntityVariantCatalog.VariantEntry variant : variants) {
                tiles.add(EntityVariantTile.variant(variant));
            }
        }
        return List.copyOf(tiles);
    }

    private static String activeFolder(TabletUiState state, String entityId) {
        String folder = state.pickers.entityVariantFolder == null ? "" : state.pickers.entityVariantFolder.trim().toLowerCase(Locale.ROOT);
        if (folder.isBlank()) {
            return "";
        }
        for (EntityVariantCatalog.VariantFolder entry : EntityVariantCatalog.variantFoldersFor(entityId, "")) {
            if (entry.key().equals(folder)) {
                state.pickers.entityVariantFolder = folder;
                return folder;
            }
        }
        state.pickers.entityVariantFolder = "";
        return "";
    }

    private static String selectedVariant(TabletUiState state, String entityId, String asset, List<EntityVariantCatalog.VariantEntry> variants) {
        String selected = EntityVariantCatalog.normalizeVariantKey(entityId, state.pickers.entityVariantSelected);
        if (selected.isBlank()) {
            selected = EntityVariantCatalog.normalizeVariantKey(entityId, EntityPreviewRenderer.entityVariant(asset));
        }
        if (selected.isBlank() && !variants.isEmpty()) {
            selected = variants.get(0).key();
        }
        state.pickers.entityVariantSelected = selected;
        return selected;
    }

    private static String currentAsset(TabletUiState state, String target) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (parsed.hasAtLeast(3) && parsed.isCanvasImage()) {
            CanvasImageLayer image = CanvasLayerMutations.findCanvasImage(state, parsed.questId(), parsed.entryId());
            return image == null ? "" : image.asset();
        }
        if (parsed.hasAtLeast(3) && parsed.isQuestDetailsImage()) {
            return QuestDetailsWindow.descriptionImageAsset(parsed.questId(), parsed.entryId());
        }
        if (parsed.hasAtLeast(2) && parsed.isQuestIcon()) {
            var quest = ClientQuestStateFacade.quest(parsed.questId());
            return quest == null ? "" : quest.getString("icon");
        }
        if (parsed.hasAtLeast(2) && parsed.isChapterIcon()) {
            return ClientQuestStateFacade.chapterIcon(parsed.questId());
        }
        if (parsed.hasAtLeast(3) && parsed.isTaskTask()) {
            return QuestTaskEditActions.taskIcon(parsed.questId(), parsed.entryId(), true);
        }
        if (parsed.hasAtLeast(3) && parsed.isTaskReward()) {
            return QuestTaskEditActions.taskIcon(parsed.questId(), parsed.entryId(), false);
        }
        return "";
    }
}
