package com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint;

public final class BlueprintPlacementState {
    private boolean active;
    private String asset = "";

    public boolean active() {
        return active;
    }

    public String asset() {
        return asset;
    }

    public boolean hasAsset() {
        return !asset.isBlank();
    }

    public void rememberAsset(String asset) {
        this.asset = clean(asset);
    }

    public void begin(String asset) {
        this.asset = clean(asset);
        this.active = !this.asset.isBlank();
    }

    public void cancel() {
        active = false;
    }

    public void finish() {
        active = false;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
