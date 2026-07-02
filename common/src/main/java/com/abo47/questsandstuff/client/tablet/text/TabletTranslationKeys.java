package com.abo47.questsandstuff.client.tablet.text;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class TabletTranslationKeys {
    public static final String COMMON_CANCEL = "ui.questsandstuff.common.cancel";
    public static final String COMMON_CONFIRM = "ui.questsandstuff.common.confirm";
    public static final String COMMON_DELETE = "ui.questsandstuff.common.delete";
    public static final String COMMON_DISABLED = "ui.questsandstuff.common.disabled";
    public static final String COMMON_ENABLED = "ui.questsandstuff.common.enabled";
    public static final String COMMON_OK = "ui.questsandstuff.common.ok";
    public static final String COMMON_SAVE = "ui.questsandstuff.common.save";
    public static final String COMMON_UNKNOWN = "ui.questsandstuff.common.unknown";
    public static final String COMMON_USE = "ui.questsandstuff.common.use";

    private TabletTranslationKeys() {
    }

    public static String text(String key, Object... args) {
        return I18n.get(key, args);
    }

    public static Component component(String key, Object... args) {
        return Component.translatable(key, args);
    }
}
