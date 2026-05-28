package hauveli.hexlikes.mixin;

import com.li64.tide.data.TideData;
import com.li64.tide.data.TideFishingManager;
import com.li64.tide.data.commands.TestType;
import com.li64.tide.data.fishing.CatchResult;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.data.fishing.selector.FishingEntry;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import hauveli.hexlikes.common.registries.HexlikesTags;
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
import java.util.Random;
import java.util.stream.Stream;

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

    @Unique
    private Random random = new Random();

    @Inject(method = "selectCatch", at = @At("HEAD"), cancellable = true)
    private void onSelectCatch(FishingContext context,
                               CallbackInfoReturnable<CatchResult> cir) {
        // Do nothing if no player or wrong bobert
        Player player = context.hook().getPlayerOwner();
        if (player == null
                || !HookAccessor.getHook(player).getBobber().is(HexlikesTags.LUCK_TWEAKING_BOBBERS)
                || random.nextFloat() < 0.999) {
            return;
        }

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
                || !HookAccessor.getHook(player).getBobber().is(HexlikesTags.LUCK_TWEAKING_BOBBERS)) {
            return;
        }

        Map<FishingEntry, Double> result = cir.getReturnValue();
        // TODO: get fish total weight, calculate what 0.1% would be, then add that in here
        // note: I am aware that this will not reflect completely accurate numbers some of the time (how often?) because of math
        // suggestions for how to fix this are welcome, but because the total difference in shown numbers is 0.1
        // it's kind of a low priority for me.
        result.put(fakeFih, 0.01);
    }
}