package betterquesting.api2.client.gui.controls;

import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.INBTSaveLoad;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import org.lwjgl.glfw.GLFW;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Consumer;

public class PanelButton implements IPanelButton, IGuiPanel, INBTSaveLoad<CompoundTag> {
    private final IGuiRect transform;
    private boolean enabled = true;
    private boolean hovered = false;

    private final IGuiTexture[] texStates = new IGuiTexture[3];
    private IGuiColor[] colStates = new IGuiColor[]{new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(255, 255, 255, 255), new GuiColorStatic(16777120)};
    private IGuiTexture texIcon = null;
    private IGuiColor colIcon = null;
    private int icoPadding = 0;
    private List<String> tooltip = null;
    private boolean txtShadow = true;
    private String btnText;
    private int textAlign = 1;
    private boolean isActive = true;
    private final int btnID;

    private boolean pendingRelease = false;

    private Consumer<PanelButton> clickAction = null;

    public PanelButton(IGuiRect rect, int id, String txt) {
        this.transform = rect;
        this.btnText = txt;
        this.btnID = id;

        this.setTextures(PresetTexture.BTN_NORMAL_0.getTexture(), PresetTexture.BTN_NORMAL_1.getTexture(), PresetTexture.BTN_NORMAL_2.getTexture());
        this.setTextHighlight(PresetColor.BTN_DISABLED.getColor(), PresetColor.BTN_IDLE.getColor(), PresetColor.BTN_HOVER.getColor());
    }

    public PanelButton setClickAction(Consumer<PanelButton> action) {
        this.clickAction = action;
        return this;
    }

    public PanelButton setTextHighlight(IGuiColor disabled, IGuiColor idle, IGuiColor hover) {
        this.colStates[0] = disabled;
        this.colStates[1] = idle;
        this.colStates[2] = hover;
        return this;
    }

    public PanelButton setTextShadow(boolean enabled) {
        this.txtShadow = enabled;
        return this;
    }

    public PanelButton setTextAlignment(int align) {
        this.textAlign = Mth.clamp(align, 0, 2);
        return this;
    }

    public PanelButton setTextures(IGuiTexture disabled, IGuiTexture idle, IGuiTexture hover) {
        this.texStates[0] = disabled;
        this.texStates[1] = idle;
        this.texStates[2] = hover;
        return this;
    }

    public PanelButton setIcon(IGuiTexture icon) {
        return this.setIcon(icon, 0);
    }

    public PanelButton setIcon(IGuiTexture icon, int padding) {
        return setIcon(icon, null, padding);
    }

    public PanelButton setIcon(IGuiTexture icon, IGuiColor color, int padding) {
        this.texIcon = icon;
        this.colIcon = color;
        this.icoPadding = padding * 2;
        return this;
    }

    public PanelButton setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public void setText(String text) {
        this.btnText = text;
    }

    public String getText() {
        return this.btnText;
    }

    @Override
    public int getButtonID() {
        return this.btnID;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void setActive(boolean state) {
        this.isActive = state;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean state) {
        this.enabled = state;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    public void setHovered(boolean state) {
        this.hovered = state;
    }

    @Override
    public IGuiRect getTransform() {
        return transform;
    }

    @Override
    public void initPanel() {
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {
        IGuiRect bounds = this.getTransform();
        poseStack.pushPose();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        this.setHovered(bounds.contains(mx, my));
        int curState = !isActive() ? 0 : (isHovered() ? 2 : 1);

        if (curState == 2 && pendingRelease && GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) {
            curState = 0;
        }

        IGuiTexture t = texStates[curState];

        if (t != null) // Support for text or icon only buttons in one or more states.
        {
            t.drawTexture(poseStack, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
        }

        if (texIcon != null) {
            int isz = Math.min(bounds.getHeight() - icoPadding, bounds.getWidth() - icoPadding);

            if (isz > 0) {
                if (colIcon != null) {
                    texIcon.drawTexture(poseStack, bounds.getX() + (bounds.getWidth() / 2) - (isz / 2), bounds.getY() + (bounds.getHeight() / 2) - (isz / 2), isz, isz, 0F, partialTick, colIcon);
                } else {
                    texIcon.drawTexture(poseStack, bounds.getX() + (bounds.getWidth() / 2) - (isz / 2), bounds.getY() + (bounds.getHeight() / 2) - (isz / 2), isz, isz, 0F, partialTick);
                }
            }
        }

        if (btnText != null && !btnText.isEmpty()) {
            drawCenteredString(guiGraphics, Minecraft.getInstance().font, btnText, bounds.getX(), bounds.getY() + bounds.getHeight() / 2 - 4, bounds.getWidth(), colStates[curState].getRGB(), txtShadow, textAlign);
        }

        poseStack.popPose();
    }

    private static void drawCenteredString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int width, int color, boolean shadow, int align) {
        float xPos;

        switch (align) {
            case 0:
                xPos = x + 4;
                break;
            case 2:
                xPos = x + width - font.width(text) / 2F - 4;
                break;
            default:
                xPos = x + Math.floorDiv(width, 2) - font.width(text) / 2F;
                break;
        }

        guiGraphics.drawString(font, text, (int) xPos, y, color, shadow);
    }


    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        pendingRelease = isActive() && (click == 0 || click == 1) && isHovered();

        return (click == 0 || click == 1) && isHovered();
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        if (!pendingRelease) {
            return false;
        }

        pendingRelease = false;

        boolean clicked = isActive() && isHovered() && (click == 1 || (click == 0 && !PEventBroadcaster.INSTANCE.postEvent(new PEventButton(this))));

        if (clicked) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (click == 0) onButtonClick();
            else if (click == 1) onRightButtonClick();
        }

        return clicked;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        return false;
    }

    @Override
    public boolean onKeyTyped(char c, int keycode) {
        return false;
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        if (isHovered()) {
            return tooltip;
        }

        return null;
    }

    @Override
    public void onButtonClick() {
        if (clickAction != null) clickAction.accept(this);
    }

    public void onRightButtonClick() {
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        // TODO: Fix me
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {

    }
}
