package com.abo47.questsandstuff.quest.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonProvider {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private GsonProvider() {
    }
}
