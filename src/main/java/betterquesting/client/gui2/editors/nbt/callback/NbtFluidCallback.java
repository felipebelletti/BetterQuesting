package betterquesting.client.gui2.editors.nbt.callback;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class NbtFluidCallback implements ICallback<FluidStack> {
    private final CompoundTag json;

    public NbtFluidCallback(CompoundTag json) {
        this.json = json;
    }

    public void setValue(FluidStack stack) {
        FluidStack baseStack;

        if (stack != null) {
            baseStack = stack;
        } else {
            baseStack = new FluidStack(FluidRegistry.WATER, 1000);
        }

        JsonHelper.ClearCompoundTag(json);
        JsonHelper.FluidStackToJson(baseStack, json);
    }
}
