package hauveli.hexlikes.common;

import com.li64.tide.data.FishLengthHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

import static com.li64.tide.data.item.TideItemData.FISH_LENGTH;
import static hauveli.hexlikes.common.registries.HexlikesItemsJ.DISC;

// melted axolotl fish
public class CursedEntity extends Axolotl {
    // TODO:
    // Some ideas that I may or may not bother to implemeent if ever
    // Make this thing accomplish the following:
    // Have a 3D model and be a useless fatty that just lays around and does nothing but eat (including poisonous potatoes)
    // possibly move on timescales of one block per one real life day
    // should rotate very slowly, too.
    // saturation level such that it becomes fatter if fed food
    // at maximum saturation, become able to be mind flayed at which point it bursts into an explosion
    // and DIES
    // a cursed existence.
    // also should be able to be turned into a yummy treat
    // should be bucketable if it is not too big
    // uses saturation to heal
    // becomes smaller when saturation decreases
    // drowns in water
    // sinks if saturation level above 0 (maybe this is how I can make it not just despawn if spawned naturally?)
    // should have a chance of being fished up from the void (any)
    // more stuff maybe.
    // should be one of only two mobs at most that this addon adds.
    public CursedEntity(EntityType<? extends Axolotl> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false);
        this.setCanPickUpLoot(false);
        this.setInvulnerable(true);
        this.setSpeed(0.01f);
    }

    @Override
    public int getHeadRotSpeed() {
        return 1;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return relevantDamageSource(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        doAllaySpawnOnLightningHitMob(this, source);
        return super.hurt(source, amount);
    }

    public static boolean relevantDamageSource(DamageSource source) {
        if (source.is(DamageTypes.GENERIC_KILL)
                || source.is(DamageTypes.LIGHTNING_BOLT)
                || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return true;
        } else {
            return false;
        }
    }

    public static void spawnAllayAtEntity(Entity entity) {
        Allay allay = EntityType.ALLAY.create(entity.level());
        allay.setPos(entity.position());
        // allay will catch fire otherwise
        allay.addEffect(
                new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        20, // this is in ticks I think so 1 second
                        1
                )
        );
        // if it is spawned in via creative, odds are the player wants to test this
        // note: this doubles as crash-prevention from null > 0.666d
        if (getFishLength(entity) >= 0.666d) {
            allay.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    DISC.getDefaultInstance()
            );
        }
        entity.level().addFreshEntity(allay);
    }

    // hmm... spell?
    // could be good for fishing up something -> immediately smiting it if under a size
    // downside is that it is hyper-specific, and other addons can already read item data (so it doesnt add much then)
    private static double getFishLength(Entity entity) {
        if (entity instanceof FishLengthHolder fishLengthHolder) {
            return fishLengthHolder.tide$getLength();
        } else if (entity instanceof ItemEntity itemEntity) {
            if (FISH_LENGTH.get(itemEntity.getItem()) instanceof Double) {
                return FISH_LENGTH.get(itemEntity.getItem());
            }
        }
        return 0;
    }

    public static void doAllaySpawnOnLightningHitMob(Entity entity, DamageSource damageSource) {
        if (!damageSource.is(DamageTypes.LIGHTNING_BOLT)) {
            return;
        }
        spawnAllayAtEntity(entity);
        entity.kill();
        entity.discard();
    }

    // I should clean this up and move this or do something smarter when I port it to kotlin I think
    public static void doAllaySpawnOnLightningHitItem(ItemEntity itemEntity, DamageSource damageSource) {
        // unreachable code but whatever man
        if (!damageSource.is(DamageTypes.LIGHTNING_BOLT)) {
            return;
        }
        while (itemEntity.getItem().getCount() > 0) {
            spawnAllayAtEntity(itemEntity);
            itemEntity.getItem().shrink(1);
        }
    }
}