package hauveli.hexlikes.mixin;

import com.li64.tide.registries.items.TideFishingRodItem;
import hauveli.hexlikes.common.HexlikesItemsJ;
import hauveli.hexlikes.common.paraphernalia.HexyRodItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TideFishingRodItem.class)
public class getDescriptionLinesTideFishingRodItemMixin {

    @Inject(
            method = "getDescriptionLines",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void hexlikes$addRodTooltip(
            ItemStack stack, CallbackInfoReturnable<List<Component>> cir
    ) {
        List<Component> components = new ArrayList<>(cir.getReturnValue());

        if (stack.is(HexlikesItemsJ.SHEPHERDS_CASTING_ROD)) {
            components.add(Component.translatable("text.hexlikes.rod_tooltip.shepherds_bonus").withStyle(ChatFormatting.GOLD));
        }
        if (stack.getItem() instanceof HexyRodItem) {
            components.add(Component.translatable("text.hexlikes.rod_tooltip.casting_bonus").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        cir.setReturnValue(List.copyOf(components));
    }
}
