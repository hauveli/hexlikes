package hauveli.fishcasting.mixin.blessed_bobber;

import com.li64.tide.data.TideData;
import com.li64.tide.data.TideFishingManager;
import com.li64.tide.data.commands.TestType;
import com.li64.tide.data.fishing.CatchResult;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.data.fishing.selector.FishingEntry;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import hauveli.fishcasting.common.registries.FishcastingTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static hauveli.fishcasting.Constants.random;

@Mixin(TideFishingManager.class)
public class ProbabilityBobberTweakTideFishingManagerMixin {

    @Unique
    private FishingEntry fakeFih = new FishingEntry() {
        @Override
        public double weight(FishingContext fishingContext) {
            return 0;
        }

        @Override
        public boolean shouldKeep(FishingContext fishingContext) {
            return true;
        }

        @Override
        public CatchResult getResult(FishingContext fishingContext) {
            return new CatchResult(List.of(Items.BUNDLE.getDefaultInstance()), null);
        }
    };

    @Unique
    private List<FishingEntry> allPossibleCatches =
            Stream.of(
                            TideData.FISHING_LOOT.get().values(),
                            TideData.FISH.get().values(),
                            TideData.CRATES.get().values()
                    )
                    .flatMap(Collection::stream)
                    .map(e -> (FishingEntry) e)
                    .toList();

    @Inject(method = "selectCatch", at = @At("HEAD"), cancellable = true)
    private void onSelectCatch(FishingContext context,
                               CallbackInfoReturnable<CatchResult> cir) {
        // Do nothing if no player or wrong bobert
        Player player = context.hook().getPlayerOwner();
        if (player == null
                || !HookAccessor.getHook(player).getBobber().is(FishcastingTags.LUCK_TWEAKING_BOBBERS)
                || random.nextFloat() < 0.999) {
            return; // note: this skews the probabilities and the test does not accurately reflect the true probabilities anymore.
        } // however, the total change should be ~0.1% so I have decied this is "good enough".
        // and yes, I know that 0.1% is kind of garbage, the main feature of the bobber is that its bell rings when there's a catch (secret bonus feature that I should maybe document...)

        CatchResult rolled = allPossibleCatches.get(
                random.nextInt(0, allPossibleCatches.size()-1)
        ).getResult(context);
        cir.setReturnValue(rolled);
        cir.cancel();
    }

    @Inject(method = "test", at = @At("RETURN"))
    private void onTestCatch(FishingContext context,
                             TestType type,
                             CallbackInfoReturnable<Map<FishingEntry, Double>> cir) {
        // Do nothing if no player or wrong bobert
        Player player = context.hook().getPlayerOwner();
        if (player == null
                || !HookAccessor.getHook(player).getBobber().is(FishcastingTags.LUCK_TWEAKING_BOBBERS)) {
            return;
        }

        Map<FishingEntry, Double> result = cir.getReturnValue();

        // Weight calculation
        // simplistic and NOT how I would prefer to do it, but it does the bare minimum.
        double sum = 0.0;
        for (double val : result.values()) {
            sum += val;
        }
        /*
            ex. for myself to reason/remember what I'm doing
            for P = 10% and W = 350
            (P * W) / (1 - P)
         */

        double targetProbability = 1d / 1000d;
        result.put(fakeFih, simpleSolver(targetProbability, sum));
    }

    // Todo: figure out how to shift the probabilities of the other fish proportionally
    // Yes that is a negligible difference but it is a difference that needs its own solution
    // plus it would allow for an accessory which redistributes the odds according to some log function or something
    // which would be quite cool.
    // Some way to reject the most recently caught fish would be awesome, too.
    @Unique
    private double simpleSolver(double targetProbability, double weightTotal) {
        return (weightTotal * targetProbability) / (1d-targetProbability);
    }

}