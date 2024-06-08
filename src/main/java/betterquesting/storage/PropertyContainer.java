package betterquesting.storage;

import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PropertyContainer implements IPropertyContainer, INBTSaveLoad<CompoundTag> {
    private final CompoundTag nbtInfo = new CompoundTag();

    @Override
    public synchronized <T> T getProperty(IPropertyType<T> prop) {
        if (prop == null) return null;

        return getProperty(prop, prop.getDefault());
    }

    @Override
    public synchronized <T> T getProperty(IPropertyType<T> prop, T def) {
        if (prop == null) return def;

        CompoundTag jProp = getDomain(prop.getKey());

        if (!jProp.hasKey(prop.getKey().getPath())) return def;

        return prop.readValue(jProp.getTag(prop.getKey().getPath()));
    }

    @Override
    public synchronized boolean hasProperty(IPropertyType<?> prop) {
        if (prop == null) return false;
        return getDomain(prop.getKey()).hasKey(prop.getKey().getPath());
    }

    @Override
    public synchronized void removeProperty(IPropertyType<?> prop) {
        if (prop == null) return;
        CompoundTag jProp = getDomain(prop.getKey());

        if (!jProp.hasKey(prop.getKey().getPath())) return;

        jProp.removeTag(prop.getKey().getPath());

        if (jProp.isEmpty()) nbtInfo.removeTag(prop.getKey().getNamespace());
    }

    @Override
    public synchronized <T> void setProperty(IPropertyType<T> prop, T value) {
        if (prop == null || value == null) return;
        CompoundTag dom = getDomain(prop.getKey());
        dom.setTag(prop.getKey().getPath(), prop.writeValue(value));
        nbtInfo.setTag(prop.getKey().getNamespace(), dom);
    }

    @Override
    public synchronized void removeAllProps() {
        List<String> keys = new ArrayList<>(nbtInfo.getKeySet());
        for (String key : keys) nbtInfo.removeTag(key);
    }

    @Override
    public synchronized CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.merge(nbtInfo);
        return nbt;
    }

    @Override
    public synchronized void readFromNBT(CompoundTag nbt) {
        for (String key : nbtInfo.getKeySet()) nbtInfo.removeTag(key);
        nbtInfo.merge(nbt);

        // TODO: FIX CASING
        /*List<String> keys = new ArrayList<>(nbtInfo.getKeySet());
        for(nbt)
        {
        
        }*/
    }

    private CompoundTag getDomain(ResourceLocation res) {
        return nbtInfo.getCompoundTag(res.getNamespace());
    }
}
