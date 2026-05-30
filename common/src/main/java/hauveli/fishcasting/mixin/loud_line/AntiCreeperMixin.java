package hauveli.fishcasting.mixin.loud_line;

import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import hauveli.fishcasting.common.registries.FishcastingTags;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static hauveli.fishcasting.Fishcasting.random;

@Mixin(Creeper.class)
public abstract class AntiCreeperMixin {

    @Shadow
    private int swell;
    @Shadow
    private int oldSwell;
    @Final
    @Shadow
    private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;

    @Unique
    private static final int radius = 11; // charged creeper is 10 blocks ish
    @Unique
    private static final int radiusSqr = radius * radius;
    @Unique
    private static final int creeperExplodeDelay = 30;

    @Unique
    private boolean playerShouldPacify(Player player) {
        var maybeHook = HookAccessor.getHook(player);
        if (maybeHook == null) {
            return false;
        }
        return maybeHook.getLine().is(FishcastingTags.MOB_PACIFYING_LINES);
    }

    @Unique
    private boolean targetShouldPacify(Creeper creeper) {
        if (creeper.getTarget() instanceof Player player) {
            return playerShouldPacify(player);
        }
        return false;
    }

    @Unique
    private int lastPacificationTime = -30; // creeper may spawn on top of the player.

    @Unique
    private void targetPlayerLobotomizes(Creeper creeper, LivingEntity target) {
        this.lastPacificationTime = creeper.tickCount;
        if (this.oldSwell > 0) {
            this.oldSwell--;
        }
        if (this.swell > 0) {
            this.swell--;
        }
        creeper.getEntityData().set(DATA_IS_IGNITED, false);
        creeper.setSwellDir(-1);
        creeper.getNavigation()
                .moveTo(
                        target.position().x + random.nextDouble(6, 16) * random.nextInt(-1,1),
                        target.position().y + random.nextDouble(6, 16) * random.nextInt(-1,1),
                        target.position().z + random.nextDouble(6, 16) * random.nextInt(-1,1),
                        1
                );
    }

    @Inject(method = "isIgnited", at = @At("RETURN"), cancellable = true)
    private void dontGoBoomTooSoon(CallbackInfoReturnable<Boolean> cir) {
        Creeper creeper = (Creeper)(Object)this;
        if (this.lastPacificationTime > creeper.tickCount - creeperExplodeDelay) {
            if (this.oldSwell > 0) {
                this.oldSwell--;
            }
            if (this.swell > 0) {
                this.swell--;
            }
            creeper.setSwellDir(-1);
            cir.setReturnValue(false);
        } else {
            // this is the most likely scenario so I am including a check for this at the top
            if (creeper.getTarget() != null && targetShouldPacify(creeper)) {
                targetPlayerLobotomizes(creeper, creeper.getTarget());
                cir.setReturnValue(false);
                return;
            }
            // A nearby player might be holding it rather than the target, check and if so move away from that player, who is not the target
            AABB box = creeper.getBoundingBox().inflate(radius);
            for (Player player : creeper.level().getEntitiesOfClass(Player.class, box)) {
                if (player.distanceToSqr(creeper) > radiusSqr)
                    continue;
                if (playerShouldPacify(player)) {
                    targetPlayerLobotomizes(creeper, player);
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }

    @Inject(method = "explodeCreeper()V", at = @At("HEAD"), cancellable = true)
    private void stopAnnoyingMe(CallbackInfo ci) {
        Creeper creeper = (Creeper)(Object)this;
        if (!creeper.isAlive()) {
            return;
        }
        if (this.lastPacificationTime > creeper.tickCount - creeperExplodeDelay) {
            ci.cancel();
        }
    }
}
