package com.abo47.questsandstuff.client.tablet.text;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class TabletTranslationKeys {
    public static final String COMMON_CANCEL = "ui.questsandstuff.common.cancel";
    public static final String COMMON_CONFIRM = "ui.questsandstuff.common.confirm";
    public static final String COMMON_REMOVE = "ui.questsandstuff.common.remove";
    public static final String COMMON_DISABLED = "ui.questsandstuff.common.disabled";
    public static final String COMMON_ENABLED = "ui.questsandstuff.common.enabled";
    public static final String COMMON_OK = "ui.questsandstuff.common.ok";
    public static final String COMMON_SAVE = "ui.questsandstuff.common.save";
    public static final String COMMON_UNKNOWN = "ui.questsandstuff.common.unknown";
    public static final String COMMON_USE = "ui.questsandstuff.common.use";

    public static final String STYLE_TOOLTIP_ALIGN_LEFT = "ui.questsandstuff.style.tooltip.align_left";
    public static final String STYLE_TOOLTIP_ALIGN_CENTER = "ui.questsandstuff.style.tooltip.align_center";
    public static final String STYLE_TOOLTIP_ALIGN_RIGHT = "ui.questsandstuff.style.tooltip.align_right";
    public static final String STYLE_TOOLTIP_COLOR = "ui.questsandstuff.style.tooltip.color";
    public static final String STYLE_TOOLTIP_BOLD = "ui.questsandstuff.style.tooltip.bold";
    public static final String STYLE_TOOLTIP_ITALIC = "ui.questsandstuff.style.tooltip.italic";
    public static final String STYLE_TOOLTIP_UNDERLINE = "ui.questsandstuff.style.tooltip.underline";
    public static final String STYLE_TOOLTIP_STRIKETHROUGH = "ui.questsandstuff.style.tooltip.strikethrough";
    public static final String STYLE_TOOLTIP_QUOTE = "ui.questsandstuff.style.tooltip.quote";
    public static final String STYLE_TOOLTIP_SPOILER = "ui.questsandstuff.style.tooltip.spoiler";
    public static final String STYLE_TOOLTIP_RESET = "ui.questsandstuff.style.tooltip.reset";
    public static final String STYLE_TOOLTIP_FONT_SIZE = "ui.questsandstuff.style.tooltip.font_size";

    private TabletTranslationKeys() {
    }

    public static String text(String key, Object... args) {
        return I18n.get(key, args);
    }

    public static Component component(String key, Object... args) {
        return Component.translatable(key, args);
    }
}
