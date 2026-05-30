package hauveli.fishcasting.hexcasting.actions;

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

public final class OpGetBobbersOwnerJ implements ConstMediaAction {

    public static final OpGetBobbersOwnerJ INSTANCE = new OpGetBobbersOwnerJ();

    private OpGetBobbersOwnerJ() {}

    @Override
    public int getArgc() {
        return 1;
    }

    // I don't understand why java complains where kotlin is happy
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

        // Not a player
        if (!(unknownEntity instanceof TideFishingHook)) {
            throw MishapBadEntity.of(unknownEntity, "tide_fishing_hook");
        }

        TideFishingHook target = (TideFishingHook) unknownEntity;
        // Too far, only check if not owned by self
        if (!target.getPlayerOwner().is(caster)) {
            env.assertEntityInRange(target);
        }

        // These should not error, as they are expected behaviour despite returning null
        var owner = target.getOwner();
        if (owner == null) {
            return List.of(new NullIota());
        }

        return List.of(new EntityIota(owner));
    }

    @Override
    public long getMediaCost() {
        return MediaConstants.SHARD_UNIT; // Should cost at least something, I feel like, but not much.
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
