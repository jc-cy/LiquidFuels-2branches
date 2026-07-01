package com.g1739.liquidfuels.compat.jei;

import com.g1739.liquidfuels.LiquidFuels;
import com.g1739.liquidfuels.registry.LiquidFuelItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class LiquidFuelJeiCategory implements IRecipeCategory<LiquidFuelJeiRecipe> {
    public static final RecipeType<LiquidFuelJeiRecipe> TYPE = RecipeType.create(LiquidFuels.MOD_ID, "liquid_fuel", LiquidFuelJeiRecipe.class);

    private static final Component TITLE = Component.translatable("jei.liquidfuels.liquid_fuel");
    private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
    private static final int WIDTH = 98;
    private static final int HEIGHT = 26;
    private static final int FLUID_SLOT_X = 6;
    private static final int TANK_SLOT_X = 76;
    private static final int SLOT_Y = 5;
    private static final int ARROW_X = 38;
    private static final int ARROW_Y = 5;
    private static final long TANK_CYCLE_MILLIS = 1000L;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrowBackground;
    private final IDrawable arrowAnimated;
    private final List<ItemStack> tankStacks;
    private final IIngredientRenderer<ItemStack> tankRenderer;

    public LiquidFuelJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(LiquidFuelItems.FUEL_TANK.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.arrowBackground = guiHelper.createDrawable(FURNACE_TEXTURE, 79, 34, 24, 16);
        this.arrowAnimated = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(FURNACE_TEXTURE, 176, 14, 24, 16),
                80,
                IDrawableAnimated.StartDirection.LEFT,
                false
        );
        this.tankStacks = List.of(
                new ItemStack(LiquidFuelItems.SMALL_FUEL_TANK.get()),
                new ItemStack(LiquidFuelItems.FUEL_TANK.get()),
                new ItemStack(LiquidFuelItems.LARGE_FUEL_TANK.get())
        );
        this.tankRenderer = new SyncedTankRenderer(tankStacks);
    }

    @Override
    public RecipeType<LiquidFuelJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LiquidFuelJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, FLUID_SLOT_X, SLOT_Y)
                .setFluidRenderer(1000, false, 16, 16)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.fluid())
                .setBackground(slot, -1, -1)
                .addTooltipCallback((slotView, tooltip) -> tooltip.add(Component.translatable(
                        "jei.liquidfuels.burn_time",
                        formatItemCount(recipe.burnTimeFor1000mb())
                )));

        builder.addSlot(RecipeIngredientRole.CATALYST, TANK_SLOT_X, SLOT_Y)
                .addItemStacks(tankStacks)
                .setCustomRenderer(VanillaTypes.ITEM_STACK, tankRenderer)
                .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(LiquidFuelJeiRecipe recipe, @Nullable IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrowBackground.draw(guiGraphics, ARROW_X, ARROW_Y);
        arrowAnimated.draw(guiGraphics, ARROW_X, ARROW_Y);
    }

    private static String formatItemCount(int burnTimeFor1000mb) {
        double items = burnTimeFor1000mb / 200.0D;
        if (items == Math.rint(items)) {
            return Integer.toString((int) items);
        }
        return String.format(Locale.ROOT, "%.2f", items);
    }

    private static final class SyncedTankRenderer implements IIngredientRenderer<ItemStack> {
        private final List<ItemStack> stacks;

        private SyncedTankRenderer(List<ItemStack> stacks) {
            this.stacks = stacks;
        }

        @Override
        public void render(GuiGraphics guiGraphics, ItemStack ingredient) {
            ItemStack stack = currentStack();
            guiGraphics.renderItem(stack, 0, 0);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, 0, 0);
        }

        @Override
        public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
            return currentStack().getTooltipLines(Minecraft.getInstance().player, tooltipFlag);
        }

        @Override
        public int getWidth() {
            return 16;
        }

        @Override
        public int getHeight() {
            return 16;
        }

        private ItemStack currentStack() {
            int index = (int) ((System.currentTimeMillis() / TANK_CYCLE_MILLIS) % stacks.size());
            return stacks.get(index);
        }
    }
}
