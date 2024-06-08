package betterquesting.storage;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.ILifeDatabase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public final class LifeDatabase implements ILifeDatabase {
    public static final LifeDatabase INSTANCE = new LifeDatabase();

    private final HashMap<UUID, Integer> playerLives = new HashMap<>();

    @Override
    public synchronized int getLives(@Nonnull UUID uuid) {
        return playerLives.computeIfAbsent(uuid, (k) -> QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_DEF));
    }

    @Override
    public synchronized void setLives(@Nonnull UUID uuid, int value) {
        playerLives.put(uuid, Mth.clamp(value, 0, QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX)));
    }

    @Override
    public synchronized CompoundTag writeToNBT(CompoundTag nbt, @Nullable List<UUID> users) {
        ListTag jul = new ListTag();
        for (Entry<UUID, Integer> entry : playerLives.entrySet()) {
            if (users != null && !users.contains(entry.getKey())) continue;
            CompoundTag j = new CompoundTag();
            j.setString("uuid", entry.getKey().toString());
            j.setInteger("lives", entry.getValue());
            jul.appendTag(j);
        }
        nbt.setTag("playerLives", jul);

        return nbt;
    }

    @Override
    public synchronized void readFromNBT(CompoundTag nbt, boolean merge) {
        if (!merge) playerLives.clear();
        ListTag tagList = nbt.getTagList("playerLives", 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            CompoundTag j = tagList.getCompoundTagAt(i);

            try {
                UUID uuid = UUID.fromString(j.getString("uuid"));
                int lives = j.getInteger("lives");
                playerLives.put(uuid, lives);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public synchronized void reset() {
        playerLives.clear();
    }
}