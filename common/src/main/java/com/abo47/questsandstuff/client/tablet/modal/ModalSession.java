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
        setTarget(TargetSlot.QUEST, state.modalQuestTarget);
        setTarget(TargetSlot.CHAPTER, state.modalChapterTarget);
        setTarget(TargetSlot.QUEST_DETAILS_PICK, state.questDetailsPickTarget);
        setTarget(TargetSlot.QUEST_DETAILS_ASSET_PICK, state.questDetailsAssetPickTarget);
        setTarget(TargetSlot.CANVAS_BACKGROUND, state.modalCanvasBackgroundTarget);
        setTarget(TargetSlot.CANVAS_IMAGE, state.modalCanvasImageTarget);
        setTarget(TargetSlot.CANVAS_ENTITY, state.modalCanvasEntityTarget);
        setTarget(TargetSlot.CANVAS_MODEL, state.modalCanvasModelTarget);
        setTarget(TargetSlot.BLUEPRINT, state.modalBlueprintTarget);
        setTarget(TargetSlot.QUEST_BACKGROUND, state.modalQuestBackgroundTarget);
        setTarget(TargetSlot.QUEST_COMPLETION_HUD_BACKGROUND, state.modalQuestCompletionHudBackgroundTarget);
        setTarget(TargetSlot.HUD_BACKGROUND, state.modalHudBackgroundTarget);
        setTarget(TargetSlot.QUEST_COMPLETION_SOUND, state.modalQuestCompletionSoundTarget);
        setTarget(TargetSlot.ENTITY_VARIANT, state.entityVariantTarget);
        setTarget(TargetSlot.COLOR_PICKER, state.colorPickerTarget);
        setTarget(TargetSlot.PREREQUISITES_MANAGER, state.prerequisitesManagerQuestId);
        setTargetSet(TargetSetSlot.QUEST_BACKGROUND, state.modalQuestBackgroundTargets);
        setTargetSet(TargetSetSlot.QUEST_COMPLETION_HUD_BACKGROUND, state.modalQuestCompletionHudBackgroundTargets);
        setTargetSet(TargetSetSlot.QUEST_COMPLETION_SOUND, state.modalQuestCompletionSoundTargets);
    }

    public void capturePickerState(TabletUiState state) {
        if (state == null || !active()) {
            return;
        }
        switch (type) {
            case ICON_PICKER -> capturePicker(state.iconSearch, state.iconSearchFocused, state.iconScroll, state.iconScrollDragging, "", state.iconMode.name());
            case ASSET_PICKER -> capturePicker(state.assetSearch, state.assetSearchFocused, state.assetGridScroll, state.assetGridScrollDragging, state.assetSelected, state.assetBrowseDir);
            case BIOME_PICKER -> capturePicker(state.biomeSearch, state.biomeSearchFocused, state.biomeScroll, state.biomeScrollDragging, "", "");
            case ADVANCEMENT_PICKER -> capturePicker(state.advancementSearch, state.advancementSearchFocused, state.advancementScroll, state.advancementScrollDragging, "", "");
            case RECIPE_PICKER -> capturePicker(state.recipeSearch, state.recipeSearchFocused, state.recipeScroll, state.recipeScrollDragging, "", state.recipeMode.name());
            case STRUCTURE_PICKER -> capturePicker(state.structureSearch, state.structureSearchFocused, state.structureScroll, state.structureScrollDragging, "", "");
            case BLOCK_PICKER -> capturePicker(state.blockSearch, state.blockSearchFocused, state.blockScroll, state.blockScrollDragging, "", state.blockTagMode ? "TAGS" : "BLOCKS");
            case STAT_PICKER -> capturePicker(state.statSearch, state.statSearchFocused, state.statScroll, state.statScrollDragging, "", "");
            case DIMENSION_PICKER -> capturePicker(state.dimensionSearch, state.dimensionSearchFocused, state.dimensionScroll, state.dimensionScrollDragging, "", "");
            case LOOT_TABLE_PICKER -> capturePicker(state.lootTableSearch, state.lootTableSearchFocused, state.lootTableScroll, state.lootTableScrollDragging, "", "");
            case ITEM_INVENTORY_PICKER -> capturePicker(state.itemInventorySearch, state.itemInventorySearchFocused, state.itemInventoryScroll, state.itemInventoryScrollDragging, "", "");
            case SOUND_PICKER -> capturePicker(state.soundSearch, state.soundSearchFocused, state.soundScroll, state.soundScrollDragging, state.soundSelected, "");
            case ENTITY_VARIANT_PICKER -> capturePicker(state.entityVariantSearch, state.entityVariantSearchFocused, state.entityVariantScroll, state.entityVariantScrollDragging, state.entityVariantSelected, state.entityVariantFolder);
            case PREREQUISITES_MANAGER -> capturePicker(state.prerequisitesManagerSearch, state.prerequisitesManagerSearchFocused, state.prerequisitesManagerScroll, state.prerequisitesManagerScrollDragging, "", state.prerequisitesManagerExternalMode ? "EXTERNAL" : "LOCAL");
            case COLOR_PICKER -> capturePicker("", false, state.colorPaletteScroll, state.colorPaletteScrollDragging, Integer.toString(state.colorDraft), "");
            case THEME_PICKER -> capturePicker("", false, state.themeScroll, state.themeScrollDragging, "", "");
            case SETTINGS_PANEL -> capturePicker("", false, state.settingsScroll, state.settingsScrollDragging, "", Integer.toString(state.settingsTab));
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
