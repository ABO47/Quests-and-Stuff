package com.abo47.questsandstuff.client.tablet.contextmenu;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Collects context actions grouped by section and flattens them into a single
 * ordered action list. A section header is inserted before each non-empty section. When only one
 * section has any actions the header is omitted so small menus stay flat.
 */
public final class ContextMenuSections {
    private final Map<ContextMenuSection, List<ContextAction>> buckets = new EnumMap<>(ContextMenuSection.class);

    public ContextMenuSections add(ContextMenuSection section, ContextAction action) {
        if (section == null || action == null) {
            return this;
        }
        buckets.computeIfAbsent(section, k -> new ArrayList<>()).add(action);
        return this;
    }

    public ContextMenuSections addAll(ContextMenuSection section, List<ContextAction> actions) {
        if (actions == null) {
            return this;
        }
        for (ContextAction action : actions) {
            add(section, action);
        }
        return this;
    }

    public List<ContextAction> build() {
        int nonEmpty = 0;
        for (ContextMenuSection section : ContextMenuSection.values()) {
            List<ContextAction> actions = buckets.get(section);
            if (actions != null && !actions.isEmpty()) {
                nonEmpty++;
            }
        }
        boolean skipHeaders = nonEmpty <= 1;
        List<ContextAction> result = new ArrayList<>();
        for (ContextMenuSection section : ContextMenuSection.values()) {
            List<ContextAction> actions = buckets.get(section);
            if (actions == null || actions.isEmpty()) {
                continue;
            }
            if (!skipHeaders) {
                ContextAction header = ContextAction.sectionHeader(section);
                result.add(header);
            }
            result.addAll(actions);
        }
        return result;
    }
}
