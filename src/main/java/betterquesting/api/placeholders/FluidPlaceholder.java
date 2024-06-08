//package betterquesting.api.placeholders;
//
//import betterquesting.core.ModReference;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraftforge.fluids.Fluid;
//
//public class FluidPlaceholder extends Fluid {
//    public static Fluid fluidPlaceholder = new FluidPlaceholder();
//
//    public FluidPlaceholder() {
//        super(ModReference.MODID + ".placeholder", new ResourceLocation(ModReference.MODID, "blocks/fluid_placeholder"), new ResourceLocation(ModReference.MODID, "blocks/fluid_placeholder"));
//    }
//}

package betterquesting.api.placeholders;

import betterquesting.core.ModReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FluidPlaceholder {
    public static final DeferredRegister<ForgeFlowingFluid> FLUIDS = DeferredRegister.create((ResourceLocation) ForgeRegistries.FLUIDS, ModReference.MODID);

    public static final RegistryObject<ForgeFlowingFluid.Source> STILL = FLUIDS.register("placeholder",
            () -> new ForgeFlowingFluid.Source(FluidPlaceholder.FLUID_PROPERTIES));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING = FLUIDS.register("placeholder_flowing",
            () -> new ForgeFlowingFluid.Flowing(FluidPlaceholder.FLUID_PROPERTIES));

    public static final ForgeFlowingFluid.Properties FLUID_PROPERTIES = new ForgeFlowingFluid.Properties(
            () -> null,
            () -> null,
            () -> null
    ).bucket(() -> null).block(() -> null);
}
