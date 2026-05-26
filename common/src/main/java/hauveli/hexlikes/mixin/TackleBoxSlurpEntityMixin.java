package hauveli.hexlikes.mixin;

import com.li64.tide.data.FishLengthHolder;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import hauveli.hexlikes.common.chair.TackleBoxChairEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static com.li64.tide.data.item.TideItemData.CATCH_TIMESTAMP;

@Mixin(ItemEntity.class)
public class TackleBoxSlurpEntityMixin {

    /*
        Note:
        edge case exists whem:
            1. Fish item fished up on exact same gametick
            2. Fish is exact same species
            3. Fish is exact same length (length is a random DOUBLE.)
            4. Fish is fished up to the exact same spot
            5. Both fish merge into one ItemEntity before they touch the player
            6. Both Fish touch the same player on the same tick
            7. The player is on a tacklebox chair

        I did not care to implement a check for this but if this happens, one fish (or more) is lost
        because this is so unlikely I'm calling it an easter egg and not caring.
        Chances of this occuring even intentionally are likely comparable to some lower Hash collisions due to
        how long it takes to fish (really really really unlikely)
     */
    // I would like to mixin to something better if I knew any...
    @Inject(
            method = "playerTouch",
            at = @At("HEAD"),
            cancellable = true)
    private void hexlikes$onTake(Player player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity)(Object)this;
        if (player.getVehicle() instanceof TackleBoxChairEntity tackleBoxChairEntity) {
            ItemStack stack = itemEntity.getItem();
            // only consider recently caught fish, implicitly these are always alive(?), but this prevents
            // some behaviours I'm not sure I'd like but I may change the maximumFishAgeForAutomaticBoxing variable
            int maximumFishAgeForAutomaticBoxing = 500; // in ticks?
            if (CATCH_TIMESTAMP.get(stack) != null
                    && Minecraft.getInstance().level.getDayTime() < CATCH_TIMESTAMP.get(stack) + maximumFishAgeForAutomaticBoxing) {
                int targetBucketSlot = -1; // remains -1 if no valid slot is found or config disallows
                // how do I access TideConfig.Items.BucketableMode.NEVER ????
                // Tide.CONFIG.items.bucketableFishItems or this?
                // please forgive me, I couldn't figure out how to check the config.
                if (true) {
                    // https://github.com/Lightning-64/Tide-2/blob/f9fc2d04ae4d544ad134025cebd83c7438f67098/src/main/java/com/li64/tide/mixin/ItemMixin.java#L38
                    Optional<FishData> dataOp = FishData.get(stack);
                    if (dataOp.isEmpty()) return; // skedaddle, I don't know when this could happen but might as well
                    FishData fishData = dataOp.get();
                    // bucket only if config allows it
                    if (fishData.bucket().get().value() instanceof BucketItem fishBucketItem) {
                        Fluid fluid = getFluid(fishBucketItem);
                        targetBucketSlot = validBucketAtPositiveIndex(fluid, tackleBoxChairEntity);
                        if (targetBucketSlot != -1) {
                            stack = bucketedFishFromFish(fishData, stack);
                        }
                    }
                }
                if (targetBucketSlot == -1) {
                    for (int slotIndex = 0; slotIndex < tackleBoxChairEntity.getContainerSize(); slotIndex++) {
                        ItemStack slotStack = tackleBoxChairEntity.getItem(slotIndex);
                        if (slotStack.isEmpty()) {
                            player.playSound(SoundEvents.BUCKET_EMPTY_FISH, 1.0f, 1.0f);
                            tackleBoxChairEntity.setItem(slotIndex, stack);
                            break;
                        }
                    }
                } else {
                    player.playSound(SoundEvents.BUCKET_FILL_FISH, 1.0f, 1.0f);
                    tackleBoxChairEntity.setItem(targetBucketSlot, stack);
                }
                itemEntity.discard(); // uhh, this feels a bit dangerous but I hope there are no edge cases
                ci.cancel();
            }
        }
    }

    // https://github.com/Lightning-64/Tide-2/blob/f9fc2d04ae4d544ad134025cebd83c7438f67098/src/main/java/com/li64/tide/mixin/ItemMixin.java#L76C19-L76
    @Unique
    private ItemStack bucketedFishFromFish(FishData data, ItemStack fish) {
        ItemStack newStack = new ItemStack(data.bucket().get());
        if (TideItemData.FISH_LENGTH.isPresent(fish) /*|| TideItemData.IS_SHINY.isPresent(fish)*/) {
            double length = TideItemData.FISH_LENGTH.getOrDefault(fish, 0.0);
            /*boolean isShiny = TideItemData.IS_SHINY.getOrDefault(fish, false); */
            CustomData.update(DataComponents.BUCKET_ENTITY_DATA, newStack, tag -> {
                if (TideItemData.FISH_LENGTH.isPresent(fish)) tag.putDouble(FishLengthHolder.tide$LENGTH_KEY, length);
                //if (TideItemData.IS_SHINY.isPresent(fish)) tag.putBoolean(ShinyFish.tide$SHINY_KEY, isShiny);
            });
        }
        return newStack;
    }

    @Unique
    private int validBucketAtPositiveIndex(Fluid fluid, TackleBoxChairEntity entity) {
        for (int i = 0; i < entity.getContainerSize(); i++) {
            ItemStack slotStack = entity.getItem(i);
            if (slotStack.getItem() instanceof BucketItem bucketItem
                    && !(bucketItem instanceof MobBucketItem)
                    && getFluid(bucketItem).isSame(fluid)) {
                return i;
            }
        }
        return -1;
    }

    @Unique
    private Fluid getFluid(BucketItem bucketItem) {
        return ((ContentAccessorBucketItemMixin) bucketItem).getContent();
    }

}