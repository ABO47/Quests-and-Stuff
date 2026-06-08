package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.TileGridLayout;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.modal.ModalContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.modal.ModalLibraryLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;

final class PrerequisiteRowsPanel {
    private static final String CONTEXT_ANIMATION_KEY = "prerequisites_manager";
    private static final int ROW_H = 30;
    private static final int ROW_GAP = 4;
    private static final int PAD = 6;

    private PrerequisiteRowsPanel() {
    }

    static void add(WidgetGroup modal, TabletUiState state, Runnable refresh, ModalLibraryLayout.Metrics layout, int modalW, int modalH, String questId, List<PrerequisiteConnectionRow> rows) {
        int rowW = rowWidth(layout, rows.size());
        TileGridLayout rowLayout = TileGridLayout.calculate(layout.rightW(), layout.bodyH(), rowW, ROW_H, ROW_GAP, PAD, PAD, rows.size(), state.prerequisitesManagerScroll);
        state.prerequisitesManagerScroll = rowLayout.scrollStart();
        addConnectionHoverTracker(modal, state, rows, layout, rowLayout);
        addConnectionList(modal, state, refresh, layout, modalW, modalH, questId, rows, rowW);
    }

    private static int rowWidth(ModalLibraryLayout.Metrics layout, int rowCount) {
        int contentH = Math.max(ROW_H, layout.bodyH() - PAD * 2);
        int visibleRows = Math.max(1, (contentH + ROW_GAP) / (ROW_H + ROW_GAP));
        boolean showScroll = rowCount > visibleRows;
        int scrollReserve = showScroll ? DragScrollBarWidget.RESERVED_WIDTH + ROW_GAP : 0;
        return Math.max(96, layout.rightW() - PAD * 2 - scrollReserve);
    }

    private static void addConnectionList(WidgetGroup modal, TabletUiState state, Runnable refresh, ModalLibraryLayout.Metrics layout, int modalW, int modalH, String questId, List<PrerequisiteConnectionRow> rows, int rowW) {
        TiledPickerPanel.add(
                modal,
                layout.rightX(),
                layout.bodyY(),
                layout.rightW(),
                layout.bodyH(),
                rowW,
                ROW_H,
                ROW_GAP,
                PAD,
                PAD,
                rows,
                TabletVocabulary.text(QuestVocabulary.PREREQUISITES_NO_CONNECTIONS),
                ScrollState.bind(
                        () -> state.prerequisitesManagerScroll,
                        value -> state.prerequisitesManagerScroll = value,
                        () -> state.prerequisitesManagerScrollDragging,
                        dragging -> state.prerequisitesManagerScrollDragging = dragging
                ),
                () -> {
                    state.prerequisitesManagerContextOpen = false;
                    state.contextDeleteConfirmKey = "";
                },
                refresh,
                (surface, row, index, x, y, cellW, cellH, tileLayout) -> renderConnectionRow(surface, state, refresh, modalW, modalH, questId, row, x, y, cellW, cellH)
        );
    }

    private static void renderConnectionRow(WidgetGroup surface, TabletUiState state, Runnable refresh, int modalW, int modalH, String questId, PrerequisiteConnectionRow row, int x, int y, int cellW, int cellH) {
        boolean selected = row.key().equals(state.prerequisitesManagerSelectedConnectionKey);
        boolean hovered = row.key().equals(state.prerequisitesManagerHoveredConnectionKey);
        WidgetGroup card = new WidgetGroup(x, y, cellW, cellH);
        card.setBackground(Surfaces.bordered(
                selected || hovered ? withAlpha(ModColors.INTERACTIVE, selected ? 64 : 44) : withAlpha(ModColors.SURFACE_PANEL_ALT, 106),
                selected || hovered ? ModColors.BORDER_ACCENT : withAlpha(ModColors.BORDER_BASE, 120)
        ));
        card.addWidget(new DisplayIconWidget(5, 7, 16, 16, row.icon()));
        int textW = Math.max(10, cellW - 34);
        String role = TabletVocabulary.text(row.kind() == PrerequisiteConnectionKind.INCOMING ? QuestVocabulary.PREREQUISITES_INCOMING : QuestVocabulary.PREREQUISITES_OUTGOING);
        card.addWidget(label(26, 4, crop(role + ": " + row.otherTitle(), Math.max(8, textW / 6)), ModColors.TEXT_SECONDARY));
        card.addWidget(label(26, 17, crop(row.sourceTitle() + " -> " + row.targetTitle(), Math.max(8, textW / 6)), ModColors.TEXT_MUTED));
        surface.addWidget(card);

        ButtonWidget hit = flatHitButton(x, y, cellW, cellH, click -> {
            state.prerequisitesManagerSelectedConnectionKey = row.key();
            if (click.button == 1) {
                openConnectionContextAtPointer(state, modalW, modalH, row);
            } else if (click.button == 0) {
                state.prerequisitesManagerContextOpen = false;
                state.contextDeleteConfirmKey = "";
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] prerequisites manager row_click quest={} connection={} button={}", questId, row.key(), click.button);
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 38)));
        surface.addWidget(hit);
    }

    private static void addConnectionHoverTracker(WidgetGroup modal, TabletUiState state, List<PrerequisiteConnectionRow> rows, ModalLibraryLayout.Metrics layout, TileGridLayout rowLayout) {
        WidgetGroup tracker = new WidgetGroup(layout.rightX(), layout.bodyY(), layout.rightW(), layout.bodyH()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                state.prerequisitesManagerHoveredConnectionKey = hoveredConnectionKey(this, rows, rowLayout, mouseX, mouseY);
            }
        };
        modal.addWidget(tracker);
    }

    static String hoveredConnectionKey(WidgetGroup tracker, List<PrerequisiteConnectionRow> rows, TileGridLayout layout, int mouseX, int mouseY) {
        int localX = mouseX - tracker.getPositionX();
        int localY = mouseY - tracker.getPositionY();
        if (localX < 0 || localY < 0 || localX >= tracker.getSizeWidth() || localY >= tracker.getSizeHeight()) {
            return "";
        }
        for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
            int visibleIndex = i - layout.scrollStart();
            int rowX = layout.tileX(visibleIndex);
            int rowY = layout.tileY(visibleIndex);
            if (localX >= rowX && localX < rowX + layout.tileW() && localY >= rowY && localY < rowY + layout.tileH()) {
                return rows.get(i).key();
            }
        }
        return "";
    }

    private static void openConnectionContextAtPointer(TabletUiState state, int modalW, int modalH, PrerequisiteConnectionRow row) {
        state.prerequisitesManagerContextOpen = true;
        state.prerequisitesManagerContextPrerequisiteId = row.sourceId();
        state.prerequisitesManagerSelectedConnectionKey = row.key();
        state.prerequisitesManagerContextX = ModalContextMenuPlacement.localPointerX(state, modalW);
        state.prerequisitesManagerContextY = ModalContextMenuPlacement.localPointerY(state, modalH);
        state.contextDeleteConfirmKey = "";
        ContextMenuAnimation.start(state, CONTEXT_ANIMATION_KEY);
    }
}
