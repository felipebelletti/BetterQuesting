package betterquesting.handlers;

import betterquesting.advancement.AdvListenerManager;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.MarkDirtyPlayerEvent;
import betterquesting.api.events.QuestEvent;
import betterquesting.api.events.QuestEvent.Type;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.cache.QuestCache.QResetTime;
import betterquesting.api2.client.gui.themes.gui_args.GArgsNone;
import betterquesting.api2.client.gui.themes.presets.PresetGUIs;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui2.GuiHome;
import betterquesting.client.gui2.GuiQuestLines;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.commands.client.QuestCommandShow;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.network.handlers.*;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.party.PartyInvitations;
import betterquesting.questing.party.PartyManager;
import betterquesting.questing.tasks.*;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Event handling for standard quests and core BetterQuesting functionality
 */
public class EventHandler {
    public static final EventHandler INSTANCE = new EventHandler();

    private static final String SPAWN_WITH_QUEST_BOOK = ModReference.MODID + ".questbook";

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.currentScreen == null && BQ_Keybindings.openQuests.isPressed()) {
            if (BQ_Settings.useBookmark && GuiHome.bookmark != null) {
                mc.displayGuiScreen(GuiHome.bookmark);
            } else {
                Screen guiToDisplay = ThemeRegistry.INSTANCE.getGui(PresetGUIs.HOME, GArgsNone.NONE);
                if (BQ_Settings.useBookmark && BQ_Settings.skipHome)
                    guiToDisplay = new GuiQuestLines(guiToDisplay);
                mc.displayGuiScreen(guiToDisplay);
            }
        }
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (event.getMessage() != null) {
            String text = event.getMessage().getFormattedText();
            int index = text.indexOf("betterquesting.msg.share_quest:");
            if (index != -1) {
                int lastIndex = index + "betterquesting.msg.share_quest:".length();
                int endIndex = lastIndex;
                int questId = 0;
                for (int i = lastIndex; i < text.length(); i++) {
                    int digit = Character.getNumericValue(text.charAt(i));
                    if (digit < 0) {
                        break;
                    }
                    endIndex++;
                    questId = (questId * 10) + digit;
                }
                IQuest quest = QuestDatabase.INSTANCE.getValue(questId);
                if (quest == null) {
                    event.setMessage(new TextComponentTranslation("betterquesting.msg.share_quest_invalid", String.valueOf(questId)));
                    return;
                }
                String questName = quest.getProperty(NativeProps.NAME);
                Component translated = new TextComponentTranslation("betterquesting.msg.share_quest", questName);
                Component newMessage = new TextComponentString(text.substring(0, index) + translated.getFormattedText() + text.substring(endIndex));
                Style newMessageStyle;
                EntityPlayerSP player = Minecraft.getMinecraft().player;
                if (QuestCache.isQuestShown(quest, QuestingAPI.getQuestingUUID(player), player)) {
                    QuestCommandShow.sentViaClick = true;
                    newMessageStyle = newMessage.getStyle()
                            .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bq_client show " + questId))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("betterquesting.msg.share_quest_hover_text_success")));
                } else {
                    newMessageStyle = newMessage.getStyle()
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("betterquesting.msg.share_quest_hover_text_failure")));
                }
                event.setMessage(newMessage.setStyle(newMessageStyle));
            }
        }
    }

    @SubscribeEvent
    public void onCapabilityPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        event.addCapability(CapabilityProviderQuestCache.LOC_QUEST_CACHE, new CapabilityProviderQuestCache());
    }

    @SubscribeEvent
    public void onPlayerClone(Clone event) {
        betterquesting.api2.cache.QuestCache oCache = event.getOriginal().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        betterquesting.api2.cache.QuestCache nCache = event.getEntityPlayer().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);

        if (oCache != null && nCache != null) nCache.deserializeNBT(oCache.serializeNBT());
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        if (!(event.getEntityLiving() instanceof ServerPlayer)) return;
        if (event.getEntityLiving().ticksExisted % 20 != 0) return; // Only triggers once per second

        ServerPlayer player = (ServerPlayer) event.getEntityLiving();
        betterquesting.api2.cache.QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        boolean editMode = QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);

        if (qc == null) return;

        List<DBEntry<IQuest>> activeQuests = QuestDatabase.INSTANCE.bulkLookup(qc.getActiveQuests());
        List<DBEntry<IQuest>> pendingAutoClaims = QuestDatabase.INSTANCE.bulkLookup(qc.getPendingAutoClaims());
        QResetTime[] pendingResets = qc.getScheduledResets();

        UUID uuid = QuestingAPI.getQuestingUUID(player);
        boolean refreshCache = false;

        if (!editMode && player.ticksExisted % 60 == 0) // Passive quest state check every 3 seconds
        {
            List<Integer> com = new ArrayList<>();

            for (DBEntry<IQuest> quest : activeQuests) {
                if (!quest.getValue().isUnlocked(uuid)) continue; // Although it IS active, it cannot be completed yet

                if (quest.getValue().canSubmit(player)) quest.getValue().update(player);

                if (quest.getValue().isComplete(uuid) && !quest.getValue().canSubmit(player)) {
                    refreshCache = true;
                    qc.markQuestDirty(quest.getID());

                    com.add(quest.getID());
                    if (!quest.getValue().getProperty(NativeProps.SILENT))
                        postPresetNotice(quest.getValue(), player, 2);

                    DBEntry<IParty> partyEntry = PartyManager.INSTANCE.getParty(uuid);
                    if (partyEntry != null && player.getServer() != null) {
                        for (UUID memID : partyEntry.getValue().getMembers()) {
                            ServerPlayer memPlayer = player.getServer().getPlayerList().getPlayerByUsername(NameCache.INSTANCE.getName(memID));
                            if (memPlayer != null) {
                                quest.getValue().detect(memPlayer);
                            }
                        }
                    }
                }
            }

            MinecraftForge.EVENT_BUS.post(new QuestEvent(Type.COMPLETED, uuid, com));
        }

        if (!editMode && player.getServer() != null) // Repeatable quest resets
        {
            List<Integer> res = new ArrayList<>();
            long totalTime = System.currentTimeMillis();

            for (QResetTime rTime : pendingResets) {
                IQuest entry = QuestDatabase.INSTANCE.getValue(rTime.questID);

                if (totalTime >= rTime.time && !entry.canSubmit(player)) // REEEEEEEEEset
                {
                    if (entry.getProperty(NativeProps.GLOBAL)) {
                        entry.resetUser(null, false);
                    } else {
                        entry.resetUser(uuid, false);
                    }

                    refreshCache = true;
                    qc.markQuestDirty(rTime.questID);
                    res.add(rTime.questID);
                    if (!entry.getProperty(NativeProps.SILENT)) postPresetNotice(entry, player, 1);
                } else break; // Entries are sorted by time so we fail fast and skip checking the others
            }

            MinecraftForge.EVENT_BUS.post(new QuestEvent(Type.RESET, uuid, res));
        }

        if (!editMode) {
            for (DBEntry<IQuest> entry : pendingAutoClaims) // Auto claims
            {
                if (entry.getValue().canClaim(player)) {
                    entry.getValue().claimReward(player);
                    refreshCache = true;
                    qc.markQuestDirty(entry.getID());
                    // Not going to notify of auto-claims anymore. Kinda pointless if they're already being pinged for completion
                }
            }
        }

        if (refreshCache || player.ticksExisted % 200 == 0) // Refresh the cache if something changed or every 10 seconds
        {
            qc.updateCache(player);
        }

        if (qc.getDirtyQuests().length > 0) NetQuestSync.sendSync(player, qc.getDirtyQuests(), false, true);
        qc.cleanAllQuests();
    }

    // TODO: Create a new message inbox system for these things. On screen popups aren't ideal in combat
    private static void postPresetNotice(IQuest quest, Player player, int preset) {
        if (!(player instanceof ServerPlayer)) return;
        ItemStack icon = quest.getProperty(NativeProps.ICON).getBaseStack();
        String mainText = "";
        String subText = quest.getProperty(NativeProps.NAME);
        String sound = "";

        switch (preset) {
            case 0: {
                mainText = "betterquesting.notice.unlock";
                sound = quest.getProperty(NativeProps.SOUND_UNLOCK);
                break;
            }
            case 1: {
                mainText = "betterquesting.notice.update";
                sound = quest.getProperty(NativeProps.SOUND_UPDATE);
                break;
            }
            case 2: {
                mainText = "betterquesting.notice.complete";
                sound = quest.getProperty(NativeProps.SOUND_COMPLETE);
                break;
            }
        }

        NetNotices.sendNotice(quest.getProperty(NativeProps.GLOBAL) ? null : new ServerPlayer[]{(ServerPlayer) player}, icon, mainText, subText, sound);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ModReference.MODID)) {
            ConfigHandler.config.save();
            ConfigHandler.initConfigs();
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            if (BQ_Settings.curWorldDir != null) {
                SaveLoadHandler.INSTANCE.saveDatabases();
            }
            if (LootSaveLoad.INSTANCE.worldDir != null) {
                LootSaveLoad.INSTANCE.SaveLoot();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

        if(BQ_Settings.spawnWithQuestBook) {
            CompoundTag playerData = event.player.getEntityData();
            CompoundTag data = playerData.hasKey(Player.PERSISTED_NBT_TAG) ? playerData.getCompoundTag(Player.PERSISTED_NBT_TAG) : new CompoundTag();

            if (!data.getBoolean(SPAWN_WITH_QUEST_BOOK)) {
                ItemStack questBook = new ItemStack(BetterQuesting.questBook);
                ItemHandlerHelper.giveItemToPlayer(event.player, questBook);
                data.setBoolean(SPAWN_WITH_QUEST_BOOK, true);
                playerData.setTag(Player.PERSISTED_NBT_TAG, data);
            }
        }

        if (!event.player.world.isRemote && event.player instanceof ServerPlayer) {
            NetLootSync.sendSync((ServerPlayer) event.player);
        }

        if (event.player.world.isRemote || event.player.getServer() == null || !(event.player instanceof ServerPlayer))
            return;

        ServerPlayer mpPlayer = (ServerPlayer) event.player;

        if (BetterQuesting.proxy.isClient() && !mpPlayer.getServer().isDedicatedServer() && event.player.getServer().getServerOwner().equals(mpPlayer.getGameProfile().getName())) {
            NameCache.INSTANCE.updateName(mpPlayer);
            return;
        }

        NetBulkSync.sendReset(mpPlayer, true, true);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.player instanceof ServerPlayer && !((ServerPlayer) event.player).queuedEndExit) {
            ServerPlayer mpPlayer = (ServerPlayer) event.player;

            int lives = LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mpPlayer));

            if (lives <= 0) {
                MinecraftServer server = mpPlayer.getServer();
                if (server == null) return;

                mpPlayer.setGameType(GameType.SPECTATOR);
                if (!server.isDedicatedServer())
                    mpPlayer.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
            } else {
                if (lives == 1) {
                    mpPlayer.sendStatusMessage(new TextComponentString("This is your last life!"), true);
                } else {
                    mpPlayer.sendStatusMessage(new TextComponentString(lives + " lives remaining!"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving().world.isRemote || !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE)) {
            return;
        }

        if (event.getEntityLiving() instanceof Player) {
            UUID uuid = QuestingAPI.getQuestingUUID(((Player) event.getEntityLiving()));

            int lives = LifeDatabase.INSTANCE.getLives(uuid);
            LifeDatabase.INSTANCE.setLives(uuid, lives - 1);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks()) {
            event.getMap().registerSprite(FluidPlaceholder.fluidPlaceholder.getStill());
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onDataUpdated(DatabaseEvent.Update event) {
        // TODO: Change this to a proper panel event. Also explain WHAT updated
        final Screen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof INeedsRefresh)
            Minecraft.getMinecraft().addScheduledTask(((INeedsRefresh) screen)::refreshGui);
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server != null && (event.getCommand().getName().equalsIgnoreCase("op") || event.getCommand().getName().equalsIgnoreCase("deop"))) {
            ServerPlayer playerMP = server.getPlayerList().getPlayerByUsername(event.getParameters()[0]);
            if (playerMP != null)
                opQueue.add(playerMP); // Has to be delayed until after the event when the command has executed
        }
    }

    private final ArrayDeque<ServerPlayer> opQueue = new ArrayDeque<>();
    private boolean openToLAN = false;

    private static final HashSet<Player> playerInventoryUpdates = new HashSet<>();
    private static boolean processingUpdates = false;

    /**
     * Schedules checking player's inventory on the next server tick.
     * Deduplicates requests to avoid scanning it multiple times per tick.
     */
    public static void schedulePlayerInventoryCheck(Player player) {
        if (processingUpdates) {
            return;
        }
        synchronized (playerInventoryUpdates) {
            playerInventoryUpdates.add(player);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (event.phase == Phase.START) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % 60 == 0) {
                AdvListenerManager.INSTANCE.updateAll();
            }
            processingUpdates = true;
            for (Player player : playerInventoryUpdates) {
                if (player == null || player.inventory == null) {
                    continue;
                }
                ParticipantInfo pInfo = new ParticipantInfo(player);

                for (DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests())) {
                    for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                        if (task.getValue() instanceof ITaskInventory) ((ITaskInventory)task.getValue()).onInventoryChange(entry, pInfo);
                    }
                }
            }
            playerInventoryUpdates.clear();
            processingUpdates = false;
        }

        if (event.phase != Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (!server.isDedicatedServer()) {
            boolean tmp = openToLAN;
            openToLAN = server instanceof IntegratedServer && ((IntegratedServer) server).getPublic();
            if (openToLAN && !tmp) opQueue.addAll(server.getPlayerList().getPlayers());
        } else if (!openToLAN) {
            openToLAN = true;
        }

        while (!opQueue.isEmpty()) {
            ServerPlayer playerMP = opQueue.poll();
            if (playerMP != null && NameCache.INSTANCE.updateName(playerMP)) {
                DBEntry<IParty> party = PartyManager.INSTANCE.getParty(QuestingAPI.getQuestingUUID(playerMP));
                if (party != null) {
                    NetNameSync.quickSync(null, party.getID());
                } else {
                    NetNameSync.sendNames(new ServerPlayer[]{playerMP}, new UUID[]{QuestingAPI.getQuestingUUID(playerMP)}, null);
                }
            }
        }

        if (server.getTickCounter() % 60 == 0) PartyInvitations.INSTANCE.cleanExpired();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote || event.getEntityPlayer() instanceof FakePlayer || event.isCanceled()) return;

        Player player = event.getEntityPlayer();
        ParticipantInfo pInfo = new ParticipantInfo(player);

        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskInteractItem)
                    ((TaskInteractItem) task.getValue()).onInteract(pInfo, entry, event.getHand(), event.getItemStack(), Blocks.AIR.getDefaultState(), event.getPos(), false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote || event.getEntityPlayer() instanceof FakePlayer || event.isCanceled()) return;

        Player player = event.getEntityPlayer();
        ParticipantInfo pInfo = new ParticipantInfo(player);

        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        IBlockState state = player.world.getBlockState(event.getPos());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskInteractItem)
                    ((TaskInteractItem) task.getValue()).onInteract(pInfo, entry, event.getHand(), event.getItemStack(), state, event.getPos(), false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote || event.getEntityPlayer() instanceof FakePlayer || event.isCanceled()) return;

        Player player = event.getEntityPlayer();
        ParticipantInfo pInfo = new ParticipantInfo(player);

        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        IBlockState state = player.world.getBlockState(event.getPos());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskInteractItem)
                    ((TaskInteractItem) task.getValue()).onInteract(pInfo, entry, event.getHand(), event.getItemStack(), state, event.getPos(), true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) // CLIENT SIDE ONLY EVENT
    {
        if (event.getEntityPlayer() == null || !event.getEntityPlayer().world.isRemote || event.getEntityPlayer() instanceof FakePlayer || event.isCanceled()) return;
        NetTaskInteract.requestInteraction(false, event.getHand() == EnumHand.MAIN_HAND);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLeftClickAir(PlayerInteractEvent.LeftClickEmpty event) // CLIENT SIDE ONLY EVENT
    {
        if (event.getEntityPlayer() == null || !event.getEntityPlayer().world.isRemote || event.getEntityPlayer() instanceof FakePlayer || event.isCanceled()) return;
        NetTaskInteract.requestInteraction(true, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityAttack(AttackEntityEvent event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote || event.getEntityPlayer() instanceof FakePlayer || event.getTarget() == null || event.isCanceled())
            return;

        Player player = event.getEntityPlayer();
        ParticipantInfo pInfo = new ParticipantInfo(player);

        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskInteractEntity)
                    ((TaskInteractEntity) task.getValue()).onInteract(pInfo, entry, EnumHand.MAIN_HAND, player.getHeldItemMainhand(), event.getTarget(), true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote || event.getEntityPlayer() instanceof FakePlayer || event.getTarget() == null || event.isCanceled())
            return;

        Player player = event.getEntityPlayer();
        ParticipantInfo pInfo = new ParticipantInfo(player);

        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskInteractEntity)
                    ((TaskInteractEntity) task.getValue()).onInteract(pInfo, entry, event.getHand(), event.getItemStack(), event.getTarget(), false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.player == null || event.player.world.isRemote || event.player instanceof FakePlayer) return;

        ParticipantInfo pInfo = new ParticipantInfo(event.player);

        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskCrafting)
                    ((TaskCrafting) task.getValue()).onItemCraft(pInfo, entry, event.crafting.copy());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.player == null || event.player.world.isRemote || event.player instanceof FakePlayer) return;

        ParticipantInfo pInfo = new ParticipantInfo(event.player);

        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskCrafting)
                    ((TaskCrafting) task.getValue()).onItemSmelt(pInfo, entry, event.smelting.copy());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemAnvil(AnvilRepairEvent event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote || event.getEntityPlayer() instanceof FakePlayer) return;

        ParticipantInfo pInfo = new ParticipantInfo(event.getEntityPlayer());
        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskCrafting)
                    ((TaskCrafting) task.getValue()).onItemAnvil(pInfo, entry, event.getItemResult().copy());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityKilled(LivingDeathEvent event) {
        if (event.getSource() == null || !(event.getSource().getTrueSource() instanceof Player) || event.getSource().getTrueSource().world.isRemote || event.getSource().getTrueSource() instanceof FakePlayer || event.isCanceled())
            return;

        ParticipantInfo pInfo = new ParticipantInfo((Player) event.getSource().getTrueSource());
        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskHunt)
                    ((TaskHunt) task.getValue()).onKilledByPlayer(pInfo, entry, event.getEntityLiving(), event.getSource());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityTamed(AnimalTameEvent event) {
        if (event.getTamer() == null || event.getTamer().world.isRemote || event.isCanceled()) return;

        Player player = event.getTamer();
        ParticipantInfo pInfo = new ParticipantInfo(player);

        for (DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests())) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskTame)
                    ((TaskTame) task.getValue()).onAnimalTamed(pInfo, entry, event.getEntityLiving());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() == null || event.getPlayer().world.isRemote || event.getPlayer() instanceof FakePlayer || event.isCanceled()) return;

        ParticipantInfo pInfo = new ParticipantInfo(event.getPlayer());

        for (DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests())) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskBlockBreak)
                    ((TaskBlockBreak) task.getValue()).onBlockBreak(pInfo, entry, event.getState(), event.getPos());
            }
        }
    }

    @SubscribeEvent
    public void onEntityLiving(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof Player) || event.getEntityLiving().world.isRemote || event.getEntityLiving().ticksExisted % 20 != 0 || QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE))
            return;

        Player player = (Player) event.getEntityLiving();
        ParticipantInfo pInfo = new ParticipantInfo(player);

        List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests());

        for (DBEntry<IQuest> entry : actQuest) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof ITaskTickable) {
                    ((ITaskTickable) task.getValue()).tickTask(pInfo, entry);
                } else if (task.getValue() instanceof TaskTrigger) {
                    ((TaskTrigger) task.getValue()).checkSetup(player, entry);
                }
            }
        }
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent event) {
        if (event.getEntityPlayer() == null || event.getEntity().world.isRemote) return;

        ParticipantInfo pInfo = new ParticipantInfo(event.getEntityPlayer());

        for (DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests())) {
            for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
                if (task.getValue() instanceof TaskAdvancement)
                    ((TaskAdvancement) task.getValue()).onAdvancementGet(entry, pInfo, event.getAdvancement());
            }
        }
    }

    @SubscribeEvent
    public void onEntityCreated(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getEntity().world.isRemote) return;

        PlayerContainerListener.refreshListener((Player) event.getEntity());
    }

    @SubscribeEvent
    public void onMarkDirtyPlayer(MarkDirtyPlayerEvent event) {
        SaveLoadHandler.INSTANCE.addDirtyPlayers(event.getDirtyPlayerIDs());
    }
}
