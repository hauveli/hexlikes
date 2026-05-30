package hauveli.fishcasting;

import hauveli.fishcasting.common.FishcastingConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// https://github.com/Lightning-64/Tide-2/blob/f9fc2d04ae4d544ad134025cebd83c7438f67098/src/main/java/com/li64/tide/loaders/neoforge/NeoforgeClientEntrypoint.java
@EventBusSubscriber(modid = Fishcasting.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEntrypoint {
    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        NeoforgeEntrypoint.CONTAINER.registerExtensionPoint(
                IConfigScreenFactory.class,
                (mc, screen) -> AutoConfig.getConfigScreen(FishcastingConfig.class, screen).get()
        );

        // for future self: this MUST be in the client class.
        // also todo: make this actually work...
        AutoConfig.getConfigHolder(FishcastingConfig.class).registerSaveListener(
                (configHolder, config) -> {
                    FishcastingConfig.configurePatchouliFlags(configHolder.getConfig());
                    return null;
                }
        );
    }
}
