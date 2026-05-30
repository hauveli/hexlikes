package hauveli.fishcasting.mixin.blessed_bobber;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static hauveli.fishcasting.Fishcasting.random;
import static hauveli.fishcasting.common.registries.FishcastingItems.BLESSED_FOCUS_BOBBER;

@Mixin(TideFishingHook.class)
public abstract class BlessedBobberJingleTideFishingHookMixin {

    @Shadow
    protected ItemStack rod;
    @Shadow
    public abstract ItemStack getBobber();
    @Shadow
    public abstract Player getPlayerOwner();

    @Inject(
            method = "catchingFish",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/li64/tide/data/fishing/mediums/FishingMedium;onFishBite(Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook;)V"
            )
    )
    private void fishcasting$blessedBobberJingle(BlockPos pos, CallbackInfo ci) {
        // When a fish first touches the hook
        if (this.getBobber().is(BLESSED_FOCUS_BOBBER)) {
            this.getPlayerOwner().level().playSound(
                    null, getPlayerOwner().blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.MASTER, 1.5f,
                    1.0f - (random.nextFloat() - random.nextFloat()) * 0.1f
            );
        }
    }
}
