package betterquesting.api2.client.gui.resources.factories.colors;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.ModReference;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public class FactoryColorStatic implements IFactoryData<IGuiColor, JsonObject> {
    public static final FactoryColorStatic INSTANCE = new FactoryColorStatic();

    private static final ResourceLocation RES_ID = new ResourceLocation(ModReference.MODID, "color_static");

    @Override
    public GuiColorStatic loadFromData(JsonObject data) {
        int color;

        try {
            // Needs to be done through long so that the signed bit isn't dropped
            color = (int) Long.parseLong(JsonHelper.GetString(data, "color", "FFFFFFFF"), 16);
        } catch (NumberFormatException ignored) {
            color = 0xFFFFFFFF;
        }

        return new GuiColorStatic(color);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RES_ID;
    }

    @Override
    public GuiColorStatic createNew() {
        return new GuiColorStatic(0xFFFFFFFF);
    }
}
