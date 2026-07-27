package com.abo47.questsandstuff.client.tablet.quest.chapter.menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ActionTone;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.BackgroundModes;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public final class ChapterContextMenuRows {
    private ChapterContextMenuRows() {
    }

    public static List<ContextAction> actions(ChapterContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh) {
        ContextMenuSections sections = new ContextMenuSections();
        String target = layout.target();
        if (layout.hasTarget()) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(tr("ui.questsandstuff.menu.new_chapter"), "add", TabletColors.SUCCESS, () -> ChapterContextMenuActions.addChapter(state, refresh)));
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promotedRename(tr("ui.questsandstuff.menu.rename"), () -> ChapterContextMenuActions.rename(state, target, refresh)));
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.changeIcon(() -> ChapterContextMenuActions.changeIcon(state, target, refresh)));
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_CHAPTER_TEXTURE), "background", ActionTone.PRIMARY, () -> ChapterContextMenuActions.changeBackground(state, target, refresh)));
            String chapterBg = ClientQuestStateFacade.chapterBackground(target);
            if (!chapterBg.isBlank() && !"default".equals(chapterBg)) {
                SkinFillOverride parsed = BackgroundModes.decode(chapterBg);
                String currentMode = parsed != null ? parsed.mode() : "stretch";
                String path = parsed != null ? parsed.path() : chapterBg;
                List<ContextAction> modeActions = new ArrayList<>();
                modeActions.add(ContextActionFactory.action(TabletTranslationKeys.text("ui.questsandstuff.skin.mode_stretch"), "size", currentMode.equals("stretch") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY, () -> {
                    EditorChapterCommandClient.runChapterAction(player, state, "set_background", target, BackgroundModes.encode("stretch", path), 0);
                    refresh.run();
                }));
                modeActions.add(ContextActionFactory.action(TabletTranslationKeys.text("ui.questsandstuff.skin.mode_tile"), "grid", currentMode.equals("tile") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY, () -> {
                    EditorChapterCommandClient.runChapterAction(player, state, "set_background", target, BackgroundModes.encode("tile", path), 0);
                    refresh.run();
                }));
                modeActions.add(ContextActionFactory.action(TabletTranslationKeys.text("ui.questsandstuff.skin.mode_original_size"), "original_size", currentMode.equals("center") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY, () -> {
                    EditorChapterCommandClient.runChapterAction(player, state, "set_background", target, BackgroundModes.encode("center", path), 0);
                    refresh.run();
                }));
                modeActions.add(ContextActionFactory.action(TabletTranslationKeys.text("ui.questsandstuff.skin.mode_dynamic"), "dynamic", currentMode.equals("dynamic") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY, () -> {
                    EditorChapterCommandClient.runChapterAction(player, state, "set_background", target, BackgroundModes.encode("dynamic", path), 0);
                    refresh.run();
                }));
                sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.submenu(TabletTranslationKeys.text("ui.questsandstuff.skin.change_mode"), "layout-dashboard", TabletColors.TEXT_PRIMARY, modeActions));
            }
        } else {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(tr("ui.questsandstuff.menu.new_chapter"), "add", TabletColors.SUCCESS, () -> ChapterContextMenuActions.addChapter(state, refresh)));
            return sections.build();
        }

        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.action(tr("ui.questsandstuff.menu.text_style"), "style", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.textStyle(state, target, refresh)));
        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.action(tr("ui.questsandstuff.context.change_completion_hud_background"), "completion_hud_background", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionHudBackground(state, target, refresh)));
        sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.action(tr("ui.questsandstuff.context.change_connection_texture"), "connect", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.changeConnectionTexture(state, target, refresh)));
        if (chapterHasConnectionTexture(state, target)) {
            String connTexKey = "chapter:remove_conn_tex:" + target;
            sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.warningDelete(state, connTexKey, tr("ui.questsandstuff.context.remove_connection_texture"), () -> ChapterContextMenuActions.removeConnectionTexture(player, state, target)));
        }
        sections.add(ContextMenuSection.BEHAVIOR, ContextActionFactory.submenu(QuestTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_COMPLETION_SOUND), "audio-lines", TabletColors.INTERACTIVE, List.of(
                ContextActionFactory.action(tr("ui.questsandstuff.context.use_game_sound"), "audio-lines", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionSoundGame(state, target, refresh)),
                ContextActionFactory.action(tr("ui.questsandstuff.context.use_custom_sound"), "audio-lines", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionSoundCustom(state, target, refresh))
        )));
        boolean locked = ClientQuestStateFacade.chapterLockUntilUnlocked(target);
        boolean hidden = ClientQuestStateFacade.chapterHideUntilUnlocked(target);
        sections.add(ContextMenuSection.BEHAVIOR, ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_VISIBILITY), "eye", TabletColors.INTERACTIVE, List.of(
                ContextActionFactory.action(
                        tr(locked ? QuestTranslationKeys.CONTEXT_SHOW_CHAPTER_BEFORE_UNLOCKED : QuestTranslationKeys.CONTEXT_LOCK_CHAPTER_UNTIL_UNLOCKED),
                        locked ? "unlock_chapter" : "lock_chapter",
                        locked ? TabletColors.SUCCESS : TabletColors.INTERACTIVE,
                        () -> ChapterContextMenuActions.setLockUntilUnlocked(player, state, target, !locked, refresh)
                ),
                ContextActionFactory.action(
                        tr(hidden ? QuestTranslationKeys.CONTEXT_REVEAL_CHAPTER : QuestTranslationKeys.CONTEXT_HIDE_CHAPTER_UNTIL_UNLOCKED),
                        hidden ? "eye" : "eye-off",
                        hidden ? TabletColors.SUCCESS : TabletColors.WARNING,
                        () -> ChapterContextMenuActions.setHideUntilUnlocked(player, state, target, !hidden, refresh)
                )
        )));

        if (layout.entityVariants()) {
            sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.changeVariant(() -> ChapterContextMenuActions.changeVariant(state, target, refresh)));
        }
        if (layout.entityIcon()) {
            sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.editMotion(() -> ChapterContextMenuActions.editMotion(state, target, refresh)));
        }
        sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.moveUp(() -> ChapterContextMenuActions.move(player, state, target, -1, refresh)));
        sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.moveDown(() -> ChapterContextMenuActions.move(player, state, target, 1, refresh)));
        sections.add(ContextMenuSection.DANGER, ContextActionFactory.delete(state, ChapterContextMenuLayout.deleteKey(target), tr("ui.questsandstuff.menu.remove"), () -> ChapterContextMenuActions.delete(player, state, target)));
        if (!ClientQuestStateFacade.chapterIcon(target).isBlank()) {
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, ChapterContextMenuLayout.removeIconKey(target), tr("ui.questsandstuff.menu.remove_icon"), () -> ChapterContextMenuActions.removeIcon(player, state, target)));
        }
        if (!"default".equals(ClientQuestStateFacade.chapterBackground(target))) {
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, ChapterContextMenuLayout.removeBackgroundKey(target), QuestTranslationKeys.text(QuestTranslationKeys.CONTEXT_REMOVE_CHAPTER_TEXTURE), () -> ChapterContextMenuActions.removeBackground(player, state, target)));
        }
        return sections.build();
    }

    public static boolean click(ChapterContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh, int x, int y) {
        List<ContextAction> actions = actions(layout, state, player, refresh);
        return ContextMenuPanel.click(
                actions,
                0,
                ContextMenuPanel.rowActionCount(actions),
                layout.menuX(),
                layout.menuY(),
                layout.menuW(),
                x,
                y,
                state,
                action -> {
                    if (action.closeAfterClick()) {
                        state.chapterPanel.chapterMenuOpen = false;
                    }
                    refresh.run();
                },
                ContextMenuAnimationBridge.CHAPTER_KEY
        );
    }

    private static boolean chapterHasConnectionTexture(TabletUiState state, String target) {
        for (String questId : ClientQuestStateFacade.questIdsInChapter(target)) {
            CompoundTag quest = ClientQuestStateFacade.quest(questId);
            if (quest != null && quest.contains("connection_textures", Tag.TAG_COMPOUND)) {
                CompoundTag textures = quest.getCompound("connection_textures");
                if (!textures.isEmpty()) return true;
            }
        }
        for (var ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(target, List.of())) {
            if (!ec.connectionTextures().isEmpty()) return true;
        }
        return false;
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
