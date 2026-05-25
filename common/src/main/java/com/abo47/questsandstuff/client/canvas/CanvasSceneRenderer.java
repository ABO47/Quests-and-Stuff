package com.abo47.questsandstuff.client.canvas;


import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.canvas.render.CanvasImageLayerRenderer;
import com.abo47.questsandstuff.client.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class CanvasSceneRenderer {
    private static final ResourceLocation DEFAULT_QUEST_BG = ResourceLocation.tryBuild("questsandstuff", "textures/gui/quest_backgrounds/default_quest_bg.png");

    private CanvasSceneRenderer() {
    }

    static int snapCanvasContentSize(int available, int cell) {
        int grid = Math.max(1, cell);
        if (available <= grid) {
            return Math.max(1, available);
        }
        return Math.max(grid, (available / grid) * grid);
    }

    static void applyCanvasBackground(WidgetGroup canvasViewport) {
        canvasViewport.setBackground(Surfaces.fill(0x00000000));
    }

    static void renderGridOverlay(WidgetGroup canvasViewport, TabletUiState state, int contentX, int contentY, int contentW, int contentH) {
        int alphaPercent = Math.max(0, Math.min(100, state.gridOpacityPercent));
        int alpha = Math.max(20, Math.min(220, (255 * alphaPercent) / 100));
        int lineColor = (alpha << 24) | (ModColors.TEXT_PRIMARY & 0x00FFFFFF);
        int cell = CanvasGeometry.gridSize(state);
        int firstCol = (int) Math.floor(CanvasGeometry.screenToLogicalX(state, contentX) / cell) - 1;
        int lastCol = (int) Math.ceil(CanvasGeometry.screenToLogicalX(state, contentX + contentW) / cell) + 1;
        int firstRow = (int) Math.floor(CanvasGeometry.screenToLogicalY(state, contentY) / cell) - 1;
        int lastRow = (int) Math.ceil(CanvasGeometry.screenToLogicalY(state, contentY + contentH) / cell) + 1;

        for (int col = firstCol; col <= lastCol; col++) {
            int x = CanvasGeometry.screenX(state, col * cell);
            if (x < contentX || x > contentX + contentW) {
                continue;
            }
            WidgetGroup line = new WidgetGroup(x, contentY, 1, contentH + 1);
            line.setBackground(Surfaces.fill(lineColor));
            canvasViewport.addWidget(line);
        }
        for (int row = firstRow; row <= lastRow; row++) {
            int y = CanvasGeometry.screenY(state, row * cell);
            if (y < contentY || y > contentY + contentH) {
                continue;
            }
            WidgetGroup line = new WidgetGroup(contentX, y, contentW + 1, 1);
            line.setBackground(Surfaces.fill(lineColor));
            canvasViewport.addWidget(line);
        }
    }

    static void renderCanvasSurfaces(WidgetGroup canvasViewport, TabletUiState state, int contentX, int contentY, int contentW, int contentH, int viewportW, int viewportH) {
        int gutterFill = ModColors.SURFACE_BASE;
        int paintW = contentW + 1;
        int paintH = contentH + 1;
        addSolidRect(canvasViewport, 0, 0, viewportW, contentY, gutterFill);
        addSolidRect(canvasViewport, 0, contentY + paintH, viewportW, viewportH - contentY - paintH, gutterFill);
        addSolidRect(canvasViewport, 0, contentY, contentX, paintH, gutterFill);
        addSolidRect(canvasViewport, contentX + paintW, contentY, viewportW - contentX - paintW, paintH, gutterFill);

        int alphaPercent = Math.max(0, Math.min(100, state.canvasBgOpacityPercent));
        int alpha = Math.max(0, Math.min(255, (255 * alphaPercent) / 100));
        int background = withAlpha(ModColors.SURFACE_BASE, alpha);
        addSolidRect(canvasViewport, contentX, contentY, paintW, paintH, background);
        IGuiTexture canvasBackground = chapterBackgroundTexture(ClientQuestCache.groupCanvasBackground(selectedGroupName(state)));
        if (canvasBackground != null) {
            canvasViewport.addWidget(alphaTexture(contentX, contentY, paintW, paintH, canvasBackground, alpha));
        }
    }

    static void renderCanvasElements(
            WidgetGroup canvasViewport,
            TabletUiState state,
            Player player,
            Runnable refresh,
            List<QuestCardLayout> visibleCards,
            int viewportW,
            int viewportH,
            BiConsumer<String, WidgetGroup> questCardLayerSink
    ) {
        String group = selectedGroupName(state);
        List<CanvasImageLayer> images = state.canvasImagesByGroup.getOrDefault(group, List.of());
        List<CanvasTextLayer> texts = state.canvasTextsByGroup.getOrDefault(group, List.of());
        if (images.isEmpty() && texts.isEmpty() && visibleCards.isEmpty()) {
            return;
        }
        Map<String, CanvasImageLayer> imagesById = new HashMap<>();
        for (CanvasImageLayer image : images) {
            imagesById.put(image.id(), image);
        }
        Map<String, CanvasTextLayer> textsById = new HashMap<>();
        for (CanvasTextLayer text : texts) {
            textsById.put(text.id(), text);
        }
        Map<String, QuestCardLayout> cardsById = new HashMap<>();
        for (QuestCardLayout card : visibleCards) {
            cardsById.put(card.questId(), card);
        }
        List<String> layerOrder = CanvasLayerOrdering.normalize(state, group, visibleCards, images, texts);
        for (String key : layerOrder) {
            if (key.startsWith(CanvasLayerOrdering.IMAGE_PREFIX)) {
                CanvasImageLayer image = imagesById.get(key.substring(CanvasLayerOrdering.IMAGE_PREFIX.length()));
                if (image != null) {
                    renderCanvasImage(canvasViewport, state, image);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.TEXT_PREFIX)) {
                CanvasTextLayer text = textsById.get(key.substring(CanvasLayerOrdering.TEXT_PREFIX.length()));
                if (text != null) {
                    CanvasTextRenderer.renderCanvasText(canvasViewport, state, text);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.QUEST_PREFIX)) {
                QuestCardLayout card = cardsById.get(key.substring(CanvasLayerOrdering.QUEST_PREFIX.length()));
                if (card != null) {
                    renderQuestCard(canvasViewport, state, player, refresh, card, viewportW, viewportH, questCardLayerSink);
                }
            }
        }
    }

    private static void renderQuestCard(
            WidgetGroup canvasViewport,
            TabletUiState state,
            Player player,
            Runnable refresh,
            QuestCardLayout card,
            int viewportW,
            int viewportH,
            BiConsumer<String, WidgetGroup> questCardLayerSink
    ) {
        if (!CanvasLayoutService.intersectsPanRenderWindow(card, viewportW, viewportH)) {
            return;
        }
        CompoundTag tag = card.tag();
        WidgetGroup cardLayer = new WidgetGroup(card.x(), card.y(), card.width(), card.height());
        QuestCardLayout localCard = localCard(card);
        float progress = questProgress(tag);
        boolean customBackground = renderQuestBackground(cardLayer, localCard, tag, progress);
        if (!customBackground && tag.getBoolean("unlocked") && progress > 0.0f && !tag.getBoolean("completed") && !tag.getBoolean("claimed")) {
            renderQuestProgressFill(cardLayer, localCard, progress);
        }
        renderQuestIcon(cardLayer, localCard);
        renderLockedPreviewState(cardLayer, localCard);
        renderSearchState(cardLayer, state, localCard);
        renderHiddenEditState(cardLayer, state, localCard);
        canvasViewport.addWidget(cardLayer);
        if (questCardLayerSink != null) {
            questCardLayerSink.accept(card.questId(), cardLayer);
        }
        if (state.canEdit && card.questId().equals(state.pendingQuestTitleChangeId)) {
            renderQuestRenameField(canvasViewport, state, player, refresh, card, viewportW, viewportH);
            return;
        }
        addQuestTooltipHit(cardLayer, localCard);
    }

    private static QuestCardLayout localCard(QuestCardLayout card) {
        return new QuestCardLayout(
                card.questId(),
                card.tag(),
                card.logicalX(),
                card.logicalY(),
                card.logicalWidth(),
                card.logicalHeight(),
                card.slotLogicalWidth(),
                card.slotLogicalHeight(),
                card.visualLogicalX(),
                card.visualLogicalY(),
                card.scale(),
                0,
                0,
                card.width(),
                card.height()
        );
    }

    private static boolean renderQuestBackground(WidgetGroup canvasViewport, QuestCardLayout card, CompoundTag tag, float progress) {
        String background = tag.getString("quest_background");
        if (background == null || background.isBlank() || QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(background)) {
            canvasViewport.addWidget(new ImageWidget(card.x(), card.y(), card.width(), card.height(), new ResourceTexture(DEFAULT_QUEST_BG).setColor(defaultQuestBackgroundTint(tag))));
            return false;
        }
        IGuiTexture texture = chapterBackgroundTexture(background, tag.getBoolean("quest_background_grayscale"));
        if (texture == null) {
            canvasViewport.addWidget(new ImageWidget(card.x(), card.y(), card.width(), card.height(), new ResourceTexture(DEFAULT_QUEST_BG).setColor(defaultQuestBackgroundTint(tag))));
            return false;
        }
        canvasViewport.addWidget(new ImageWidget(card.x(), card.y(), card.width(), card.height(), texture));
        int filter = questBackgroundFilter(tag);
        if ((filter >>> 24) != 0) {
            addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), filter);
        } else if (tag.getBoolean("unlocked") && progress > 0.0f && progress < 1.0f && !tag.getBoolean("completed") && !tag.getBoolean("claimed")) {
            int fillW = Math.max(1, Math.min(card.width(), Math.round(card.width() * progress)));
            addSolidRect(canvasViewport, card.x(), card.y(), fillW, card.height(), withAlpha(ModColors.SUCCESS, 54));
        }
        return true;
    }

    private static int defaultQuestBackgroundTint(CompoundTag tag) {
        if (ClientQuestCache.questLockedPreview(tag)) {
            return withAlpha(ModColors.TEXT_SECONDARY, 255);
        }
        if (tag.getBoolean("claimed")) {
            return withAlpha(ModColors.WARNING, 255);
        }
        if (tag.getBoolean("completed")) {
            return withAlpha(ModColors.SUCCESS, 255);
        }
        return tag.getBoolean("unlocked") ? withAlpha(ModColors.INTERACTIVE, 255) : withAlpha(ModColors.TEXT_SECONDARY, 255);
    }

    private static int questBackgroundFilter(CompoundTag tag) {
        if (ClientQuestCache.questLockedPreview(tag)) {
            return withAlpha(ModColors.SURFACE_BASE, 138);
        }
        if (tag.getBoolean("claimed")) {
            return withAlpha(ModColors.WARNING, 94);
        }
        if (tag.getBoolean("completed")) {
            return withAlpha(ModColors.SUCCESS, 82);
        }
        return 0x00000000;
    }

    private static void renderQuestRenameField(WidgetGroup canvasViewport, TabletUiState state, Player player, Runnable refresh, QuestCardLayout card, int viewportW, int viewportH) {
        int fieldW = Math.max(64, Math.min(124, Math.max(card.width() + 28, card.width() * 2)));
        int fieldH = 16;
        int x = Math.max(4, Math.min(card.centerX() - fieldW / 2, Math.max(4, viewportW - fieldW - 4)));
        int belowY = card.y() + card.height() + 4;
        int aboveY = card.y() - fieldH - 4;
        int y = belowY + fieldH <= viewportH - 4 ? belowY : Math.max(4, Math.min(aboveY, viewportH - fieldH - 4));

        InlineRenameField field = new InlineRenameField(x, y, fieldW, fieldH, () -> state.questTitleDraft, value -> {
            state.questTitleDraft = value == null ? "" : value;
        }, () -> {
            EditorCommandClient.commitQuestTitleChange(player, state);
            refresh.run();
        }, () -> {
            EditorCommandClient.cancelQuestTitleChange(state);
            refresh.run();
        }, null, null);
        field.setClientSideWidget();
        field.setCurrentString(state.questTitleDraft == null ? "" : state.questTitleDraft);
        field.setMaxStringLength(80);
        field.setBordered(false);
        field.setTextColor(ModColors.TEXT_PRIMARY);
        field.setBackground(Surfaces.bordered(withAlpha(ModColors.SURFACE_BASE, 246), ModColors.BORDER_ACCENT));
        field.setFocus(true);
        canvasViewport.addWidget(field);
    }

    private static void renderHiddenEditState(WidgetGroup canvasViewport, TabletUiState state, QuestCardLayout card) {
        if (!state.canEdit || card.tag().getBoolean("completed")) {
            return;
        }
        boolean hidden = card.tag().getBoolean("visual_hidden") && !card.tag().getBoolean("unlocked");
        if (hidden) {
            addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), withAlpha(ModColors.SURFACE_BASE, 190));
        }
    }

    private static void renderLockedPreviewState(WidgetGroup canvasViewport, QuestCardLayout card) {
        if (ClientQuestCache.questLockedPreview(card.tag())) {
            addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), withAlpha(ModColors.SURFACE_BASE, 150));
        }
    }

    private static float questProgress(CompoundTag tag) {
        boolean hasTasks = !tag.getCompound("tasks").isEmpty();
        float progress = Math.max(0.0f, Math.min(1.0f, tag.getFloat("progress")));
        if (hasTasks && (tag.getBoolean("completed") || tag.getBoolean("claimed"))) {
            progress = 1.0f;
        }
        return progress;
    }

    private static void renderQuestProgressFill(WidgetGroup canvasViewport, QuestCardLayout card, float progress) {
        int fillW = Math.max(1, Math.min(card.width(), Math.round(card.width() * progress)));
        ResourceTexture fillTexture = new ResourceTexture(DEFAULT_QUEST_BG).setColor(withAlpha(ModColors.SUCCESS, 255));
        canvasViewport.addWidget(new WidgetGroup(card.x(), card.y(), card.width(), card.height()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasViewportScissor.draw(graphics, getPositionX(), getPositionY(), fillW, getSizeHeight(),
                        () -> fillTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight()));
            }
        });
    }

    private static void renderSearchState(WidgetGroup canvasViewport, TabletUiState state, QuestCardLayout card) {
        String query = state.search == null ? "" : state.search.trim();
        if (query.isBlank()) {
            return;
        }
        boolean matches = CanvasRenderer.matchesSearchOnly(card.tag(), query);
        if (!matches) {
            addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), withAlpha(ModColors.SURFACE_BASE, 150));
            return;
        }
        WidgetGroup highlight = new WidgetGroup(card.x() - 2, card.y() - 2, card.width() + 4, card.height() + 4);
        highlight.setBackground(Surfaces.transparentBorder(ModColors.WARNING));
        canvasViewport.addWidget(highlight);
    }

    private static void renderCanvasImage(WidgetGroup canvasViewport, TabletUiState state, CanvasImageLayer image) {
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                int screenLeft = CanvasGeometry.screenX(state, image.x());
                int screenTop = CanvasGeometry.screenY(state, image.y());
                int x = originX + screenLeft;
                int y = originY + screenTop;
                int w = Math.max(1, CanvasGeometry.screenX(state, image.x() + image.w()) - screenLeft);
                int h = Math.max(1, CanvasGeometry.screenY(state, image.y() + image.h()) - screenTop);
                int pivotX = CanvasGeometry.screenX(state, image.x() + image.pivotX()) - screenLeft;
                int pivotY = CanvasGeometry.screenY(state, image.y() + image.pivotY()) - screenTop;
                CanvasImageLayerRenderer.draw(graphics, mouseX, mouseY, image, x, y, w, h, pivotX, pivotY);
                if (state.canEdit && CanvasRenderer.isImageSelected(state, image.id())) {
                    if (CanvasRenderer.totalCanvasSelectionCount(state) > 1) {
                        return;
                    }
                    if (CanvasTransformGizmo.supports(image.asset())) {
                        CanvasTransformGizmo.drawAtPivot(graphics, state, originX, originY, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch());
                        return;
                    }
                    CanvasPoint selectionDragStart = state.dragStartImagePositions.get(image.id());
                    if (state.draggingSelection && selectionDragStart != null) {
                        CanvasElementSelectionSlot.drawDragging(
                                graphics,
                                state,
                                originX,
                                originY,
                                image.x(),
                                image.y(),
                                selectionDragStart.x,
                                selectionDragStart.y,
                                image.w(),
                                image.h(),
                                image.rotation()
                        );
                    } else if (state.draggingCanvasImage && image.id().equals(state.selectedCanvasImageId)) {
                        CanvasElementSelectionSlot.drawDragging(
                                graphics,
                                state,
                                originX,
                                originY,
                                image.x(),
                                image.y(),
                                state.canvasImageStartX,
                                state.canvasImageStartY,
                                state.canvasImageStartW,
                                state.canvasImageStartH,
                                state.canvasImageStartRotation
                        );
                    } else {
                        CanvasElementSelectionSlot.draw(graphics, state, originX, originY, image.x(), image.y(), image.w(), image.h(), image.rotation());
                    }
                }
            }
        });
    }

    private static void addSolidRect(WidgetGroup parent, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
            return;
        }
        WidgetGroup rect = new WidgetGroup(x, y, width, height);
        rect.setBackground(Surfaces.fill(color));
        parent.addWidget(rect);
    }

    private static WidgetGroup alphaTexture(int x, int y, int width, int height, IGuiTexture texture, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        return new WidgetGroup(x, y, width, height) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, safeAlpha / 255.0f);
                texture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        };
    }

    private static void renderQuestIcon(WidgetGroup canvasViewport, QuestCardLayout card) {
        String icon = card.tag().getString("icon");
        int min = Math.min(card.width(), card.height());
        int pad = Math.max(1, Math.round(min * 0.16f));
        int iconSize = Math.max(1, min - pad * 2);
        int iconX = card.x() + (card.width() - iconSize) / 2;
        int iconY = card.y() + (card.height() - iconSize) / 2;
        canvasViewport.addWidget(new DisplayIconWidget(iconX, iconY, iconSize, iconSize, icon));
    }

    private static void addQuestTooltipHit(WidgetGroup canvasViewport, QuestCardLayout card) {
        CompoundTag tag = card.tag();
        String title = tag.getString("title");
        if (title == null || title.isBlank()) {
            title = card.questId();
        }
        int progress = Math.round(Math.max(0.0f, Math.min(1.0f, tag.getFloat("progress"))) * 100.0f);
        if (!tag.getCompound("tasks").isEmpty() && (tag.getBoolean("completed") || tag.getBoolean("claimed"))) {
            progress = 100;
        }
        Component status = ClientQuestCache.questLockedPreview(tag)
                ? Component.translatable("ui.questsandstuff.quest.locked")
                : Component.literal(progress + "%");
        ButtonWidget hit = new ButtonWidget(card.x(), card.y(), card.width(), card.height(), Surfaces.fill(0x00000000), click -> {});
        hit.setClientSideWidget();
        hit.setHoverTexture(Surfaces.fill(0x00000000));
        hit.setClickedTexture(Surfaces.fill(0x00000000));
        hit.setHoverTooltips(new Component[]{
                Component.literal(title),
                status
        });
        canvasViewport.addWidget(hit);
    }
}
