package betterquesting.core.proxies;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.placeholders.EntityPlaceholder;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import betterquesting.client.renderer.PlaceholderRenderFactory;
import betterquesting.client.themes.BQSTextures;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.commands.BQ_CommandClient;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ExpansionLoader;
import betterquesting.core.ModReference;
import betterquesting.importers.AdvImporter;
import betterquesting.importers.NativeFileImporter;
import betterquesting.importers.ftbq.FTBQQuestImporter;
import betterquesting.importers.hqm.HQMBagImporter;
import betterquesting.importers.hqm.HQMQuestImporter;
import betterquesting.misc.QuestResourcesFile;
import betterquesting.misc.QuestResourcesFolder;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import java.util.ArrayList;

public class ClientProxy extends CommonProxy {
    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerHandlers() {
        super.registerHandlers();

        if (!Minecraft.getInstance().getFramebuffer().isStencilEnabled()) {
            if (!Minecraft.getInstance().getFramebuffer().enableStencil()) {
                BetterQuesting.logger.error("[!] FAILED TO ENABLE STENCIL BUFFER. GUIS WILL BREAK! [!]");
            }
        }

        MinecraftForge.EVENT_BUS.register(PEventBroadcaster.INSTANCE);

        ExpansionLoader.INSTANCE.initClientAPIs();

        MinecraftForge.EVENT_BUS.register(new QuestNotification());
        BQ_Keybindings.RegisterKeys();

        try {
            //String tmp = "defaultResourcePacks";
            ArrayList list = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getInstance(), "field_110449_ao", "defaultResourcePacks");
            QuestResourcesFolder qRes1 = new QuestResourcesFolder();
            QuestResourcesFile qRes2 = new QuestResourcesFile();
            list.add(qRes1);
            list.add(qRes2);
            ((SimpleReloadableResourceManager) Minecraft.getInstance().getResourceManager()).reloadResourcePack(qRes1); // Make sure the pack(s) are visible to everything
            ((SimpleReloadableResourceManager) Minecraft.getInstance().getResourceManager()).reloadResourcePack(qRes2); // Make sure the pack(s) are visible to everything
        } catch (Exception e) {
            BetterQuesting.logger.error("Unable to install questing resource loaders", e);
        }

        RenderingRegistry.registerEntityRenderingHandler(EntityPlaceholder.class, new PlaceholderRenderFactory());

        ToolboxRegistry.INSTANCE.registerToolTab(new ResourceLocation(ModReference.MODID, "main"), ToolboxTabMain.INSTANCE);
    }

    @Override
    public void registerRenderers() {
        super.registerRenderers();

        registerBlockModel(BetterQuesting.submitStation);
        registerItemModel(ItemPlaceholder.placeholder);
        registerItemModel(BetterQuesting.extraLife, 0, new ResourceLocation( ModReference.MODID, "heart_full").toString());
        registerItemModel(BetterQuesting.extraLife, 1, new ResourceLocation( ModReference.MODID, "heart_half").toString());
        registerItemModel(BetterQuesting.extraLife, 2, new ResourceLocation( ModReference.MODID, "heart_quarter").toString());
        registerItemModel(BetterQuesting.guideBook);
        registerItemModelSubtypes(BetterQuesting.lootChest, 0, 104, BetterQuesting.lootChest.getRegistryName().toString());
        registerItemModel(BetterQuesting.questBook);

        ThemeRegistry.INSTANCE.loadResourceThemes();
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerBlockModel(Block block) {
        registerBlockModel(block, 0, block.getRegistryName().toString());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerBlockModel(Block block, int meta, String name) {
        Item item = Item.getItemFromBlock(block);
        ModelResourceLocation model = new ModelResourceLocation(name, "inventory");

        if (!name.equals(item.getRegistryName().toString())) {
            ModelBakery.registerItemVariants(item, model);
        }

        ModelLoader.setCustomModelResourceLocation(item, meta, model);
    }

    @OnlyIn(Dist.CLIENT)
    private void registerItemModelSubtypes(Item item, int metaStart, int metaEnd, String name) {
        if (metaStart > metaEnd) {
            int tmp = metaStart;
            metaStart = metaEnd;
            metaEnd = tmp;
        }

        for (int m = metaStart; m <= metaEnd; m++) {
            registerItemModel(item, m, name);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerItemModel(Item item) {
        registerItemModel(item, 0, item.getRegistryName().toString());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerItemModel(Item item, int meta, String name) {
        ModelResourceLocation model = new ModelResourceLocation(name, "inventory");

        if (!name.equals(item.getRegistryName().toString())) {
            ModelBakery.registerItemVariants(item, model);
        }

        ModelLoader.setCustomModelResourceLocation(item, meta, model);
    }

    @Override
    public void registerExpansion() {
        super.registerExpansion();

        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(NativeFileImporter.INSTANCE);

        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(HQMQuestImporter.INSTANCE);
        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(HQMBagImporter.INSTANCE);

        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(FTBQQuestImporter.INSTANCE);
        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(AdvImporter.INSTANCE);

        BQSTextures.registerTextures();

        ClientCommandHandler.instance.registerCommand(new BQ_CommandClient());
    }
}
