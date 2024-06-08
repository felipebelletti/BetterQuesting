package betterquesting.api2.registry;

import net.minecraft.resources.ResourceLocation;

@Deprecated // Stop... just use lambdas
public interface IFactory<T> {
    ResourceLocation getRegistryName();

    T createNew();
}
