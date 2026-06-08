package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.EdgeHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.shell.TabletShellComposer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class TabletUiFactory {
    public static final int ROOT_W = TabletLayout.ROOT_W;
    public static final int ROOT_H = TabletLayout.ROOT_H;
    public static final int ROOT_PAD_X = TabletLayout.ROOT_PAD_X;
    public static final int ROOT_PAD_Y = TabletLayout.ROOT_PAD_Y;
    public static final int PANEL_GAP = TabletLayout.PANEL_GAP;
    public static final int PANEL_INSET = TabletLayout.PANEL_INSET;
    public static final int CANVAS_VIEWPORT_GUTTER_X = TabletLayout.CANVAS_VIEWPORT_GUTTER_X;
    public static final int CANVAS_VIEWPORT_GUTTER_TOP = TabletLayout.CANVAS_VIEWPORT_GUTTER_TOP;
    public static final int CANVAS_VIEWPORT_GUTTER_BOTTOM = TabletLayout.CANVAS_VIEWPORT_GUTTER_BOTTOM;
    public static final int CHAPTER_PANEL_GUTTER_X = TabletLayout.CHAPTER_PANEL_GUTTER_X;
    public static final int CHAPTER_PANEL_GUTTER_BOTTOM = TabletLayout.CHAPTER_PANEL_GUTTER_BOTTOM;
    public static final int HEADER_H = TabletLayout.HEADER_H;
    public static final int HEADER_GAP = TabletLayout.HEADER_GAP;
    public static final int PAD = TabletLayout.PAD;
    public static final int PAD_Y = TabletLayout.PAD_Y;
    public static final int GAP = TabletLayout.GAP;
    public static final int BODY_X = TabletLayout.BODY_X;
    public static final int BODY_Y = TabletLayout.BODY_Y;
    public static final int BODY_W = TabletLayout.BODY_W;
    public static final int BODY_H = TabletLayout.BODY_H;
    public static final int CHAPTER_W = TabletLayout.CHAPTER_W;
    public static final int CHAPTER_W_MIN = TabletLayout.CHAPTER_W_MIN;
    public static final int CHAPTER_W_ICON = TabletLayout.CHAPTER_W_ICON;
    public static final int CHAPTER_W_MAX = TabletLayout.CHAPTER_W_MAX;
    public static final int CHAPTER_W_ICON_SNAP = TabletLayout.CHAPTER_W_ICON_SNAP;
    public static final int SPLITTER_W = TabletLayout.SPLITTER_W;
    public static final int CANVAS_W = TabletLayout.CANVAS_W;
    public static final int CHAPTER_CARD_H = TabletLayout.CHAPTER_CARD_H;
    public static final int CHAPTER_CARD_GAP = TabletLayout.CHAPTER_CARD_GAP;
    public static final int CHAPTER_COLLAPSED_ROW_STEP = TabletLayout.CHAPTER_COLLAPSED_ROW_STEP;
    public static final String DRAFT_CHAPTER = TabletLayout.DRAFT_CHAPTER;
    public static final Path ASSETS_ROOT_DIR = TabletLayout.ASSETS_ROOT_DIR;
    public static final int CHAPTER_X = TabletLayout.CHAPTER_X;
    public static final int CHAPTER_Y = TabletLayout.CHAPTER_Y;
    public static final int CHAPTER_H = TabletLayout.CHAPTER_H;
    public static final int CANVAS_X = TabletLayout.CANVAS_X;
    public static final int CANVAS_Y = TabletLayout.CANVAS_Y;
    public static final int CANVAS_H = TabletLayout.CANVAS_H;
    public static final int CANVAS_TOP_H_COMPACT = TabletLayout.CANVAS_TOP_H_COMPACT;
    public static final int CANVAS_TOP_H_EXPANDED = TabletLayout.CANVAS_TOP_H_EXPANDED;
    public static final int CANVAS_GRID_ROWS = TabletLayout.CANVAS_GRID_ROWS;
    public static final int CANVAS_GRID_COLS = TabletLayout.CANVAS_GRID_COLS;
    public static final int CARD_W = TabletLayout.CARD_W;
    public static final int CARD_H = TabletLayout.CARD_H;
    public static final int CONTEXT_ROW_H = TabletLayout.CONTEXT_ROW_H;
    public static final int[] GRID_SIZES = TabletLayout.GRID_SIZES;
    public static final int[] GRID_OPACITY = TabletLayout.GRID_OPACITY;
    public static final int[] CANVAS_BG_OPACITY = TabletLayout.CANVAS_BG_OPACITY;
    public static final int[] CANVAS_LIMIT_WIDTH = TabletLayout.CANVAS_LIMIT_WIDTH;
    public static final int[] CANVAS_LIMIT_HEIGHT = TabletLayout.CANVAS_LIMIT_HEIGHT;
    public static final String[] CANVAS_LIMIT_LABELS = TabletLayout.CANVAS_LIMIT_LABELS;
    public static final int CHAPTER_SCROLL_W = TabletLayout.CHAPTER_SCROLL_W;
    public static final int SHARED_MENU_W = TabletLayout.SHARED_MENU_W;
    public static final int CONTENT_ICON_SIZE = 16;
    public static final int ACTION_ICON_SIZE = 12;

    private TabletUiFactory() {
    }

    public static void setActiveTabletRefresh(Runnable refresh) {
        TabletActiveState.setActiveTabletRefresh(refresh);
    }

    public static void setActiveTabletState(TabletUiState state) {
        TabletActiveState.setActiveTabletState(state);
    }

    public static void refreshActiveTablet() {
        TabletActiveState.refreshActiveTablet();
    }

    public static String activeSelectedGroup() {
        return TabletActiveState.activeSelectedGroup();
    }

    public static void syncCanvasStateFromCache(TabletUiState state) {
        TabletActiveState.syncCanvasStateFromCache(state);
    }

    public static void syncActiveCanvasStateFromCache() {
        TabletActiveState.syncActiveCanvasStateFromCache();
    }

    public static void selectPastedQuests(ListTag ids) {
        TabletActiveState.selectPastedQuests(ids);
    }

    public static void selectPastedQuests(CompoundTag payload) {
        TabletActiveState.selectPastedQuests(payload);
    }

    public static WidgetGroup create(Player player) {
        return TabletShellComposer.create(player);
    }

    public static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletShellComposer.create(player, rootWidth, rootHeight, fullScreenMode);
    }

    public static void applyRootSize(TabletUiState state, int width, int height, boolean fullScreenMode) {
        TabletLayout.applyRootSize(state, width, height, fullScreenMode);
    }

    public static int chapterHeight(TabletUiState state) {
        return TabletLayout.chapterHeight(state);
    }

    public static int canvasHeight(TabletUiState state) {
        return TabletLayout.canvasHeight(state);
    }

    public static int chapterPanelWidth(TabletUiState state) {
        return TabletLayout.chapterPanelWidth(state);
    }

    public static boolean isChapterPanelCollapsed(TabletUiState state) {
        return TabletLayout.isChapterPanelCollapsed(state);
    }

    public static int canvasPanelX(TabletUiState state) {
        return TabletLayout.canvasPanelX(state);
    }

    public static int canvasPanelWidth(TabletUiState state) {
        return TabletLayout.canvasPanelWidth(state);
    }

    public static int[] canvasViewportBounds(int panelW, int panelH, int topH) {
        return TabletLayout.canvasViewportBounds(panelW, panelH, topH);
    }

    public static String uniqueGroupName(String preferred, String excludeCurrent) {
        return EditorCommandClient.uniqueGroupName(preferred, excludeCurrent);
    }

    public static String sanitizeGroupName(String value) {
        return EditorCommandClient.sanitizeGroupName(value);
    }

    public static void runGroupAction(Player player, TabletUiState state, String action, String group, String value, int offset) {
        EditorCommandClient.runGroupAction(player, state, action, group, value, offset);
    }

    public static void runCanvasMoveAction(Player player, TabletUiState state, Map<String, CanvasPoint> positions) {
        EditorCommandClient.runCanvasMoveAction(player, state, positions);
    }

    public static void runPrerequisiteAction(Player player, String questId, String prerequisiteId, boolean add) {
        EditorCommandClient.runPrerequisiteAction(player, questId, prerequisiteId, add);
    }

    public static void runQuestIconAction(Player player, String questId, String icon) {
        EditorCommandClient.runQuestIconAction(player, questId, icon);
    }

    public static void runRemoveQuestAction(Player player, String questId) {
        EditorCommandClient.runRemoveQuestAction(player, questId);
    }

    public static void addQuestAt(Player player, TabletUiState state, int logicalX, int logicalY, String title) {
        EditorCommandClient.addQuestAt(player, state, logicalX, logicalY, title);
    }

    public static int snapToGrid(TabletUiState state, int value) {
        return CanvasRenderer.snapToGrid(state, value);
    }

    public static QuestCardLayout hitTestCard(List<QuestCardLayout> cards, int x, int y) {
        return CanvasRenderer.hitTestCard(cards, x, y);
    }

    public static EdgeHit hitTestEdge(TabletUiState state, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId, int x, int y) {
        return CanvasRenderer.hitTestEdge(state, cards, byQuestId, x, y);
    }

    public static boolean isContextMenuHit(TabletUiState state, int x, int y) {
        return CanvasRenderer.isContextMenuHit(state, x, y);
    }

    public static int chapterBackgroundFill(String background, int fallback) {
        return TabletAssets.chapterBackgroundFill(background, fallback);
    }

    public static IGuiTexture chapterBackgroundTexture(String background) {
        return TabletAssets.chapterBackgroundTexture(background);
    }

    public static IGuiTexture chapterBackgroundTexture(String background, boolean grayscale) {
        return TabletAssets.chapterBackgroundTexture(background, grayscale);
    }

    public static ItemStackTexture iconTexture(String iconId) {
        return TabletAssets.iconTexture(iconId);
    }

    public static List<AssetLibrary.AssetEntry> listAssetEntries(String relativeDir) {
        return TabletAssets.listAssetEntries(relativeDir);
    }

    public static List<AssetLibrary.AssetEntry> searchAssetEntries(String relativeDir, String query) {
        return TabletAssets.searchAssetEntries(relativeDir, query);
    }

    public static AssetLibrary.AssetDimensions assetDimensions(String relativePath) {
        return TabletAssets.assetDimensions(relativePath);
    }

    public static IGuiTexture assetThumbnailTexture(String relativePath) {
        return TabletAssets.assetThumbnailTexture(relativePath);
    }

    public static void ensureAssetsDirs() {
        TabletAssets.ensureAssetsDirs();
    }

    public static void prewarmClientUiAssets() {
        TabletAssets.prewarmClientUiAssets();
    }

    public static int indexAtY(int localY, TabletUiState state) {
        return TabletLayout.indexAtY(localY, state);
    }

    public static int chapterInsertIndexAtY(int localY, TabletUiState state) {
        return TabletLayout.chapterInsertIndexAtY(localY, state);
    }

    public static int chapterIndexAtY(int localY, TabletUiState state) {
        return TabletLayout.chapterIndexAtY(localY, state);
    }

    public static int chapterRowStep(TabletUiState state) {
        return TabletLayout.chapterRowStep(state);
    }

    public static boolean isChapterScrollBarHit(int localX, int localY, TabletUiState state) {
        return TabletLayout.isChapterScrollBarHit(localX, localY, state);
    }

    public static boolean isChapterCardAreaHit(int localX, int localY, TabletUiState state) {
        return TabletLayout.isChapterCardAreaHit(localX, localY, state);
    }

    public static void updateChapterScrollByMouse(double mouseY, TabletUiState state) {
        TabletLayout.updateChapterScrollByMouse(mouseY, state);
    }

    public static String chapterAtY(int localY, TabletUiState state) {
        return TabletLayout.chapterAtY(localY, state);
    }

    public static int chapterTextMenuY(TabletUiState state, int listHeight) {
        return TabletLayout.chapterTextMenuY(state, listHeight);
    }

    public static int chapterTextMenuX(TabletUiState state) {
        return TabletLayout.chapterTextMenuX(state);
    }

    public static int chapterTextMenuWidth(TabletUiState state) {
        return TabletLayout.chapterTextMenuWidth(state);
    }

    public static int chapterTextMenuHeight(TabletUiState state) {
        return TabletLayout.chapterTextMenuHeight(state);
    }

    public static void deleteAssetFile(String relativePath) {
        TabletAssets.deleteAssetFile(relativePath);
    }

    public static void renameAssetFile(String relativePath, String targetNameRaw) {
        TabletAssets.renameAssetFile(relativePath, targetNameRaw);
    }

    public static String shortQuestId(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        int slash = value.lastIndexOf('/');
        return slash >= 0 && slash + 1 < value.length() ? value.substring(slash + 1) : value;
    }

    public static WidgetGroup panel(int x, int y, int w, int h, int fill, int border) {
        return TabletWidgets.panel(x, y, w, h, fill, border);
    }

    public static LabelWidget label(int x, int y, String text, int color) {
        return TabletWidgets.label(x, y, text, color);
    }

    public static LabelWidget dynamicLabel(int x, int y, java.util.function.Supplier<String> supplier, int color) {
        return TabletWidgets.dynamicLabel(x, y, supplier, color);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        TabletWidgets.addWindowsContextRow(menu, y, width, text, icon, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, boolean submenu, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        TabletWidgets.addWindowsContextRow(menu, y, width, text, icon, submenu, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, int iconColor, boolean submenu, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        TabletWidgets.addWindowsContextRow(menu, y, width, text, icon, iconColor, submenu, callback);
    }

    public static String contextIconForLabel(String label) {
        return TabletWidgets.contextIconForLabel(label);
    }

    public static ButtonWidget button(int x, int y, int w, int h, String text, int baseColor, int activeColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return TabletWidgets.button(x, y, w, h, text, baseColor, activeColor, callback);
    }

    public static ButtonWidget closeIconButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return TabletWidgets.closeIconButton(x, y, w, h, callback);
    }

    public static ButtonWidget flatHitButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return TabletWidgets.flatHitButton(x, y, w, h, callback);
    }

    public static String pendingDeleteLabel(TabletUiState state, String key, String fallback) {
        return TabletWidgets.pendingDeleteLabel(state, key, fallback);
    }

    public static boolean confirmDeleteClick(TabletUiState state, String key) {
        return TabletWidgets.confirmDeleteClick(state, key);
    }

    public static void readPersistedUiState(TabletUiState state) {
        TabletPersistence.readPersistedUiState(state);
    }

    public static boolean readPersistedEditMode() {
        return TabletPersistence.readPersistedEditMode();
    }

    public static void persistUiState(TabletUiState state) {
        TabletPersistence.persistUiState(state);
    }

    public static void persistEditMode(boolean enabled) {
        TabletPersistence.persistEditMode(enabled);
    }
}
