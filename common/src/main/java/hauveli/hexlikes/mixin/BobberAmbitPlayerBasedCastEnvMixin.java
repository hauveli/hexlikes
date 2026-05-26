package hauveli.hexlikes.mixin;

import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import hauveli.hexlikes.platform.Services;
import hauveli.hexlikes.common.paraphernalia.TideyFocusItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerBasedCastEnv.class)
public abstract class BobberAmbitPlayerBasedCastEnvMixin {


    @Shadow
    public abstract @Nullable LivingEntity getCastingEntity();

    // A little bit ugly when nested but whatever
    @Inject(method = "isVecInRangeEnvironment", at = @At("RETURN"), cancellable = true)
    private void hexlikes$vectorInRange(Vec3 vec, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return; // if it's already true, which it will almost always be, return asap to avoid wasting time
        }
        if (this.getCastingEntity() instanceof ServerPlayer serverPlayer) {
            var maybeHook = HookAccessor.getHook(serverPlayer);
            if (maybeHook != null
                    && maybeHook.getBobber().getItem() instanceof TideyFocusItem
                    && serverPlayer.level().dimension() == maybeHook.level().dimension()) {
                // god I'm such a fat chud noob it took me at least an hour to figure out that this was an ok ish way to do this
                double ambitBobberRadius = serverPlayer.getAttributeValue(Services.PLATFORM.getHolderForBobberRadius());
                cir.setReturnValue(
                        vec.distanceToSqr(maybeHook.position()) <= ambitBobberRadius * ambitBobberRadius + 0.00000000001
                );
            }
        }
    }
}
