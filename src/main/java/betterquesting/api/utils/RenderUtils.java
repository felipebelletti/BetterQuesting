package betterquesting.api.utils;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.core.BetterQuesting;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// TODO: Move text related stuff to its own utility class
@OnlyIn(Dist.CLIENT)
public class RenderUtils {
    public static final String REGEX_NUMBER = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable

    public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text) {
        RenderItemStack(mc, stack, x, y, text, Color.WHITE.getRGB());
    }

    public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, Color color) {
        RenderItemStack(mc, stack, x, y, text, color.getRGB());
    }

    public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, int color) {
        RenderItemStack(mc, stack, x, y, 16F, text, color);
    }

    public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, float z, String text, int color) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, 1.0F);
        RenderSystem.enableDepthTest();

        poseStack.translate(0.0F, 0.0F, z);

        Font font = mc.font;

        try {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            ItemRenderer itemRenderer = mc.getItemRenderer();
            BakedModel model = itemRenderer.getModel(stack, null, null, 0);

            itemRenderer.render(stack, ItemDisplayContext.GUI, false, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, model);

            if (stack.getCount() != 1 || text != null) {
                poseStack.pushPose();

                int w = font.width(text);
                float tx;
                float ty;
                float s = 1F;

                if (w > 17) {
                    s = 17F / w;
                    tx = 0;
                    ty = 17 - font.lineHeight * s;
                } else {
                    tx = 17 - w;
                    ty = 18 - font.lineHeight;
                }

                poseStack.translate(x + tx, y + ty, 0);
                poseStack.scale(s, s, 1F);

                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();

                Matrix4f matrix4f = poseStack.last().pose();
                font.drawInBatch(text, 0, 0, 16777215, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, false);

                bufferSource.endBatch();

                RenderSystem.enableDepthTest();
                RenderSystem.enableBlend();

                poseStack.popPose();
            }
        } catch (Exception e) {
            BetterQuesting.logger.warn("Unable to render item " + stack, e);
        }

        RenderSystem.disableDepthTest();

        poseStack.popPose();
    }

    public static void RenderEntity(PoseStack poseStack, float posX, float posY, float posZ, int scale, float rotation, float pitch, Entity entity) {
        try {
            poseStack.pushPose();
            poseStack.translate(posX, posY, posZ);
            poseStack.scale(-scale, scale, scale);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            float f3 = entity.getYRot();
            float f4 = entity.getXRot();
            float f5 = entity.yRotO;
            float f6 = entity.xRotO;
            entity.setYRot(0);
            entity.setXRot(0);
            entity.yRotO = 0;
            entity.xRotO = 0;

            LivingEntity livingEntity = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            float f7 = livingEntity == null ? 0 : livingEntity.yBodyRot;
            float f8 = livingEntity == null ? 0 : livingEntity.yHeadRot;
            float f9 = livingEntity == null ? 0 : livingEntity.yHeadRotO;
            if (livingEntity != null) {
                livingEntity.yBodyRot = 0;
                livingEntity.yHeadRot = 0;
                livingEntity.yHeadRotO = 0;
            }

            RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutShader);
            RenderSystem.enableDepthTest();
            Lighting.setupFor3DItems();
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            entityRenderDispatcher.setRenderShadow(false);
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            entityRenderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, bufferSource, 15728880);
            bufferSource.endBatch();
            entityRenderDispatcher.setRenderShadow(true);

            entity.setYRot(f3);
            entity.setXRot(f4);
            entity.yRotO = f5;
            entity.xRotO = f6;
            if (livingEntity != null) {
                livingEntity.yBodyRot = f7;
                livingEntity.yHeadRot = f8;
                livingEntity.yHeadRotO = f9;
            }

            poseStack.popPose();
            Lighting.setupForFlatItems();
            RenderSystem.disableDepthTest();
        } catch (Exception e) {
            // Hides rendering errors with entities which are common for invalid/technical entities
        }
    }

    public static void drawLine(int x1, int y1, int x2, int y2, float width, int color) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(r, g, b, 1F);
        RenderSystem.lineWidth(width);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(x1, y1, 0).color(r, g, b, 1F).endVertex();
        bufferbuilder.vertex(x2, y2, 0).color(r, g, b, 1F).endVertex();
        tesselator.end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    public static void drawSplitString(GuiGraphics guiGraphics, PoseStack poseStack, Font renderer, String string, int x, int y, int width, int color, boolean shadow) {
        drawSplitString(guiGraphics, poseStack, renderer, string, x, y, width, color, shadow, 0, splitString(string, width, renderer).size() - 1);
    }

    public static void drawSplitString(GuiGraphics guiGraphics, PoseStack poseStack, Font renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end) {
        drawHighlightedSplitString(guiGraphics, poseStack, renderer, string, x, y, width, color, shadow, start, end, 0, 0, 0);
    }

    // TODO: Clean this up. The list of parameters is getting a bit excessive

    public static void drawHighlightedSplitString(GuiGraphics guiGraphics, PoseStack poseStack, Font renderer, String string, int x, int y, int width, int color, boolean shadow, int highlightColor, int highlightStart, int highlightEnd) {
        drawHighlightedSplitString(guiGraphics, poseStack, renderer, string, x, y, width, color, shadow, 0, splitString(string, width, renderer).size() - 1, highlightColor, highlightStart, highlightEnd);
    }

    public static void drawHighlightedSplitString(GuiGraphics guiGraphics, PoseStack poseStack, Font renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end, int highlightColor, int highlightStart, int highlightEnd) {
        if (renderer == null || string == null || string.length() <= 0 || start > end) {
            return;
        }

        string = string.replaceAll("\r", ""); // Line endings from localizations break things so we remove them

        List<String> list = splitString(string, width, renderer);
        List<String> noFormat = splitStringWithoutFormat(string, width, renderer); // Needed for accurate highlight index positions

        if (list.size() != noFormat.size()) {
            // BetterQuesting.logger.error("Line count mismatch (" + list.size() + " != " + noFormat.size() + ") while drawing formatted text!");
            return;
        }

        int hlStart = Math.min(highlightStart, highlightEnd);
        int hlEnd = Math.max(highlightStart, highlightEnd);
        int idxStart = 0;

        for (int i = 0; i < start; i++) {
            if (i >= noFormat.size()) {
                break;
            }

            idxStart += noFormat.get(i).length();
        }

        // Text rendering is very vulnerable to colour leaking
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        for (int i = start; i <= end; i++) {
            if (i < 0 || i >= list.size()) {
                continue;
            }

            guiGraphics.drawString(renderer, list.get(i), x, y + (renderer.lineHeight * (i - start)), color, shadow);

            int lineSize = noFormat.get(i).length();
            int idxEnd = idxStart + lineSize;

            int i1 = Math.max(idxStart, hlStart) - idxStart;
            int i2 = Math.min(idxEnd, hlEnd) - idxStart;

            if (!(i1 == i2 || i1 < 0 || i2 < 0 || i1 > lineSize || i2 > lineSize)) {
                Component textComponent = Component.literal(noFormat.get(i).substring(0, i2)).setStyle(Style.EMPTY);
                int x1 = renderer.width(textComponent.getString().substring(0, i1));
                int x2 = renderer.width(textComponent.getString());

                drawHighlightBox(guiGraphics, poseStack, x + x1, y + (renderer.lineHeight * (i - start)), x + x2, y + (renderer.lineHeight * (i - start)) + renderer.lineHeight, highlightColor);
            }

            idxStart = idxEnd;
        }
    }

    public static void drawHighlightedString(GuiGraphics guiGraphics, PoseStack poseStack, Font renderer, String string, int x, int y, int color, boolean shadow, int highlightColor, int highlightStart, int highlightEnd) {
        if (renderer == null || string == null || string.length() <= 0) {
            return;
        }

        guiGraphics.drawString(renderer, string, x, y, color, shadow);

        int hlStart = Math.min(highlightStart, highlightEnd);
        int hlEnd = Math.max(highlightStart, highlightEnd);
        int size = string.length();

        int i1 = Mth.clamp(hlStart, 0, size);
        int i2 = Mth.clamp(hlEnd, 0, size);

        if (i1 != i2) {
            int x1 = getStringWidth(string.substring(0, i1), renderer);
            int x2 = getStringWidth(string.substring(0, i2), renderer);

            drawHighlightBox(guiGraphics, poseStack, x + x1, y, x + x2, y + renderer.lineHeight, highlightColor);
        }
    }

    public static void drawHighlightBox(GuiGraphics guiGraphics, PoseStack poseStack, IGuiRect rect, IGuiColor color) {
        drawHighlightBox(guiGraphics, poseStack, rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color.getRGB());
    }

    public static void drawHighlightBox(GuiGraphics guiGraphics, PoseStack poseStack, int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        RenderSystem.setShaderColor(f, f1, f2, f3);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(poseStack.last().pose(), (float) left, (float) bottom, 0.0F).endVertex();
        bufferbuilder.vertex(poseStack.last().pose(), (float) right, (float) bottom, 0.0F).endVertex();
        bufferbuilder.vertex(poseStack.last().pose(), (float) right, (float) top, 0.0F).endVertex();
        bufferbuilder.vertex(poseStack.last().pose(), (float) left, (float) top, 0.0F).endVertex();
        tesselator.end();

        RenderSystem.disableBlend();
    }


    public static void drawColoredRect(PoseStack poseStack, IGuiRect rect, IGuiColor color) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        color.applyGlColor();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(poseStack.last().pose(), (float) rect.getX(), (float) rect.getY() + rect.getHeight(), 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), (float) rect.getX() + rect.getWidth(), (float) rect.getY() + rect.getHeight(), 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), (float) rect.getX() + rect.getWidth(), (float) rect.getY(), 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), (float) rect.getX(), (float) rect.getY(), 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tesselator.end();

        RenderSystem.disableBlend();
    }

    private static final IGuiColor STENCIL_COLOR = new GuiColorStatic(0, 0, 0, 255);
    private static int stencilDepth = 0;

    public static void startScissor(PoseStack poseStack, IGuiRect rect) {
        if (stencilDepth >= 255) {
            throw new IndexOutOfBoundsException("Exceeded the maximum number of nested stencils (255)");
        }

        if (stencilDepth == 0) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glStencilMask(0xFF);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        }

        // Note: This is faster with inverted logic (skips depth tests when writing)
        GL11.glStencilFunc(GL11.GL_LESS, stencilDepth, 0xFF);
        GL11.glStencilOp(GL11.GL_INCR, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0xFF);

        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);

        drawColoredRect(poseStack, rect, STENCIL_COLOR);

        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, stencilDepth + 1, 0xFF);

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);

        stencilDepth++;
    }

    private static void fillScreen(PoseStack poseStack) {
        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_TEST | GL11.GL_LIGHTING);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, w, h, 0, -1, 1);  //or whatever size you want

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

//        PoseStack poseStack = new PoseStack();
        drawColoredRect(poseStack, new GuiRectangle(0, 0, w, h), STENCIL_COLOR);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        GL11.glPopAttrib();
    }

    /**
     * Pops the last scissor off the stack and returns to the last parent scissor or disables it if there are none
     */
    public static void endScissor(PoseStack poseStack) {
        stencilDepth--;

        if (stencilDepth < 0) {
            throw new IndexOutOfBoundsException("No stencil to end");
        } else if (stencilDepth == 0) {
            GL11.glStencilMask(0xFF);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // Note: Clearing actually requires the mask to be enabled

            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glStencilMask(0x00);

            GL11.glDisable(GL11.GL_STENCIL_TEST);
        } else {
            GL11.glStencilFunc(GL11.GL_LEQUAL, stencilDepth, 0xFF);
            GL11.glStencilOp(GL11.GL_DECR, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glStencilMask(0xFF);

            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);

            fillScreen(poseStack);

            GL11.glColorMask(true, true, true, true);
            GL11.glDepthMask(true);

            GL11.glStencilFunc(GL11.GL_EQUAL, stencilDepth, 0xFF);
            GL11.glStencilMask(0x00);
        }
    }

    /**
     * Similar to normally splitting a string with the fontRenderer however this variant does
     * not attempt to preserve the formatting between lines. This is particularly important when the
     * index positions in the text are required to match the original unwrapped text.
     */
    public static List<String> splitStringWithoutFormat(String str, int wrapWidth, Font font) {
        List<String> list = new ArrayList<>();

        String lastFormat = ""; // Formatting like bold can affect the wrapping width
        String temp = str;

        while (true) {
            int i = sizeStringToWidth(lastFormat + temp, wrapWidth, font); // Cut to size WITH formatting
            i -= lastFormat.length(); // Remove formatting characters from count

            if (temp.length() <= i) {
                list.add(temp);
                break;
            } else {
                String s = temp.substring(0, i);
                char c0 = temp.charAt(i);
                boolean flag = c0 == ' ' || c0 == '\n';
//                lastFormat = Font.getFormatFromString(lastFormat + s);
                lastFormat = Component.literal(lastFormat + s).setStyle(Style.EMPTY).toString();
                temp = temp.substring(i + (flag ? 1 : 0));
                // NOTE: The index actually stops just before the space/nl so we don't need to remove it from THIS line. This is why the previous line moves forward by one for the NEXT line
                list.add(s + (flag ? "\n" : "")); // Although we need to remove the spaces between each line we have to replace them with invisible new line characters to preserve the index count

                if (temp.length() <= 0 && !flag) {
                    break;
                }
            }
        }

        return list;
    }

    public static List<String> splitString(String str, int wrapWidth, Font font) {
        List<String> list = new ArrayList<>();

        String temp = str;

        while (true) {
            int i = sizeStringToWidth(temp, wrapWidth, font); // Cut to size WITH formatting

            if (temp.length() <= i) {
                list.add(temp);
                break;
            } else {
                String s = temp.substring(0, i);
                char c0 = temp.charAt(i);
                boolean flag = c0 == ' ' || c0 == '\n';
//                temp = Font.getFormatFromString(s) + temp.substring(i + (flag ? 1 : 0));
                temp = Component.literal(s).setStyle(Style.EMPTY).toString() + temp.substring(i + (flag ? 1 : 0));
                list.add(s);

                if (temp.length() <= 0 && !flag) {
                    break;
                }
            }
        }

        return list;
    }

    /**
     * Returns the index position under a given set of coordinates in a piece of text
     */
    public static int getCursorPos(String text, int x, Font font) {
        if (text.length() <= 0) {
            return 0;
        }

        int i = 0;

        for (; i < text.length(); i++) {
            if (getStringWidth(text.substring(0, i + 1), font) > x) {
                break;
            }
        }

        if (i - 1 >= 0 && text.charAt(i - 1) == '\n') {
            return i - 1;
        }

        return i;
    }

    /**
     * Returns the index position under a given set of coordinates in a wrapped piece of text
     */
    public static int getCursorPos(String text, int x, int y, int width, Font font) {
        List<String> tLines = RenderUtils.splitStringWithoutFormat(text, width, font);

        if (tLines.size() <= 0) {
            return 0;
        }

        int row = Mth.clamp(y / font.lineHeight, 0, tLines.size() - 1);
        String lastFormat = "";
        String line;
        int idx = 0;

        for (int i = 0; i < row; i++) {
            line = tLines.get(i);
            idx += line.length();
//            lastFormat = Font.getFormatFromString(lastFormat + line);
            lastFormat = Component.literal(lastFormat + line).setStyle(Style.EMPTY).toString();
        }

        return idx + getCursorPos(lastFormat + tLines.get(row), x, font) - lastFormat.length();
    }

    private static int sizeStringToWidth(String str, int wrapWidth, Font font) {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k) {
            char c0 = str.charAt(k);

            switch (c0) {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += font.width(String.valueOf(c0));

                    if (flag) {
                        ++j;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1) {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 'l' && c1 != 'L') {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
            }

            if (c0 == '\n') {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth) {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    public static float lerpFloat(float f1, float f2, float blend) {
        return (f2 * blend) + (f1 * (1F - blend));
    }

    public static double lerpDouble(double d1, double d2, double blend) {
        return (d2 * blend) + (d1 * (1D - blend));
    }

    public static int lerpRGB(int c1, int c2, float blend) {
        float a1 = c1 >> 24 & 255;
        float r1 = c1 >> 16 & 255;
        float g1 = c1 >> 8 & 255;
        float b1 = c1 & 255;

        float a2 = c2 >> 24 & 255;
        float r2 = c2 >> 16 & 255;
        float g2 = c2 >> 8 & 255;
        float b2 = c2 & 255;

        int a3 = (int) lerpFloat(a1, a2, blend);
        int r3 = (int) lerpFloat(r1, r2, blend);
        int g3 = (int) lerpFloat(g1, g2, blend);
        int b3 = (int) lerpFloat(b1, b2, blend);

        return (a3 << 24) + (r3 << 16) + (g3 << 8) + b3;
    }

    public static void drawHoveringText(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, Font font, PoseStack poseStack, GuiGraphics guiGraphics) {
        drawHoveringText(ItemStack.EMPTY, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font, poseStack, guiGraphics);
    }

    /**
     * Modified version of Forge's tooltip rendering that doesn't adjust Z depth
     */
    // first rewrite of drawHoveringText which doesnt require the fill function
//    public static void drawHoveringText(@Nonnull final ItemStack stack, List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, Font font, PoseStack poseStack, GuiGraphics guiGraphics) {
//        if (textLines == null || textLines.isEmpty()) return;
//
//        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, guiGraphics, mouseX, mouseY, screenWidth, screenHeight, font, new ArrayList<>(), (tooltipWidth, tooltipHeight, screenWidth1, screenHeight1, x, y) -> {
//            int newX = x;
//            int newY = y;
//
//            if (x + tooltipWidth + 4 > screenWidth1) {
//                newX = x - 16 - tooltipWidth;
//                if (newX < 4) {
//                    newX = screenWidth1 - tooltipWidth - 4;
//                }
//            }
//
//            if (y + tooltipHeight + 4 > screenHeight1) {
//                newY = y - tooltipHeight - 4;
//                if (newY < 4) {
//                    newY = screenHeight1 - tooltipHeight - 4;
//                }
//            }
//
//            return new Vector2i(newX, newY);
//        });
//        if (MinecraftForge.EVENT_BUS.post(event)) return;
//
//        mouseX = event.getX();
//        mouseY = event.getY();
//        screenWidth = event.getScreenWidth();
//        screenHeight = event.getScreenHeight();
//        maxTextWidth = event.getGraphics().guiWidth(); // doesn't seems right...
//        font = event.getFont();
//
//        poseStack.pushPose();
//        poseStack.translate(0F, 0F, 32F);
//        Lighting.setupForFlatItems();
//        RenderSystem.disableDepthTest();
//        RenderSystem.disableBlend();
//
//        int tooltipTextWidth = 0;
//
//        for (String textLine : textLines) {
//            int textLineWidth = getStringWidth(textLine, font);
//
//            if (textLineWidth > tooltipTextWidth) {
//                tooltipTextWidth = textLineWidth;
//            }
//        }
//
//        boolean needsWrap = false;
//
//        int titleLinesCount = 1;
//        int tooltipX = mouseX + 12;
//
//        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
//            tooltipX = mouseX - 16 - tooltipTextWidth;
//
//            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
//            {
//                if (mouseX > screenWidth / 2) {
//                    tooltipTextWidth = mouseX - 12 - 8;
//                } else {
//                    tooltipTextWidth = screenWidth - 16 - mouseX;
//                }
//                needsWrap = true;
//            }
//        }
//
//        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
//            tooltipTextWidth = maxTextWidth;
//            needsWrap = true;
//        }
//
//        if (needsWrap) {
//            int wrappedTooltipWidth = 0;
//            List<String> wrappedTextLines = new ArrayList<>();
//
//            for (int i = 0; i < textLines.size(); i++) {
//                String textLine = textLines.get(i);
//                List<FormattedCharSequence> wrappedLine = font.split(Component.literal(textLine), tooltipTextWidth);
//                if (i == 0) {
//                    titleLinesCount = wrappedLine.size();
//                }
//
//                for (FormattedCharSequence line : wrappedLine) {
//                    int lineWidth = getStringWidth(line.toString(), font);
//                    if (lineWidth > wrappedTooltipWidth) {
//                        wrappedTooltipWidth = lineWidth;
//                    }
//                    wrappedTextLines.add(line.toString());
//                }
//            }
//
//            tooltipTextWidth = wrappedTooltipWidth;
//            textLines = wrappedTextLines;
//
//            if (mouseX > screenWidth / 2) {
//                tooltipX = mouseX - 16 - tooltipTextWidth;
//            } else {
//                tooltipX = mouseX + 12;
//            }
//        }
//
//        int tooltipY = mouseY - 12;
//        int tooltipHeight = 8;
//
//        if (textLines.size() > 1) {
//            tooltipHeight += (textLines.size() - 1) * 10;
//
//            if (textLines.size() > titleLinesCount) {
//                tooltipHeight += 2; // gap between title lines and next lines
//            }
//        }
//
//        if (tooltipY < 4) {
//            tooltipY = 4;
//        } else if (tooltipY + tooltipHeight + 4 > screenHeight) {
//            tooltipY = screenHeight - tooltipHeight - 4;
//        }
//
//        PresetTexture.TOOLTIP_BG.getTexture().drawTexture(poseStack, tooltipX - 4, tooltipY - 4, tooltipTextWidth + 8, tooltipHeight + 8, 0F, 1F);
//        int tooltipTop = tooltipY;
//
//        poseStack.translate(0F, 0F, 0.1F);
//
//        for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
//            String line = textLines.get(lineNumber);
//            guiGraphics.drawString(font, line, (float) tooltipX, (float) tooltipY, -1, true);
////            font.drawStringWithShadow(line, (float) tooltipX, (float) tooltipY, -1);
//
//            if (lineNumber + 1 == titleLinesCount) {
//                tooltipY += 2;
//            }
//
//            tooltipY += 10;
//        }
//
//        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.Pre(stack, textLines, tooltipX, tooltipTop, font, tooltipTextWidth, tooltipHeight));
//
//        Lighting.setupFor3DItems();
//        RenderSystem.enableDepthTest();
//        RenderSystem.enableBlend();
//        poseStack.popPose();
//    }

    public static void drawHoveringText(@Nonnull final ItemStack stack, List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, Font font, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (textLines == null || textLines.isEmpty()) return;

        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, guiGraphics, mouseX, mouseY, screenWidth, screenHeight, font, new ArrayList<>(), (tooltipWidth, tooltipHeight, screenWidth1, screenHeight1, x, y) -> {
            int newX = x;
            int newY = y;

            if (x + tooltipWidth + 4 > screenWidth1) {
                newX = x - 16 - tooltipWidth;
                if (newX < 4) {
                    newX = screenWidth1 - tooltipWidth - 4;
                }
            }

            if (y + tooltipHeight + 4 > screenHeight1) {
                newY = y - tooltipHeight - 4;
                if (newY < 4) {
                    newY = screenHeight1 - tooltipHeight - 4;
                }
            }

            return new Vector2i(newX, newY); // Replace with appropriate positioner return
        });
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        mouseX = event.getX();
        mouseY = event.getY();
        screenWidth = event.getScreenWidth();
        screenHeight = event.getScreenHeight();
        maxTextWidth = event.getGraphics().guiWidth();
        font = event.getFont();

        poseStack.pushPose();
        poseStack.translate(0F, 0F, 400F);
        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        int tooltipTextWidth = 0;

        for (String textLine : textLines) {
            int textLineWidth = font.width(textLine);

            if (textLineWidth > tooltipTextWidth) {
                tooltipTextWidth = textLineWidth;
            }
        }

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;

        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;

            if (tooltipX < 4) {
                if (mouseX > screenWidth / 2) {
                    tooltipTextWidth = mouseX - 12 - 8;
                } else {
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                }
                needsWrap = true;
            }
        }

        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }

        if (needsWrap) {
            int wrappedTooltipWidth = 0;
            List<String> wrappedTextLines = new ArrayList<>();

            for (int i = 0; i < textLines.size(); i++) {
                String textLine = textLines.get(i);
                List<FormattedCharSequence> wrappedLine = font.split(Component.literal(textLine), tooltipTextWidth);
                if (i == 0) {
                    titleLinesCount = wrappedLine.size();
                }

                for (FormattedCharSequence line : wrappedLine) {
                    int lineWidth = font.width(line);
                    if (lineWidth > wrappedTooltipWidth) {
                        wrappedTooltipWidth = lineWidth;
                    }
                    wrappedTextLines.add(line.toString());
                }
            }

            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines;

            if (mouseX > screenWidth / 2) {
                tooltipX = mouseX - 16 - tooltipTextWidth;
            } else {
                tooltipX = mouseX + 12;
            }
        }

        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;

        if (textLines.size() > 1) {
            tooltipHeight += (textLines.size() - 1) * 10;

            if (textLines.size() > titleLinesCount) {
                tooltipHeight += 2;
            }
        }

        if (tooltipY < 4) {
            tooltipY = 4;
        } else if (tooltipY + tooltipHeight + 4 > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 4;
        }

        // Draw background
        fill(poseStack, tooltipX - 4, tooltipY - 4, tooltipX + tooltipTextWidth + 4, tooltipY - 3, 0xF0100010);
        fill(poseStack, tooltipX - 4, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 4, 0xF0100010);
        fill(poseStack, tooltipX - 4, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, 0xF0100010);
        fill(poseStack, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, 0xF0100010);
        fill(poseStack, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, 0xF0100010);
        fill(poseStack, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 2, 0x505000FF);
        fill(poseStack, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, 0x505000FF);
        fill(poseStack, tooltipX - 3, tooltipY - 3, tooltipX - 2, tooltipY + tooltipHeight + 3, 0x505000FF);
        fill(poseStack, tooltipX + tooltipTextWidth + 2, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, 0x505000FF);

        // Draw text
        for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
            String line = textLines.get(lineNumber);
            guiGraphics.drawString(font, line, tooltipX, tooltipY, -1, true);

            if (lineNumber + 1 == titleLinesCount) {
                tooltipY += 2;
            }

            tooltipY += 10;
        }

        // this is fucked up, I dont know what replaces it
//        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.Post(stack, guiGraphics, mouseX, mouseY, font, new ArrayList<>(), event.getTooltipPositioner()));

        Lighting.setupFor3DItems();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        poseStack.popPose();
    }

    private static void fill(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(1.0F);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.0D);

        var bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(poseStack.last().pose(), x1, y1, 0).color(color).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), x1, y2, 0).color(color).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), x2, y2, 0).color(color).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), x2, y1, 0).color(color).endVertex();
        bufferBuilder.end();

        poseStack.popPose();
    }

    /**
     * A version of getStringWidth that actually behaves according to the format resetting rules of colour codes. Minecraft's built in one is busted!
     */
    public static int getStringWidth(String text, Font font) {
        if (text == null || text.length() == 0) return 0;

        int maxWidth = 0;
        int curLineWidth = 0;
        boolean bold = false;

        for (int j = 0; j < text.length(); ++j) {
            char c0 = text.charAt(j);
            int k = font.width(String.valueOf(c0));

            if (k < 0 && j < text.length() - 1) // k should only be negative when the section sign has been used!
            {
                // Move the caret to the formatting character and read from there
                ++j;
                c0 = text.charAt(j);

                if (c0 != 'l' && c0 != 'L') {
                    int ci = "0123456789abcdefklmnor".indexOf(String.valueOf(c0).toLowerCase(Locale.ROOT).charAt(0));
                    //if (c0 == 'r' || c0 == 'R') // Minecraft's original implemention. This is broken...
                    if (ci < 16 || ci == 21) // Reset bolding. Now supporting colour AND reset codes!
                    {
                        bold = false;
                    }
                } else // This is the bold format on. Time to get T H I C C
                {
                    bold = true;
                }

                k = 0; // Fix the negative value the section symbol previously set
            }

            curLineWidth += k;

            if (bold && k > 0) // This is a bolded normal character which is 1px thicker
            {
                ++curLineWidth;
            }

            if (c0 == '\n') // New line. Reset counting width
            {
                maxWidth = Math.max(maxWidth, curLineWidth);
                curLineWidth = 0;
            }
        }

        return Math.max(maxWidth, curLineWidth);
    }
}
