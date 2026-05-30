package hauveli.fishcasting;


import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.hex.HexArithmetics;
import at.petrak.hexcasting.forge.cap.HexCapabilities;
import com.li64.tide.client.TideItemModelProperties;
import com.li64.tide.registries.TideEntityTypes;
import com.li64.tide.registries.TideFish;
import com.li64.tide.registries.TideItems;
import hauveli.fishcasting.common.FishcastingConfig;
import hauveli.fishcasting.common.chair.TackleBoxChairEntity;
import hauveli.fishcasting.common.chair.TackleBoxChairModel;
import hauveli.fishcasting.common.chair.TackleBoxChairRenderer;
import hauveli.fishcasting.common.registries.FishcastingEntityTypes;
import hauveli.fishcasting.hexcasting.FishcastingActions;
import hauveli.fishcasting.common.CursedEntity;
import hauveli.fishcasting.common.registries.FishcastingItems;
import hauveli.fishcasting.hexcasting.FishcastingFishArithmetic;
import hauveli.fishcasting.hexcasting.iota.FishcastingIotaTypes;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

import static hauveli.fishcasting.Fishcasting.MOD_ID;
import static hauveli.fishcasting.common.chair.TackleBoxChairModel.LAYER_LOCATION;

@Mod(MOD_ID)
public class NeoforgeEntrypoint {

    // I gotta move this out when I hexdummy this
    // vvvv technological debt here vvvv
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(
                    Registries.SOUND_EVENT,
                    MOD_ID
            );

    public static final Supplier<SoundEvent> RETURNING_TO_THE_SURFACE =
            SOUND_EVENTS.register(
                    "music_disc.returning_to_the_surface",
                    () -> SoundEvent.createVariableRangeEvent(
                            Fishcasting.id("music_disc.returning_to_the_surface")
                    )
            );

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CursedEntity>> CURSED =
            ENTITY_TYPES.register("cursed",
                    () -> FishcastingEntityTypes.CURSED
            );
    public static final DeferredHolder<EntityType<?>, EntityType<CursedEntity>> BLESSED =
            ENTITY_TYPES.register("blessed",
                    () -> FishcastingEntityTypes.BLESSED
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TackleBoxChairEntity>> TACKLEBOX_CHAIR =
            ENTITY_TYPES.register("tacklebox_chair",
                    () -> FishcastingEntityTypes.TACKLEBOX_CHAIR
            );

    // Yep, placeholders...
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(CURSED.get(),
                Axolotl.createAttributes().build()
        );
        event.put(BLESSED.get(),
                Villager.createAttributes().build()
        );
    }

    public static final double DEFAULT_BOBBER_RADIUS = 4.0;

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
            BuiltInRegistries.ATTRIBUTE, MOD_ID);

    public static final Holder<Attribute> BOBBER_RADIUS = ATTRIBUTES
            .register("bobber_radius", () -> new RangedAttribute(
            MOD_ID + ".attributes.bobber_radius",
            DEFAULT_BOBBER_RADIUS, 0.0, Double.MAX_VALUE).setSyncable(true));

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                LAYER_LOCATION,
                TackleBoxChairModel::createBodyLayer
        );
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // todo: custom entity renderer later
        event.registerEntityRenderer(
                CURSED.get(),
                AxolotlRenderer::new
        );
        event.registerEntityRenderer(
                BLESSED.get(),
                AxolotlRenderer::new // lol
        );

        event.registerEntityRenderer(
                TACKLEBOX_CHAIR.get(),
                 TackleBoxChairRenderer::new
        );
    }

    // from the neoforge documentation, found it after looking at the fabric documentation, god I'm just glad it works now
    public static void registerItemModelProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    FishcastingItems.SHEPHERDS_CASTING_ROD,
                    TideItemModelProperties.CAST_PROPERTY,
                    (stack, level, player, seed) -> {
                        return TideItemModelProperties.CAST_FUNCTION.call(stack, level, player, seed);
                    }
            );
        });
    }

    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                HexCapabilities.Entity.IOTA,
                TideEntityTypes.FISHING_BOBBER,
                (entity, ctx) -> new FishcastingItems.ToTideFishingHookEntity(entity)
        );
    }

    public static final DeferredRegister<CreativeModeTab> HEXLIKES_CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<CreativeModeTab> HEXLIKES_TAB =
            HEXLIKES_CREATIVE_TABS.register("main", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("fishcasting.creative_tab.title"))
                            .icon(() -> new ItemStack(FishcastingItems.LOUD_FISHING_LINE))
                            .displayItems((parameters, output) -> {
                                // Uhh I'm putting these here for convenience
                                output.accept(TideItems.CRYSTAL_FISHING_ROD);
                                output.accept(TideItems.AMETHYST_FISHING_BOBBER);
                                output.accept(TideFish.CRYSTAL_SHRIMP);
                                output.accept(TideFish.CRYSTALLINE_CARP);
                                // but I don't actually know if this is sensible
                                output.accept(FishcastingItems.SHEPHERDS_CASTING_ROD);
                                output.accept(FishcastingItems.BLESSED_FOCUS_BOBBER);
                                output.accept(FishcastingItems.LOUD_FISHING_LINE);
                                output.accept(FishcastingItems.FISHLESS_FISHING_HOOK);
                                output.accept(FishcastingItems.UNLUCKY_BAIT);
                                output.accept(FishcastingItems.TACKLEBOX_CHAIR);
                                output.accept(FishcastingItems.MESSAGE_IN_A_BOTTLE);
                                output.accept(FishcastingItems.GLASS_SHARD);
                                output.accept(FishcastingItems.CURSED);
                                output.accept(FishcastingItems.DISC);
                                output.accept(FishcastingItems.HEXLIKES_LORE_FRAGMENT);
                            })
                            .build()
            );

    public static ModContainer CONTAINER;
    public NeoforgeEntrypoint(ModContainer modContainer) {
        CONTAINER = modContainer;
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.
        Fishcasting.init();
        var modBus = modContainer.getEventBus();
        SOUND_EVENTS.register(modBus);
        ENTITY_TYPES.register(modBus);
        ATTRIBUTES.register(modBus);
        HEXLIKES_CREATIVE_TABS.register(modBus);
        modBus.addListener((RegisterEvent event) -> {
            FishcastingActions.registerActions((k, v) -> event.register(HexRegistries.ACTION, k, () -> v));
            FishcastingIotaTypes.registerTypes((k, v) -> event.register(HexRegistries.IOTA_TYPE, v, () -> k));
            Registry.register(HexArithmetics.REGISTRY, Fishcasting.id("patterns"), new FishcastingFishArithmetic());
            // Hmmm almost
            FishcastingItems.registerItems((k, v) -> event.register(Registries.ITEM, k, () -> v.apply(new Item.Properties())));
            // wowie that was less annoying than I expected thanks to hexmod yippee
        });
        // I don't know how much I have to do to register an attribute but I don't like what I've done
        modBus.addListener((EntityAttributeModificationEvent event) -> {
            event.add(EntityType.PLAYER, BOBBER_RADIUS);
        });
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            registerLayerDefinitions(event);
        });
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            registerCaps(event);
        });
        modBus.addListener((EntityAttributeCreationEvent event) -> {
            registerAttributes(event);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            registerEntityRenderers(event);
        });
        modBus.addListener((FMLClientSetupEvent event) -> {
            registerItemModelProperties(event);
        });
    }
}
