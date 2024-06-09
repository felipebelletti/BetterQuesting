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
//import betterquesting.misc.QuestResourcesFile;
//import betterquesting.misc.QuestResourcesFolder;
import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.block.model.ModelResourceLocation;
//import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class ClientProxy extends CommonProxy {
    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public void registerHandlers() {
        super.registerHandlers();

        Minecraft.getInstance().getMainRenderTarget().enableStencil();

        if (!Minecraft.getInstance().getMainRenderTarget().isStencilEnabled()) {
            BetterQuesting.logger.error("[!] FAILED TO ENABLE STENCIL BUFFER. GUIS WILL BREAK! [!]");
        }

        MinecraftForge.EVENT_BUS.register(PEventBroadcaster.INSTANCE);

        ExpansionLoader.INSTANCE.initClientAPIs();

        MinecraftForge.EVENT_BUS.register(new QuestNotification());
        MinecraftForge.EVENT_BUS.register(BQ_Keybindings.class);

        /*
        try {
//            ArrayList<Object> list = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getInstance(), "f_91022_");
            ArrayList list = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getInstance(), "field_110449_ao");
            QuestResourcesFolder qRes1 = new QuestResourcesFolder();
            QuestResourcesFile qRes2 = new QuestResourcesFile();
            list.add(qRes1);
            list.add(qRes2);
//            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(qRes1);
//            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(qRes2);
            Minecraft.getInstance().getResourceManager().
            Minecraft.getInstance().reloadResourcePacks();
        } catch (Exception e) {
            BetterQuesting.logger.error("Unable to install questing resource loaders", e);
        }
        */

        // @todo 1.20, this might be needed, however for now I dont know how to properly register it
//        ForgeRegistries.ENTITY_TYPES.register(EntityPlaceholder.class.getName(), new PlaceholderRenderFactory());

        ToolboxRegistry.INSTANCE.registerToolTab(new ResourceLocation(ModReference.MODID, "main"), ToolboxTabMain.INSTANCE);
    }

    @Override
    public void registerRenderers() {
        super.registerRenderers();

        registerBlockModel(BetterQuesting.submitStation);
        registerItemModel(ItemPlaceholder.placeholder);
        registerItemModel(BetterQuesting.extraLife, 0, new ResourceLocation(ModReference.MODID, "heart_full").toString());
        registerItemModel(BetterQuesting.extraLife, 1, new ResourceLocation(ModReference.MODID, "heart_half").toString());
        registerItemModel(BetterQuesting.extraLife, 2, new ResourceLocation(ModReference.MODID, "heart_quarter").toString());
        registerItemModel(BetterQuesting.guideBook);
        registerItemModelSubtypes(BetterQuesting.lootChest, 0, 104, BetterQuesting.lootChest.getDescriptionId());
        registerItemModel(BetterQuesting.questBook);

        ThemeRegistry.INSTANCE.loadResourceThemes();
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerBlockModel(Block block) {
        registerBlockModel(block, 0, block.getDescriptionId());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerBlockModel(Block block, int meta, String name) {
        Item item = Item.byBlock(block);
        // @todo 1.20 I dont think we should be registering stuff here, however I guess the mod won't work properly without it
//        ModelResourceLocation model = new ModelResourceLocation(name, "inventory");
//
//        if (!name.equals(item.getRegistryName().toString())) {
//            ModelBakery.registerItemVariants(item, model);
//        }
//
//        ModelLoader.setCustomModelResourceLocation(item, meta, model);
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
        registerItemModel(item, 0, item.getDescriptionId());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerItemModel(Item item, int meta, String name) {
        // @todo 1.20 yeah, I fucked up here too
//        ModelResourceLocation model = new ModelResourceLocation(name, "inventory");

        if (!name.equals(item.getDescriptionId())) {
            // ModelBakery.registerItemVariants(item, model);
        }

        // ModelLoader.setCustomModelResourceLocation(item, meta, model);
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

        // @todo 1.20 it should be done within main class
        ClientCommandHandler.instance.registerCommand(new BQ_CommandClient());
    }
}
