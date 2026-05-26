package hauveli.hexlikes.mixin;
// https://github.com/Lightning-64/Tide-2/blob/main/src/main/java/com/li64/tide/mixin/ItemsMixin.java
// holymoly what a funny thank you

import com.li64.tide.registries.TideItems;
import hauveli.hexlikes.common.paraphernalia.HexyRodItem;
import hauveli.hexlikes.common.paraphernalia.TideyFocusItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.function.Function;

@Mixin(TideItems.class)
public abstract class HexyTideyItemsMixin {
    @ModifyArg(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=crystal_fishing_rod",
                            ordinal = 0
                    ),
                    to   = @At(
                            value = "CONSTANT",
                            args = "stringValue=diamond_fishing_rod"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/li64/tide/registries/TideItems;register(Ljava/lang/String;Ljava/util/function/Function;)Lnet/minecraft/world/item/Item;"
            ),
            index = 1
    )
    private static Function<Item.Properties, Item> replaceCrystalRod(
            Function<Item.Properties, Item> original
    ) {
        // make it hexy, which includes removing the durability. Sorry hardcore durability enjoyers!
        // TODO: make the durability-ness configurable.
        // Dis-allow casting at 0 or 1 durability.
        return props -> new HexyRodItem(2, 216, props);
    }

    @ModifyArg(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=amethyst_fishing_bobber",
                            ordinal = 0
                    ),
                    to   = @At(
                            value = "CONSTANT",
                            args = "stringValue=echo_fishing_bobber"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/li64/tide/registries/TideItems;register(Ljava/lang/String;Ljava/util/function/Function;)Lnet/minecraft/world/item/Item;"
            ),
            index = 1
    )
    private static Function<Item.Properties, Item> replaceAmethystBobber(Function<Item.Properties, Item> original) {
        return props -> new TideyFocusItem(props);
    }
}
