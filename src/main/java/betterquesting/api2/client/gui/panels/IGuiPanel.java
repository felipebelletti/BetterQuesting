package betterquesting.api2.client.gui.panels;

import betterquesting.api2.client.gui.misc.IGuiRect;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IGuiPanel {
    IGuiRect getTransform();

    void initPanel();

    void setEnabled(boolean state);

    boolean isEnabled();

    void drawPanel(int mx, int my, float partialTick, PoseStack poseStack, GuiGraphics guiGraphics);

    boolean onMouseClick(int mx, int my, int button);

    boolean onMouseRelease(int mx, int my, int button);

    boolean onMouseScroll(int mx, int my, int scroll);

    boolean onKeyTyped(char c, int keycode);

    List<Component> getTooltip(int mx, int my);
}
