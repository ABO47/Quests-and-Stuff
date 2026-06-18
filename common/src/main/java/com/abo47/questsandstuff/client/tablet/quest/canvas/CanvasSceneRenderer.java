package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;


import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasBackgroundOpacity;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasImageLayerRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasQuestEffectBadges;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionLine;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.QuestCardBackgroundRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class CanvasSceneRenderer {
    private static final ResourceTexture EXCLUSIVE_CHOICE_TEXTURE = new ResourceTexture(new ResourceLocation("questsandstuff", "textures/gui/other/exclusive_choice.png"));

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
        canvasViewport.setBackground(Surfaces.transparentFill());
    }

    static void renderGridOverlay(WidgetGroup canvasViewport, TabletUiState state, int contentX, int contentY, int contentW, int contentH) {
        if (contentW <= 0 || contentH <= 0) {
            return;
        }
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int alphaPercent = Math.max(0, Math.min(100, state.canvas.gridOpacityPercent));
                int alpha = Math.max(20, Math.min(220, (255 * alphaPercent) / 100));
                int lineColor = (alpha << 24) | (TabletGridControls.defaultGridColor(state) & 0x00FFFFFF);
                int cell = CanvasGeometry.gridSize(state);
                int originX = getPositionX();
                int originY = getPositionY();
                int visibleLeft = contentX - state.canvas.canvasLivePanX;
                int visibleTop = contentY - state.canvas.canvasLivePanY;
                int visibleRight = contentX + contentW - state.canvas.canvasLivePanX;
                int visibleBottom = contentY + contentH - state.canvas.canvasLivePanY;

                int firstCol = (int) Math.floor(CanvasCameraController.screenToLogicalX(state, contentX, true) / cell) - 1;
                int lastCol = (int) Math.ceil(CanvasCameraController.screenToLogicalX(state, contentX + contentW, true) / cell) + 1;
                int firstRow = (int) Math.floor(CanvasCameraController.screenToLogicalY(state, contentY, true) / cell) - 1;
                int lastRow = (int) Math.ceil(CanvasCameraController.screenToLogicalY(state, contentY + contentH, true) / cell) + 1;

                for (int col = firstCol; col <= lastCol; col++) {
                    int x = CanvasGeometry.screenX(state, col * cell);
                    if (x < visibleLeft || x > visibleRight) {
                        continue;
                    }
                    graphics.fill(originX + x, originY + visibleTop, originX + x + 1, originY + visibleBottom + 1, lineColor);
                }
                for (int row = firstRow; row <= lastRow; row++) {
                    int y = CanvasGeometry.screenY(state, row * cell);
                    if (y < visibleTop || y > visibleBottom) {
                        continue;
                    }
                    graphics.fill(originX + visibleLeft, originY + y, originX + visibleRight + 1, originY + y + 1, lineColor);
                }
            }
        });
    }

    static void renderCanvasSurfaces(WidgetGroup canvasViewport, TabletUiState state, int contentX, int contentY, int contentW, int contentH, int viewportW, int viewportH) {
        int opacityPercent = Math.max(0, Math.min(100, state.canvas.canvasBgOpacityPercent));
        int canvasFill = CanvasBackgroundOpacity.color(ModColors.SURFACE_BASE, opacityPercent);
        int paintW = contentW + 1;
        int paintH = contentH + 1;
        addSolidRect(canvasViewport, 0, 0, viewportW, contentY, canvasFill);
        addSolidRect(canvasViewport, 0, contentY + paintH, viewportW, viewportH - contentY - paintH, canvasFill);
        addSolidRect(canvasViewport, 0, contentY, contentX, paintH, canvasFill);
        addSolidRect(canvasViewport, contentX + paintW, contentY, viewportW - contentX - paintW, paintH, canvasFill);

        IGuiTexture canvasBackground = chapterBackgroundTexture(ClientQuestCache.groupCanvasBackground(selectedGroupName(state)));
        if (canvasBackground == null) {
            addSolidRect(canvasViewport, contentX, contentY, paintW, paintH, canvasFill);
        } else if (CanvasBackgroundOpacity.alpha(opacityPercent) > 0) {
            canvasViewport.addWidget(alphaTexture(contentX, contentY, paintW, paintH, canvasBackground, opacityPercent));
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
        List<CanvasExclusiveChoice> exclusiveChoices = state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of());
        List<CanvasImageLayer> images = state.canvas.canvasImagesByGroup.getOrDefault(group, List.of());
        List<CanvasTextLayer> texts = state.canvas.canvasTextsByGroup.getOrDefault(group, List.of());
        if (exclusiveChoices.isEmpty() && images.isEmpty() && texts.isEmpty() && visibleCards.isEmpty()) {
            return;
        }
        Map<String, CanvasExclusiveChoice> exclusiveChoicesById = new HashMap<>();
        for (CanvasExclusiveChoice ec : exclusiveChoices) {
            exclusiveChoicesById.put(ec.id(), ec);
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
        List<ConnectionLine> connections = ConnectionRenderer.prerequisiteConnectionLines(state, visibleCards, cardsById, viewportW, viewportH);
        Map<String, ConnectionLine> connectionsByKey = new HashMap<>();
        List<String> connectionKeys = new ArrayList<>();
        for (ConnectionLine connection : connections) {
            String key = CanvasLayerOrdering.connectionKey(connection.edgeId());
            connectionsByKey.put(key, connection);
            connectionKeys.add(key);
        }
        List<String> layerOrder = CanvasLayerOrdering.normalize(state, group, visibleCards, images, texts, connectionKeys, exclusiveChoices);
        for (String key : layerOrder) {
            if (key.startsWith(CanvasLayerOrdering.CONNECTION_PREFIX)) {
                ConnectionLine connection = connectionsByKey.get(key);
                if (connection != null) {
                    ConnectionRenderer.renderConnectionLayer(canvasViewport, state, connection);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX)) {
                CanvasExclusiveChoice ec = exclusiveChoicesById.get(key.substring(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX.length()));
                if (ec != null) {
                    renderCanvasExclusiveChoice(canvasViewport, state, ec);
                }
                continue;
            }
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
        float progress = QuestCardBackgroundRenderer.questProgress(tag);
        boolean customBackground = QuestCardBackgroundRenderer.renderWidgetBackground(cardLayer, localCard.x(), localCard.y(), localCard.width(), localCard.height(), tag, progress);
        if (!customBackground && QuestCardBackgroundRenderer.shouldShowProgressFill(tag, progress)) {
            QuestCardBackgroundRenderer.renderWidgetProgressFill(cardLayer, localCard.x(), localCard.y(), localCard.width(), localCard.height(), progress);
        }
        renderQuestIcon(cardLayer, localCard);
        renderLockedPreviewState(cardLayer, localCard);
        if (!ClientQuestCache.questLockedPreview(card.tag())) {
            renderECConnectionLockedPreview(cardLayer, state, card);
        }
        renderSearchState(cardLayer, state, localCard);
        renderHiddenEditState(cardLayer, state, localCard);
        CanvasQuestEffectBadges.render(cardLayer, state, localCard);
        canvasViewport.addWidget(cardLayer);
        if (questCardLayerSink != null) {
            questCardLayerSink.accept(card.questId(), cardLayer);
        }
        if (state.root.canEdit && card.questId().equals(state.questDetails.pendingQuestTitleChangeId)) {
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

    private static void renderQuestRenameField(WidgetGroup canvasViewport, TabletUiState state, Player player, Runnable refresh, QuestCardLayout card, int viewportW, int viewportH) {
        int fieldW = Math.max(64, Math.min(124, Math.max(card.width() + 28, card.width() * 2)));
        int fieldH = 16;
        int x = Math.max(4, Math.min(card.centerX() - fieldW / 2, Math.max(4, viewportW - fieldW - 4)));
        int belowY = card.y() + card.height() + 4;
        int aboveY = card.y() - fieldH - 4;
        int y = belowY + fieldH <= viewportH - 4 ? belowY : Math.max(4, Math.min(aboveY, viewportH - fieldH - 4));

        InlineRenameField field = new InlineRenameField(x, y, fieldW, fieldH, () -> state.questDetails.questTitleDraft, value -> {
            state.questDetails.questTitleDraft = value == null ? "" : value;
        }, () -> {
            EditorCommandClient.commitQuestTitleChange(player, state);
            refresh.run();
        }, () -> {
            EditorCommandClient.cancelQuestTitleChange(state);
            refresh.run();
        }, null, null);
        field.setClientSideWidget();
        field.setCurrentString(state.questDetails.questTitleDraft == null ? "" : state.questDetails.questTitleDraft);
        field.setMaxStringLength(80);
        field.setBordered(false);
        field.setTextColor(ModColors.TEXT_PRIMARY);
        field.setBackground(Surfaces.bordered(withAlpha(ModColors.SURFACE_BASE, 246), ModColors.BORDER_ACCENT));
        field.setFocus(true);
        canvasViewport.addWidget(field);
    }

    private static void renderHiddenEditState(WidgetGroup canvasViewport, TabletUiState state, QuestCardLayout card) {
        if (!state.root.canEdit || card.tag().getBoolean("completed")) {
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

    private static void renderECConnectionLockedPreview(WidgetGroup canvasViewport, TabletUiState state, QuestCardLayout card) {
        String group = selectedGroupName(state);
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())) {
            CanvasExclusiveChoice drawEc = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
            if (drawEc.connectionQuestIds().contains(card.questId()) && !drawEc.prerequisiteQuestIds().isEmpty()) {
                addSolidRect(canvasViewport, card.x(), card.y(), card.width(), card.height(), withAlpha(ModColors.SURFACE_BASE, 150));
                return;
            }
        }
    }

    private static void renderSearchState(WidgetGroup canvasViewport, TabletUiState state, QuestCardLayout card) {
        String query = state.root.search == null ? "" : state.root.search.trim();
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
                CanvasImageLayer drawImage = CanvasLayerMutations.effectiveCanvasImage(state, image);
                int originX = getPositionX();
                int originY = getPositionY();
                CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
                int w = box.width();
                int h = box.height();
                int pivotX = -box.left();
                int pivotY = -box.top();
                CanvasImageLayerRenderer.drawAtPivot(graphics, mouseX, mouseY, drawImage, originX + box.centerX(), originY + box.centerY(), w, h, pivotX, pivotY);
                if (state.root.canEdit && CanvasSelectionActions.isImageSelected(state, drawImage.id())) {
                    if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
                        return;
                    }
                    if (CanvasTransformGizmo.supports(drawImage.asset())) {
                        CanvasTransformGizmo.drawAtPivot(graphics, state, originX, originY, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation(), drawImage.entityYaw(), drawImage.modelPitch());
                        return;
                    }
                    CanvasElementSelectionSlot.drawAtPivot(graphics, state, originX, originY, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
                }
            }
        });
    }

    private static void renderCanvasExclusiveChoice(WidgetGroup canvasViewport, TabletUiState state, CanvasExclusiveChoice ec) {
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasExclusiveChoice drawEc = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
                int originX = getPositionX();
                int originY = getPositionY();
                CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, drawEc.x(), drawEc.y(), drawEc.w(), drawEc.h(), 0, 0, drawEc.rotation());
                int w = box.width();
                int h = box.height();
                int pivotX = -box.left();
                int pivotY = -box.top();
                graphics.pose().pushPose();
                graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
                graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(drawEc.rotation())));
                String bg = QuestDisplay.normalizeQuestBackground(drawEc.background());
                if (!QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(bg)) {
                    IGuiTexture bgTexture = chapterBackgroundTexture(bg, false);
                    if (bgTexture != null) {
                        bgTexture.draw(graphics, mouseX, mouseY, -pivotX, -pivotY, w, h);
                    }
                } else {
                    EXCLUSIVE_CHOICE_TEXTURE.draw(graphics, mouseX, mouseY, -pivotX, -pivotY, w, h);
                }
                graphics.pose().popPose();
                if (state.root.canEdit && CanvasSelectionActions.isExclusiveChoiceSelected(state, drawEc.id())) {
                    CanvasElementSelectionSlot.drawAtPivot(graphics, state, originX, originY, drawEc.x(), drawEc.y(), drawEc.w(), drawEc.h(), 0, 0, drawEc.rotation());
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

    private static WidgetGroup alphaTexture(int x, int y, int width, int height, IGuiTexture texture, int opacityPercent) {
        return new WidgetGroup(x, y, width, height) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasBackgroundOpacity.drawTexture(graphics, texture, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), opacityPercent);
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
        int progress = QuestCardBackgroundRenderer.progressPercent(tag);
        Component status = ClientQuestCache.questLockedPreview(tag)
                ? Component.translatable("ui.questsandstuff.quest.locked")
                : Component.literal(progress + "%");
        ButtonWidget hit = new ButtonWidget(card.x(), card.y(), card.width(), card.height(), Surfaces.transparentFill(), click -> {});
        hit.setClientSideWidget();
        hit.setHoverTexture(Surfaces.transparentFill());
        hit.setClickedTexture(Surfaces.transparentFill());
        hit.setHoverTooltips(new Component[]{
                Component.literal(title),
                status
        });
        canvasViewport.addWidget(hit);
    }
}
