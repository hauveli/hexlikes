package hauveli.fishcasting.mixin.loud_line;

import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import hauveli.fishcasting.common.registries.FishcastingTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static hauveli.fishcasting.Constants.random;

@Mixin(Phantom.class)
public class AntiPhantomMixin {
    @Unique
    private static final int radius = 11; // charged creeper is 10 blocks ish

    @Unique
    private boolean playerShouldPacify(Player player) {
        var maybeHook = HookAccessor.getHook(player);
        if (maybeHook == null) {
            return false;
        }
        return maybeHook.getLine().is(FishcastingTags.MOB_PACIFYING_LINES);
    }

    @Unique
    private boolean targetShouldPacify(Phantom phantom) {
        if (phantom.getTarget() instanceof Player player) {
            return playerShouldPacify(player);
        }
        return false;
    }
    @Unique
    private void lobotomize(Phantom phantom, LivingEntity livingEntity) {
        // other players may be nearby, so let those players be chased
        phantom.setAggressive(false);
        phantom.setTarget(null);
        // Select some random point in a partially hollowed out sphere to move to
        phantom.getNavigation()
                .moveTo(
                        livingEntity.position().x + random.nextDouble(6, 16) * random.nextInt(-1,1),
                        livingEntity.position().y + random.nextDouble(6, 16), // probably go straight up?
                        livingEntity.position().z + random.nextDouble(6, 16) * random.nextInt(-1,1),
                        1
                );
    }

    @Unique
    private void lobotomize(Phantom phantom, Player player) {
        // other players may be nearby, so let those players be chased
        if (phantom.getTarget() == player) {
            lobotomize(phantom, (LivingEntity) player);
        }
    }

    @Unique
    private void stopAnnoyingMe(CallbackInfo ci) {
        Phantom phantom = (Phantom)(Object)this;
        if (targetShouldPacify(phantom)) { // this is the most likely scenario so I am including a check for this at the top
            lobotomize(phantom, phantom);
            ci.cancel();
            return;
        }

        AABB box = phantom.getBoundingBox().inflate(radius);

        for (Player player : phantom.level().getEntitiesOfClass(Player.class, box)) {
            if (playerShouldPacify(player)) {
                lobotomize(phantom, player);
                ci.cancel();
                return;
            }
        }
    }

    // phantoms suck, I don't care. Usually a max of 8 around, so this will check at most 8 times per second (per player?)
    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void phantomsNeedToStop(CallbackInfo ci) {
        Phantom phantom = (Phantom) (Object) this;
        if (phantom.tickCount % 20 != 0) {
            return;
        }
        stopAnnoyingMe(ci);
    }
}
