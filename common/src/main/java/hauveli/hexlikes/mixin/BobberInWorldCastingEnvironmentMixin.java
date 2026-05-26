package hauveli.hexlikes.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import hauveli.hexlikes.common.paraphernalia.TideyFocusItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static hauveli.hexlikes.hexcasting.actions.OpGetCatchesBobberJ.getFishingHook;

@Mixin(CastingEnvironment.class)
public class BobberInWorldCastingEnvironmentMixin {

    // A little bit ugly when nested but whatever
    @Inject(
            method = "assertEntityInRange",
            at = @At(
                    value = "HEAD",
                    target = "Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;isVecInWorld(Lnet/minecraft/world/phys/Vec3;)Z",
                    ordinal = 0
            ),
            cancellable = true)
    private void hexlikes$bobberOutsideWorld(Entity e, CallbackInfo ci) {
        // TODO: config here maybe to escape early if users do not like this idea
        // allows entities directly hooked by the caster to be accessed if the bobber extends their ambit
        CastingEnvironment env = (CastingEnvironment) (Object) this;
        if (env.getCastingEntity() instanceof ServerPlayer serverPlayer) {
            var maybeHook = HookAccessor.getHook(serverPlayer);
            if (maybeHook != null
                    && maybeHook.getBobber().getItem() instanceof TideyFocusItem
                    && serverPlayer.level().dimension() == maybeHook.level().dimension()) {
                if (maybeHook.getHookedIn() == e
                        || maybeHook == getFishingHook(e)) {
                    ci.cancel();
                }
            }
        }
    }
}
