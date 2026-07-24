package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.joml.Quaternionf;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

public final class CanvasTextRenderer {
    private CanvasTextRenderer() {
    }

    public static void renderCanvasText(WidgetGroup canvasViewport, TabletUiState state, CanvasTextLayer text) {
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasTextLayer drawText = CanvasLayerMutations.effectiveCanvasText(state, text);
                int originX = getPositionX();
                int originY = getPositionY();
                CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
                int w = box.width();
                int h = box.height();
                boolean inlineEditing = isMainCanvasTextEditing(state, drawText);
                graphics.pose().pushPose();
                graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
                graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(drawText.rotation())));
                drawCanvasTextLines(graphics, state, drawText, w, h, inlineEditing);
                if (inlineEditing) {
                    drawCanvasTextCaret(graphics, state, drawText, w, h);
                }
                graphics.pose().popPose();
                if (state.root.canEdit && CanvasSelectionActions.isTextSelected(state, drawText.id())) {
                    if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
                        CanvasElementSelectionSlot.drawFillAndOutline(graphics, state, originX, originY, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
                    } else {
                        CanvasElementSelectionSlot.draw(graphics, state, originX, originY, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
                    }
                }
            }
        });
    }

    public static int canvasTextCursorAt(TabletUiState state, CanvasTextLayer text, int x, int y) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
        double[] local = CanvasRenderer.canvasTextLocalScreenPoint(state, text, x, y);
        float scale = fontScale(text);
        int layoutW = layoutSize(box.width(), scale);
        int layoutH = layoutSize(box.height(), scale);
        return cursorAtLocalPoint(text, layoutW, layoutH, (local[0] - box.width() / 2.0) / scale, (local[1] - box.height() / 2.0) / scale);
    }

    public static int textCursorAtLocal(CanvasTextLayer text, int width, int height, double localX, double localY) {
        float scale = fontScale(text);
        int layoutW = layoutSize(width, scale);
        int layoutH = layoutSize(height, scale);
        return cursorAtLocalPoint(text, layoutW, layoutH, localX / scale, localY / scale);
    }

    public static void drawTextLayer(GuiGraphics graphics, TabletUiState state, CanvasTextLayer text, int width, int height, boolean inlineEditing) {
        drawCanvasTextLines(graphics, state, text, width, height, inlineEditing);
        if (inlineEditing) {
            drawCanvasTextCaret(graphics, state, text, width, height);
        }
    }

    public static CanvasTextLayer fitTextHeight(CanvasTextLayer text) {
        if (text == null) {
            return null;
        }
        int preferred = preferredTextHeight(text, text.w());
        return preferred > text.h() ? text.resizeTo(text.w(), preferred) : text;
    }

    public static int preferredTextHeight(CanvasTextLayer text, int width) {
        if (text == null) {
            return 14;
        }
        var font = Minecraft.getInstance().font;
        float scale = fontScale(text);
        int layoutW = layoutSize(Math.max(1, width), scale);
        int pad = 3;
        int textW = Math.max(1, layoutW - pad * 2);
        String value = text.text() == null ? "" : text.text();
        List<LineRun> lines = buildWrappedTextLines(text, value, textW, font);
        int layoutH = pad * 2 + Math.max(1, lines.size()) * Math.max(1, font.lineHeight);
        return Math.max(14, Math.round(layoutH * scale));
    }

    public static int textSelectionStart(TabletUiState state) {
        return TextEditSession.selectionStart(state);
    }

    public static int textSelectionEnd(TabletUiState state) {
        return TextEditSession.selectionEnd(state);
    }

    public static boolean hasTextSelection(TabletUiState state) {
        return TextEditSession.hasSelection(state);
    }

    public static CanvasTextLayer applyTextStyleSelection(TabletUiState state, CanvasTextLayer text, String style) {
        if (TextEditSession.isEditingTarget(state, text.id()) && hasTextSelection(state)) {
            return text.withStyleRange(textSelectionStart(state), textSelectionEnd(state), style);
        }
        return text.withStyle(style);
    }

    public static CanvasTextLayer toggleTextStyleSelection(TabletUiState state, CanvasTextLayer text, String flag) {
        if (TextEditSession.isEditingTarget(state, text.id()) && hasTextSelection(state)) {
            return text.toggleStyleFlagRange(textSelectionStart(state), textSelectionEnd(state), flag);
        }
        return text.withStyle(CanvasTextLayer.toggleStyleFlag(text.style(), flag));
    }

    public static boolean isTextStyleFlagActive(TabletUiState state, CanvasTextLayer text, String flag) {
        if (TextEditSession.isEditingTarget(state, text.id()) && hasTextSelection(state)) {
            return text.rangeHasStyleFlag(textSelectionStart(state), textSelectionEnd(state), flag);
        }
        return CanvasTextLayer.hasStyleFlag(activeTextStyle(state, text), flag);
    }

    public static int activeTextColor(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null || !TextEditSession.isEditingTarget(state, text.id()) || text.text().isEmpty()) {
            return text == null ? TabletColors.TEXT_PRIMARY : text.color();
        }
        int index = activeTextIndex(state, text);
        return text.colorAt(index);
    }

    public static CanvasTextLayer applyTextColorSelection(TabletUiState state, CanvasTextLayer text, int color) {
        if (TextEditSession.isEditingTarget(state, text.id()) && hasTextSelection(state)) {
            return text.withColorRange(textSelectionStart(state), textSelectionEnd(state), color);
        }
        return text.withColor(color);
    }

    private static void drawCanvasTextLines(GuiGraphics graphics, TabletUiState state, CanvasTextLayer text, int w, int h, boolean inlineEditing) {
        var font = Minecraft.getInstance().font;
        float scale = fontScale(text);
        int layoutW = layoutSize(w, scale);
        int layoutH = layoutSize(h, scale);
        TextLayout layout = layoutCanvasText(text, layoutW, layoutH, inlineEditing);
        int selectionStart = inlineEditing ? textSelectionStart(state) : -1;
        int selectionEnd = inlineEditing ? textSelectionEnd(state) : -1;
        StringBuilder run = new StringBuilder();
        int runX = 0;
        int runY = 0;
        int runColor = text.color();
        int previousIndex = -1;
        String runStyle = "normal";
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        for (TextGlyph glyph : layout.glyphs()) {
            if (selectionStart < selectionEnd && glyph.index() >= selectionStart && glyph.index() < selectionEnd) {
                SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 95)).draw(graphics, 0, 0, glyph.x(), glyph.y() - GRID_1, Math.max(1, glyph.width()), font.lineHeight + 2);
            }
            String style = text.styleAt(glyph.index());
            int color = text.colorAt(glyph.index());
            boolean startsNewRun = run.length() == 0
                    || glyph.y() != runY
                    || glyph.index() != previousIndex + 1
                    || color != runColor
                    || !style.equals(runStyle);
            if (startsNewRun) {
                drawCanvasTextRun(graphics, run, runX, runY, runColor, runStyle);
                run.setLength(0);
                runX = glyph.x();
                runY = glyph.y();
                runColor = color;
                runStyle = style;
            }
            run.append(glyph.value());
            previousIndex = glyph.index();
        }
        drawCanvasTextRun(graphics, run, runX, runY, runColor, runStyle);
        graphics.pose().popPose();
    }

    private static void drawCanvasTextRun(GuiGraphics graphics, StringBuilder run, int x, int y, int color, String style) {
        if (run.length() == 0) {
            return;
        }
        graphics.drawString(Minecraft.getInstance().font, styledCanvasTextComponent(run.toString(), style), x, y, color, false);
    }

    private static Component styledCanvasTextComponent(String value, String style) {
        Component component = Component.literal(value);
        if (isBoldStyle(style) && isItalicStyle(style)) {
            return component.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC);
        }
        if (isBoldStyle(style)) {
            return component.copy().withStyle(ChatFormatting.BOLD);
        }
        if (isItalicStyle(style)) {
            return component.copy().withStyle(ChatFormatting.ITALIC);
        }
        return component;
    }

    private static void drawCanvasTextCaret(GuiGraphics graphics, TabletUiState state, CanvasTextLayer text, int w, int h) {
        if ((System.currentTimeMillis() / 500L) % 2L != 0L) {
            return;
        }
        var font = Minecraft.getInstance().font;
        String value = text.text() == null ? "" : text.text();
        int cursor = Math.max(0, Math.min(TextEditSession.cursor(state), value.length()));
        float scale = fontScale(text);
        int layoutW = layoutSize(w, scale);
        int layoutH = layoutSize(h, scale);
        TextLayout layout = layoutCanvasText(text, layoutW, layoutH, true);
        CursorPoint point = layout.cursorPoint(cursor);
        int x = point.x();
        int y = point.y();
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        SurfaceFactory.fill(text.color()).draw(graphics, 0, 0, x, y - GRID_1, GRID_1, font.lineHeight + 2);
        graphics.pose().popPose();
    }

    private static float fontScale(CanvasTextLayer text) {
        return Math.max(0.1f, text.fontSize() / (float) CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    private static boolean isMainCanvasTextEditing(TabletUiState state, CanvasTextLayer text) {
        return TextEditSession.isMainCanvasEditing(state) && text.id().equals(state.canvas.canvasTextEditTarget);
    }

    private static int layoutSize(int screenSize, float scale) {
        return Math.max(1, Math.round(screenSize / Math.max(0.1f, scale)));
    }

    private static String activeTextStyle(TabletUiState state, CanvasTextLayer text) {
        if (!TextEditSession.isEditingTarget(state, text.id()) || text.text().isEmpty()) {
            return text.style();
        }
        return text.styleAt(activeTextIndex(state, text));
    }

    private static int activeTextIndex(TabletUiState state, CanvasTextLayer text) {
        int cursor = Math.max(0, Math.min(TextEditSession.cursor(state), text.text().length()));
        return Math.max(0, Math.min(text.text().length() - 1, cursor == 0 ? 0 : cursor - 1));
    }

    private static TextLayout layoutCanvasText(CanvasTextLayer text, int w, int h, boolean editing) {
        var font = Minecraft.getInstance().font;
        int pad = 3;
        int textW = Math.max(1, w - pad * 2);
        int maxLines = Math.max(1, (h - pad * 2) / Math.max(1, font.lineHeight));
        String value = text.text() == null ? "" : text.text();
        if (!editing && value.isBlank()) {
            value = "Text";
        }
        List<LineRun> lines = buildWrappedTextLines(text, value, textW, font);
        if (lines.size() > maxLines) {
            lines = new ArrayList<>(lines.subList(0, maxLines));
        }
        List<TextGlyph> glyphs = new ArrayList<>();
        List<CursorPoint> cursors = new ArrayList<>();
        int y = -h / 2 + pad;
        for (LineRun line : lines) {
            int lineWidth = styledLineWidth(text, line, font);
            int x = switch (text.align()) {
                case "center" -> -lineWidth / 2;
                case "right" -> w / 2 - pad - lineWidth;
                default -> -w / 2 + pad;
            };
            cursors.add(new CursorPoint(line.start(), x, y));
            int cx = x;
            for (int i = 0; i < line.value().length(); i++) {
                char c = line.value().charAt(i);
                int index = line.start() + i;
                int cw = styledCharWidth(text, index, c, font);
                glyphs.add(new TextGlyph(index, c, cx, y, cw));
                cursors.add(new CursorPoint(index + 1, cx + cw, y));
                cx += cw;
            }
            y += font.lineHeight;
        }
        if (cursors.isEmpty()) {
            cursors.add(new CursorPoint(0, -w / 2 + pad, -h / 2 + pad));
        }
        return new TextLayout(glyphs, cursors);
    }

    private static int styledLineWidth(CanvasTextLayer text, LineRun line, net.minecraft.client.gui.Font font) {
        int width = 0;
        for (int i = 0; i < line.value().length(); i++) {
            int index = line.start() + i;
            width += styledCharWidth(text, index, line.value().charAt(i), font);
        }
        return width;
    }

    private static List<LineRun> buildWrappedTextLines(CanvasTextLayer text, String value, int maxWidth, net.minecraft.client.gui.Font font) {
        List<LineRun> lines = new ArrayList<>();
        int width = Math.max(1, maxWidth);
        String safeValue = value == null ? "" : value;
        String[] paragraphs = safeValue.split("\n", -1);
        int globalIndex = 0;
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i];
            if (paragraph.isEmpty()) {
                lines.add(new LineRun(globalIndex, "", 0));
            } else {
                int local = 0;
                int lineStart = 0;
                int lineWidth = 0;
                while (local < paragraph.length()) {
                    char c = paragraph.charAt(local);
                    int charWidth = styledCharWidth(text, globalIndex + local, c, font);
                    if (lineWidth > 0 && lineWidth + charWidth > width) {
                        String piece = paragraph.substring(lineStart, local);
                        lines.add(new LineRun(globalIndex + lineStart, piece, lineWidth));
                        lineStart = local;
                        lineWidth = 0;
                        continue;
                    }
                    lineWidth += charWidth;
                    local++;
                }
                if (lineStart < paragraph.length()) {
                    String piece = paragraph.substring(lineStart);
                    lines.add(new LineRun(globalIndex + lineStart, piece, lineWidth));
                }
            }
            globalIndex += paragraph.length();
            if (i < paragraphs.length - 1) {
                globalIndex += 1;
            }
        }
        if (lines.isEmpty()) {
            lines.add(new LineRun(0, "", 0));
        }
        return lines;
    }

    private static int styledCharWidth(CanvasTextLayer text, int index, char value, net.minecraft.client.gui.Font font) {
        return Math.max(1, font.width(String.valueOf(value)) + (isBoldStyle(text.styleAt(index)) ? 1 : 0));
    }

    private static int cursorAtLocalPoint(CanvasTextLayer text, int w, int h, double localX, double localY) {
        TextLayout layout = layoutCanvasText(text, w, h, true);
        if (layout.glyphs().isEmpty()) {
            return 0;
        }
        var font = Minecraft.getInstance().font;
        int best = 0;
        double bestDistance = Double.MAX_VALUE;
        for (CursorPoint point : layout.cursors()) {
            double dx = localX - point.x();
            double dy = localY - (point.y() + font.lineHeight / 2.0);
            double distance = dx * dx + dy * dy * 4.0;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = point.index();
            }
        }
        return Math.max(0, Math.min(text.text().length(), best));
    }

    private static boolean isBoldStyle(String style) {
        return "bold".equals(style) || "bold_italic".equals(style);
    }

    private static boolean isItalicStyle(String style) {
        return "italic".equals(style) || "bold_italic".equals(style);
    }

    private record LineRun(int start, String value, int width) {
    }

    private record TextGlyph(int index, char value, int x, int y, int width) {
    }

    private record CursorPoint(int index, int x, int y) {
    }

    private record TextLayout(List<TextGlyph> glyphs, List<CursorPoint> cursors) {
        CursorPoint cursorPoint(int index) {
            CursorPoint nearest = cursors.isEmpty() ? new CursorPoint(0, 0, 0) : cursors.get(cursors.size() - 1);
            for (CursorPoint point : cursors) {
                if (point.index() == index) {
                    return point;
                }
                if (point.index() < index) {
                    nearest = point;
                }
            }
            return nearest;
        }
    }
}
