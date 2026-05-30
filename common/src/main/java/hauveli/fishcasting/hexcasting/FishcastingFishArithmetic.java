package hauveli.fishcasting.hexcasting;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBasic;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.li64.tide.data.FishLengthHolder;
import com.li64.tide.data.item.TideDataComponents;
import hauveli.fishcasting.hexcasting.iota.FishcastingIotaTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;


public class FishcastingFishArithmetic implements Arithmetic {

    // so, I thought it would be funny.
    public static final List<HexPattern> OPS = List.of(
            ABS
    );

    public static final IotaMultiPredicate ACCEPTS = IotaMultiPredicate.any(
            IotaPredicate.ofType(HexIotaTypes.ENTITY),
            IotaPredicate.ofType(FishcastingIotaTypes.FISH)
    );

    @Override
    public String arithName() {
        return "fishcasting_arithmetic_fish";
    }

    @Override
    public Iterable<HexPattern> opTypes() {
        return OPS;
    }

    // Any future additions for detecting length of an ENTITY (fish or otherwise) would go here
    // This includes item entities as clearly shown below
    // env is here so I can debug
    public double getFishLength(Entity entity, CastingEnvironment env) {
        if (entity instanceof FishLengthHolder fish) {
            return fish.tide$getLength(); // holyyy thank you tide dev
        }
        if (entity instanceof ItemEntity item) {
            ItemStack stack = item.getItem();
            // Ugh this was annoying to figure out
            var fishLength = stack.get(TideDataComponents.FISH_LENGTH);
            if (fishLength != null) {
                return fishLength;
            }
            // this does not work...
            // return TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0d); // FishLengthHolder.tide$LENGTH_KEY
        }
        AABB box = entity.getBoundingBox();

        double largestDimension = Math.max(
                box.getXsize(),
                Math.max(box.getYsize(), box.getZsize())
        );
        return largestDimension;
    }

    @Override
    public Operator getOperator(HexPattern pattern) {
        if (pattern.equals(ABS)) {
            return make1Double(
                    (entity, env) ->  {
                        return getFishLength(entity, env);
                    }
            );
        }
        throw new InvalidOperatorException(pattern + " is not a valid operator in Arithmetic " + this + ".");
    }

    public static OperatorBasic make1Double(
            BiFunction<Entity, CastingEnvironment, Double> op) {

        return new OperatorBasic(1, ACCEPTS) {
            @Override
            public Iterable<Iota> apply(Iterable<? extends Iota> iotas, @NotNull CastingEnvironment env) {
                Entity entity = downcast(
                        iotas.iterator().next(),
                        HexIotaTypes.ENTITY
                ).getEntity((ServerLevel) env.getCastingEntity().level());

                double result = op.apply(entity, env);

                return List.of(new DoubleIota(result));
            }
        };
    }
}
