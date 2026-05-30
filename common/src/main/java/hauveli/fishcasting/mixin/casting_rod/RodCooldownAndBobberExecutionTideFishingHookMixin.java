package hauveli.fishcasting.mixin.casting_rod;

import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.TideFishingRodItem;
import com.li64.tide.util.BaitUtils;
import hauveli.fishcasting.Constants;
import hauveli.fishcasting.common.FishcastingConfig;
import hauveli.fishcasting.common.paraphernalia.HexyRodItem;
import hauveli.fishcasting.common.paraphernalia.TideyFocusItem;
import hauveli.fishcasting.common.registries.FishcastingTags;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static hauveli.fishcasting.Constants.random;
import static hauveli.fishcasting.common.registries.FishcastingItems.BLESSED_FOCUS_BOBBER;

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
                            FishcastingConfig.CONFIG.getCooldownAfterFishingMinigame());
        }
    }
}
