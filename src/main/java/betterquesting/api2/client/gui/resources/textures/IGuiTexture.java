package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

public interface IGuiTexture {
    void drawTexture(PoseStack poseStack, int x, int y, int width, int height, float zDepth, float partialTick);

    void drawTexture(PoseStack poseStack, int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color);

    ResourceLocation getTexture();

    IGuiRect getBounds();
}
