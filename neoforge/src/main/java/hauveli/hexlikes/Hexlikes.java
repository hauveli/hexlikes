package hauveli.hexlikes;


import at.petrak.hexcasting.common.lib.HexCreativeTabs;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.forge.cap.HexCapabilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.li64.tide.Tide;
import com.li64.tide.client.TideItemModelProperties;
import com.li64.tide.registries.TideEntityTypes;
import com.li64.tide.registries.TideItems;
import hauveli.hexlikes.common.chair.TackleBoxChairEntity;
import hauveli.hexlikes.common.chair.TackleBoxChairModel;
import hauveli.hexlikes.common.chair.TackleBoxChairRenderer;
import hauveli.hexlikes.common.HexlikesEntityTypes;
import hauveli.hexlikes.hexcasting.HexlikesActionsJ;
import hauveli.hexlikes.common.CursedEntity;
import hauveli.hexlikes.common.HexlikesItemsJ;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;
import java.util.function.Supplier;

import static hauveli.hexlikes.Constants.MOD_ID;
import static hauveli.hexlikes.common.chair.TackleBoxChairModel.LAYER_LOCATION;

@Mod(MOD_ID)
public class Hexlikes {

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
                            ResourceLocation.fromNamespaceAndPath(
                                    MOD_ID,
                                    "music_disc.returning_to_the_surface"
                            )
                    )
            );

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CursedEntity>> CURSED =
            ENTITY_TYPES.register("cursed",
                    () -> HexlikesEntityTypes.CURSED
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TackleBoxChairEntity>> TACKLEBOX_CHAIR =
            ENTITY_TYPES.register("tacklebox_chair",
                    () -> HexlikesEntityTypes.TACKLEBOX_CHAIR
            );


    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(CURSED.get(),
                Axolotl.createAttributes().build()
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
        event.registerEntityRenderer(
                CURSED.get(),
                AxolotlRenderer::new
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
                    HexlikesItemsJ.SHEPHERDS_CASTING_ROD,
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
                (entity, ctx) -> new HexlikesItemsJ.ToTideFishingHookEntity(entity)
        );
    }

    public static final DeferredRegister<CreativeModeTab> HEXLIKES_CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<CreativeModeTab> HEXLIKES_TAB =
            HEXLIKES_CREATIVE_TABS.register("main", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("hexlikes.creative_tab.title"))
                            .icon(() -> new ItemStack(HexlikesItemsJ.LOUD_FISHING_LINE))
                            .displayItems((parameters, output) -> {
                                output.accept(TideItems.CRYSTAL_FISHING_ROD);
                                output.accept(TideItems.AMETHYST_FISHING_BOBBER);
                                output.accept(HexlikesItemsJ.SHEPHERDS_CASTING_ROD);
                                output.accept(HexlikesItemsJ.BLESSED_FOCUS_BOBBER);
                                output.accept(HexlikesItemsJ.LOUD_FISHING_LINE);
                                output.accept(HexlikesItemsJ.FISHLESS_FISHING_HOOK);
                                output.accept(HexlikesItemsJ.UNLUCKY_BAIT);
                                output.accept(HexlikesItemsJ.TACKLEBOX_CHAIR);
                                output.accept(HexlikesItemsJ.MESSAGE_IN_A_BOTTLE);
                                output.accept(HexlikesItemsJ.GLASS_SHARD);
                                output.accept(HexlikesItemsJ.CURSED);
                                output.accept(HexlikesItemsJ.DISC);
                                output.accept(HexlikesItemsJ.HEXLIKES_LORE_FRAGMENT);
                            })
                            .build()
            );

    public Hexlikes(ModContainer modContainer) {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        var modBus = modContainer.getEventBus();
        SOUND_EVENTS.register(modBus);
        ENTITY_TYPES.register(modBus);
        ATTRIBUTES.register(modBus);
        HEXLIKES_CREATIVE_TABS.register(modBus);
        modBus.addListener((RegisterEvent event) -> {
            HexlikesActionsJ.registerActions((k, v) -> event.register(HexRegistries.ACTION, k, () -> v));
            // Hmmm almost
            HexlikesItemsJ.registerItems((k, v) -> event.register(Registries.ITEM, k, () -> v.apply(new Item.Properties())));
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
        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello world!");
    }
}
