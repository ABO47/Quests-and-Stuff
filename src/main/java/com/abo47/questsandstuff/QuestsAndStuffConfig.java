package com.abo47.questsandstuff;

import net.minecraftforge.common.ForgeConfigSpec;

public final class QuestsAndStuffConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("debug");
        DEBUG_LOGGING = builder
                .comment("Enable verbose Quests and Stuff debug logging and debug files.")
                .define("debugLogging", false);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private QuestsAndStuffConfig() {
    }

    public static boolean debugLoggingEnabled() {
        return DEBUG_LOGGING.get();
    }
}
