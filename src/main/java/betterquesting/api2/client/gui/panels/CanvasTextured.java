package betterquesting.api2.client.gui.panels;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

public class CanvasTextured extends CanvasEmpty {
    private IGuiTexture bgTexture;

    public CanvasTextured(IGuiRect rect, IGuiTexture texture) {
        super(rect);

        this.bgTexture = texture;
    }

    public void changeBG(@Nullable IGuiTexture texture) {
        this.bgTexture = texture;
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        if (bgTexture != null) {
            IGuiRect bounds = this.getTransform();
            GlStateManager.pushMatrix();
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            bgTexture.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
            GlStateManager.popMatrix();
        }

        super.drawPanel(mx, my, partialTick);
    }
}
