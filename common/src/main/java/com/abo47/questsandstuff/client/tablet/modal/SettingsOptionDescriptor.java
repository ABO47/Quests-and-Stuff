package com.abo47.questsandstuff.client.tablet.modal;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public record SettingsOptionDescriptor(
        String id,
        String labelKey,
        String descriptionKey,
        SettingsOptionKind kind,
        BooleanSupplier getter,
        Consumer<Boolean> setter,
        IntSupplier intGetter,
        IntConsumer intSetter,
        Runnable action,
        int min,
        int max,
        int maxLength,
        boolean restartRequired,
        boolean requiresGlobalAnimation,
        String unitKey
) {
    SettingsOptionDescriptor(
            String id,
            String labelKey,
            String descriptionKey,
            BooleanSupplier getter,
            Consumer<Boolean> setter,
            boolean restartRequired,
            boolean requiresGlobalAnimation
    ) {
        this(
                id,
                labelKey,
                descriptionKey,
                SettingsOptionKind.TOGGLE,
                getter,
                setter,
                null,
                null,
                null,
                0,
                0,
                0,
                restartRequired,
                requiresGlobalAnimation,
                ""
        );
    }

    SettingsOptionDescriptor(
            String id,
            String labelKey,
            String descriptionKey,
            IntSupplier intGetter,
            IntConsumer intSetter,
            int min,
            int max,
            int maxLength,
            boolean restartRequired,
            String unitKey
    ) {
        this(
                id,
                labelKey,
                descriptionKey,
                SettingsOptionKind.NUMBER,
                null,
                null,
                intGetter,
                intSetter,
                null,
                min,
                max,
                maxLength,
                restartRequired,
                false,
                unitKey
        );
    }

    SettingsOptionDescriptor(
            String id,
            String labelKey,
            String descriptionKey,
            Runnable action
    ) {
        this(
                id,
                labelKey,
                descriptionKey,
                SettingsOptionKind.ACTION,
                null,
                null,
                null,
                null,
                action,
                0,
                0,
                0,
                false,
                false,
                ""
        );
    }

    boolean isAction() {
        return kind == SettingsOptionKind.ACTION;
    }

    boolean number() {
        return kind == SettingsOptionKind.NUMBER;
    }

    public String unitKey() {
        return unitKey == null ? "" : unitKey;
    }

    boolean enabled() {
        return getter.getAsBoolean();
    }

    void setEnabled(boolean enabled) {
        setter.accept(enabled);
    }

    int intValue() {
        return intGetter.getAsInt();
    }

    void setIntValue(int value) {
        intSetter.accept(value);
    }

    void runAction() {
        action.run();
    }
}
