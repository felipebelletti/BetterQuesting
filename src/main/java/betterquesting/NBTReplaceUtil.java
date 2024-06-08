package betterquesting;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;

public class NBTReplaceUtil {
    @SuppressWarnings("unchecked")
    public static <T extends NBTBase> T replaceStrings(T baseTag, String key, String replace) {
        if (baseTag == null) {
            return null;
        }

        if (baseTag instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag) baseTag;

            for (String k : compound.getKeySet()) {
                compound.setTag(k, replaceStrings(compound.getTag(k), key, replace));
            }
        } else if (baseTag instanceof ListTag) {
            ListTag list = (ListTag) baseTag;

            for (int i = 0; i < list.tagCount(); i++) {
                list.set(i, replaceStrings(list.get(i), key, replace));
            }
        } else if (baseTag instanceof NBTTagString) {
            NBTTagString tString = (NBTTagString) baseTag;
            return (T) new NBTTagString(tString.getString().replaceAll(key, replace));
        }

        return baseTag; // Either isn't a string or doesn't contain one
    }
}
