package betterquesting.api2.client.gui.controls.callbacks;

import betterquesting.api.misc.ICallback;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;

public class CallbackNBTTagString implements ICallback<String> {
    private final NBTBase tag;
    private final String sKey;
    private final int iKey;

    public CallbackNBTTagString(CompoundTag tag, String key) {
        this.tag = tag;
        this.sKey = key;
        this.iKey = -1;
    }

    public CallbackNBTTagString(ListTag tag, int key) {
        this.tag = tag;
        this.sKey = null;
        this.iKey = key;
    }

    @Override
    public void setValue(String value) {
        if (tag.getId() == 10) {
            ((CompoundTag) tag).setString(sKey, value);
        } else {
            ((ListTag) tag).set(iKey, new NBTTagString(value));
        }
    }
}
