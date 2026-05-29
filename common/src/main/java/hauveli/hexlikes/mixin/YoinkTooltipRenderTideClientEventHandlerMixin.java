package hauveli.hexlikes.mixin;


import com.li64.tide.events.TideClientEventHandler;
import hauveli.hexlikes.common.registries.HexlikesItemsJ;
import hauveli.hexlikes.common.registries.HexlikesTags;
import hauveli.hexlikes.common.paraphernalia.TideyFocusItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TideClientEventHandler.class)
public class YoinkTooltipRenderTideClientEventHandlerMixin {

    @Inject(
            method = "onTooltipRender",
            at = @At("TAIL")
    )
    private static void hexlikes$onTooltipRender(
            ItemStack stack,
            List<Component> lines,
            CallbackInfo ci
    ) {
        if (stack.is(HexlikesTags.LUCK_TWEAKING_BOBBERS)) {
            lines.add(Component.translatable("text.hexlikes.bobber_tooltip.blessed_bonus").withStyle(ChatFormatting.GOLD));
        }
        // Yes, really. The bell ring is more important to me than the luck tweak, but with the cost of this bobber, I thought having both was justified
        // and neither affects (positively) hexcasting afaik. the 0.1% luck tweak is likely detrimental in some scenarios, too... but it is funny, and allows
        // (technically) access to loot from loot pools which would otherwise be impossible to reach depending on the server
        // Maybe I should make the luck tweak only work when void-fishing, though....
        // I could make it be a hidden bonus too, to be cheeky.
        if (stack.is(HexlikesItemsJ.BLESSED_FOCUS_BOBBER)) {
            lines.add(Component.translatable("text.hexlikes.bobber_tooltip.sound_bonus").withStyle(ChatFormatting.GOLD));
        }
        if (stack.getItem() instanceof TideyFocusItem) {
            lines.add(Component.translatable("text.hexlikes.bobber_tooltip.focus_bonus").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
