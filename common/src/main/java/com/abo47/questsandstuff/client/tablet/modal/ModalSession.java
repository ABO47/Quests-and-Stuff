package com.abo47.questsandstuff.client.tablet.modal;

public record ModalSession(ModalWindowManager.ModalType type) {
    private static final ModalSession NONE = new ModalSession(ModalWindowManager.ModalType.NONE);

    public ModalSession {
        type = type == null ? ModalWindowManager.ModalType.NONE : type;
    }

    public static ModalSession none() {
        return NONE;
    }

    public static ModalSession open(ModalWindowManager.ModalType type) {
        ModalWindowManager.ModalType safeType = type == null ? ModalWindowManager.ModalType.NONE : type;
        return safeType == ModalWindowManager.ModalType.NONE ? NONE : new ModalSession(safeType);
    }

    public boolean active() {
        return type != ModalWindowManager.ModalType.NONE;
    }
}
