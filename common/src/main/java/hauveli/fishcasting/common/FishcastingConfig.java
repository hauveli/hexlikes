package hauveli.fishcasting.common;


import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.world.InteractionHand;
import vazkii.patchouli.api.PatchouliAPI;

import static hauveli.fishcasting.Constants.MOD_ID;

@Config(name = MOD_ID)
public class FishcastingConfig implements ConfigData {
    public static FishcastingConfig CONFIG;

    // These three might be ideally kept as server-side
    private int castingTicksToCast = 5; // must be greater than or equal to 0
    private int cooldownAfterFishingMinigame = 5; // must be greater than or equal to 0
    private CASTING_TYPE castingType = CASTING_TYPE.MOMENTARY;
    private boolean lengthPurificationOnlyFish = true; // I'm unsure what the default should be....

    // This is set to false by default because as rightly pointed out it would break conventions, but I thought
    // it would be interesting to add as an option just to learn a bit and get a feel for having it toggleable.
    private boolean showForbiddenPatchouliKnowledge = false; // true = we show the entry that would be shown with jsonpatcher installed

    public static void init() {
        AutoConfig.register(FishcastingConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(FishcastingConfig.class).getConfig();
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
        configurePatchouliFlag("length_purification_only_fish", CONFIG.isLengthPurificationOnlyFish());
        configurePatchouliFlag("forbidden_patchouli_knowledge", CONFIG.isShowForbiddenPatchouliKnowledge());
    }

    private static void configurePatchouliFlag(String name, boolean bool) {
        PatchouliAPI.get().setConfigFlag(MOD_ID + ":" + name, bool);
    }

    public int getCooldownAfterFishingMinigame() {
        return cooldownAfterFishingMinigame;
    }

    public void setCooldownAfterFishingMinigame(int cooldownAfterFishingMinigame) {
        this.cooldownAfterFishingMinigame = cooldownAfterFishingMinigame;
    }

    public boolean isLengthPurificationOnlyFish() {
        return lengthPurificationOnlyFish;
    }

    public void setLengthPurificationOnlyFish(boolean lengthPurificationOnlyFish) {
        this.lengthPurificationOnlyFish = lengthPurificationOnlyFish;
    }

    public boolean isShowForbiddenPatchouliKnowledge() {
        return showForbiddenPatchouliKnowledge;
    }

    public void setShowForbiddenPatchouliKnowledge(boolean showForbiddenPatchouliKnowledge) {
        this.showForbiddenPatchouliKnowledge = showForbiddenPatchouliKnowledge;
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