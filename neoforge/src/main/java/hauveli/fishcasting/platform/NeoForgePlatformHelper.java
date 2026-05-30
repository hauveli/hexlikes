package hauveli.fishcasting.platform;

import hauveli.fishcasting.platform.services.IPlatformHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

import static hauveli.fishcasting.Fishcasting.BOBBER_RADIUS;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Holder<Attribute> getHolderForBobberRadius() {
        return BOBBER_RADIUS.getDelegate();
    }
}
