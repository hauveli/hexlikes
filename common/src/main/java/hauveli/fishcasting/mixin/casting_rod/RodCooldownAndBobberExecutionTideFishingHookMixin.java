package hauveli.fishcasting.mixin.casting_rod;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.TideFishingRodItem;
import hauveli.fishcasting.Fishcasting;
import hauveli.fishcasting.common.FishcastingConfig;
import hauveli.fishcasting.common.paraphernalia.HexyRodItem;
import hauveli.fishcasting.common.paraphernalia.TideyFocusItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static hauveli.fishcasting.Fishcasting.CONFIG;

@Mixin(TideFishingHook.class)
public abstract class RodCooldownAndBobberExecutionTideFishingHookMixin {

    @Shadow
    public abstract TideFishingRodItem getRodItem();
    @Shadow
    public abstract ItemStack getHook();
    @Shadow
    public abstract Player getPlayerOwner();
    @Shadow
    public abstract ItemStack getBobber();

    // executed bobber if it is attach but also applies a cooldown if we did retrieve it.
    @Inject(method = "startRetrieving", at = @At("HEAD"))
    public void retrieve(CallbackInfo ci) {
        // For executing bobbert
        ItemStack bobberItemStack = this.getBobber();
        if (bobberItemStack != null
                && bobberItemStack.getItem() instanceof TideyFocusItem
                && this.getRodItem() instanceof HexyRodItem castingRod) {
            Player player = this.getPlayerOwner();
            //if (player.level().isClientSide()) return;
            castingRod.executeBobber(player.level(),
                    player, player.getUsedItemHand(),
                    bobberItemStack,
                    ((TideFishingHook)(Object)this).position());
        }
        // For adding a cooldown to the rod, I would like a less jank way at some point...
        if (this.getRodItem() instanceof HexyRodItem hexyRodItem) {
            //hexyRodItem.setTicksSinceFishingMinigame(0);
            this.getPlayerOwner()
                    .getCooldowns()
                    .addCooldown(
                            this.getRodItem(),
                            CONFIG.gameplay.getCooldownAfterFishingMinigame());
        }
    }
}
