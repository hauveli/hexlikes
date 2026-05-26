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
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/*
I'm doing this another time if I feel like I need or want it for anything
to consider:
bucketing fish spell by right "writing" a stored fish iota (attached to focus bobber) to your bucket
unbucketing fish spell by reading a stored fish bucket (with a focus bobber out)
 */

public final class OpGetFishLengthJ implements ConstMediaAction {

    public static final OpGetFishLengthJ INSTANCE = new OpGetFishLengthJ();

    private OpGetFishLengthJ() {}

    @Override
    public int getArgc() {
        return 1;
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
        // Too far
        env.assertEntityInRange(target);

        // These should not error, as they are expected behaviour despite returning null
        var haul = target.getHookedIn();
        if (haul == null) {
            return List.of(new NullIota());
        }

        return List.of(new EntityIota(haul));
    }

    @Override
    public long getMediaCost() {
        return MediaConstants.DUST_UNIT; // should also cost something, unsure how much...
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