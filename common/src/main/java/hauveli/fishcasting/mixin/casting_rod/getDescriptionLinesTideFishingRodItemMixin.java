package hauveli.fishcasting.mixin.casting_rod;

import com.li64.tide.registries.items.TideFishingRodItem;
import hauveli.fishcasting.common.FishcastingConfig;
import hauveli.fishcasting.common.registries.FishcastingItems;
import hauveli.fishcasting.common.paraphernalia.HexyRodItem;
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
    private static void fishcasting$addRodTooltip(
            ItemStack stack, CallbackInfoReturnable<List<Component>> cir
    ) {
        List<Component> components = new ArrayList<>(cir.getReturnValue());

        if (stack.is(FishcastingItems.SHEPHERDS_CASTING_ROD)) {
            components.add(Component.translatable("text.fishcasting.rod_tooltip.shepherds_bonus").withStyle(ChatFormatting.GOLD));
        }
        if (stack.getItem() instanceof HexyRodItem) {
            if (FishcastingConfig.CONFIG.castingIsMomentary()) {
                components.add(Component.translatable("text.fishcasting.rod_tooltip.casting_bonus.momentary").withStyle(ChatFormatting.LIGHT_PURPLE));
            } else if (FishcastingConfig.CONFIG.castingIsOffhandOnly()) {
                components.add(Component.translatable("text.fishcasting.rod_tooltip.casting_bonus.offhand").withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                // fallback just in case?
                components.add(Component.translatable("text.fishcasting.rod_tooltip.casting_bonus").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

        cir.setReturnValue(List.copyOf(components));
    }
}
