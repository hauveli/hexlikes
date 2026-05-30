package hauveli.fishcasting;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Constants {

    public static final String MOD_ID = "fishcasting";
    public static final String MOD_NAME = "Fishcasting";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final String FISHBERT_TAG = MOD_ID + ":recently_caught";

    // I dont know if I should avoid using this or not, I noticed some classes have access to Entity.random...
    public static final Random random = new Random();

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}


