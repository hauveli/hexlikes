package hauveli.fishcasting;

import hauveli.fishcasting.common.FishcastingConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static hauveli.fishcasting.platform.Services.PLATFORM;

public class Fishcasting {

    public static final String MOD_ID = "fishcasting";
    public static final String MOD_NAME = "Fishcasting";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final String FISHBERT_TAG = MOD_ID + ":recently_caught";

    public static FishcastingConfig CONFIG;

    // I dont know if I should avoid using this or not, I noticed some classes have access to Entity.random...
    public static final Random random = new Random();

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void init() {
        CONFIG = AutoConfig.register(FishcastingConfig.class, Toml4jConfigSerializer::new).getConfig();
        // I should maybe move this to somewhere smarter later? idk...
        /*
        AutoConfig.getConfigHolder(FishcastingConfig.class)
                .registerSaveListener((holder, config) -> {
                    FishcastingConfig.configurePatchouliFlags(config);
                    return null;
                });

         */
    }
}


