package hauveli.fishcasting.mixin;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.util.BaitUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TideFishingHook.class)
public abstract class UnluckyBaitTideFishingHookMixin {
    @Shadow
    private int timeUntilLured;
    @Shadow
    protected ItemStack rod;

    @Inject(method = "catchingFish", at = @At("TAIL"))
    private void fishcasting$setHugeLureTime(BlockPos pos, CallbackInfo ci) {
        if (shouldPreventFish()) {
            this.timeUntilLured = 999;
        }
    }

    @Unique
    private boolean shouldPreventFish() {
        if (BaitUtils.isHoldingBait(rod)) {
            if (BaitUtils.getBaitSpeed(BaitUtils.getPrimaryBait(rod)) <= -13)  {
                return true;
            }
        }
        return false;
    }
}
