package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ModalSession {
    private static final ModalSession NONE = new ModalSession(ModalWindowManager.ModalType.NONE);

    private final ModalWindowManager.ModalType type;
    private final EnumMap<TargetSlot, String> targets = new EnumMap<>(TargetSlot.class);
    private final EnumMap<TargetSetSlot, LinkedHashSet<String>> targetSets = new EnumMap<>(TargetSetSlot.class);
    private final PickerSession picker = new PickerSession();
    private String selectedValue = "";
    private String mode = "";

    private ModalSession(ModalWindowManager.ModalType type) {
        this.type = type == null ? ModalWindowManager.ModalType.NONE : type;
    }

    public static ModalSession none() {
        return NONE;
    }

    public static ModalSession open(ModalWindowManager.ModalType type) {
        ModalWindowManager.ModalType safeType = type == null ? ModalWindowManager.ModalType.NONE : type;
        return safeType == ModalWindowManager.ModalType.NONE ? NONE : new ModalSession(safeType);
    }

    public static ModalSession capture(ModalWindowManager.ModalType type, TabletUiState state) {
        ModalSession session = open(type);
        if (session.active()) {
            session.captureTargets(state);
            session.capturePickerState(state);
        }
        return session;
    }

    public ModalWindowManager.ModalType type() {
        return type;
    }

    public boolean active() {
        return type != ModalWindowManager.ModalType.NONE;
    }

    public String target(TargetSlot slot) {
        return slot == null ? "" : targets.getOrDefault(slot, "");
    }

    public ModalTargetParser.Target parsedTarget(TargetSlot slot) {
        return ModalTargetParser.parse(target(slot));
    }

    public Set<String> targetSet(TargetSetSlot slot) {
        if (slot == null) {
            return Set.of();
        }
        LinkedHashSet<String> values = targetSets.get(slot);
        return values == null ? Set.of() : Collections.unmodifiableSet(values);
    }

    public PickerSession picker() {
        return picker;
    }

    public String selectedValue() {
        return selectedValue;
    }

    public String mode() {
        return mode;
    }

    public void setPickerSearch(String search) {
        picker.setSearch(search);
    }

    public void setPickerFocused(boolean focused) {
        picker.setFocused(focused);
    }

    public void setPickerScroll(int scroll) {
        picker.setScroll(scroll);
    }

    public void setPickerDragging(boolean dragging) {
        picker.setDragging(dragging);
    }

    public void captureTargets(TabletUiState state) {
        if (state == null || !active()) {
            return;
        }
        targets.clear();
        targetSets.clear();
        setTarget(TargetSlot.QUEST, state.modal.modalQuestTarget);
        setTarget(TargetSlot.CHAPTER, state.modal.modalChapterTarget);
        setTarget(TargetSlot.QUEST_DETAILS_PICK, state.questDetails.questDetailsPickTarget);
        setTarget(TargetSlot.QUEST_DETAILS_ASSET_PICK, state.questDetails.questDetailsAssetPickTarget);
        setTarget(TargetSlot.CANVAS_BACKGROUND, state.modal.modalCanvasBackgroundTarget);
        setTarget(TargetSlot.CANVAS_IMAGE, state.modal.modalCanvasImageTarget);
        setTarget(TargetSlot.CANVAS_ENTITY, state.modal.modalCanvasEntityTarget);
        setTarget(TargetSlot.CANVAS_MODEL, state.modal.modalCanvasModelTarget);
        setTarget(TargetSlot.BLUEPRINT, state.modal.modalBlueprintTarget);
        setTarget(TargetSlot.QUEST_BACKGROUND, state.modal.modalQuestBackgroundTarget);
        setTarget(TargetSlot.QUEST_COMPLETION_HUD_BACKGROUND, state.modal.modalQuestCompletionHudBackgroundTarget);
        setTarget(TargetSlot.HUD_BACKGROUND, state.modal.modalHudBackgroundTarget);
        setTarget(TargetSlot.QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTarget);
        setTarget(TargetSlot.ENTITY_VARIANT, state.pickers.entityVariantTarget);
        setTarget(TargetSlot.COLOR_PICKER, state.pickers.colorPickerTarget);
        setTarget(TargetSlot.PREREQUISITES_MANAGER, state.modal.prerequisitesManagerQuestId);
        setTargetSet(TargetSetSlot.QUEST_BACKGROUND, state.modal.modalQuestBackgroundTargets);
        setTargetSet(TargetSetSlot.QUEST_COMPLETION_HUD_BACKGROUND, state.modal.modalQuestCompletionHudBackgroundTargets);
        setTargetSet(TargetSetSlot.QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTargets);
    }

    public void capturePickerState(TabletUiState state) {
        if (state == null || !active()) {
            return;
        }
        switch (type) {
            case ICON_PICKER -> capturePicker(state.pickers.iconSearch, state.pickers.iconSearchFocused, state.pickers.iconScroll, state.pickers.iconScrollDragging, "", state.pickers.iconMode.name());
            case ASSET_PICKER -> capturePicker(state.pickers.assetSearch, state.pickers.assetSearchFocused, state.pickers.assetGridScroll, state.pickers.assetGridScrollDragging, state.pickers.assetSelected, state.pickers.assetBrowseDir);
            case BIOME_PICKER -> capturePicker(state.pickers.biomeSearch, state.pickers.biomeSearchFocused, state.pickers.biomeScroll, state.pickers.biomeScrollDragging, "", "");
            case ADVANCEMENT_PICKER -> capturePicker(state.pickers.advancementSearch, state.pickers.advancementSearchFocused, state.pickers.advancementScroll, state.pickers.advancementScrollDragging, "", "");
            case RECIPE_PICKER -> capturePicker(state.pickers.recipeSearch, state.pickers.recipeSearchFocused, state.pickers.recipeScroll, state.pickers.recipeScrollDragging, "", state.pickers.recipeMode.name());
            case STRUCTURE_PICKER -> capturePicker(state.pickers.structureSearch, state.pickers.structureSearchFocused, state.pickers.structureScroll, state.pickers.structureScrollDragging, "", "");
            case BLOCK_PICKER -> capturePicker(state.pickers.blockSearch, state.pickers.blockSearchFocused, state.pickers.blockScroll, state.pickers.blockScrollDragging, "", state.pickers.blockTagMode ? "TAGS" : "BLOCKS");
            case STAT_PICKER -> capturePicker(state.pickers.statSearch, state.pickers.statSearchFocused, state.pickers.statScroll, state.pickers.statScrollDragging, "", "");
            case DIMENSION_PICKER -> capturePicker(state.pickers.dimensionSearch, state.pickers.dimensionSearchFocused, state.pickers.dimensionScroll, state.pickers.dimensionScrollDragging, "", "");
            case LOOT_TABLE_PICKER -> capturePicker(state.pickers.lootTableSearch, state.pickers.lootTableSearchFocused, state.pickers.lootTableScroll, state.pickers.lootTableScrollDragging, "", "");
            case ITEM_INVENTORY_PICKER -> capturePicker(state.pickers.itemInventorySearch, state.pickers.itemInventorySearchFocused, state.pickers.itemInventoryScroll, state.pickers.itemInventoryScrollDragging, "", "");
            case SOUND_PICKER -> capturePicker(state.pickers.soundSearch, state.pickers.soundSearchFocused, state.pickers.soundScroll, state.pickers.soundScrollDragging, state.pickers.soundSelected, "");
            case ENTITY_VARIANT_PICKER -> capturePicker(state.pickers.entityVariantSearch, state.pickers.entityVariantSearchFocused, state.pickers.entityVariantScroll, state.pickers.entityVariantScrollDragging, state.pickers.entityVariantSelected, state.pickers.entityVariantFolder);
            case PREREQUISITES_MANAGER -> capturePicker(state.modal.prerequisitesManagerSearch, state.modal.prerequisitesManagerSearchFocused, state.modal.prerequisitesManagerScroll, state.modal.prerequisitesManagerScrollDragging, "", state.modal.prerequisitesManagerExternalMode ? "EXTERNAL" : "LOCAL");
            case COLOR_PICKER -> capturePicker("", false, state.pickers.colorPaletteScroll, state.pickers.colorPaletteScrollDragging, Integer.toString(state.pickers.colorDraft), "");
            case THEME_PICKER -> capturePicker("", false, state.modal.themeScroll, state.modal.themeScrollDragging, "", "");
            case SETTINGS_PANEL -> capturePicker("", false, state.modal.settingsScroll, state.modal.settingsScrollDragging, "", Integer.toString(state.modal.settingsTab));
            case NONE -> capturePicker("", false, 0, false, "", "");
        }
    }

    private void capturePicker(String search, boolean focused, int scroll, boolean dragging, String selectedValue, String mode) {
        picker.setSearch(search);
        picker.setFocused(focused);
        picker.setScroll(scroll);
        picker.setDragging(dragging);
        this.selectedValue = clean(selectedValue);
        this.mode = clean(mode);
    }

    private void setTarget(TargetSlot slot, String value) {
        String clean = clean(value);
        if (!clean.isBlank()) {
            targets.put(slot, clean);
        }
    }

    private void setTargetSet(TargetSetSlot slot, Set<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        LinkedHashSet<String> cleanValues = new LinkedHashSet<>();
        for (String value : values) {
            String clean = clean(value);
            if (!clean.isBlank()) {
                cleanValues.add(clean);
            }
        }
        if (!cleanValues.isEmpty()) {
            targetSets.put(slot, cleanValues);
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    public enum TargetSlot {
        QUEST,
        CHAPTER,
        QUEST_DETAILS_PICK,
        QUEST_DETAILS_ASSET_PICK,
        CANVAS_BACKGROUND,
        CANVAS_IMAGE,
        CANVAS_ENTITY,
        CANVAS_MODEL,
        BLUEPRINT,
        QUEST_BACKGROUND,
        QUEST_COMPLETION_HUD_BACKGROUND,
        HUD_BACKGROUND,
        QUEST_COMPLETION_SOUND,
        ENTITY_VARIANT,
        COLOR_PICKER,
        PREREQUISITES_MANAGER
    }

    public enum TargetSetSlot {
        QUEST_BACKGROUND,
        QUEST_COMPLETION_HUD_BACKGROUND,
        QUEST_COMPLETION_SOUND
    }

    public static final class PickerSession {
        private String search = "";
        private boolean focused;
        private int scroll;
        private boolean dragging;

        private PickerSession() {
        }

        public String search() {
            return search;
        }

        public boolean focused() {
            return focused;
        }

        public int scroll() {
            return scroll;
        }

        public boolean dragging() {
            return dragging;
        }

        private void setSearch(String search) {
            this.search = clean(search);
        }

        private void setFocused(boolean focused) {
            this.focused = focused;
        }

        private void setScroll(int scroll) {
            this.scroll = scroll;
        }

        private void setDragging(boolean dragging) {
            this.dragging = dragging;
        }
    }
}
