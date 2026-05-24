package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsMouse;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class QuestObjectiveSectionWidget {
    private QuestObjectiveSectionWidget() {
    }

    static void renderRequirements(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        List<QuestDetailsObjectiveEntry> tasks = QuestObjectiveEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING));
        WidgetGroup section = sectionWidget(state, player, refresh, questId, x, y, w, h, "requirements", tasks, QuestDetailsObjectivesPanel.TITLE_H, 4, false);
        section.addWidget(label(8, 6, QuestVocabulary.requirements(), ModColors.TEXT_PRIMARY));
        renderCards(section, state, player, refresh, questId, tasks, w, h, QuestDetailsObjectivesPanel.TITLE_H, true);
        modal.addWidget(section);
    }

    static void renderRewards(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        List<QuestDetailsObjectiveEntry> rewards = QuestObjectiveEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING));
        List<QuestDetailsObjectiveEntry> displayRewards = QuestObjectiveSelectableRewards.displayEntries(rewards, QuestDetailsEditState.canEdit(state));
        boolean rewardsClaimed = quest.getBoolean("claimed") || questId.equals(state.questDetailsClaimedOverrideQuestId);
        WidgetGroup section = sectionWidget(state, player, refresh, questId, x, y, w, h, "rewards", displayRewards, QuestDetailsObjectivesPanel.TITLE_H, 4, rewardsClaimed);
        section.addWidget(label(8, 6, QuestVocabulary.rewards(), ModColors.TEXT_PRIMARY));
        renderCards(section, state, player, refresh, questId, displayRewards, w, h, QuestDetailsObjectivesPanel.TITLE_H, false, rewardsClaimed);
        modal.addWidget(section);
    }

    private static WidgetGroup sectionWidget(TabletUiState state, Player player, Runnable refresh, String questId, int x, int y, int w, int h, String kind, List<QuestDetailsObjectiveEntry> entries, int listY, int bottomPad, boolean rewardsClaimed) {
        WidgetGroup section = new WidgetGroup(x, y, w, h) {
            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!isMouseOverElement(mouseX, mouseY)) {
                    return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                }
                int delta = wheelDelta < 0 ? 16 : -16;
                if ("requirements".equals(kind)) {
                    state.questDetailsReqScroll = Math.max(0, state.questDetailsReqScroll + delta);
                } else {
                    state.questDetailsRewardScroll = Math.max(0, state.questDetailsRewardScroll + delta);
                }
                refresh.run();
                return true;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (!isMouseOverElement(mouseX, mouseY)) {
                    return super.mouseClicked(mouseX, mouseY, button);
                }
                int lx = QuestDetailsMouse.localX(state, mouseX, getPositionX(), w);
                int ly = QuestDetailsMouse.localY(state, mouseY, getPositionY(), h);
                QuestDetailsObjectiveEntry hitEntry = hitEntry(state, entries, kind, listY, h - bottomPad, ly);
                String id = hitEntry == null ? "" : hitEntry.id();
                boolean editMode = QuestDetailsEditState.canEdit(state);
                boolean cardBodyHit = isCardBodyHit(lx, w);
                boolean claimChoice = "rewards".equals(kind)
                        && !rewardsClaimed
                        && QuestObjectiveSelectableRewards.isClaimChoiceEntry(hitEntry);
                if (button == 0 && !id.isBlank() && cardBodyHit) {
                    if (claimChoice) {
                        QuestObjectiveSelectableRewards.selectChoice(state, id);
                    }
                    if (editMode) {
                        QuestObjectiveListInteractions.selectAndBeginDrag(state, kind, id, mouseX, mouseY);
                        refresh.run();
                        return true;
                    }
                    if (claimChoice) {
                        refresh.run();
                        return true;
                    }
                }
                if (button == 1 && editMode) {
                    QuestObjectiveListInteractions.select(state, kind, id);
                    QuestDetailsMouse.openContextAtPointer(state, "requirements".equals(kind) ? "requirement" : "reward", id, mouseX, mouseY, getPositionX(), getPositionY(), lx, ly);
                    refresh.run();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                if (isObjectiveScrollDragging(state, kind)) {
                    return super.mouseDragged(QuestDetailsMouse.screenX(state, getPositionX()) + scrollbarX(w), mouseY, button, dragX, dragY);
                }
                int ly = QuestDetailsMouse.localY(state, mouseY, getPositionY(), h);
                if (QuestObjectiveListInteractions.handleDrag(player, state, refresh, questId, entries, kind, listY, h - bottomPad, ly, mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (isObjectiveScrollDragging(state, kind)) {
                    return super.mouseReleased(QuestDetailsMouse.screenX(state, getPositionX()) + scrollbarX(w), mouseY, button);
                }
                if (QuestObjectiveListInteractions.handleRelease(player, state, refresh, questId, entries, kind)) {
                    return true;
                }
                return super.mouseReleased(mouseX, mouseY, button);
            }
        };
        section.setBackground(Surfaces.insetPanel());
        return section;
    }

    private static boolean isObjectiveScrollDragging(TabletUiState state, String kind) {
        return "requirements".equals(kind) ? state.questDetailsReqScrollDragging : state.questDetailsRewardScrollDragging;
    }

    private static int scrollbarX(int sectionW) {
        return Math.max(0, sectionW - DragScrollBarWidget.RESERVED_WIDTH / 2);
    }

    private static void renderCards(WidgetGroup section, TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, int w, int h, int listY, boolean requirements) {
        renderCards(section, state, player, refresh, questId, entries, w, h, listY, requirements, false);
    }

    private static void renderCards(WidgetGroup section, TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, int w, int h, int listY, boolean requirements, boolean rewardsClaimed) {
        int visibleH = Math.max(1, h - listY - 4);
        int maxStart = scrollMax(entries, visibleH);
        if (requirements) {
            state.questDetailsReqScroll = ScrollController.clamp(state.questDetailsReqScroll, maxStart);
        } else {
            state.questDetailsRewardScroll = ScrollController.clamp(state.questDetailsRewardScroll, maxStart);
        }
        String kind = requirements ? "requirements" : "rewards";
        int listW = maxStart > 0 ? w - DragScrollBarWidget.RESERVED_WIDTH : w;
        WidgetGroup list = clippedList(0, listY, listW, visibleH, state, player, refresh, questId, entries, kind, listY, h - 4);
        section.addWidget(list);
        int scroll = requirements ? state.questDetailsReqScroll : state.questDetailsRewardScroll;
        if (entries.isEmpty()) {
            return;
        }
        int cardW = maxStart > 0 ? w - 12 - DragScrollBarWidget.RESERVED_WIDTH : w - 12;
        int y = QuestDetailsObjectivesPanel.LIST_PAD - scroll;
        int ghostIndex = Math.max(0, Math.min(entries.size(), state.questDetailsObjectiveDragTargetIndex));
        boolean showGhost = state.questDetailsObjectiveDragActive
                && kind.equals(state.questDetailsObjectiveDragKind)
                && !state.questDetailsObjectiveDragId.isBlank();
        QuestDetailsObjectiveEntry ghostEntry = draggedEntry(entries, state.questDetailsObjectiveDragId);
        for (int index = 0; index < entries.size(); index++) {
            if (showGhost && index == ghostIndex) {
                QuestObjectiveGhostCards.render(list, ghostEntry, requirements, 6, y, cardW);
                y += QuestDetailsObjectivesPanel.CARD_H + QuestDetailsObjectivesPanel.CARD_GAP;
            }
            QuestDetailsObjectiveEntry entry = entries.get(index);
            if (y < visibleH && y + QuestDetailsObjectivesPanel.CARD_H > 0) {
                if (requirements) {
                    QuestObjectiveCardRenderer.renderTaskCard(list, state, player, refresh, questId, entry, 6, y, cardW, entries, listY, h - 4);
                } else {
                    QuestObjectiveCardRenderer.renderRewardCard(list, state, player, refresh, questId, entry, 6, y, cardW, entries, listY, h - 4, rewardsClaimed);
                }
            }
            y += QuestDetailsObjectivesPanel.CARD_H + QuestDetailsObjectivesPanel.CARD_GAP;
        }
        if (showGhost && ghostIndex == entries.size()) {
            QuestObjectiveGhostCards.render(list, ghostEntry, requirements, 6, y, cardW);
        }
        renderScrollbar(section, state, refresh, requirements, w, listY, visibleH, maxStart);
    }

    private static QuestDetailsObjectiveEntry draggedEntry(List<QuestDetailsObjectiveEntry> entries, String id) {
        for (QuestDetailsObjectiveEntry entry : entries) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    private static WidgetGroup clippedList(int x, int y, int w, int h, TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, String kind, int listY, int listBottom) {
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
                int localY = QuestDetailsMouse.localY(state, mouseY, getPositionY(), h) + listY;
                if (QuestObjectiveListInteractions.handleDrag(player, state, refresh, questId, entries, kind, listY, listBottom, localY, mouseX, mouseY, button)) {
                    return true;
                }
                return inside(mouseX, mouseY) && super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (QuestObjectiveListInteractions.handleRelease(player, state, refresh, questId, entries, kind)) {
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

    static int scrollMax(List<QuestDetailsObjectiveEntry> entries, int visibleH) {
        if (entries.isEmpty()) {
            return 0;
        }
        int contentH = QuestDetailsObjectivesPanel.LIST_PAD * 2
                + entries.size() * QuestDetailsObjectivesPanel.CARD_H
                + Math.max(0, entries.size() - 1) * QuestDetailsObjectivesPanel.CARD_GAP;
        return Math.max(0, contentH - visibleH);
    }

    private static void renderScrollbar(WidgetGroup section, TabletUiState state, Runnable refresh, boolean requirements, int w, int y, int h, int maxStart) {
        if (maxStart <= 0) {
            return;
        }
        int knobH = Math.max(12, Math.min(h, Math.round((float) h * ((float) h / (float) (h + maxStart)))));
        section.addWidget(new DragScrollBarWidget(
                w - DragScrollBarWidget.RESERVED_WIDTH,
                y,
                DragScrollBarWidget.RESERVED_WIDTH,
                h,
                () -> requirements ? state.questDetailsReqScroll : state.questDetailsRewardScroll,
                () -> maxStart,
                () -> knobH,
                value -> {
                    if (requirements) {
                        state.questDetailsReqScroll = value;
                    } else {
                        state.questDetailsRewardScroll = value;
                    }
                },
                () -> requirements ? state.questDetailsReqScrollDragging : state.questDetailsRewardScrollDragging,
                dragging -> {
                    if (requirements) {
                        state.questDetailsReqScrollDragging = dragging;
                    } else {
                        state.questDetailsRewardScrollDragging = dragging;
                    }
                },
                refresh,
                ModColors.scrollTrack(requirements ? state.questDetailsReqScrollDragging : state.questDetailsRewardScrollDragging),
                ModColors.scrollThumb(false),
                ModColors.scrollThumb(true),
                DragScrollBarWidget.WIDTH
        ));
    }

    private static QuestDetailsObjectiveEntry hitEntry(TabletUiState state, List<QuestDetailsObjectiveEntry> entries, String kind, int listY, int listBottom, int localY) {
        if (localY < listY || localY > listBottom || entries.isEmpty()) {
            return null;
        }
        int scroll = "requirements".equals(kind) ? state.questDetailsReqScroll : state.questDetailsRewardScroll;
        int contentY = localY - listY + scroll - QuestDetailsObjectivesPanel.LIST_PAD;
        if (contentY < 0) {
            return null;
        }
        int slot = contentY / (QuestDetailsObjectivesPanel.CARD_H + QuestDetailsObjectivesPanel.CARD_GAP);
        int inSlot = contentY % (QuestDetailsObjectivesPanel.CARD_H + QuestDetailsObjectivesPanel.CARD_GAP);
        if (slot < 0 || slot >= entries.size() || inSlot >= QuestDetailsObjectivesPanel.CARD_H) {
            return null;
        }
        return entries.get(slot);
    }

    private static boolean isCardBodyHit(int localX, int sectionW) {
        return localX >= 30 && localX <= sectionW - 44;
    }
}
