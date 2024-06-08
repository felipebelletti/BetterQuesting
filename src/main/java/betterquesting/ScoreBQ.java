package betterquesting;

import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

public class ScoreBQ implements INBTPartial<NBTTagList, UUID> {
    private final TreeMap<UUID, Integer> playerScores = new TreeMap<>();

    public synchronized int getScore(@Nonnull UUID uuid) {
        Integer value = playerScores.get(uuid);
        return value == null ? 0 : value;
    }

    public synchronized void setScore(@Nonnull UUID uuid, int value) {
        playerScores.put(uuid, value);
    }

    public synchronized boolean hasEntry(@Nonnull UUID uuid) {
        return playerScores.containsKey(uuid);
    }

    @Override
    public synchronized NBTTagList writeToNBT(NBTTagList nbt, @Nullable List<UUID> subset) {
        for (Entry<UUID, Integer> entry : playerScores.entrySet()) {
            if (subset != null && !subset.contains(entry.getKey())) continue;
            CompoundTag jObj = new CompoundTag();
            jObj.setString("uuid", entry.getKey().toString());
            jObj.setInteger("value", entry.getValue());
            nbt.appendTag(jObj);
        }

        return nbt;
    }

    @Override
    public synchronized void readFromNBT(NBTTagList nbt, boolean merge) {
        if (!merge) playerScores.clear();

        for (int i = 0; i < nbt.tagCount(); i++) {
            try {
                CompoundTag tag = nbt.getCompoundTagAt(i);
                playerScores.put(UUID.fromString(tag.getString("uuid")), tag.getInteger("value"));

            } catch (Exception ignored) {
            }
        }
    }
}
