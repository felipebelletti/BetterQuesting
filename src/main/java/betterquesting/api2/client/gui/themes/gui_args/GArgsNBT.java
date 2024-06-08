package betterquesting.api2.client.gui.themes.gui_args;

import betterquesting.api.misc.ICallback;
import betterquesting.api.nbt_doc.INbtDoc;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public class GArgsNBT<T extends Tag> extends GArgsCallback<T> {
    public final INbtDoc doc;

    public GArgsNBT(@Nullable Screen parent, T nbt, ICallback<T> callback, INbtDoc doc) {
        super(parent, nbt, callback);
        this.doc = doc;
    }
}
