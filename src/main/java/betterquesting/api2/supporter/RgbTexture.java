package betterquesting.api2.supporter;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nonnull;
import java.io.IOException;

public class RgbTexture extends AbstractTexture {
    private final int[] rgbAry;
    private final int w;
    private final int h;
    private DynamicTexture dynamicTexture;

    public RgbTexture(int w, int h, int[] rgbAry) {
        this.w = w;
        this.h = h;
        this.rgbAry = rgbAry;
    }

    @Override
    public void load(@Nonnull ResourceManager resourceManager) throws IOException {
        if (this.dynamicTexture != null) {
            this.dynamicTexture.close();
        }

        NativeImage nativeImage = new NativeImage(w, h, false);
        nativeImage.upload(0, 0, 0, false);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                nativeImage.setPixelRGBA(x, y, rgbAry[y * w + x]);
            }
        }

        this.dynamicTexture = new DynamicTexture(nativeImage);
        this.bind();
        this.dynamicTexture.upload();
    }

    @Override
    public void bind() {
        if (RenderSystem.isOnRenderThreadOrInit()) {
            this.dynamicTexture.bind();
        } else {
            RenderSystem.recordRenderCall(this::bind);
        }
    }

    @Override
    public void close() {
        if (this.dynamicTexture != null) {
            this.dynamicTexture.close();
        }
    }
}
