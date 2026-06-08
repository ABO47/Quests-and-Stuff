package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerSelectionBridge;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.FluidIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class TabletRecipePickerModal {
    private static final int TILE = 18;
    private static RecipeManager cachedManager;
    private static RegistryAccess cachedRegistryAccess;
    private static RecipeChoices cachedChoices;
    private static String cachedQuery = null;
    private static boolean cachedTagMode;
    private static List<RecipeChoice> cachedValues = List.of();

    private TabletRecipePickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletVocabulary.text(QuestVocabulary.CHOOSE_RECIPE), w, state, refresh);
        int sidePad = 8;
        int headY = 24;
        int headH = 18;
        int modeW = headH;
        int gap = 4;
        int gridX = sidePad;
        int gridW = w - sidePad * 2;
        int searchX = gridX + modeW + gap;
        int searchW = gridW - modeW - gap;
        int gridY = headY + headH + 4;
        int gridH = h - gridY - 8;

        TextFieldWidget search = ModalShell.addSearchField(modal, searchX, headY, Math.max(24, searchW), headH, state.recipeSearch, 96, value -> {
            state.recipeSearch = SearchFilter.normalizeUserInput(value);
            state.recipeScroll = 0;
            RecipePickerMode mode = RecipePickerMode.safe(state.recipeMode);
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe search mode={} query='{}'", mode.logName(state.recipeSearch), state.recipeSearch);
            refresh.run();
        }, focused -> state.recipeSearchFocused = focused);
        RecipePickerMode mode = RecipePickerMode.safe(state.recipeMode);
        TabletModalPanel.addModeToggleIconButton(modal, gridX, headY, modeW, headH, mode.icon(), mode.tooltip(state.recipeSearch), click -> {
            int direction = click.button == 1 ? -1 : 1;
            RecipePickerMode.cycle(state, direction);
            RecipePickerMode nextMode = RecipePickerMode.safe(state.recipeMode);
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe picker mode={} direction={}", nextMode.logName(state.recipeSearch), direction < 0 ? "backward" : "forward");
            refresh.run();
        });
        addRecipeViewerKeyHandler(modal, state, player, refresh);

        mode = RecipePickerMode.safe(state.recipeMode);
        if (mode == RecipePickerMode.INVENTORY) {
            List<ItemStack> entries = TabletItemInventoryPickerModal.inventoryEntries(player, state.recipeSearch);
            TiledPickerPanel.add(
                    modal,
                    gridX,
                    gridY,
                    gridW,
                    gridH,
                    TILE,
                    TILE,
                    0,
                    6,
                    6,
                    entries,
                    TabletVocabulary.text(QuestVocabulary.NO_INVENTORY_ITEMS),
                    ScrollState.bind(
                            () -> state.recipeScroll,
                            value -> state.recipeScroll = value,
                            () -> state.recipeScrollDragging,
                            dragging -> state.recipeScrollDragging = dragging
                    ),
                    null,
                    refresh,
                    (surface, stack, index, x, y, tileW, tileH, layout) -> TabletItemInventoryPickerModal.renderStackTile(surface, stack, x, y, picked -> applyInventoryRecipePick(player, state, picked, refresh), hovered -> trackRecipeHover(state, ItemStackIconCodec.iconFromStack(hovered)))
            );
        } else if (mode == RecipePickerMode.FLUIDS) {
            List<String> entries = DisplayIconProvider.searchableFluidEntries(state.recipeSearch);
            TiledPickerPanel.add(
                    modal,
                    gridX,
                    gridY,
                    gridW,
                    gridH,
                    TILE,
                    TILE,
                    0,
                    6,
                    6,
                    entries,
                    TabletVocabulary.text(QuestVocabulary.NO_FLUIDS),
                    ScrollState.bind(
                            () -> state.recipeScroll,
                            value -> state.recipeScroll = value,
                            () -> state.recipeScrollDragging,
                            dragging -> state.recipeScrollDragging = dragging
                    ),
                    null,
                    refresh,
                    (surface, entry, index, x, y, tileW, tileH, layout) -> renderFluidTile(surface, player, state, refresh, entry, x, y)
            );
        } else {
            List<RecipeChoice> entries = recipes(state.recipeSearch, mode.showingTags(state.recipeSearch));
            TiledPickerPanel.add(
                    modal,
                    gridX,
                    gridY,
                    gridW,
                    gridH,
                    TILE,
                    TILE,
                    0,
                    6,
                    6,
                    entries,
                    TabletVocabulary.text(QuestVocabulary.NO_RECIPES),
                    ScrollState.bind(
                            () -> state.recipeScroll,
                            value -> state.recipeScroll = value,
                            () -> state.recipeScrollDragging,
                            dragging -> state.recipeScrollDragging = dragging
                    ),
                    null,
                    refresh,
                    (surface, entry, index, x, y, tileW, tileH, layout) -> renderTile(surface, player, state, refresh, entry, x, y)
            );
        }
        return search;
    }

    private static void renderTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, RecipeChoice entry, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        if (entry.previews().length == 0) {
            surface.addWidget(new DisplayIconWidget(x + 1, y + 1, 16, 16, entry.tag() ? "name_tag" : "recipe"));
        } else {
            surface.addWidget(new ImageWidget(x + 1, y + 1, 16, 16, new ScopedItemStackTexture(entry.previews())));
        }
        ButtonWidget hit = new ButtonWidget(x + 1, y + 1, 16, 16, Surfaces.fill(0x00000000), click -> {
            if (!entry.value().isBlank()) {
                applyRecipePick(player, state, entry.value(), refresh);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe picked kind={} value={} recipes={}", entry.tag() ? "tag" : "output", entry.value(), entry.recipeIds());
        }) {
            @Override
            public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                if (isMouseOverElement(mouseX, mouseY)) {
                    trackRecipeHover(state, entry.value());
                }
            }
        };
        hit.setHoverTooltips(entry.tooltip());
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 66)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        hit.setClientSideWidget();
        surface.addWidget(hit);
    }

    private static void renderFluidTile(WidgetGroup surface, Player player, TabletUiState state, Runnable refresh, String entry, int x, int y) {
        surface.addWidget(new ImageWidget(x, y, TILE, TILE, SlotWidget.ITEM_SLOT_TEXTURE));
        surface.addWidget(new DisplayIconWidget(x + 1, y + 1, 16, 16, entry));
        ButtonWidget hit = new ButtonWidget(x + 1, y + 1, 16, 16, Surfaces.fill(0x00000000), click -> {
            if (entry != null && !entry.isBlank()) {
                applyRecipePick(player, state, entry, refresh);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe picked kind=fluid value={}", entry);
        }) {
            @Override
            public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                if (isMouseOverElement(mouseX, mouseY)) {
                    trackRecipeHover(state, entry);
                }
            }
        };
        hit.setHoverTooltips(TabletModalPanel.iconTooltip(entry));
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 66)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        hit.setClientSideWidget();
        surface.addWidget(hit);
    }

    private static void applyInventoryRecipePick(Player player, TabletUiState state, ItemStack stack, Runnable refresh) {
        String pick = ItemStackIconCodec.iconFromStack(stack);
        if (!pick.isBlank()) {
            applyRecipePick(player, state, pick, refresh);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] recipe picked kind=inventory value={} hasNbt={}", pick, stack != null && stack.hasTag());
    }

    private static void applyRecipePick(Player player, TabletUiState state, String pick, Runnable refresh) {
        String value = pick == null ? "" : pick.trim();
        if (value.isBlank()) {
            return;
        }
        if (isRecipeCardTarget(state)) {
            String asset = CanvasRecipeCardAsset.assetForPick(value);
            String target = recipeTargetForPick(value, CanvasRecipeCardAsset.outputStack(asset));
            if (!target.isBlank()) {
                List<RecipeView> recipes = CanvasRecipeCardRecipes.recipesForTarget(target);
                if (!recipes.isEmpty() && CanvasRecipeCardAsset.recipeId(value).isBlank()) {
                    if (FluidIconCodec.isFluidIcon(target)) {
                        if (RecipeViewerSelectionBridge.begin(player, state, target, recipes, refresh)) {
                            return;
                        }
                        QuestsAndStuffMod.debugLog("[QnS:UI] recipe fluid pick ignored target={} reason=viewer_unavailable", target);
                        return;
                    }
                    if (recipes.size() > 1 && RecipeViewerSelectionBridge.begin(player, state, target, recipes, pickerStack(asset, recipes), refresh)) {
                        return;
                    }
                    value = CanvasRecipeCardAsset.assetForRecipe(target, recipes.get(0).id());
                } else if (recipes.isEmpty() && FluidIconCodec.isFluidIcon(target) && CanvasRecipeCardAsset.recipeId(value).isBlank()) {
                    if (RecipeViewerSelectionBridge.begin(player, state, target, recipes, refresh)) {
                        return;
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] recipe fluid pick ignored target={} reason=viewer_unavailable", target);
                    return;
                }
            }
        }
        QuestDetailsWindow.applyRecipePick(player, state, value);
        closeAll(state);
        refresh.run();
    }

    private static ItemStack pickerStack(String asset, List<RecipeView> recipes) {
        ItemStack stack = CanvasRecipeCardAsset.outputStack(asset);
        if (!stack.isEmpty()) {
            return stack;
        }
        if (recipes == null || recipes.isEmpty() || recipes.get(0).output().isEmpty()) {
            return ItemStack.EMPTY;
        }
        return recipes.get(0).output().copy();
    }

    private static void addRecipeViewerKeyHandler(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh) {
        state.recipeHoveredPick = "";
        modal.addWidget(new WidgetGroup(0, 0, 0, 0) {
            @Override
            public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                state.recipeHoveredPick = "";
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                return handleRecipeViewerSelectionShortcut(player, state, refresh, keyCode, scanCode);
            }
        });
    }

    private static boolean handleRecipeViewerSelectionShortcut(Player player, TabletUiState state, Runnable refresh, int keyCode, int scanCode) {
        if (state.recipeSearchFocused || !isRecipeCardTarget(state)) {
            return false;
        }
        RecipeViewerIntegrations.SelectionKeybind keybind = RecipeViewerIntegrations.selectionKeybind(keyCode, scanCode);
        if (keybind == null) {
            return false;
        }
        String pick = state.recipeHoveredPick == null ? "" : state.recipeHoveredPick.trim();
        if (pick.isBlank()) {
            return false;
        }
        ItemStack stack = viewerStackForPick(pick);
        String target = recipeTargetForPick(pick, stack);
        if (target.isBlank()) {
            return false;
        }
        if (FluidIconCodec.isFluidIcon(target)) {
            List<RecipeView> recipes = keybind.recipes()
                    ? CanvasRecipeCardRecipes.recipesForTarget(target)
                    : CanvasRecipeCardRecipes.usesForTarget(target);
            boolean opened = RecipeViewerSelectionBridge.beginFromKeybind(player, state, target, recipes, refresh, keybind);
            if (opened) {
                QuestsAndStuffMod.debugLog("[QnS:UI] recipe picker viewer key mode={} target={} provider={}", keybind.recipes() ? "recipes" : "uses", target, keybind.providerName());
            }
            return opened;
        }
        if (stack.isEmpty()) {
            return false;
        }
        List<RecipeView> recipes = keybind.recipes()
                ? CanvasRecipeCardRecipes.recipesForTarget(target)
                : CanvasRecipeCardRecipes.usesForTarget(target);
        if (recipes.isEmpty()) {
            return false;
        }
        boolean opened = RecipeViewerSelectionBridge.beginFromKeybind(player, state, target, recipes, stack, refresh, keybind);
        if (opened) {
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe picker viewer key mode={} target={} provider={}", keybind.recipes() ? "recipes" : "uses", target, keybind.providerName());
        }
        return opened;
    }

    private static void trackRecipeHover(TabletUiState state, String pick) {
        String value = pick == null ? "" : pick.trim();
        if (!value.isBlank()) {
            state.recipeHoveredPick = value;
        }
    }

    private static ItemStack viewerStackForPick(String pick) {
        ItemStack stack = CanvasRecipeCardAsset.outputStack(CanvasRecipeCardAsset.assetForPick(pick));
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        return stack;
    }

    private static String recipeTargetForPick(String pick, ItemStack stack) {
        String target = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(pick));
        if (target.isBlank()) {
            return "";
        }
        if (FluidIconCodec.isFluidIcon(target)) {
            return target;
        }
        if (ItemStackIconCodec.isStackIcon(target) && stack != null && !stack.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return id == null ? "" : id.toString();
        }
        return target;
    }

    private static boolean isRecipeCardTarget(TabletUiState state) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(state.questDetailsPickTarget);
        return parsed.isCanvasRecipeNew() || parsed.isCanvasRecipeChange() || parsed.isDescRecipe() || parsed.isDescRecipeNew();
    }

    private static List<RecipeChoice> recipes(String query, boolean tagMode) {
        String normalizedQuery = SearchFilter.normalize(query);
        boolean showingTags = tagMode || (query != null && query.trim().startsWith("#"));
        synchronized (TabletRecipePickerModal.class) {
            RecipeChoices choices = choices();
            if (normalizedQuery.equals(cachedQuery) && showingTags == cachedTagMode) {
                return cachedValues;
            }
            List<RecipeChoice> source = showingTags ? choices.tags() : choices.outputs();
            List<RecipeChoice> values;
            if (normalizedQuery.isBlank()) {
                values = source;
            } else {
                String compactQuery = SearchFilter.normalizeKey(normalizedQuery);
                values = source.stream()
                        .filter(choice -> choice.matches(normalizedQuery, compactQuery))
                        .toList();
            }
            cachedQuery = normalizedQuery;
            cachedTagMode = showingTags;
            cachedValues = values;
            return values;
        }
    }

    private static RecipeChoices choices() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        RecipeManager manager = connection == null ? null : connection.getRecipeManager();
        RegistryAccess registryAccess = connection == null ? null : connection.registryAccess();
        if (cachedChoices != null && manager == cachedManager && registryAccess == cachedRegistryAccess) {
            return cachedChoices;
        }
        RecipeChoices choices = buildChoices(manager, registryAccess);
        cachedManager = manager;
        cachedRegistryAccess = registryAccess;
        cachedChoices = choices;
        cachedQuery = null;
        cachedValues = List.of();
        return choices;
    }

    private static RecipeChoices buildChoices(RecipeManager manager, RegistryAccess registryAccess) {
        Map<String, RecipeChoiceBuilder> found = new LinkedHashMap<>();
        if (manager != null && registryAccess != null) {
            for (Recipe<?> recipe : manager.getRecipes()) {
                ItemStack result = resultItem(recipe, registryAccess);
                if (result == null || result.isEmpty()) {
                    continue;
                }
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
                if (itemId == null) {
                    continue;
                }
                String value = itemId.toString();
                RecipeChoiceBuilder builder = found.computeIfAbsent(value, ignored -> RecipeChoiceBuilder.of(value, result));
                builder.addRecipe(recipe.getId().toString());
            }
        }
        if (found.isEmpty()) {
            addFallback(found, "minecraft:crafting_table");
            addFallback(found, "minecraft:furnace");
            addFallback(found, "minecraft:stick");
            addFallback(found, "minecraft:bread");
        }
        List<RecipeChoice> outputs = found.values().stream()
                .map(RecipeChoiceBuilder::build)
                .sorted(Comparator.comparing(RecipeChoice::displayName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RecipeChoice::value))
                .toList();
        List<RecipeChoice> tags = BuiltInRegistries.ITEM.getTagNames()
                .map(TabletRecipePickerModal::tagChoice)
                .sorted(Comparator.comparing(RecipeChoice::value))
                .toList();
        return new RecipeChoices(outputs, tags);
    }

    private static ItemStack resultItem(Recipe<?> recipe, RegistryAccess registryAccess) {
        try {
            return recipe.getResultItem(registryAccess);
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe output skipped recipe={} error={}", recipe.getId(), exception.toString());
            return ItemStack.EMPTY;
        }
    }

    private static void addFallback(Map<String, RecipeChoiceBuilder> found, String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR && !"minecraft:air".equals(value)) {
            return;
        }
        RecipeChoiceBuilder builder = RecipeChoiceBuilder.of(value, new ItemStack(item));
        builder.addRecipe(value);
        found.put(value, builder);
    }

    private static RecipeChoice tagChoice(TagKey<Item> tag) {
        String value = "#" + tag.location();
        String displayName = DisplayNameFormatter.resourceLeaf(tag.location().toString());
        return RecipeChoice.of(value, displayName, tagPreviews(tag), List.of(), true);
    }

    private static ItemStack[] tagPreviews(TagKey<Item> tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            Item item = holder.value();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    private record RecipeChoices(List<RecipeChoice> outputs, List<RecipeChoice> tags) {
    }

    private static final class RecipeChoiceBuilder {
        private final String value;
        private final String displayName;
        private final ItemStack icon;
        private final List<String> recipeIds = new ArrayList<>();

        private RecipeChoiceBuilder(String value, String displayName, ItemStack icon) {
            this.value = value;
            this.displayName = displayName;
            this.icon = icon.copy();
            this.icon.setCount(1);
        }

        static RecipeChoiceBuilder of(String value, ItemStack stack) {
            String displayName = stack.isEmpty() ? "" : stack.getHoverName().getString();
            if (displayName.isBlank()) {
                displayName = DisplayNameFormatter.resourceLeaf(value);
            }
            return new RecipeChoiceBuilder(value, displayName, stack);
        }

        void addRecipe(String recipeId) {
            if (recipeId != null && !recipeId.isBlank() && !recipeIds.contains(recipeId)) {
                recipeIds.add(recipeId);
            }
        }

        RecipeChoice build() {
            return RecipeChoice.of(value, displayName, new ItemStack[]{icon.copy()}, recipeIds, false);
        }
    }

    private record RecipeChoice(
            String value,
            String displayName,
            ItemStack[] previews,
            List<String> recipeIds,
            boolean tag,
            String normalizedValue,
            String normalizedDisplayName,
            String normalizedRecipes,
            String compactValue,
            String compactDisplayName,
            String compactRecipes
    ) {
        static RecipeChoice of(String value, String displayName, ItemStack[] previews, List<String> recipeIds, boolean tag) {
            List<String> recipes = recipeIds == null ? List.of() : List.copyOf(recipeIds);
            String joinedRecipes = String.join(" ", recipes);
            String normalizedValue = SearchFilter.normalize(value);
            String normalizedDisplayName = SearchFilter.normalize(displayName);
            String normalizedRecipes = SearchFilter.normalize(joinedRecipes);
            return new RecipeChoice(
                    value,
                    displayName,
                    previews == null ? new ItemStack[0] : copyStacks(previews),
                    recipes,
                    tag,
                    normalizedValue,
                    normalizedDisplayName,
                    normalizedRecipes,
                    SearchFilter.normalizeKey(normalizedValue),
                    SearchFilter.normalizeKey(normalizedDisplayName),
                    SearchFilter.normalizeKey(normalizedRecipes)
            );
        }

        boolean matches(String query, String compactQuery) {
            return normalizedValue.contains(query)
                    || normalizedDisplayName.contains(query)
                    || normalizedRecipes.contains(query)
                    || (!compactQuery.isBlank()
                    && (compactValue.contains(compactQuery)
                    || compactDisplayName.contains(compactQuery)
                    || compactRecipes.contains(compactQuery)));
        }

        Component[] tooltip() {
            return PickerTooltips.nameAndId(displayName, value);
        }

        private static ItemStack[] copyStacks(ItemStack[] stacks) {
            ItemStack[] copy = new ItemStack[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                ItemStack stack = stacks[i] == null ? ItemStack.EMPTY : stacks[i].copy();
                if (!stack.isEmpty()) {
                    stack.setCount(1);
                }
                copy[i] = stack;
            }
            return copy;
        }
    }
}
