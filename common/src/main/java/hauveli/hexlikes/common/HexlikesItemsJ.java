package hauveli.hexlikes.common;

import at.petrak.hexcasting.api.addldata.ItemDelegatingEntityIotaHolder;
import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.FishingHookItem;
import hauveli.hexlikes.common.paraphernalia.LoudFishingLineItem;
import hauveli.hexlikes.common.chair.TackleBoxChairItem;
import hauveli.hexlikes.common.gacha.GachaBottleItem;
import hauveli.hexlikes.common.paraphernalia.HexyRodItem;
import hauveli.hexlikes.common.paraphernalia.TideyFocusItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static hauveli.hexlikes.Constants.MOD_ID;

// https://github.com/YukkuriC/HexOverpowered/blob/1.21/common/src/main/java/io/yukkuric/hexop/actions/HexOPActions.kt
// code adapted from the above
public class HexlikesItemsJ {

    // Oh my god thank you hexmod for this api I was going to pull my hair out if I had to re-implement
    // the TideFishingHook class and the IotaHolder thingermabob
    public static class ToTideFishingHookEntity extends ItemDelegatingEntityIotaHolder {
        public ToTideFishingHookEntity(TideFishingHook entity) {
            super(() -> {
                        ItemStack bobber = entity.getBobber();
                        if (bobber.getItem() instanceof TideyFocusItem) {
                            return bobber;
                        }
                        return ItemStack.EMPTY;
                    },
                    stack -> {
                    if (stack.getItem() instanceof TideyFocusItem) {
                        CustomRodManager.setBobber(
                                HookAccessor.getHook(
                                        entity.getPlayerOwner()
                                ).rod(),
                                stack
                        );
                    }
                    });
        }
    }

    private static final Map<ResourceLocation, Function<Item.Properties, Item>> CACHED =
            new HashMap<>();

    public static final HexyRodItem SHEPHERDS_CASTING_ROD = new HexyRodItem(3, 0,
            (new Item.Properties())
            .rarity(Rarity.UNCOMMON)
    );
    public static final TideyFocusItem BLESSED_FOCUS_BOBBER = new TideyFocusItem((new Item.Properties())
            .rarity(Rarity.UNCOMMON) // stacks to 1 by default, maybe I should move rarity in there too?
    );
    public static final Item UNLUCKY_BAIT = new Item((new Item.Properties()));
    public static final LoudFishingLineItem LOUD_FISHING_LINE = new LoudFishingLineItem((new Item.Properties()));
    public static final FishingHookItem FISHLESS_FISHING_HOOK = new FishingHookItem(new Item.Properties(),
                        "item.hexlikes.hookless_fishing_hook.desc");
    public static final Item DISC = new Item((new Item.Properties())
            .stacksTo(1)
            .rarity(Rarity.UNCOMMON)
            .jukeboxPlayable(HexlikesSoundsJ.RETURNING_TO_THE_SURFACE_JUKEBOX));
    public static final Item CURSED = new Item(
            (new Item.Properties())
                    .rarity(Rarity.UNCOMMON)
                    .fireResistant());
    public static final Item MESSAGE_IN_A_BOTTLE = new GachaBottleItem(
            (new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );
    public static final Item TACKLEBOX_CHAIR = new TackleBoxChairItem(
            (new Item.Properties().stacksTo(1))
    );

    public static final Item GLASS_SHARD = new Item(
            (new Item.Properties().stacksTo(16))
    );
    public static final Item HEXLIKES_LORE_FRAGMENT = new HexlikesLoreFragment(
            (new Item.Properties().stacksTo(1))
    );
    // @devs if you're reading this please feel free to add anything from this mod
    static {
        wrap("shepherds_casting_rod", props -> SHEPHERDS_CASTING_ROD); // +1 luck instead of jingle
        wrap("blessed_focus_bobber", props -> BLESSED_FOCUS_BOBBER); // Prevents hostile mobs from approaching when bobber is in water
        wrap("unlucky_bait", props -> UNLUCKY_BAIT); // Reduces amount of bites to 0
        wrap("loud_fishing_line", props -> LOUD_FISHING_LINE); // Prevents creepers from approaching, and plays random jingle sounds
        wrap("hookless_fishing_hook", props -> FISHLESS_FISHING_HOOK); // Passes through entities when cast, useful if you have an entity you can't move in the way of fishing.
        wrap("music_disc_returning_to_the_surface", props -> DISC); // Prevents hostile mobs from approaching when bobber is in water
        wrap("cursed", props -> CURSED); // Fish that enables hexcasting progression via flay mind
        wrap("blessed", props -> new Item(props.rarity(Rarity.EPIC))); // Does not exist yet but I'm putting it here anyway
        wrap("message_in_a_bottle", props -> MESSAGE_IN_A_BOTTLE); // Fish that enables hexcasting progression via flay mind
        wrap("tacklebox_chair", props -> TACKLEBOX_CHAIR); // Fish that enables hexcasting progression via flay mind
        wrap("glass_shard", props -> GLASS_SHARD); // Fish that enables hexcasting progression via flay mind
        wrap("fishy_fragment", props -> HEXLIKES_LORE_FRAGMENT); // Fish that enables hexcasting progression via flay mind
    }


    public static void registerItems(
            BiConsumer<ResourceLocation, Function<Item.Properties, Item>> handler
    ) {
        for (Map.Entry<ResourceLocation, Function<Item.Properties, Item>> entry
                : CACHED.entrySet()) {

            handler.accept(entry.getKey(), entry.getValue());
        }
    }

    private static Function<Item.Properties, Item> wrap(
            String name,
            Function<Item.Properties, Item> factory
    ) {
        ResourceLocation key =
                ResourceLocation.fromNamespaceAndPath(MOD_ID, name);

        CACHED.put(key, factory);

        return factory;
    }
}