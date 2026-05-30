package hauveli.fishcasting.common.registries;

import hauveli.fishcasting.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

public class FishcastingSounds {

    /*
    public static final SoundEvent RETURNING_TO_THE_SURFACE =
            SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(
                            MOD_ID,
                            "returning_to_the_surface"
                    )
            );

     */

    public static final ResourceKey<JukeboxSong> RETURNING_TO_THE_SURFACE_JUKEBOX =
            ResourceKey.create(
                    Registries.JUKEBOX_SONG,
                    Constants.id("returning_to_the_surface")
            );

    // TODO:
    // Register these things!!!
}