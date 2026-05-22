package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TabletDimensionPickerModal {
    private TabletDimensionPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        QuestVocabulary.text(QuestVocabulary.CHOOSE_DIMENSION),
                        QuestVocabulary.text(QuestVocabulary.NO_DIMENSIONS),
                        "dimension",
                        () -> state.dimensionSearch,
                        value -> state.dimensionSearch = value,
                        value -> state.dimensionScroll = value,
                        focused -> state.dimensionSearchFocused = focused,
                        ScrollState.bind(
                                () -> state.dimensionScroll,
                                value -> state.dimensionScroll = value,
                                () -> state.dimensionScrollDragging,
                                dragging -> state.dimensionScrollDragging = dragging
                        ),
                        TabletDimensionPickerModal::dimensions,
                        TabletDimensionPickerModal::displayName,
                        QuestDetailsWindow::applyDimensionPick,
                        "minecraft:compass",
                        24,
                        48,
                        150,
                        80,
                        80
                ));
    }

    private static List<String> dimensions(String query) {
        Set<String> found = new LinkedHashSet<>();
        if (Minecraft.getInstance().getConnection() != null) {
            for (ResourceKey<Level> key : Minecraft.getInstance().getConnection().levels()) {
                found.add(key.location().toString());
            }
        }
        if (found.isEmpty()) {
            found.add("minecraft:overworld");
            found.add("minecraft:the_nether");
            found.add("minecraft:the_end");
        }
        List<String> values = new ArrayList<>(found);
        values.sort(Comparator.naturalOrder());
        if (SearchFilter.normalize(query).isBlank()) {
            return values;
        }
        return values.stream().filter(value -> SearchFilter.matches(query, value, displayName(value))).toList();
    }

    private static String displayName(String dimension) {
        if ("minecraft:overworld".equals(dimension)) {
            return "Overworld";
        }
        if ("minecraft:the_nether".equals(dimension)) {
            return "The Nether";
        }
        if ("minecraft:the_end".equals(dimension)) {
            return "The End";
        }
        return DisplayNameFormatter.resourceLeaf(dimension);
    }
}
