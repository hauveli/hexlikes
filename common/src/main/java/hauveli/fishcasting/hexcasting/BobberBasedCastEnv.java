package hauveli.fishcasting.hexcasting;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.common.msgs.MsgNewSpiralPatternsS2C;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.List;


public class BobberBasedCastEnv extends PlayerBasedCastEnv {

    protected EvalSound sound = HexEvalSounds.NOTHING;
    private TideFishingHook bobber;
    private final Long distSqrToOwner;

    public BobberBasedCastEnv(ServerPlayer caster, InteractionHand castingHand, TideFishingHook bobber) {
        super(caster, castingHand);
        this.bobber = bobber;
        this.distSqrToOwner = (long) bobber.position().distanceToSqr(caster.position());
    }

    @Override
    public void postExecution(CastResult result) {
        super.postExecution(result);

        if (result.component1() instanceof PatternIota patternIota) {
            var packet = new MsgNewSpiralPatternsS2C(
                    this.caster.getUUID(), List.of(patternIota.getPattern()), 140
            );
            IXplatAbstractions.INSTANCE.sendPacketToPlayer(this.caster, packet);
            IXplatAbstractions.INSTANCE.sendPacketTracking(this.caster, packet);
        }

        // TODO: how do we know when to actually play this sound?
        this.sound = this.sound.greaterOf(result.getSound());
    }

    @Override
    public long extractMediaEnvironment(long costLeft, boolean simulate) {
        if (this.caster.isCreative())
            return 0;
        // leaving this just in case I decide to add something

            /*
            //var casterStack = this.caster.getItemInHand(this.castingHand);
            var casterStack = this.bobber.getBobber();
            var casterHexHolder = IXplatAbstractions.INSTANCE.findHexHolder(casterStack);
            if (casterHexHolder == null)
                return costLeft;
            var canCastFromInv = true; // Media is not stored in the bobbers

            var casterMediaHolder = IXplatAbstractions.INSTANCE.findMediaHolder(casterStack);
            // The contracts on the AD and on this function are different.
            // ADs return the amount extracted, this wants the amount left
            if (casterMediaHolder != null) {
                long extracted = casterMediaHolder.withdrawMedia((int) costLeft, simulate);
                costLeft -= extracted;
            }
             */
        boolean canCastFromInv = true;
        long bobberPenaltyCost = this.distSqrToOwner;
        if (canCastFromInv && costLeft > 0) {
            costLeft = this.extractMediaFromInventory(costLeft + bobberPenaltyCost, this.canOvercast(), simulate);
        }

        return costLeft;
    }

    @Override
    public InteractionHand getCastingHand() {
        return this.castingHand;
    }

    @Override
    public FrozenPigment getPigment() {
        var casterStack = this.caster.getItemInHand(this.castingHand);
        var casterHexHolder = IXplatAbstractions.INSTANCE.findHexHolder(casterStack);
        if (casterHexHolder == null)
            return IXplatAbstractions.INSTANCE.getPigment(this.caster);
        var hexHolderPigment = casterHexHolder.getPigment();
        if (hexHolderPigment != null)
            return hexHolderPigment;
        return IXplatAbstractions.INSTANCE.getPigment(this.caster);
    }

    public EvalSound getSound() {
        return sound;
    }
}
