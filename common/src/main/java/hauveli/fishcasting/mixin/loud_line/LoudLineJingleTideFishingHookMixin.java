package hauveli.fishcasting.mixin.loud_line;

import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.TideFishingRodItem;
import hauveli.fishcasting.common.registries.FishcastingTags;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static hauveli.fishcasting.Fishcasting.random;

@Mixin(TideFishingHook.class)
public abstract class LoudLineJingleTideFishingHookMixin {

    @Shadow
    protected ItemStack rod;
    @Shadow
    public abstract Player getPlayerOwner();
    @Shadow
    public abstract ItemStack getLine();
    @Shadow
    public abstract FishingContext getContext();

    // I think I understand now, and @Unique is treated as a field in the instance? which is great!
    @Unique
    private Long nextDing = 0L;
    // Implicitly, this is called on each tick as the first thing that is called if a hook is equipped
    // todo: figure out a less costly way of doing this, maybe by doing a mixin into the creeper and phantom classes?
    // I guess doing a radius check to see if players are within the phantom's target/creeper's radius is cheaper?
    @Inject(method = "getRodItem", at = @At("HEAD"))
    private void fishcasting$antiAnnoyingBeam(CallbackInfoReturnable<TideFishingRodItem> cir) {
        if (this.getLine().is(FishcastingTags.MOB_PACIFYING_LINES)) {
            Player player = this.getPlayerOwner();
            if (player.level() instanceof ClientLevel) {
                return; // abort!!!
            }
            Level level = player.level();
            if (level.getGameTime() % 10 != 0) {
                return; // run this as infrequently as possible while still making it work
            }
            // a little convoluted just to play a jingle, maybe...
            // v + (1-t)u
            // u = v - v'
            Vec3 offsetToMidwayPoint = this.getContext().pos()
                    .subtract(player.position()).scale(0.5);
            Vec3 midwayPoint = player.position().add(offsetToMidwayPoint);
            if (level.getGameTime() > nextDing) {
                level.playSound(
                        null, midwayPoint.x, midwayPoint.y, midwayPoint.z,
                        SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.5f,
                        1.8f - (random.nextFloat() - random.nextFloat()) * 0.1f
                        // Higher pitch hopefully differentiates it from the other type
                );
                nextDing = level.getGameTime() + random.nextLong(20 * 2, 20 * 8);
            }
        }
    }
}
