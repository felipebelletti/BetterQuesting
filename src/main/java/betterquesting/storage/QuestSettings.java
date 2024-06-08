package betterquesting.storage;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.IQuestSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

public class QuestSettings extends PropertyContainer implements IQuestSettings {
    public static final QuestSettings INSTANCE = new QuestSettings();

    public QuestSettings() {
        this.setupProps();
    }

    @Override
    public boolean canUserEdit(Player player) {
        if (player == null) return false;
        return this.getProperty(NativeProps.EDIT_MODE) && NameCache.INSTANCE.isOP(QuestingAPI.getQuestingUUID(player));
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);

        this.setupProps();
    }

    @Override
    public void reset() {
        this.readFromNBT(new CompoundTag());
    }

    private void setupProps() {
        this.setupValue(NativeProps.PACK_NAME);
        this.setupValue(NativeProps.PACK_VER);

        this.setupValue(NativeProps.PARTY_ENABLE);
        this.setupValue(NativeProps.EDIT_MODE);
        this.setupValue(NativeProps.HARDCORE);
        this.setupValue(NativeProps.LIVES_DEF);
        this.setupValue(NativeProps.LIVES_MAX);

        this.setupValue(NativeProps.HOME_IMAGE);
        this.setupValue(NativeProps.HOME_ANC_X);
        this.setupValue(NativeProps.HOME_ANC_Y);
        this.setupValue(NativeProps.HOME_OFF_X);
        this.setupValue(NativeProps.HOME_OFF_Y);
    }

    private <T> void setupValue(IPropertyType<T> prop) {
        this.setupValue(prop, prop.getDefault());
    }

    private <T> void setupValue(IPropertyType<T> prop, T def) {
        this.setProperty(prop, this.getProperty(prop, def));
    }
}
