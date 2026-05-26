package hauveli.hexlikes.hexcasting;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import hauveli.hexlikes.Constants;
import hauveli.hexlikes.hexcasting.actions.OpGetBobbersOwnerJ;
import hauveli.hexlikes.hexcasting.actions.OpGetCatchesBobberJ;
import hauveli.hexlikes.hexcasting.actions.OpGetOwnersBobberJ;
import hauveli.hexlikes.hexcasting.actions.OpGetBobbersCatchJ;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

// https://github.com/YukkuriC/HexOverpowered/blob/1.21/common/src/main/java/io/yukkuric/hexop/actions/HexOPActions.kt
// code adapted from the above
public class HexlikesActionsJ {
    private static final Map<ResourceLocation, ActionRegistryEntry> CACHED = new HashMap<>();

    static {
        // with the exception of the bobber thingies here
        wrap("bobber/from_owner", "weeed", HexDir.SOUTH_EAST, OpGetOwnersBobberJ.INSTANCE);
        wrap("owner/from_bobber", "aqqqw", HexDir.WEST, OpGetBobbersOwnerJ.INSTANCE);
        wrap("catch/from_bobber", "weeede", HexDir.SOUTH_EAST, OpGetBobbersCatchJ.INSTANCE);
        wrap("bobber/from_catch", "qaqqqw", HexDir.NORTH_WEST, OpGetCatchesBobberJ.INSTANCE);
    }

    public static void registerActions(BiConsumer<ResourceLocation, ActionRegistryEntry> handler) {
        for (Map.Entry<ResourceLocation, ActionRegistryEntry> entry : CACHED.entrySet()) {
            handler.accept(entry.getKey(), entry.getValue());
        }
    }

    private static ActionRegistryEntry wrap(String name, String signature, HexDir dir, Action action) {
        HexPattern pattern = HexPattern.fromAngles(signature, dir);
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        ActionRegistryEntry entry = new ActionRegistryEntry(pattern, action);
        CACHED.put(key, entry); // why won't java let me index normally here CACHED[key] = entry
        return entry;
    }
}