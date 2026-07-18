package com.abo47.questsandstuff.client.tablet.quest.editor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.IntegratedServerActions;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorChapterPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorOpenChapterPacket;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public final class EditorChapterCommandClient {
    private EditorChapterCommandClient() {
    }

    public static void cycleChapter(TabletUiState state, int dir) {
        List<String> groups = ClientQuestStateFacade.selectableChapterOrder(state != null && state.root.canEdit);
        if (groups.isEmpty()) {
            state.root.selectedChapter = "";
            state.chapterPanel.chapterDraft = "";
            return;
        }
        int idx = groups.indexOf(state.root.selectedChapter);
        if (idx < 0) {
            idx = 0;
        }
        idx = (idx + dir + groups.size()) % groups.size();
        state.root.selectedChapter = groups.get(idx);
        state.chapterPanel.chapterDraft = state.root.selectedChapter;
    }

    public static String selectedChapterName(TabletUiState state) {
        return TabletStateQueries.selectedChapterName(state);
    }

    public static boolean canEditChapters(TabletUiState state) {
        return state.root.canEdit;
    }

    public static boolean canManageChapters(TabletUiState state) {
        return state.root.canEdit;
    }

    public static String resolveChapterDraft(TabletUiState state, String fallback) {
        String sanitized = sanitizeChapterName(state.chapterPanel.chapterDraft);
        if (!sanitized.isBlank()) {
            return sanitized;
        }
        return sanitizeChapterName(fallback);
    }

    public static String nextChapterName() {
        return nextChapterName("chapter");
    }

    public static String nextChapterName(String baseName) {
        String base = sanitizeChapterName(baseName);
        if (base.isBlank()) {
            base = "chapter";
        }
        Set<String> groups = new HashSet<>(ClientQuestStateFacade.chapterOrder());
        if (!groups.contains(base)) {
            return base;
        }
        int index = 2;
        while (true) {
            String candidate = base + " " + index;
            if (!groups.contains(candidate)) {
                return candidate;
            }
            index++;
        }
    }

    public static String uniqueChapterName(String preferred, String excludeCurrent) {
        String candidate = sanitizeChapterName(preferred);
        if (candidate.isBlank()) {
            candidate = tr("ui.questsandstuff.chapter.default_name");
        }
        String excluded = sanitizeChapterName(excludeCurrent);
        Set<String> groups = new HashSet<>(ClientQuestStateFacade.chapterOrder());
        if (!excluded.isBlank()) {
            groups.remove(excluded);
        }
        if (!groups.contains(candidate)) {
            return candidate;
        }
        int index = 2;
        while (true) {
            String next = candidate + " " + index;
            if (!groups.contains(next)) {
                return next;
            }
            index++;
        }
    }

    public static String nextRenamedChapter(String source) {
        String base = sanitizeChapterName(source);
        if (base.isBlank()) {
            base = "chapter";
        }
        Set<String> groups = new HashSet<>(ClientQuestStateFacade.chapterOrder());
        String first = base + "_renamed";
        if (!groups.contains(first)) {
            return first;
        }
        int index = 2;
        while (true) {
            String candidate = base + "_renamed_" + index;
            if (!groups.contains(candidate)) {
                return candidate;
            }
            index++;
        }
    }

    public static String sanitizeChapterName(String value) {
        return TabletStateQueries.sanitizeChapterName(value);
    }

    public static void runChapterAction(Player player, TabletUiState state, String action, String chapter, String value, int offset) {
        String op = action == null ? "" : action;
        String from = sanitizeChapterName(chapter);
        String rawValue = value == null ? "" : value.trim();
        String to = switch (op) {
            case "create", "rename" -> sanitizeChapterName(value);
            default -> rawValue;
        };
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter action op={} from={} to={} offset={}", op, from, to, offset);

        String packetGroup = switch (op) {
            case "create" -> to;
            case "rename", "delete", "move", "move_to", "set_icon", "set_background", "set_canvas_background", "set_text_align", "set_text_color", "set_text_style", "set_text_size", "set_lock_until_unlocked", "set_hide_until_unlocked" -> from;
            default -> "";
        };
        String packetValue = switch (op) {
            case "rename", "set_icon", "set_background", "set_canvas_background", "set_text_align", "set_text_color", "set_text_style", "set_text_size", "set_lock_until_unlocked", "set_hide_until_unlocked" -> to;
            default -> "";
        };
        Runnable optimisticApply = () -> applyLocalGroupAction(state, op, from, to, offset);
        Runnable sendToServer = () -> {
            ModNetwork.sendToServer(new C2SEditorChapterPacket(op, packetGroup, packetValue, offset));
            if ("create".equals(op) || "rename".equals(op) || "move".equals(op)) {
                String openTarget = "create".equals(op) || "rename".equals(op) ? to : from;
                if (!openTarget.isBlank()) {
                    ModNetwork.sendToServer(new C2SEditorOpenChapterPacket(openTarget));
                }
            }
        };
        optimisticApply.run();
        IntegratedServerActions.run(
                player,
                integratedServerGroupAction(state, op, from, to, offset),
                () -> EditorPreviewDeduplicator.dispatch("chapter:" + op + ":" + from + ":" + to + ":" + offset, optimisticApply, sendToServer));
    }

    private static IntegratedServerActions.LocalAction integratedServerGroupAction(TabletUiState state, String op, String from, String to, int offset) {
        return serverPlayer -> {
            var editor = QuestServiceRegistry.editor(serverPlayer.server);
            switch (op) {
                case "create" -> {
                    editor.createChapter(serverPlayer, to);
                    if (!to.isBlank()) {
                        state.chapterPanel.recentlyCreatedChapters.add(to);
                    }
                }
                case "rename" -> {
                    editor.renameChapter(serverPlayer, from, to);
                    if (state.chapterPanel.recentlyCreatedChapters.remove(from) && !to.isBlank()) {
                        state.chapterPanel.recentlyCreatedChapters.add(to);
                    }
                }
                case "delete" -> {
                    editor.deleteChapter(serverPlayer, from);
                    state.chapterPanel.recentlyCreatedChapters.remove(from);
                }
                case "move" -> editor.moveChapter(serverPlayer, from, offset);
                case "move_to" -> editor.moveChapterToIndex(serverPlayer, from, offset);
                case "set_icon" -> editor.setChapterIcon(serverPlayer, from, to);
                case "set_background" -> editor.setChapterBackground(serverPlayer, from, to);
                case "set_canvas_background" -> editor.setChapterCanvasBackground(serverPlayer, from, to);
                case "set_text_align" -> editor.setChapterTextAlign(serverPlayer, from, to);
                case "set_text_color" -> {
                    try {
                        editor.setChapterTextColor(serverPlayer, from, Integer.parseInt(to));
                    } catch (NumberFormatException ignored) {
                    }
                }
                case "set_text_style" -> editor.setChapterTextStyle(serverPlayer, from, to);
                case "set_text_size" -> {
                    try {
                        editor.setChapterTextSize(serverPlayer, from, Integer.parseInt(to));
                    } catch (NumberFormatException ignored) {
                    }
                }
                case "set_lock_until_unlocked" -> editor.setChapterLockUntilUnlocked(serverPlayer, from, Boolean.parseBoolean(to));
                case "set_hide_until_unlocked" -> editor.setChapterHideUntilUnlocked(serverPlayer, from, Boolean.parseBoolean(to));
                default -> {
                }
            }
        };
    }

    private static void applyLocalGroupAction(TabletUiState state, String op, String from, String to, int offset) {
        switch (op) {
            case "create" -> {
                ClientQuestStateFacade.createChapterLocal(to);
                if (!to.isBlank()) {
                    state.chapterPanel.recentlyCreatedChapters.add(to);
                }
            }
            case "rename" -> {
                ClientQuestStateFacade.renameChapterLocal(from, to);
                if (state.chapterPanel.recentlyCreatedChapters.remove(from) && !to.isBlank()) {
                    state.chapterPanel.recentlyCreatedChapters.add(to);
                }
            }
            case "delete" -> {
                ClientQuestStateFacade.deleteChapterLocal(from);
                state.chapterPanel.recentlyCreatedChapters.remove(from);
            }
            case "move" -> ClientQuestStateFacade.moveChapterLocal(from, offset);
            case "move_to" -> ClientQuestStateFacade.moveChapterToIndexLocal(from, offset);
            case "set_icon" -> ClientQuestStateFacade.setChapterIconLocal(from, to);
            case "set_background" -> ClientQuestStateFacade.setChapterBackgroundLocal(from, to);
            case "set_canvas_background" -> ClientQuestStateFacade.setChapterCanvasBackgroundLocal(from, to);
            case "set_text_align" -> ClientQuestStateFacade.setChapterTextAlignLocal(from, to);
            case "set_text_color" -> {
                try {
                    ClientQuestStateFacade.setChapterTextColorLocal(from, Integer.parseInt(to));
                } catch (NumberFormatException ignored) {
                }
            }
            case "set_text_style" -> ClientQuestStateFacade.setChapterTextStyleLocal(from, to);
            case "set_text_size" -> {
                try {
                    ClientQuestStateFacade.setChapterTextSizeLocal(from, Integer.parseInt(to));
                } catch (NumberFormatException ignored) {
                }
            }
            case "set_lock_until_unlocked" -> ClientQuestStateFacade.setChapterLockUntilUnlockedLocal(from, Boolean.parseBoolean(to));
            case "set_hide_until_unlocked" -> ClientQuestStateFacade.setChapterHideUntilUnlockedLocal(from, Boolean.parseBoolean(to));
            default -> {
            }
        }
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
