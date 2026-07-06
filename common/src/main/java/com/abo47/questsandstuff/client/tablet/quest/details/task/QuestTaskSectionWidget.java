package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsCoordinates;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;

final class QuestTaskSectionWidget {
    private QuestTaskSectionWidget() {
    }

    static void renderTasks(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        List<QuestDetailsTaskEntry> tasks = QuestTaskEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING));
        WidgetGroup section = sectionWidget(state, player, refresh, questId, x, y, w, h, "tasks", tasks, QuestDetailsTasksPanel.TITLE_H, 4, false);
        section.addWidget(label(8, 6, QuestTranslationKeys.tasks(), TabletColors.TEXT_PRIMARY));
        addChangeIconButton(section, state, refresh, questId, "tasks", w);
        renderCards(section, state, player, refresh, questId, tasks, w, h, QuestDetailsTasksPanel.TITLE_H, true);
        modal.addWidget(section);
    }

    static void renderRewards(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        List<QuestDetailsTaskEntry> rewards = QuestTaskEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING));
        List<QuestDetailsTaskEntry> displayRewards = QuestTaskSelectableRewards.displayEntries(rewards, QuestDetailsEditController.canEdit(state));
        boolean rewardsClaimed = quest.getBoolean("claimed") || questId.equals(state.questDetails.questDetailsClaimedOverrideQuestId);
        WidgetGroup section = sectionWidget(state, player, refresh, questId, x, y, w, h, "rewards", displayRewards, QuestDetailsTasksPanel.TITLE_H, 4, rewardsClaimed);
        section.addWidget(label(8, 6, QuestTranslationKeys.rewards(), TabletColors.TEXT_PRIMARY));
        addChangeIconButton(section, state, refresh, questId, "rewards", w);
        renderCards(section, state, player, refresh, questId, displayRewards, w, h, QuestDetailsTasksPanel.TITLE_H, false, rewardsClaimed);
        modal.addWidget(section);
    }

    private static WidgetGroup sectionWidget(TabletUiState state, Player player, Runnable refresh, String questId, int x, int y, int w, int h, String kind, List<QuestDetailsTaskEntry> entries, int listY, int bottomPad, boolean rewardsClaimed) {
        WidgetGroup section = new WidgetGroup(x, y, w, h) {
            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!isMouseOverElement(mouseX, mouseY)) {
                    return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                }
                int delta = wheelDelta < 0 ? 16 : -16;
                if ("tasks".equals(kind)) {
                    state.questDetails.questDetailsTaskScroll = Math.max(0, state.questDetails.questDetailsTaskScroll + delta);
                } else {
                    state.questDetails.questDetailsRewardScroll = Math.max(0, state.questDetails.questDetailsRewardScroll + delta);
                }
                refresh.run();
                return true;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (!isMouseOverElement(mouseX, mouseY)) {
                    return super.mouseClicked(mouseX, mouseY, button);
                }
                int lx = QuestDetailsCoordinates.localX(state, mouseX, getPositionX(), w);
                int ly = QuestDetailsCoordinates.localY(state, mouseY, getPositionY(), h);
                QuestDetailsTaskEntry hitEntry = hitEntry(state, entries, kind, listY, h - bottomPad, ly);
                String id = hitEntry == null ? "" : hitEntry.id();
                boolean editMode = QuestDetailsEditController.canEdit(state);
                boolean cardBodyHit = isCardBodyHit(lx, w);
                boolean claimChoice = "rewards".equals(kind)
                        && !rewardsClaimed
                        && QuestTaskSelectableRewards.isClaimChoiceEntry(hitEntry);
                if (button == 0 && !id.isBlank() && cardBodyHit) {
                    if (claimChoice) {
                        QuestTaskSelectableRewards.selectChoice(state, id);
                    }
                    if (editMode) {
                        QuestTaskListInteractions.selectAndBeginDrag(state, kind, id, mouseX, mouseY);
                        refresh.run();
                        return true;
                    }
                    if (claimChoice) {
                        refresh.run();
                        return true;
                    }
                }
                if (button == 1 && editMode) {
                    QuestTaskListInteractions.select(state, kind, id);
                    QuestDetailsCoordinates.openContextAtPointer(state, "tasks".equals(kind) ? "task" : "reward", id, mouseX, mouseY, getPositionX(), getPositionY(), lx, ly);
                    refresh.run();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                if (isTaskScrollDragging(state, kind)) {
                    return super.mouseDragged(QuestDetailsCoordinates.screenX(state, getPositionX()) + scrollbarX(w), mouseY, button, dragX, dragY);
                }
                int ly = QuestDetailsCoordinates.localY(state, mouseY, getPositionY(), h);
                if (QuestTaskListInteractions.handleDrag(player, state, refresh, questId, entries, kind, listY, h - bottomPad, ly, mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (isTaskScrollDragging(state, kind)) {
                    return super.mouseReleased(QuestDetailsCoordinates.screenX(state, getPositionX()) + scrollbarX(w), mouseY, button);
                }
                if (QuestTaskListInteractions.handleRelease(player, state, refresh, questId, entries, kind)) {
                    return true;
                }
                return super.mouseReleased(mouseX, mouseY, button);
            }
        };
        section.setBackground(SurfaceFactory.insetPanel());
        return section;
    }

    private static boolean isTaskScrollDragging(TabletUiState state, String kind) {
        return "tasks".equals(kind) ? state.questDetails.questDetailsTaskScrollDragging : state.questDetails.questDetailsRewardScrollDragging;
    }

    private static int scrollbarX(int sectionW) {
        return Math.max(0, sectionW - DragScrollBarWidget.RESERVED_WIDTH / 2);
    }

    private static void renderCards(WidgetGroup section, TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsTaskEntry> entries, int w, int h, int listY, boolean tasks) {
        renderCards(section, state, player, refresh, questId, entries, w, h, listY, tasks, false);
    }

    private static void renderCards(WidgetGroup section, TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsTaskEntry> entries, int w, int h, int listY, boolean tasks, boolean rewardsClaimed) {
        int visibleH = Math.max(1, h - listY - 4);
        int maxStart = scrollMax(entries, visibleH);
        if (tasks) {
            state.questDetails.questDetailsTaskScroll = ScrollMath.clamp(state.questDetails.questDetailsTaskScroll, maxStart);
        } else {
            state.questDetails.questDetailsRewardScroll = ScrollMath.clamp(state.questDetails.questDetailsRewardScroll, maxStart);
        }
        String kind = tasks ? "tasks" : "rewards";
        int listW = maxStart > 0 ? w - DragScrollBarWidget.RESERVED_WIDTH : w;
        WidgetGroup list = clippedList(0, listY, listW, visibleH, state, player, refresh, questId, entries, kind, listY, h - 4);
        section.addWidget(list);
        SkinAnchorRegistry.register(tasks ? "quests_task_cards" : "quests_reward_cards", list);
        int scroll = tasks ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll;
        if (entries.isEmpty()) {
            return;
        }
        int cardW = maxStart > 0 ? w - 12 - DragScrollBarWidget.RESERVED_WIDTH : w - 12;
        int y = QuestDetailsTasksPanel.LIST_PAD - scroll;
        int ghostIndex = Math.max(0, Math.min(entries.size(), state.questDetails.questDetailsTaskDragTargetIndex));
        boolean showGhost = state.questDetails.questDetailsTaskDragActive
                && kind.equals(state.questDetails.questDetailsTaskDragKind)
                && !state.questDetails.questDetailsTaskDragId.isBlank();
        QuestDetailsTaskEntry ghostEntry = draggedEntry(entries, state.questDetails.questDetailsTaskDragId);
        for (int index = 0; index < entries.size(); index++) {
            if (showGhost && index == ghostIndex) {
                QuestTaskGhostCards.render(list, ghostEntry, tasks, 6, y, cardW);
                y += QuestDetailsTasksPanel.CARD_H + QuestDetailsTasksPanel.CARD_GAP;
            }
            QuestDetailsTaskEntry entry = entries.get(index);
            if (y < visibleH && y + QuestDetailsTasksPanel.CARD_H > 0) {
                if (tasks) {
                    QuestTaskCardRenderer.renderTaskCard(list, state, player, refresh, questId, entry, 6, y, cardW, entries, listY, h - 4);
                } else {
                    QuestTaskCardRenderer.renderRewardCard(list, state, player, refresh, questId, entry, 6, y, cardW, entries, listY, h - 4, rewardsClaimed);
                }
            }
            y += QuestDetailsTasksPanel.CARD_H + QuestDetailsTasksPanel.CARD_GAP;
        }
        if (showGhost && ghostIndex == entries.size()) {
            QuestTaskGhostCards.render(list, ghostEntry, tasks, 6, y, cardW);
        }
        renderScrollbar(section, state, refresh, tasks, w, listY, visibleH, maxStart);
    }

    private static QuestDetailsTaskEntry draggedEntry(List<QuestDetailsTaskEntry> entries, String id) {
        for (QuestDetailsTaskEntry entry : entries) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    private static WidgetGroup clippedList(int x, int y, int w, int h, TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsTaskEntry> entries, String kind, int listY, int listBottom) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public boolean isMouseOverElement(double mouseX, double mouseY) {
                return inside(mouseX, mouseY);
            }

            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                return inside(mouseX, mouseY) && super.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return inside(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                int localY = QuestDetailsCoordinates.localY(state, mouseY, getPositionY(), h) + listY;
                if (QuestTaskListInteractions.handleDrag(player, state, refresh, questId, entries, kind, listY, listBottom, localY, mouseX, mouseY, button)) {
                    return true;
                }
                return inside(mouseX, mouseY) && super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (QuestTaskListInteractions.handleRelease(player, state, refresh, questId, entries, kind)) {
                    return true;
                }
                return inside(mouseX, mouseY) && super.mouseReleased(mouseX, mouseY, button);
            }

            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(),
                        () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
            }

            @Override
            public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(),
                        () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
            }

            @Override
            public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(),
                        () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
            }

            private boolean inside(double mouseX, double mouseY) {
                int left = getPositionX();
                int top = getPositionY();
                return mouseX >= left && mouseX < left + getSizeWidth() && mouseY >= top && mouseY < top + getSizeHeight();
            }
        };
    }

    private static void addChangeIconButton(WidgetGroup section, TabletUiState state, Runnable refresh, String questId, String kind, int w) {
        if (!QuestDetailsEditController.canEdit(state)) return;
        String selectedKind = state.questDetails.questDetailsSelectedTaskKind;
        String selectedId = state.questDetails.questDetailsSelectedTaskId;
        if (!kind.equals(selectedKind) || selectedId == null || selectedId.isBlank()) return;
        boolean task = "tasks".equals(kind);
        String target = task ? ModalTargets.taskIcon(questId, selectedId) : ModalTargets.rewardIcon(questId, selectedId);
        var btn = IconOnlyButton.create(
                w - QuestDetailsTasksPanel.HEADER_H,
                1,
                QuestDetailsTasksPanel.HEADER_H - 2,
                "icon",
                TabletColors.INTERACTIVE,
                click -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestDetailsWindow.openIconPicker(state, target);
                    refresh.run();
                }
        );
        btn.tooltips(new Component[]{TabletTranslationKeys.component(QuestTranslationKeys.CONTEXT_CHANGE_ICON)});
        section.addWidget(btn);
    }

    static int scrollMax(List<QuestDetailsTaskEntry> entries, int visibleH) {
        if (entries.isEmpty()) {
            return 0;
        }
        int contentH = QuestDetailsTasksPanel.LIST_PAD * 2
                + entries.size() * QuestDetailsTasksPanel.CARD_H
                + Math.max(0, entries.size() - 1) * QuestDetailsTasksPanel.CARD_GAP;
        return Math.max(0, contentH - visibleH);
    }

    private static void renderScrollbar(WidgetGroup section, TabletUiState state, Runnable refresh, boolean tasks, int w, int y, int h, int maxStart) {
        if (maxStart <= 0) {
            return;
        }
        int knobH = Math.max(12, Math.min(h, Math.round((float) h * ((float) h / (float) (h + maxStart)))));
        section.addWidget(new DragScrollBarWidget(
                w - DragScrollBarWidget.RESERVED_WIDTH,
                y,
                DragScrollBarWidget.RESERVED_WIDTH,
                h,
                () -> tasks ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll,
                () -> maxStart,
                () -> knobH,
                value -> {
                    if (tasks) {
                        state.questDetails.questDetailsTaskScroll = value;
                    } else {
                        state.questDetails.questDetailsRewardScroll = value;
                    }
                },
                () -> tasks ? state.questDetails.questDetailsTaskScrollDragging : state.questDetails.questDetailsRewardScrollDragging,
                dragging -> {
                    if (tasks) {
                        state.questDetails.questDetailsTaskScrollDragging = dragging;
                    } else {
                        state.questDetails.questDetailsRewardScrollDragging = dragging;
                    }
                },
                refresh,
                TabletColors.scrollTrack(tasks ? state.questDetails.questDetailsTaskScrollDragging : state.questDetails.questDetailsRewardScrollDragging),
                TabletColors.scrollThumb(false),
                TabletColors.scrollThumb(true),
                DragScrollBarWidget.WIDTH
        ));
    }

    private static QuestDetailsTaskEntry hitEntry(TabletUiState state, List<QuestDetailsTaskEntry> entries, String kind, int listY, int listBottom, int localY) {
        if (localY < listY || localY > listBottom || entries.isEmpty()) {
            return null;
        }
        int scroll = "tasks".equals(kind) ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll;
        int contentY = localY - listY + scroll - QuestDetailsTasksPanel.LIST_PAD;
        if (contentY < 0) {
            return null;
        }
        int slot = contentY / (QuestDetailsTasksPanel.CARD_H + QuestDetailsTasksPanel.CARD_GAP);
        int inSlot = contentY % (QuestDetailsTasksPanel.CARD_H + QuestDetailsTasksPanel.CARD_GAP);
        if (slot < 0 || slot >= entries.size() || inSlot >= QuestDetailsTasksPanel.CARD_H) {
            return null;
        }
        return entries.get(slot);
    }

    private static boolean isCardBodyHit(int localX, int sectionW) {
        return localX >= 30 && localX <= sectionW - 44;
    }
}
