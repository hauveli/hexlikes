package hauveli.fishcasting.mixin.blessed_bobber;

import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.TideFishingRodItem;
import com.li64.tide.util.BaitUtils;
import com.llamalad7.mixinextras.sugar.Local;
import hauveli.fishcasting.Constants;
import hauveli.fishcasting.common.registries.FishcastingTags;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
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
