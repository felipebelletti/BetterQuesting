package betterquesting.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.client.QuestNotification;
import betterquesting.client.gui2.GuiHome;
import betterquesting.commands.admin.QuestCommandDefaults;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.legacy.ILegacyLoader;
import betterquesting.legacy.LegacyLoaderRegistry;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyInvitations;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonObject;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class SaveLoadHandler {

    public static SaveLoadHandler INSTANCE = new SaveLoadHandler();

    private boolean hasUpdate = false;
    private boolean isDirty = false;

    private File fileDatabase = null;
    private File fileProgress = null;
    private File dirProgress = null;
    private File fileParties = null;
    private File fileLives = null;
    private File fileNames = null;

    private ILegacyLoader legacyLoader = null;

    private final Set<UUID> dirtyPlayers = new ConcurrentSet<>();

    public boolean hasUpdate() {
        return this.hasUpdate;
    }

    public void resetUpdate() {
        this.hasUpdate = false;
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public void addDirtyPlayers(UUID... players) {
        this.dirtyPlayers.addAll(Arrays.asList(players));
    }

    public void addDirtyPlayers(Collection<UUID> players) {
        this.dirtyPlayers.addAll(players);
    }

    public void loadDatabases(MinecraftServer server) {
        hasUpdate = false;

        if (BetterQuesting.proxy.isClient()) {
            GuiHome.bookmark = null;
            QuestNotification.resetNotices();
        }

        File rootDir;

        if (BetterQuesting.proxy.isClient()) {
            BQ_Settings.curWorldDir = server.getFile("saves/" + server.getFolderName() + "/betterquesting");
            rootDir = server.getFile("saves/" + server.getFolderName());
        } else {
            BQ_Settings.curWorldDir = server.getFile(server.getFolderName() + "/betterquesting");
            rootDir = server.getFile(server.getFolderName());
        }

        fileDatabase = new File(BQ_Settings.curWorldDir, "QuestDatabase.json");
        fileProgress = new File(BQ_Settings.curWorldDir, "QuestProgress.json");
        dirProgress = new File(BQ_Settings.curWorldDir, "QuestProgress");
        fileParties = new File(BQ_Settings.curWorldDir, "QuestingParties.json");
        fileLives = new File(BQ_Settings.curWorldDir, "LifeDatabase.json");
        fileNames = new File(BQ_Settings.curWorldDir, "NameCache.json");

        checkLegacyFiles(rootDir);

        loadConfig();

        loadProgress();

        loadParties();

        loadNames();

        loadLives();

        legacyLoader = null;

        BetterQuesting.logger.info("Loaded " + QuestDatabase.INSTANCE.size() + " quests");
        BetterQuesting.logger.info("Loaded " + QuestLineDatabase.INSTANCE.size() + " quest lines");
        BetterQuesting.logger.info("Loaded " + PartyManager.INSTANCE.size() + " parties");
        BetterQuesting.logger.info("Loaded " + NameCache.INSTANCE.size() + " names");

        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Load(DBType.ALL));
    }

    public void saveDatabases() {
        List<Future<Void>> allFutures = new ArrayList<>();

        if (isDirty || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) {
            allFutures.add(saveConfig());
        }

        allFutures.addAll(saveProgress());

        allFutures.add(saveParties());

        allFutures.add(saveNames());

        allFutures.add(saveLives());

        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Save(DBType.ALL));

        for (Future<Void> future : allFutures) {
            try {
                future.get();
                isDirty = false;
            } catch (InterruptedException e) {
                BetterQuesting.logger.warn("Saving interrupted!", e);
            } catch (ExecutionException e) {
                BetterQuesting.logger.warn("Saving failed!", e.getCause());
            }
        }
    }

    public void unloadDatabases() {
        BQThreadedIO.INSTANCE.enqueue(() -> {
            BQ_Settings.curWorldDir = null;
            hasUpdate = false;
            isDirty = false;

            QuestSettings.INSTANCE.reset();
            QuestDatabase.INSTANCE.reset();
            QuestLineDatabase.INSTANCE.reset();
            LifeDatabase.INSTANCE.reset();
            NameCache.INSTANCE.reset();
            PartyInvitations.INSTANCE.reset();
            PartyManager.INSTANCE.reset();

            if (BetterQuesting.proxy.isClient()) {
                GuiHome.bookmark = null;
                QuestNotification.resetNotices();
            }

            // TODO: Fire an event to that expansions can use to reset their own databases if necessary
        });
    }

    private void loadConfig() {
        QuestSettings.INSTANCE.reset();
        QuestDatabase.INSTANCE.reset();
        QuestLineDatabase.INSTANCE.reset();

        int packVer = 0;
        String packName = "";

        File defaultDatabaseFile = new File(BQ_Settings.defaultDir, QuestCommandDefaults.DEFAULT_FILE + ".json");
        File defaultDatabaseDir = new File(BQ_Settings.defaultDir, QuestCommandDefaults.DEFAULT_FILE);
        File defaultDatabaseSettingsFile = new File(defaultDatabaseDir, QuestCommandDefaults.SETTINGS_FILE);

        if (fileDatabase.exists()) {
            boolean legacySettings = !defaultDatabaseSettingsFile.exists();
            File settingsFile = legacySettings ? defaultDatabaseFile : defaultDatabaseSettingsFile;
            JsonObject settingsJson = JsonHelper.ReadFromFile(settingsFile);
            CompoundTag settingsTag = NBTConverter.JSONtoNBT_Object(settingsJson, new CompoundTag(), true);

            QuestSettings tmpSettings = new QuestSettings();
            tmpSettings.readFromNBT(legacySettings ? settingsTag.getCompoundTag("questSettings") : settingsTag);
            packVer = tmpSettings.getProperty(NativeProps.PACK_VER);
            packName = tmpSettings.getProperty(NativeProps.PACK_NAME);

            // Getting the build version like this is a bit wasteful, as we read the JSON twice.
            // Perhaps we should improve this.
            JsonObject databaseJson = JsonHelper.ReadFromFile(fileDatabase);
            String buildVer =
                    NBTConverter.JSONtoNBT_Object(databaseJson, new CompoundTag(), true)
                            .getString("build");
            String currVer = Loader.instance().activeModContainer().getVersion();

            if (!currVer.equalsIgnoreCase(buildVer)) // RUN BACKUPS
            {
                String fsVer = JsonHelper.makeFileNameSafe(buildVer);

                if (fsVer.isEmpty())
                {
                    fsVer = "pre-251";
                }

                BetterQuesting.logger.warn("BetterQuesting has been updated to from \"" + fsVer + "\" to \"" + currVer + "\"! Creating backups...");

                JsonHelper.CopyPaste(fileDatabase, new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "QuestDatabase_backup_" + fsVer + ".json"));
                JsonHelper.CopyPaste(fileProgress, new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "QuestProgress_backup_" + fsVer + ".json"));
                JsonHelper.CopyPaste(fileParties, new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "QuestingParties_backup_" + fsVer + ".json"));
                JsonHelper.CopyPaste(fileNames, new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "NameCache_backup_" + fsVer + ".json"));
                JsonHelper.CopyPaste(fileLives, new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "LifeDatabase_backup_" + fsVer + ".json"));
            }

            QuestCommandDefaults.loadLegacy(null, null, fileDatabase, true);

        } else { // LOAD DEFAULTS
            if (defaultDatabaseDir.exists())
            {
                QuestCommandDefaults.load(null, null, defaultDatabaseDir, true);
            }
            else
            {
                QuestCommandDefaults.loadLegacy(null, null, defaultDatabaseFile, true);
            }

            isDirty = true;
            QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, false); // Force edit off
        }



        hasUpdate = packName.equals(QuestSettings.INSTANCE.getProperty(NativeProps.PACK_NAME)) && packVer > QuestSettings.INSTANCE.getProperty(NativeProps.PACK_VER);
    }

    private void loadProgress() {
        if (fileProgress.exists()) {
            JsonObject json = JsonHelper.ReadFromFile(fileProgress);

            if (legacyLoader == null) {
                CompoundTag nbt = NBTConverter.JSONtoNBT_Object(json, new CompoundTag(), true);
                QuestDatabase.INSTANCE.readProgressFromNBT(nbt.getTagList("questProgress", 10), false);
            } else {
                legacyLoader.readProgressFromJson(json);
            }
        }

        for (File file : getPlayerProgressFiles()) {
            JsonObject json = JsonHelper.ReadFromFile(file);
            CompoundTag nbt = NBTConverter.JSONtoNBT_Object(json, new CompoundTag(), true);
            QuestDatabase.INSTANCE.readProgressFromNBT(nbt.getTagList("questProgress", 10), true);
        }
    }

    private void loadParties() {
        JsonObject json = JsonHelper.ReadFromFile(fileParties);

        CompoundTag nbt = NBTConverter.JSONtoNBT_Object(json, new CompoundTag(), true);
        PartyManager.INSTANCE.readFromNBT(nbt.getTagList("parties", 10), false);
    }

    private void loadNames() {
        NameCache.INSTANCE.reset();
        JsonObject json = JsonHelper.ReadFromFile(fileNames);

        CompoundTag nbt = NBTConverter.JSONtoNBT_Object(json, new CompoundTag(), true);
        NameCache.INSTANCE.readFromNBT(nbt.getTagList("nameCache", 10), false);
    }

    private void loadLives() {
        LifeDatabase.INSTANCE.reset();
        JsonObject json = JsonHelper.ReadFromFile(fileLives);

        CompoundTag nbt = NBTConverter.JSONtoNBT_Object(json, new CompoundTag(), true);
        LifeDatabase.INSTANCE.readFromNBT(nbt.getCompoundTag("lifeDatabase"), false);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void checkLegacyFiles(File rootDir) {
        if (new File(rootDir, "QuestDatabase.json").exists() && !fileDatabase.exists()) {
            File legacyDatabase = new File(rootDir, "QuestDatabase.json");
            File legacyProgress = new File(rootDir, "QuestProgress.json");
            File legacyParties = new File(rootDir, "QuestingParties.json");
            File legacyLives = new File(rootDir, "LifeDatabase.json");
            File legacyNames = new File(rootDir, "NameCache.json");

            JsonHelper.CopyPaste(legacyDatabase, fileDatabase);
            JsonHelper.CopyPaste(legacyProgress, fileProgress);
            JsonHelper.CopyPaste(legacyParties, fileParties);
            JsonHelper.CopyPaste(legacyLives, fileLives);
            JsonHelper.CopyPaste(legacyNames, fileNames);

            legacyDatabase.delete();
            legacyProgress.delete();
            legacyParties.delete();
            legacyLives.delete();
            legacyNames.delete();
        }
    }

    private Future<Void> saveConfig() {
        CompoundTag json = new CompoundTag();

        json.setTag("questSettings", QuestSettings.INSTANCE.writeToNBT(new CompoundTag()));
        json.setTag("questDatabase", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), null));
        json.setTag("questLines", QuestLineDatabase.INSTANCE.writeToNBT(new NBTTagList(), null));

        json.setString("format", BetterQuesting.FORMAT);
        json.setString("build", ModReference.VERSION);

        return JsonHelper.WriteToFile(fileDatabase, NBTConverter.NBTtoJSON_Compound(json, new JsonObject(), true));
    }

    private List<Future<Void>> saveProgress() {
        final List<Future<Void>> futures = dirtyPlayers.stream().map(this::savePlayerProgress).collect(Collectors.toList());
        dirtyPlayers.clear();
        return futures;
    }

    private Future<Void> saveParties() {
        CompoundTag json = new CompoundTag();

        json.setTag("parties", PartyManager.INSTANCE.writeToNBT(new NBTTagList(), null));

        return JsonHelper.WriteToFile(fileParties, NBTConverter.NBTtoJSON_Compound(json, new JsonObject(), true));
    }

    private Future<Void> saveNames() {
        CompoundTag json = new CompoundTag();

        json.setTag("nameCache", NameCache.INSTANCE.writeToNBT(new NBTTagList(), null));

        return JsonHelper.WriteToFile(fileNames, NBTConverter.NBTtoJSON_Compound(json, new JsonObject(), true));
    }

    private Future<Void> saveLives() {
        CompoundTag json = new CompoundTag();

        json.setTag("lifeDatabase", LifeDatabase.INSTANCE.writeToNBT(new CompoundTag(), null));

        return JsonHelper.WriteToFile(fileLives, NBTConverter.NBTtoJSON_Compound(json, new JsonObject(), true));
    }

    public Future<Void> savePlayerProgress(UUID player) {
        CompoundTag json = new CompoundTag();

        json.setTag("questProgress", QuestDatabase.INSTANCE.writeProgressToNBT(new NBTTagList(), Collections.singletonList(player)));

        return JsonHelper.WriteToFile(new File(dirProgress, player.toString() + ".json"), NBTConverter.NBTtoJSON_Compound(json, new JsonObject(), true));
    }

    private List<File> getPlayerProgressFiles() {
        final File[] files = dirProgress.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(files).filter(f -> f.getName().endsWith(".json")).collect(Collectors.toList());
    }
}
