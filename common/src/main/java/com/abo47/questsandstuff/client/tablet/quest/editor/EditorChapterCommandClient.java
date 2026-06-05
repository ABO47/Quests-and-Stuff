package com.abo47.questsandstuff.client.tablet.quest.editor;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorGroupPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorOpenGroupPacket;
import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class EditorChapterCommandClient {
    private EditorChapterCommandClient() {
    }

    static void cycleGroup(TabletUiState state, int dir) {
        List<String> groups = ClientQuestCache.selectableGroupOrder(state != null && state.canEdit);
        if (groups.isEmpty()) {
            state.selectedGroup = "";
            state.groupDraft = "";
            return;
        }
        int idx = groups.indexOf(state.selectedGroup);
        if (idx < 0) {
            idx = 0;
        }
        idx = (idx + dir + groups.size()) % groups.size();
        state.selectedGroup = groups.get(idx);
        state.groupDraft = state.selectedGroup;
    }

    static String selectedGroupName(TabletUiState state) {
        String selected = sanitizeGroupName(state == null ? "" : state.selectedGroup);
        if (state == null || state.canEdit || selected.isBlank() || ClientQuestCache.groupOpenablePreview(selected)) {
            return selected;
        }
        for (String group : ClientQuestCache.selectableGroupOrder(false)) {
            String sanitized = sanitizeGroupName(group);
            if (!sanitized.isBlank()) {
                return sanitized;
            }
        }
        return "";
    }

    static boolean canEditGroups(TabletUiState state) {
        return state.canEdit;
    }

    static boolean canManageGroups(TabletUiState state) {
        return state.canEdit;
    }

    static String resolveGroupDraft(TabletUiState state, String fallback) {
        String sanitized = sanitizeGroupName(state.groupDraft);
        if (!sanitized.isBlank()) {
            return sanitized;
        }
        return sanitizeGroupName(fallback);
    }

    static String nextGroupName() {
        return nextGroupName("chapter");
    }

    static String nextGroupName(String baseName) {
        String base = sanitizeGroupName(baseName);
        if (base.isBlank()) {
            base = "chapter";
        }
        Set<String> groups = new HashSet<>(ClientQuestCache.groupOrder());
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

    static String uniqueGroupName(String preferred, String excludeCurrent) {
        String candidate = sanitizeGroupName(preferred);
        if (candidate.isBlank()) {
            candidate = tr("ui.questsandstuff.chapter.default_name");
        }
        String excluded = sanitizeGroupName(excludeCurrent);
        Set<String> groups = new HashSet<>(ClientQuestCache.groupOrder());
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

    static String nextRenamedGroup(String source) {
        String base = sanitizeGroupName(source);
        if (base.isBlank()) {
            base = "chapter";
        }
        Set<String> groups = new HashSet<>(ClientQuestCache.groupOrder());
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

    static String sanitizeGroupName(String value) {
        if (value == null) {
            return "";
        }
        String result = value.trim().replace('\n', ' ').replace('\r', ' ');
        return result.length() > 40 ? result.substring(0, 40) : result;
    }

    static void runGroupAction(Player player, TabletUiState state, String action, String group, String value, int offset) {
        String op = action == null ? "" : action;
        String from = sanitizeGroupName(group);
        String rawValue = value == null ? "" : value.trim();
        String to = switch (op) {
            case "create", "rename" -> sanitizeGroupName(value);
            default -> rawValue;
        };
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter action op={} from={} to={} offset={}", op, from, to, offset);

        if (player instanceof ServerPlayer serverPlayer) {
            runIntegratedServerGroupAction(serverPlayer, state, op, from, to, offset);
            return;
        }

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
            ModNetwork.sendToServer(new C2SEditorGroupPacket(op, packetGroup, packetValue, offset));
            if ("create".equals(op) || "rename".equals(op) || "move".equals(op)) {
                String openTarget = "create".equals(op) || "rename".equals(op) ? to : from;
                if (!openTarget.isBlank()) {
                    ModNetwork.sendToServer(new C2SEditorOpenGroupPacket(openTarget));
                }
            }
        };
        EditorPreviewBus.dispatch("group:" + op + ":" + from + ":" + to + ":" + offset, optimisticApply, sendToServer);
    }

    private static void runIntegratedServerGroupAction(ServerPlayer serverPlayer, TabletUiState state, String op, String from, String to, int offset) {
        var editor = QuestServices.editor(serverPlayer.server);
        switch (op) {
            case "create" -> {
                editor.createGroup(serverPlayer, to);
                if (!to.isBlank()) {
                    state.recentlyCreatedGroups.add(to);
                }
            }
            case "rename" -> {
                editor.renameGroup(serverPlayer, from, to);
                if (state.recentlyCreatedGroups.remove(from) && !to.isBlank()) {
                    state.recentlyCreatedGroups.add(to);
                }
            }
            case "delete" -> {
                editor.deleteGroup(serverPlayer, from);
                state.recentlyCreatedGroups.remove(from);
            }
            case "move" -> editor.moveGroup(serverPlayer, from, offset);
            case "move_to" -> editor.moveGroupToIndex(serverPlayer, from, offset);
            case "set_icon" -> editor.setGroupIcon(serverPlayer, from, to);
            case "set_background" -> editor.setGroupBackground(serverPlayer, from, to);
            case "set_canvas_background" -> editor.setGroupCanvasBackground(serverPlayer, from, to);
            case "set_text_align" -> editor.setGroupTextAlign(serverPlayer, from, to);
            case "set_text_color" -> {
                try {
                    editor.setGroupTextColor(serverPlayer, from, Integer.parseInt(to));
                } catch (NumberFormatException ignored) {
                }
            }
            case "set_text_style" -> editor.setGroupTextStyle(serverPlayer, from, to);
            case "set_text_size" -> {
                try {
                    editor.setGroupTextSize(serverPlayer, from, Integer.parseInt(to));
                } catch (NumberFormatException ignored) {
                }
            }
            case "set_lock_until_unlocked" -> editor.setGroupLockUntilUnlocked(serverPlayer, from, Boolean.parseBoolean(to));
            case "set_hide_until_unlocked" -> editor.setGroupHideUntilUnlocked(serverPlayer, from, Boolean.parseBoolean(to));
            default -> {
            }
        }
    }

    private static void applyLocalGroupAction(TabletUiState state, String op, String from, String to, int offset) {
        switch (op) {
            case "create" -> {
                ClientQuestCache.createGroupLocal(to);
                if (!to.isBlank()) {
                    state.recentlyCreatedGroups.add(to);
                }
            }
            case "rename" -> {
                ClientQuestCache.renameGroupLocal(from, to);
                if (state.recentlyCreatedGroups.remove(from) && !to.isBlank()) {
                    state.recentlyCreatedGroups.add(to);
                }
            }
            case "delete" -> {
                ClientQuestCache.deleteGroupLocal(from);
                state.recentlyCreatedGroups.remove(from);
            }
            case "move" -> ClientQuestCache.moveGroupLocal(from, offset);
            case "move_to" -> ClientQuestCache.moveGroupToIndexLocal(from, offset);
            case "set_icon" -> ClientQuestCache.setGroupIconLocal(from, to);
            case "set_background" -> ClientQuestCache.setGroupBackgroundLocal(from, to);
            case "set_canvas_background" -> ClientQuestCache.setGroupCanvasBackgroundLocal(from, to);
            case "set_text_align" -> ClientQuestCache.setGroupTextAlignLocal(from, to);
            case "set_text_color" -> {
                try {
                    ClientQuestCache.setGroupTextColorLocal(from, Integer.parseInt(to));
                } catch (NumberFormatException ignored) {
                }
            }
            case "set_text_style" -> ClientQuestCache.setGroupTextStyleLocal(from, to);
            case "set_text_size" -> {
                try {
                    ClientQuestCache.setGroupTextSizeLocal(from, Integer.parseInt(to));
                } catch (NumberFormatException ignored) {
                }
            }
            case "set_lock_until_unlocked" -> ClientQuestCache.setGroupLockUntilUnlockedLocal(from, Boolean.parseBoolean(to));
            case "set_hide_until_unlocked" -> ClientQuestCache.setGroupHideUntilUnlockedLocal(from, Boolean.parseBoolean(to));
            default -> {
            }
        }
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
