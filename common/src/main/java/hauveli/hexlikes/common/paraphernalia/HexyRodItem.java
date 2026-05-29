package hauveli.hexlikes.common.paraphernalia;

// https://github.com/FallingColors/HexMod/blob/cdc023a93fa31a3a69a90e1c6538f34c88cdcd98/Common/src/main/java/at/petrak/hexcasting/common/items/ItemStaff.java#L19
// HexMod for hexcasting related code
// https://github.com/Lightning-64/Tide-2/blob/main/src/main/java/com/li64/tide/registries/items/TideFishingRodItem.java#L57
// Tide for Tide related code

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.common.lib.HexAttributes;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.common.msgs.MsgClearSpiralPatternsS2C;
import at.petrak.hexcasting.common.msgs.MsgNewSpiralPatternsS2C;
import at.petrak.hexcasting.common.msgs.MsgOpenSpellGuiS2C;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.items.TideFishingRodItem;
import hauveli.hexlikes.common.HexlikesConfig;
import hauveli.hexlikes.hexcasting.BobberBasedCastEnv;
import hauveli.hexlikes.mixin.WindUpCastBarOverlayMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class HexyRodItem extends TideFishingRodItem {
    // I don't know what this is for but just in case, I'm including it.
    // 0 = normal. 1 = old. 2 = cherry preview
    public static final ResourceLocation FUNNY_LEVEL_PREDICATE = ResourceLocation.fromNamespaceAndPath(HexAPI.MOD_ID, "funny_level");
    private final int baitSlots;

    public HexyRodItem(int baitSlots, double baseDurability, Properties properties) {
        super(baseDurability, properties);
        this.baitSlots = baitSlots; // why does TideFishingRodItem take no baitslots here when it does in the source?
    }


    // retrieveHook from Tide: https://github.com/Lightning-64/Tide-2/blob/f9fc2d04ae4d544ad134025cebd83c7438f67098/src/main/java/com/li64/tide/registries/items/TideFishingRodItem.java#L57
    // Modification: rod.hurtAndBreak is not called
    @Override
    public void retrieveHook(ItemStack rod, Player player, Level level) {
        // todo: figure out how to attach non-existent fish to this hook
        // this is because retrieveHook is called before the fish is spawned?
        // or because the hook is technically never attached to the fish when it does spawn...
        // (for later when I feel like it)
        // Maybe I should check if minigame was just completed?
        // I think it makes sense for the player to assume that it is hooked for at least the same tick that this is called...

        TideFishingHook activeHook = HookAccessor.getHook(player);
        if (activeHook != null) {
            ItemStack bobberItemStack = activeHook.getBobber();

            if (!level.isClientSide) {
                Vec3 bobberPos = activeHook.getPosition(0);
                if (bobberItemStack != null && bobberItemStack.getItem() instanceof TideyFocusItem) {
                    // NOOOOOO BOOOOOBBEEEERTTTTT
                    //executeBobber(level, player, player.getUsedItemHand(), bobberItemStack, bobberPos);
                    // This was moved to a different method
                }

                int durabilityLoss = activeHook.retrieve(rod, (ServerLevel) level, player);
                // if durability is 0, respect vanilla behavior
                if (rod.getMaxDamage() > 0) {
                    rod.hurtAndBreak(durabilityLoss, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE,
                    SoundSource.NEUTRAL, 1.2F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }
    }

    // https://github.com/SuperKnux/HexMod/blob/indev/1.21.1/Common/src/main/java/at/petrak/hexcasting/common/items/magic/ItemPackagedHex.java
    public boolean hasHex(ItemStack stack) {
        return stack.has(HexDataComponents.IOTA_HOLDER_IOTA);
    }

    public @Nullable Iota getHex(ItemStack stack, ServerLevel level) {
        return stack.get(HexDataComponents.IOTA_HOLDER_IOTA);
    }

    public InteractionResultHolder<ItemStack> executeBobber(Level world, Player player, InteractionHand usedHand, ItemStack stack, Vec3 bobberPos) {
        // NOOO WHY ARE WE KILLING BOB
        if (!hasHex(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        //List<Iota> instrs = getHex(stack, (ServerLevel) world);
        Iota iota = getHex(stack, (ServerLevel) world);
        if (iota == null) {
            return InteractionResultHolder.fail(stack);
        }
        if (iota.subIotas() == null) {
            return InteractionResultHolder.fail(stack);
        }
        Iterable<Iota> subIotas = iota.subIotas();
        // ugh me monkey me lazy think about this later, want to have cake and eat too so use focus but treat like trinket

        List<Iota> iotas = new ArrayList<>();
        subIotas.forEach(iotas::add);

        var sPlayer = (ServerPlayer) player;
        var ctx = new BobberBasedCastEnv(sPlayer, usedHand, this.getHook(sPlayer));
        var vm = CastingVM.empty(ctx);
        var clientView = vm.queueExecuteAndWrapIotas(iotas, sPlayer.serverLevel());

        var patterns = iotas.stream()
                .filter(i -> i instanceof PatternIota)
                .map(i -> ((PatternIota) i).getPattern())
                .toList();
        var packet = new MsgNewSpiralPatternsS2C(sPlayer.getUUID(), patterns, 140);
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(sPlayer, packet);
        IXplatAbstractions.INSTANCE.sendPacketTracking(sPlayer, packet);

        Stat<?> stat = Stats.ITEM_USED.get(this);
        player.awardStat(stat);

        // Cooldown exists by virtue of casting rod
        // sPlayer.getCooldowns().addCooldown(this, this.cooldown());

        if (clientView.getResolutionType().getSuccess()) {
            // Somehow we lost spraying particles on each new pattern, so do it here
            // this also nicely prevents particle spam on trinkets
            new ParticleSpray(bobberPos, new Vec3(0.0, 0, 0.0), 0.4, Math.PI / 3, 30)
                    .sprayParticles(sPlayer.serverLevel(), ctx.getPigment());
        }

        var sound = ctx.getSound().sound();
        if (sound != null) {
            var soundPos = sPlayer.position();
            sPlayer.level().playSound(null, soundPos.x, soundPos.y, soundPos.z,
                    sound, SoundSource.PLAYERS, 1f, 1f);
        }

        return InteractionResultHolder.success(stack);
    }

    private void playNoise(Player player) {
        //Minecraft.getInstance().getSoundManager().play(new ElytraOnPlayerSoundInstance((LocalPlayer) player));
        player.playSound(HexSounds.CASTING_AMBIANCE, 1.0f, 1.0f);
    }

    private void stopNoise() {
        Minecraft.getInstance().getSoundManager().stop(HexSounds.CASTING_AMBIANCE.getLocation(), null);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // if bobber is already cast, we have to be able to pull it back in!
        // at least, I prefer it to behave this way.
        if (HookAccessor.getHook(player) != null) {
            return super.use(level, player, hand);
        }
        if (HexlikesConfig.CONFIG.castingIsMomentary()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        } else if (HexlikesConfig.CONFIG.shouldHexOffhand(hand)) { // a little bit silly, but whatever
            return useStaff(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    // https://github.com/Lightning-64/Tide-2/blob/f9fc2d04ae4d544ad134025cebd83c7438f67098/src/main/java/com/li64/tide/registries/items/TideFishingRodItem.java#L368
    // for some reason, this is called TWICE each tick.
    // For this reason, ticksHeld needs to be divided by two...
    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity user, @NotNull ItemStack rod, int charge) {
        super.onUseTick(level, user, rod, charge + HexlikesConfig.CONFIG.getCastingDelay());
        if (level.isClientSide) {
            if (HexlikesConfig.CONFIG.shouldHexMomentary(charge, getUseDuration(rod, user))) {
                playNoise((Player) user);
                setHexyDischargePercent(
                        (float) (getUseDuration(rod, user) - charge)
                        /
                        HexlikesConfig.CONFIG.getCastingDelay()
                );
            } else {
                stopNoise();
            }
        }
    }

    private static float hexyDischargePercent = 0.0f;

    public static float getHexyDischargePercent() {
        return hexyDischargePercent;
    }

    public void setHexyDischargePercent(float percent) {
        HexyRodItem.hexyDischargePercent = Mth.clamp(percent, 0.0f, 1.0f);
    }

    // todo: if player just caught a fish, cooldown for 5 ticks. (configurable as well...)
    @Override
    public void releaseUsing(@NotNull ItemStack rod, @NotNull Level level, @NotNull LivingEntity user, int charge) {
        if (HexlikesConfig.CONFIG.shouldHexMomentary(charge, getUseDuration(rod, user))
                && user instanceof Player player) {
            useStaff(level, player, player.getUsedItemHand());
        } else {
            super.releaseUsing(rod, level, user, charge);
        }
        if (level.isClientSide) {
            stopNoise();
        }
    }

    // From hexcasting github repository
    public InteractionResultHolder<ItemStack> useStaff(Level world, Player player, InteractionHand hand) {
        stopNoise(); // just in case...
        player.swing(hand, true);
        if (player.getAttributeValue(HexAttributes.FEEBLE_MIND) > 0){
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        if (player.isShiftKeyDown()) {
            if (world.isClientSide()) {
                player.playSound(HexSounds.STAFF_RESET, 1f, 1f);
            } else if (player instanceof ServerPlayer serverPlayer) {
                IXplatAbstractions.INSTANCE.clearCastingData(serverPlayer);
                var packet = new MsgClearSpiralPatternsS2C(player.getUUID());
                IXplatAbstractions.INSTANCE.sendPacketToPlayer(serverPlayer, packet);
                IXplatAbstractions.INSTANCE.sendPacketTracking(serverPlayer, packet);
            }
        }

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            var vm = IXplatAbstractions.INSTANCE.getStaffcastVM(serverPlayer, hand);
            var patterns = IXplatAbstractions.INSTANCE.getPatternsSavedInUi(serverPlayer);

            @Nullable CompoundTag ravenmind = vm.getImage().ravenmind().orElse(null);


            IXplatAbstractions.INSTANCE.sendPacketToPlayer(serverPlayer,
                    new MsgOpenSpellGuiS2C(hand, patterns, vm.getImage().getStack(), ravenmind,
                            0)); // TODO: Fix!
        }

        player.awardStat(Stats.ITEM_USED.get(this));
//        player.gameEvent(GameEvent.ITEM_INTERACT_START);

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}