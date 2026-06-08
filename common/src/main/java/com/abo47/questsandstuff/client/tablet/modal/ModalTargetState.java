package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ModalTargetState {
    private static final Set<String> MALFORMED_TARGET_LOG_KEYS = ConcurrentHashMap.newKeySet();

    private ModalTargetState() {
    }

    public static String target(TabletUiState state, ModalSession.TargetSlot slot, String fallback) {
        String sessionTarget = state == null || state.modalSession == null ? "" : state.modalSession.target(slot);
        return clean(sessionTarget.isBlank() ? fallback : sessionTarget);
    }

    public static ModalTargetParser.Target parsedTarget(TabletUiState state, ModalSession.TargetSlot slot, String fallback) {
        return ModalTargetParser.parse(target(state, slot, fallback));
    }

    public static Set<String> targetSet(TabletUiState state, ModalSession.TargetSetSlot slot, Set<String> fallback) {
        Set<String> sessionTargets = state == null || state.modalSession == null ? Set.of() : state.modalSession.targetSet(slot);
        if (!sessionTargets.isEmpty()) {
            return sessionTargets;
        }
        if (fallback == null || fallback.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> cleanTargets = new LinkedHashSet<>();
        for (String value : fallback) {
            String clean = clean(value);
            if (!clean.isBlank()) {
                cleanTargets.add(clean);
            }
        }
        return cleanTargets.isEmpty() ? Set.of() : Collections.unmodifiableSet(cleanTargets);
    }

    public static boolean requireParts(String owner, ModalTargetParser.Target target, int count) {
        if (target != null && target.hasAtLeast(count)) {
            return true;
        }
        logMalformed(owner, target, "expected_parts_" + count);
        return false;
    }

    public static boolean requireNonBlankParts(String owner, ModalTargetParser.Target target, int... indexes) {
        if (target == null) {
            logMalformed(owner, null, "missing_target");
            return false;
        }
        for (int index : indexes) {
            if (target.part(index).isBlank()) {
                logMalformed(owner, target, "blank_part_" + index);
                return false;
            }
        }
        return true;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static void logMalformed(String owner, ModalTargetParser.Target target, String reason) {
        String safeOwner = clean(owner);
        String raw = target == null ? "" : target.raw();
        String key = safeOwner + "|" + reason + "|" + raw;
        if (MALFORMED_TARGET_LOG_KEYS.add(key)) {
            QuestsAndStuffMod.debugLog("[QnS:UI] malformed modal target owner={} reason={} target={}", safeOwner, reason, raw);
        }
    }
}
