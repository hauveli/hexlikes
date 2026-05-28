package hauveli.hexlikes.mixin;

import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.TideFishingRodItem;
import com.li64.tide.util.BaitUtils;
import hauveli.hexlikes.common.registries.HexlikesTags;
import hauveli.hexlikes.common.paraphernalia.HexyRodItem;
import hauveli.hexlikes.common.paraphernalia.TideyFocusItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
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

import java.util.Random;

import static hauveli.hexlikes.Constants.MOD_ID;
import static hauveli.hexlikes.common.registries.HexlikesItemsJ.*;

@Mixin(TideFishingHook.class)
public abstract class CustomBehaviorTideFishingHookMixin {

    @Shadow
    private int timeUntilLured;
    @Shadow
    protected ItemStack rod;

    @Shadow
    public abstract TideFishingRodItem getRodItem();


    /*
    Vec3 bobberPos = activeHook.getPosition(0);
                if (bobberItemStack != null && bobberItemStack.getItem() instanceof TideyFocusItem) {
        // NOOOOOO BOOOOOBBEEEERTTTTT
        //);
    }

     */

    @Inject(method = "startRetrieving", at = @At("HEAD"))
    public void retrieve(CallbackInfo ci) {
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
    }

    @Inject(
            method = "catchingFish",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/li64/tide/data/fishing/mediums/FishingMedium;onFishBite(Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook;)V"
            )
    )
    private void hexlikes$blessedBobberJingle(BlockPos pos, CallbackInfo ci) {
        // When a fish first touches the hook
        if (this.getBobber().is(BLESSED_FOCUS_BOBBER)) {
            this.getPlayerOwner().level().playSound(
                    null, getPlayerOwner().blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.MASTER, 1.5f,
                    1.0f - (this.random.nextFloat() - this.random.nextFloat()) * 0.1f
            );
        }
    }

    @Inject(method = "catchingFish", at = @At("TAIL"))
    private void hexlikes$setHugeLureTime(BlockPos pos, CallbackInfo ci) {
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

    @Shadow
    private Entity hookedIn;

    @Shadow
    public abstract ItemStack getHook();

    @Shadow
    public abstract ItemStack getLine();
    @Shadow
    public abstract Player getPlayerOwner();

    @Shadow
    public abstract FishingContext getContext();

    @Shadow
    public abstract ItemStack getBobber();

    @Unique
    private Random random = new Random();
    // Note: this limits the ding-y-ness, perhaps globally.
    // A little bit funny to have this type of effect.
    @Unique
    private Long nextDing = 0L;

    @Unique
    private static final TagKey<EntityType<?>> PACIFIED_BY_LOUD_LINES =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "pacified_by_loud_lines")
            );

    @Unique
    private int radius = 6;
    @Unique
    private int maxDistanceSq = radius * radius;

    // Implicitly, this is called on each tick as the first thing that is called if a hook is equipped
    // I should probably do this in a smarter way, but whatever tee-hee
    @Inject(method = "getRodItem", at = @At("HEAD"))
    private void hexlikes$antiAnnoyingBeam(CallbackInfoReturnable<TideFishingRodItem> cir) {
        if (this.getLine().is(HexlikesTags.MOB_PACIFYING_LINES)) {
            Player player = this.getPlayerOwner();
            if (player.level() instanceof ClientLevel) {
                return; // abort!!!
            }
            Level level = player.level();
            if (level.getGameTime() > nextDing) {
                // v + (1-t)u
                // u = v - v'
                Vec3 midwayPoint = player.position()
                        .add(
                                this.getContext().pos()
                                        .subtract(player.position()).scale(0.5));
                level.playSound(
                        null, midwayPoint.x, midwayPoint.y, midwayPoint.z,
                        SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.5f,
                        1.8f - (random.nextFloat() - random.nextFloat()) * 0.1f
                        // Higher pitch hopefully differentiates it from the other type
                );
                nextDing = level.getGameTime() + random.nextLong(60, 666);
            }

            // get every mob which should be pacified and pacify it.
            // god I hate being blown up randomly when chilling
            // god I hate randomly being slapped by phantoms
            for (var entity : level.getEntities(
                    player,
                    player.getBoundingBox().inflate(radius),
                    entity -> entity.isAlive() && // alive because otherwise some irrelevant entities may be checked
                            entity.getType().is(PACIFIED_BY_LOUD_LINES))) {

                if (entity.distanceToSqr(player) > maxDistanceSq)
                    continue;

                if (entity instanceof Creeper creeper) {
                    // todo: make it explode if the player hits it
                    creeper.setSwellDir(-1);
                }

                // Lobotomize
                if (entity instanceof Mob mob) {
                    // other players may be nearby, so let those players be chased
                    if (mob.getTarget() == player) {
                        mob.setAggressive(false);
                        mob.setTarget(null);
                        // Select some random point in a partially hollowed out sphere to move to
                        mob.getNavigation()
                                .moveTo(
                                        player.position().x + random.nextDouble(6, 16) * random.nextInt(-1,1),
                                        player.position().y + random.nextDouble(6, 16) * random.nextInt(-1,1),
                                        player.position().z + random.nextDouble(6, 16) * random.nextInt(-1,1),
                                        1
                                );
                    }
                }
            }
        }
    }

    @Inject(method = "setHookedEntity", at = @At("HEAD"), cancellable = true)
    private void hexlikes$smelly(CallbackInfo ci) {
        if (this.getHook().is(HexlikesTags.NO_ENTITY_COLLISION_HOOK)) {
            this.hookedIn = null;
            ci.cancel();
        }
    }

}
