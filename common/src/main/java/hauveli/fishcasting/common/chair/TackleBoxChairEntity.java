package hauveli.fishcasting.common.chair;

import com.li64.tide.Tide;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.network.messages.OpenJournalMsg;
import hauveli.fishcasting.common.registries.FishcastingEntityTypes;
import hauveli.fishcasting.common.registries.FishcastingItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

// melted axolotl fish
public class TackleBoxChairEntity extends ChestBoat {

    public TackleBoxChairEntity(EntityType<? extends TackleBoxChairEntity> type, Level level) {
        super(type, level);
        this.stuckSpeedMultiplier = Vec3.ZERO;
        this.setPaddleState(false, false);
    }

    public TackleBoxChairEntity(Level level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        float f = this.getSinglePassengerXOffset();
        return (new Vec3((double)0.0F, // Left/Right ?
                (double)(dimensions.height() * 1.1F), // up/down
                (double)f)).yRot(-this.getYRot() * ((float)Math.PI / 180F)); // Forward/Backward + rotation?
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return -3.0F / 16.0F;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // super.interact(player, hand);
        if (!player.isSecondaryUseActive()) {
            InteractionResult interactionresult = super.interact(player, hand);
            if (interactionresult != InteractionResult.PASS) {
                return interactionresult;
            }
        }

        if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else {
            InteractionResult interactionresult1 = this.interactWithContainerVehicle(player);
            if (interactionresult1.consumesAction()) {
                // if player is looking at the WEST side of the chair, open up the journal
                // Uhhh get vector boat is facing, get player's look vector, (both unit vectors)
                // measure Z component? no, then it's worthless.
                // options: dot product, cross product
                // dot product gives me how parallel they are. if they are more or less parallel I don't care
                // when are they not parallel?
                // in the negative space of two opposite pyramids/cones, but I only care about one quarter
                // how to determine which quarter I am looking at?
                // I should re-evaluate my approach but whatever
                // can I just obtain the normal of the side of the boat?
                // I could take the position of the boat, the lookdir and up to get a vector orthogonal and then reduce
                // the number of pyramids/cones I care about to just two, meaning I only need to check am I on the left or the right?
                // can check if we are within 0.6+? to see if we are looking at the right spot
                // god I'm so glad I know at least a bit of math this was a for-once satisfying implementation.
                // TL;DR this code first reduces the valid region to two cone-ish shapes (defined by a scalar, in this case 0.6) relative to the boat's facing direction
                // then reduces it to a narrow cone on just one side because we only look at values greater than 0.6
                Vec3 doubleCone = this.getUpVector(0).cross(this.getLookAngle()); // idk what to use for dt
                if (doubleCone.dot(player.getLookAngle()) > 0.6) {
                    openJournal(player);
                    return interactionresult1;
                }

                this.gameEvent(GameEvent.CONTAINER_OPEN, player);
                PiglinAi.angerNearbyPiglins(player, true);
            }

            return interactionresult1;
        }

    }

    private void openJournal(Player player) {
        // I'm a big noobert and couldnt figure out how to give these stats
        /*
        ItemStack defaultJournalItemStack = (new FishingJournalItem(new Item.Properties())).getDefaultInstance();
        CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, defaultJournalItemStack);
        serverPlayer.awardStat(Stats.ITEM_USED.get(defaultJournalItemStack.getItem()));
         */
        if (player.level().isClientSide()) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        TidePlayerData.getOrCreate(serverPlayer).syncTo(serverPlayer);
        Tide.NETWORK.sendToPlayer(new OpenJournalMsg(), serverPlayer);
    }

    @Override
    public Item getDropItem() {
        return FishcastingItems.TACKLEBOX_CHAIR;
    }

    @Override
    public EntityType<?> getType() {
        return FishcastingEntityTypes.TACKLEBOX_CHAIR;
    }

    @Override
    protected void clampRotation(@NotNull Entity entityToUpdate) {
        super.clampRotation(entityToUpdate);
    }

    @Override
    public boolean canControlVehicle() {
        return false;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return super.makeBoundingBox();
    }

    @Override
    public float getGroundFriction() {
        return super.getGroundFriction() * 0.1f; // super slow
    }

    @Override
    protected float getBlockSpeedFactor() {
        return super.getBlockSpeedFactor() * 0.1f;
    }
}