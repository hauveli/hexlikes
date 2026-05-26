package hauveli.hexlikes.hexcasting.actions;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.mishaps.*;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.registries.TideFish;
import com.li64.tide.registries.entities.fish.TideFishEntity;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.li64.tide.data.item.TideItemData.CATCH_TIMESTAMP;

public final class OpGetBobbersCatchJ implements ConstMediaAction {

    public static final OpGetBobbersCatchJ INSTANCE = new OpGetBobbersCatchJ();

    private OpGetBobbersCatchJ() {}

    @Override
    public int getArgc() {
        return 1;
    }



    // This is specifically for checking if a fish was just reeled in this exact same tick
    // and if the bobber is on top of it
    // I imagine if the bobber is inside an ItemEntity of a Fish that was reeled up the same tick
    // that the player calls "check what is on the hook" the player expects the returned value to be
    // the ItemEntity of the fish they just reeled in...
    // it will only do this if:
    // Fish was reeled (pulled from water/generated) that same tick
    // ex. throwing a fish onto the bobber and calling "get whats on bobber" will not actually get it
    // unless it is properly caught on it.
    // I am making this possibly opinionated exception because I think it makes sense
    // The undefined behaviour here (?) is if you somehow get your bobber to be in a minigame
    // also be hooked on an entity
    // then reel in
    // I do not know if this is possible though...
    @Nullable
    public static ItemEntity getFishOnHook(TideFishingHook hook) {
        // certain identify the correct entity?
        for (ItemEntity fishbert : hook.level().getEntitiesOfClass(
                ItemEntity.class,
                hook.getBoundingBox().inflate(10)
        )) {
            // TODO: somehow obtain when player began fishing minigame?
            int marginOfError = 500;
            Optional<FishData> opData = FishData.get(fishbert.getItem());
            if (fishbert.tickCount == 0
                    && (CATCH_TIMESTAMP.get(fishbert.getItem()) != null
                    && Minecraft.getInstance().level.getDayTime() < 500 + CATCH_TIMESTAMP.get(fishbert.getItem()))) {
                return fishbert;
            }
        }
        return null;
    }

    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment env) {
        LivingEntity caster = env.getCastingEntity();
        ServerLevel serverLevel = caster.getServer().getLevel(caster.level().dimension());
        var unknownIota = args.getFirst();

        // Not an entity
        if (!(unknownIota instanceof EntityIota)) {
            throw MishapInvalidIota.ofType(unknownIota, getArgc(), "entity");
        }
        var unknownEntity = ((EntityIota) unknownIota).getEntity(serverLevel);

        // Not a hook
        if (!(unknownEntity instanceof TideFishingHook)) {
            throw MishapBadEntity.of(unknownEntity, "tide_fishing_hook");
        }

        TideFishingHook target = (TideFishingHook) unknownEntity;
        // Too far, only check if not owned by self
        if (!target.getPlayerOwner().is(caster)) {
            env.assertEntityInRange(target);
        }

        // These should not error, as they are expected behaviour despite returning null
        var justReeled = getFishOnHook(target);
        var haul = target.getHookedIn();
        if (justReeled != null) {
            return List.of(new EntityIota(justReeled));
        } else if (haul != null) {
            return List.of(new EntityIota(haul));
        }
        return List.of(new NullIota()); // no catch

    }

    @Override
    public long getMediaCost() {
        return MediaConstants.SHARD_UNIT; // should also cost something, unsure how much...
    }

    // Oh my god why does java require me to implement these
    // https://github.com/FallingColors/HexMod/blob/cdc023a93fa31a3a69a90e1c6538f34c88cdcd98/Common/src/main/java/at/petrak/hexcasting/api/casting/castables/ConstMediaAction.kt#L17

    public CostMediaActionResult executeWithOpCount(List<? extends Iota> args, CastingEnvironment env) throws Mishap {
        var stack = this.execute(args, env);
        return new CostMediaActionResult(stack, 1);
    }

    @Override
    public OperationResult operate(CastingEnvironment env,
                                   CastingImage image,
                                   SpellContinuation continuation) {

        List<Iota> stack = new ArrayList<>(image.getStack());

        if (getArgc() > stack.size()) {
            throw new MishapNotEnoughArgs(getArgc(), stack.size());
        }

        List<Iota> args = stack.subList(stack.size() - getArgc(), stack.size());
        List<Iota> argsCopy = new ArrayList<>(args);

        for (int i = 0; i < getArgc(); i++) {
            stack.remove(stack.size() - 1);
        }

        CostMediaActionResult result = this.executeWithOpCount(argsCopy, env);
        stack.addAll(result.getResultStack());

        if (env.extractMedia(getMediaCost(), true) > 0) {
            throw new MishapNotEnoughMedia(getMediaCost());
        }

        List<OperatorSideEffect> sideEffects = new ArrayList<>();
        sideEffects.add(new OperatorSideEffect.ConsumeMedia(getMediaCost()));

        // java is so yucky..........
        CastingImage image2 = image.copy(
                stack,
                image.getParenCount(),
                image.getParenthesized(),
                image.getEscapeNext(),
                (int) (image.getOpsConsumed() + result.getOpCount()),
                image.getUserData()
        );

        return new OperationResult(
                image2,
                sideEffects,
                continuation,
                HexEvalSounds.NORMAL_EXECUTE
        );
    }
}