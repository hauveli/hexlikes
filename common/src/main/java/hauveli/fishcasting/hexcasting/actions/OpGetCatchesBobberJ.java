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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class OpGetCatchesBobberJ implements ConstMediaAction {

    public static final OpGetCatchesBobberJ INSTANCE = new OpGetCatchesBobberJ();

    private OpGetCatchesBobberJ() {}

    @Nullable
    public static TideFishingHook getFishingHook(Entity entity) {
        Level level = entity.level();

        // certain identify the correct entity?
        for (TideFishingHook hook : level.getEntitiesOfClass(
                TideFishingHook.class,
                entity.getBoundingBox().inflate(32)
        )) {
            if (hook.getHookedIn() == entity) {
                return hook;
            }
        }

        return null;
    }

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
        // Too far, we do NOT check if the entity is attached to our bobber despite it being a possibility.
        // the list of things that should be considered "in range" imo:
        // Self, hook
        env.assertEntityInRange(unknownEntity);

        // Not a player
        var hook = getFishingHook(unknownEntity);
        if (hook == null) {
            return List.of(new NullIota());
        }

        return List.of(new EntityIota(hook));
    }

    @Override
    public long getMediaCost() {
        return MediaConstants.CRYSTAL_UNIT; // should also cost something, unsure how much...
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