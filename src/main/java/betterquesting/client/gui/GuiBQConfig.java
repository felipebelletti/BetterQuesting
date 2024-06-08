package betterquesting.client.gui;

import betterquesting.core.ModReference;
import betterquesting.handlers.ConfigHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiBQConfig extends GuiConfig {
    public GuiBQConfig(Screen parent) {
        super(parent, getCategories(ConfigHandler.config), ModReference.MODID, false, false, ModReference.NAME);
    }

    private static List<IConfigElement> getCategories(Configuration config) {
        List<IConfigElement> cats = new ArrayList<>();
        config.getCategoryNames().forEach((s) -> cats.add(new ConfigElement(config.getCategory(s))));
        return cats;
    }
}
