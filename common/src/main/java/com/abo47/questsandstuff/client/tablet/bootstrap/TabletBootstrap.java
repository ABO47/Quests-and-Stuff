package com.abo47.questsandstuff.client.tablet.bootstrap;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.app.AppDescriptor;
import com.abo47.questsandstuff.client.tablet.app.TabletAppRegistry;
import com.abo47.questsandstuff.client.tablet.home.TabletHomeComposer;
import com.abo47.questsandstuff.client.tablet.quest.QuestAppComposer;
import com.abo47.questsandstuff.client.tablet.teams.TeamsAppComposer;
import com.abo47.questsandstuff.client.tablet.ui.IntegratedServerActions;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorControlPacket;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.clampGridSizeIndex;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.applyCanvasBgOpacityPercent;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.defaultGridOpacityPercent;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ensureAssetsDirs;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.readPersistedUiState;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.readPersistedSkinState;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.syncCanvasStateFromCache;

public final class TabletBootstrap {
    private static boolean appsRegistered;

    private TabletBootstrap() {
    }

    static void ensureAppsRegistered() {
        if (appsRegistered) return;
        appsRegistered = true;
        TabletAppRegistry.register(new AppDescriptor("home",
                "ui.questsandstuff.app.home",
                new ResourceLocation("questsandstuff", "textures/gui/home.png"),
                TabletHomeComposer::create));
        TabletAppRegistry.register(new AppDescriptor("QUESTS",
                "ui.questsandstuff.app.quests",
                new ResourceLocation("questsandstuff", "textures/gui/questsandstuff.png"),
                QuestAppComposer::create));
        TabletAppRegistry.register(new AppDescriptor("TEAMS",
                "ui.questsandstuff.app.teams",
                new ResourceLocation("questsandstuff", "textures/gui/teams.png"),
                TeamsAppComposer::create));
    }

    public static TabletUiState prepare(Player player) {
        ensureAppsRegistered();
        UiThemeManager.activeThemeName();
        ensureAssetsDirs();
        TabletUiState state = new TabletUiState();
        state.root.editorAvailable = player.hasPermissions(2);
        readPersistedUiState(state);
        readPersistedSkinState(state);
        state.root.canEdit = state.root.editorAvailable && state.root.editMode;
        state.chapterPanel.chapterPanelWidth = chapterPanelWidth(state);
        state.chapterPanel.chapterPanelCollapsed = isChapterPanelCollapsed(state);
        if (!state.chapterPanel.chapterPanelCollapsed) {
            state.chapterPanel.chapterPanelLastExpandedWidth = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanel.chapterPanelWidth));
        }
        keepSelectedChapterValid(state, false);
        state.chapterPanel.chapterDraft = selectedChapterName(state);
        state.chapterPanel.chapterDraftName = state.chapterPanel.chapterDraft;
        state.canvas.gridSizeIndex = clampGridSizeIndex(state.canvas.gridSizeIndex);
        state.canvas.gridOpacityPercent = defaultGridOpacityPercent(state);
        state.canvas.toolsGridOpacityDraft = Integer.toString(state.canvas.gridOpacityPercent);
        applyCanvasBgOpacityPercent(state, state.canvas.canvasBgOpacityPercent);
        syncCanvasStateFromCache(state);
        return state;
    }

    public static Runnable undoAction(TabletUiState state, Player player) {
        return () -> {
            if (!canUseEditorHistory(state)) {
                return;
            }
            IntegratedServerActions.run(
                    player,
                    serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).undo(serverPlayer),
                    () -> ModNetwork.sendToServer(new C2SEditorControlPacket("undo")));
        };
    }

    public static Runnable redoAction(TabletUiState state, Player player) {
        return () -> {
            if (!canUseEditorHistory(state)) {
                return;
            }
            IntegratedServerActions.run(
                    player,
                    serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).redo(serverPlayer),
                    () -> ModNetwork.sendToServer(new C2SEditorControlPacket("redo")));
        };
    }

    private static boolean canUseEditorHistory(TabletUiState state) {
        return state != null && state.root.editorAvailable && (state.root.canEdit || QuestDetailsEditController.canEdit(state));
    }

    public static void keepSelectedChapterValid(TabletUiState state, boolean persist) {
        List<String> groups = ClientQuestStateFacade.selectableChapterOrder(state.root.canEdit);
        if (groups.isEmpty()) {
            state.root.selectedChapter = "";
            return;
        }
        if (state.root.selectedChapter == null) {
            state.root.selectedChapter = groups.get(0);
            state.chapterPanel.chapterDraft = state.root.selectedChapter;
            state.chapterPanel.chapterDraftName = state.root.selectedChapter;
            if (persist) {
                persistUiState(state);
            }
            return;
        }
        String selected = state.root.selectedChapter.trim();
        if (selected.isBlank() || groups.contains(selected)) {
            state.root.selectedChapter = selected;
            return;
        }
        if (state.chapterPanel.recentlyCreatedChapters.contains(selected)) {
            ClientQuestStateFacade.createChapterLocal(selected);
            state.root.selectedChapter = selected;
            state.chapterPanel.chapterDraft = selected;
            state.chapterPanel.chapterDraftName = selected;
            if (persist) {
                persistUiState(state);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] preserved optimistic chapter selected={} chapters={}", selected, ClientQuestStateFacade.chapterOrder());
            return;
        }
        state.root.selectedChapter = groups.get(0);
        state.chapterPanel.chapterDraft = state.root.selectedChapter;
        state.chapterPanel.chapterDraftName = state.root.selectedChapter;
        if (persist) {
            persistUiState(state);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] selected chapter missing, reset to={} available={}", state.root.selectedChapter, groups);
    }
}
