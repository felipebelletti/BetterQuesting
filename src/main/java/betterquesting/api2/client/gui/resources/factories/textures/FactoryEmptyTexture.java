package betterquesting.api2.client.gui.resources.factories.textures;

import betterquesting.api2.client.gui.resources.textures.EmptyTexture;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.ModReference;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public class FactoryEmptyTexture implements IFactoryData<IGuiTexture, JsonObject> {
    public static final FactoryEmptyTexture INSTANCE = new FactoryEmptyTexture();

    private static final ResourceLocation RES_ID = new ResourceLocation(ModReference.MODID, "texture_none");

    @Override
    public IGuiTexture loadFromData(JsonObject data) {
        return createNew();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RES_ID;
    }

    @Override
    public IGuiTexture createNew() {
        return new EmptyTexture();
    }
}
