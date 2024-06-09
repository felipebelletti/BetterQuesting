package betterquesting.api2.client.gui;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.popups.PopChoice;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.BQ_Keybindings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

@OnlyIn(Dist.CLIENT)
public class GuiContainerCanvas extends AbstractContainerScreen<AbstractContainerMenu> implements IScene {
    private final List<IGuiPanel> guiPanels = new CopyOnWriteArrayList<>();
    private final GuiRectangle rootTransform = new GuiRectangle(0, 0, 0, 0, 0);
    private final GuiTransform transform = new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0);
    private boolean enabled = true;
    private boolean useMargins = true;
    private boolean useDefaultBG = false;
    private boolean isVolatile = false;

    public final Screen parent;

    private IGuiPanel popup = null;

    public GuiContainerCanvas(Screen parent, Inventory inventory, AbstractContainerMenu container) {
        super(container, inventory, parent.getTitle());
        this.parent = parent;
    }

    @Override
    public void openPopup(@Nonnull IGuiPanel panel) {
        this.popup = panel;
    }

    @Override
    public void closePopup() {
        this.popup = null;
    }

    @Override
    public IGuiRect getTransform() {
        return transform;
    }

    @Nonnull
    @Override
    public List<IGuiPanel> getChildren() {
        return this.guiPanels;
    }

    public GuiContainerCanvas useMargins(boolean enable) {
        this.useMargins = enable;
        return this;
    }

    public GuiContainerCanvas useDefaultBG(boolean enable) {
        this.useDefaultBG = enable;
        return this;
    }

    public GuiContainerCanvas setVolatile(boolean state) {
        this.isVolatile = state;
        return this;
    }

    /**
     * Use initPanel() for embed support
     */
    @Override
    protected void init() {
        super.init();

        // Make the container somewhat behave using the root transform bounds
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = width;
        this.imageHeight = height;

        initPanel();
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public void initPanel() {
        rootTransform.w = this.width;
        rootTransform.h = this.height;
        transform.setParent(rootTransform);

        if (useMargins) {
            int marginX = BQ_Settings.guiWidth <= 0 ? 16 : Math.max(16, (this.width - BQ_Settings.guiWidth) / 2);
            int marginY = BQ_Settings.guiHeight <= 0 ? 16 : Math.max(16, (this.height - BQ_Settings.guiHeight) / 2);
            transform.getPadding().setPadding(marginX, marginY, marginX, marginY);
        } else {
            transform.getPadding().setPadding(0, 0, 0, 0);
        }

        this.guiPanels.clear();
    }

    @Override
    public void setEnabled(boolean state) {
        this.enabled = state;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mx, int my) {
        if (useDefaultBG) {
            this.renderBackground(guiGraphics);
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        this.drawPanel(mx, my, partialTick, guiGraphics.pose(), guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * Use panel buttons and the event broadcaster
     */
//    @Override
//    @Deprecated
//    protected void buttonPressed(Button button) {
//    }

    // Remembers the last mouse buttons states. Required to fire release events
    private boolean[] mBtnState = new boolean[3];

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);

        if (button >= 0 && button < 3) {
            if (this.mBtnState[button] != result) {
                if (result) {
                    this.onMouseClick((int) mouseX, (int) mouseY, button);
                }
                this.mBtnState[button] = result;
            }
        }
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean result = super.mouseReleased(mouseX, mouseY, button);

        if (button >= 0 && button < 3) {
            if (this.mBtnState[button] != result) {
                this.onMouseRelease((int) mouseX, (int) mouseY, button);
                this.mBtnState[button] = result;
            }
        }
        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        boolean result = super.mouseScrolled(mouseX, mouseY, scroll);

        if (scroll != 0) {
            this.onMouseScroll((int) mouseX, (int) mouseY, (int) scroll);
        }
        return result;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (modifiers == 1) {
            if (this.isVolatile || this instanceof IVolatileScreen) {
                openPopup(new PopChoice(QuestTranslation.translate("betterquesting.gui.closing_warning") + "\n\n" + QuestTranslation.translate("betterquesting.gui.closing_confirm"), PresetIcon.ICON_NOTICE.getTexture(), this::confirmClose, QuestTranslation.translate("gui.yes"), QuestTranslation.translate("gui.no")));
            } else {
                this.minecraft.setScreen(null);
                if (this.minecraft.screen == null) this.minecraft.mouseHandler.grabMouse();
            }
            return true;
        }

        return this.onKeyTyped(codePoint, modifiers);
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {
        for (IGuiPanel entry : guiPanels) {
            if (entry.isEnabled()) {
                entry.drawPanel(mx, my, partialTick, poseStack, guiGraphics);
            }
        }

        if (popup != null && popup.isEnabled()) {
            popup.drawPanel(mx, my, partialTick, poseStack, guiGraphics);
        }
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        boolean used = false;

        if (popup != null && popup.isEnabled()) {
            popup.onMouseClick(mx, my, click);
            return true;
        }

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onMouseClick(mx, my, click)) {
                used = true;
                break;
            }
        }

        return used;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        boolean used = false;

        if (popup != null && popup.isEnabled()) {
            popup.onMouseRelease(mx, my, click);
            return true;
        }

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onMouseRelease(mx, my, click)) {
                used = true;
                break;
            }
        }

        return used;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        boolean used = false;

        if (popup != null && popup.isEnabled()) {
            popup.onMouseScroll(mx, my, scroll);
            return true;
        }

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onMouseScroll(mx, my, scroll)) {
                used = true;
                break;
            }
        }

        return used;
    }

    @Override
    public boolean onKeyTyped(char c, int keycode) {
        boolean used = false;

        if (popup != null) {
            if (popup.isEnabled()) {
                popup.onKeyTyped(c, keycode);
                return true;
            }
        }

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onKeyTyped(c, keycode)) {
                used = true;
                break;
            }
        }

        if (!used && (BQ_Keybindings.openQuests.getKey().getValue() == keycode || minecraft.options.keyInventory.getKey().getValue() == keycode)) {
            if (this.isVolatile || this instanceof IVolatileScreen) {
                openPopup(new PopChoice(QuestTranslation.translate("betterquesting.gui.closing_warning") + "\n\n" + QuestTranslation.translate("betterquesting.gui.closing_confirm"), PresetIcon.ICON_NOTICE.getTexture(), this::confirmClose, QuestTranslation.translate("gui.yes"), QuestTranslation.translate("gui.no")));
            } else {
                this.minecraft.setScreen(null);
                if (this.minecraft.screen == null) this.minecraft.mouseHandler.grabMouse();
            }
        }

        return used;
    }

    @Override
    public List<Component> getTooltip(int mx, int my) {
        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
        List<Component> tt = null;

        if (popup != null && popup.isEnabled()) {
            tt = popup.getTooltip(mx, my);
            if (tt != null) return tt;
        }

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();
            if (!entry.isEnabled()) continue;

            tt = entry.getTooltip(mx, my);
            if (tt != null && tt.size() > 0) return tt;
        }

        if (tt == null) {
            for (Slot slot : this.menu.slots) {
                if (slot.isActive() && slot.hasItem() && isHovering(slot.x, slot.y, 16, 16, mx, my)) {
                    tt = slot.getItem().getTooltipLines(minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                    return tt.size() <= 0 ? null : tt;
                }
            }
        }

        return null;
    }

    @Override
    public void addPanel(IGuiPanel panel) {
        if (panel == null || guiPanels.contains(panel)) {
            return;
        }

        guiPanels.add(panel);
        guiPanels.sort(ComparatorGuiDepth.INSTANCE);
        panel.getTransform().setParent(getTransform());
        panel.initPanel();
    }

    @Override
    public boolean removePanel(IGuiPanel panel) {
        return guiPanels.remove(panel);
    }

    @Override
    public void resetCanvas() {
        guiPanels.clear();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

//    @Override
//    protected void renderTooltip(PoseStack poseStack, int x, int y) {
//        Font font = stack.getItem().getFontRenderer(stack);
//        RenderUtils.drawHoveringText(stack, this.getTooltipFromItem(stack), x, y, width, height, -1, (font == null ? this.font : font));
//    }
//
//    @Override
//    protected void renderTooltip(PoseStack poseStack, List<Component> textLines, int x, int y, Font font) {
//        RenderUtils.drawHoveringText(textLines, x, y, width, height, -1, font);
//    }

    public void confirmClose(int id) {
        if (id == 0) {
            this.minecraft.setScreen(null);
            if (this.minecraft.screen == null) this.minecraft.mouseHandler.grabMouse();
        }
    }
}
