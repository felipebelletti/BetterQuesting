package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelFluidSlot;
import betterquesting.core.BetterQuesting;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.Iterator;

public class CanvasFluidDatabase extends CanvasSearch<FluidStack, Fluid> {
    private final int btnId;

    public CanvasFluidDatabase(IGuiRect rect, int buttonId) {
        super(rect);

        this.btnId = buttonId;
    }

    @Override
    protected Iterator<Fluid> getIterator() { return ForgeRegistries.FLUIDS.iterator();
    }

    @Override
    protected void queryMatches(Fluid fluid, String query, final ArrayDeque<FluidStack> results) {
        if (fluid == null || ForgeRegistries.FLUIDS.getKey(fluid) == null) {
            return;
        }

        try {
            FluidStack stack = new FluidStack(fluid, 1000);
            String fluidName = fluid.getFluidType().getDescriptionId().toLowerCase();
            String localizedName = fluid.getFluidType().getDescriptionId().toLowerCase();
            String registryName = ForgeRegistries.FLUIDS.getKey(fluid).toString().toLowerCase();

            if (fluidName.contains(query) || localizedName.contains(query) || registryName.contains(query)) {
                results.add(stack);
            }
        } catch (Exception e) {
            BetterQuesting.logger.error("An error occurred while searching fluid \"" + ForgeRegistries.FLUIDS.getKey(fluid) + "\" (" + fluid.getClass().getName() + ")", e);
        }
    }

    @Override
    protected boolean addResult(FluidStack stack, int index, int cachedWidth) {
        if (stack == null) {
            return false;
        }

        int x = (index % (cachedWidth / 18)) * 18;
        int y = (index / (cachedWidth / 18)) * 18;

        this.addPanel(new PanelFluidSlot(new GuiRectangle(x, y, 18, 18, 0), btnId, stack));

        return true;
    }
}
