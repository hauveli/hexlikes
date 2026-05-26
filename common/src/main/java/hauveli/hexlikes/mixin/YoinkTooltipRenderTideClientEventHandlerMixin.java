package hauveli.hexlikes.mixin;


import com.li64.tide.events.TideClientEventHandler;
import hauveli.hexlikes.common.HexlikesTags;
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
        if (stack.getItem() instanceof TideyFocusItem) {
            lines.add(Component.translatable("text.hexlikes.bobber_tooltip.focus_bonus").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
