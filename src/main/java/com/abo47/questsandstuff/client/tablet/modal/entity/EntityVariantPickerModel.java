package com.abo47.questsandstuff.client.tablet.modal.entity;

import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.details.objective.QuestObjectiveEditActions;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        String target = state.entityVariantTarget == null ? "" : state.entityVariantTarget.trim();
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
                ? EntityVariantCatalog.variantFoldersFor(entityId, state.entityVariantSearch)
                : List.of();
        List<EntityVariantCatalog.VariantEntry> variants = browsingFolder
                ? EntityVariantCatalog.variantsForFolder(entityId, activeFolder, state.entityVariantSearch)
                : foldered ? List.of() : EntityVariantCatalog.search(entityId, state.entityVariantSearch);
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
        return QuestVocabulary.text(browsingFolder || !foldered ? QuestVocabulary.NO_VARIANTS : QuestVocabulary.NO_BIOME_FOLDERS);
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
        String folder = state.entityVariantFolder == null ? "" : state.entityVariantFolder.trim().toLowerCase(Locale.ROOT);
        if (folder.isBlank()) {
            return "";
        }
        for (EntityVariantCatalog.VariantFolder entry : EntityVariantCatalog.variantFoldersFor(entityId, "")) {
            if (entry.key().equals(folder)) {
                state.entityVariantFolder = folder;
                return folder;
            }
        }
        state.entityVariantFolder = "";
        return "";
    }

    private static String selectedVariant(TabletUiState state, String entityId, String asset, List<EntityVariantCatalog.VariantEntry> variants) {
        String selected = EntityVariantCatalog.normalizeVariantKey(entityId, state.entityVariantSelected);
        if (selected.isBlank()) {
            selected = EntityVariantCatalog.normalizeVariantKey(entityId, EntityPreviewRenderer.entityVariant(asset));
        }
        if (selected.isBlank() && !variants.isEmpty()) {
            selected = variants.get(0).key();
        }
        state.entityVariantSelected = selected;
        return selected;
    }

    private static String currentAsset(TabletUiState state, String target) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (parsed.hasAtLeast(3) && parsed.isCanvasImage()) {
            CanvasImageLayer image = CanvasRenderer.findCanvasImage(state, parsed.questId(), parsed.entryId());
            return image == null ? "" : image.asset();
        }
        if (parsed.hasAtLeast(3) && parsed.isQuestDetailsImage()) {
            return QuestDetailsWindow.descriptionImageAsset(parsed.questId(), parsed.entryId());
        }
        if (parsed.hasAtLeast(2) && parsed.isQuestIcon()) {
            var quest = ClientQuestCache.quest(parsed.questId());
            return quest == null ? "" : quest.getString("icon");
        }
        if (parsed.hasAtLeast(2) && parsed.isChapterIcon()) {
            return ClientQuestCache.groupIcon(parsed.questId());
        }
        if (parsed.hasAtLeast(3) && parsed.isObjectiveTask()) {
            return QuestObjectiveEditActions.objectiveIcon(parsed.questId(), parsed.entryId(), true);
        }
        if (parsed.hasAtLeast(3) && parsed.isObjectiveReward()) {
            return QuestObjectiveEditActions.objectiveIcon(parsed.questId(), parsed.entryId(), false);
        }
        return "";
    }
}
