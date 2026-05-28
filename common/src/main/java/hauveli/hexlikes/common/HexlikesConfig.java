package hauveli.hexlikes.common;


import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import vazkii.patchouli.api.PatchouliAPI;

import static hauveli.hexlikes.Constants.MOD_ID;

@Config(name = MOD_ID)
public class HexlikesConfig implements ConfigData {
    public static HexlikesConfig CONFIG;
    private CASTING_TYPE castingType = CASTING_TYPE.MOMENTARY;
    private int castingTicksToCast = 5; // must be greater than or equal to 0

    public static void init() {
        AutoConfig.register(HexlikesConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(HexlikesConfig.class).getConfig();
        configurePatchouliFlags();
    }
    // whe config is updated, patchouliFlags must be configured again:

    // some descriptions must change based on the config
    // all my flags will go here if I have more
    public static void configurePatchouliFlags() {
        /*
            fallback should always be false. Used for making items point
            SOMEWHERE if there's a page with branching options based on config
         */
        configurePatchouliFlag("fallback", false);
        configurePatchouliFlag("momentary_casting", CONFIG.castingIsMomentary());
    }

    private static void configurePatchouliFlag(String name, boolean bool) {
        PatchouliAPI.get().setConfigFlag(MOD_ID + ":" + name, bool);
    }

    enum CASTING_TYPE {
        OFFHAND_ONLY,
        MOMENTARY
    }

    public boolean shouldHexMomentary(int charge, int chargeStartValue) {
        int heldDuration = chargeStartValue - charge;
        return castingIsMomentary() && heldDurationShortEnoughToHex(heldDuration);
    }
    // how many ticks you can hold down your mouse
    // note: this means there's a minimum value before your rod even begins charging.
    public boolean heldDurationShortEnoughToHex(int heldDuration) {
        // heldDuration in [0, 5]
        return castingTicksToCast >= heldDuration && heldDuration > -1;
    }
    public boolean shouldHexOffhand(InteractionHand hand) {
        return castingIsOffhandOnly() && InteractionHand.OFF_HAND == hand;
    }
    public int getCastingDelay() {
        if (castingIsMomentary()) {
            return castingTicksToCast;
        }
        return 0;
    }
    // default
    public boolean castingIsMomentary() {
        return castingType == CASTING_TYPE.MOMENTARY;
    }
    public boolean castingIsOffhandOnly() {
        return castingType == CASTING_TYPE.OFFHAND_ONLY;
    }
}