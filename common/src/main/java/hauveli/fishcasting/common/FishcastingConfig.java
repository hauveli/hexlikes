package hauveli.fishcasting.common;


import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.world.InteractionHand;
import vazkii.patchouli.api.PatchouliAPI;

import static hauveli.fishcasting.Fishcasting.CONFIG;
import static hauveli.fishcasting.Fishcasting.MOD_ID;

@Config(name = MOD_ID)
public class FishcastingConfig implements ConfigData {
    public FishcastingConfig() {
        this.gameplay = new Gameplay();
        this.client = new Client();
        configurePatchouliFlags(this);
    }


    public int atLeastZero(int val) {
        if (val > 0) {
            return val;
        } else {
            return 0;
        }
    }

    @Override
    public void validatePostLoad() {
        this.gameplay.castingTicksToCast = atLeastZero(this.gameplay.castingTicksToCast);
        this.gameplay.cooldownAfterFishing = atLeastZero(this.gameplay.cooldownAfterFishing);
        if (!this.gameplay.castingIsMomentary()
                && !this.gameplay.castingIsOffhandOnly()) {
            this.gameplay.castingType = Gameplay.CASTING_TYPE.MOMENTARY;
        }
        configurePatchouliFlags(this);
    }

    @ConfigEntry.Category("gameplay")
    @ConfigEntry.Gui.TransitiveObject
    public Gameplay gameplay;
    public static class Gameplay {
        // These three might be ideally kept as server-side
        @ConfigEntry.Gui.Tooltip
        private int castingTicksToCast = 5; // must be greater than or equal to 0

        @ConfigEntry.Gui.Tooltip
        private int cooldownAfterFishing = 5; // must be greater than or equal to 0

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        private CASTING_TYPE castingType = CASTING_TYPE.MOMENTARY;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        private boolean lengthPurificationOnlyFish = true; // I'm unsure what the default should be....


        public int getCooldownAfterFishingMinigame() {
            return cooldownAfterFishing;
        }

        public void setCooldownAfterFishingMinigame(int cooldownAfterFishing) {
            this.cooldownAfterFishing = cooldownAfterFishing;
        }

        public boolean isLengthPurificationOnlyFish() {
            return lengthPurificationOnlyFish;
        }

        public void setLengthPurificationOnlyFish(boolean lengthPurificationOnlyFish) {
            this.lengthPurificationOnlyFish = lengthPurificationOnlyFish;
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

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    public Client client;
    public static class Client {

        // This is set to false by default because as rightly pointed out it would break conventions, but I thought
        // it would be interesting to add as an option just to learn a bit and get a feel for having it toggleable.
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        private boolean showForbiddenPatchouliKnowledge = false; // true = we show the entry that would be shown with jsonpatcher installed.

        public boolean isShowForbiddenPatchouliKnowledge() {
            return showForbiddenPatchouliKnowledge;
        }

        public void setShowForbiddenPatchouliKnowledge(boolean showForbiddenPatchouliKnowledge) {
            this.showForbiddenPatchouliKnowledge = showForbiddenPatchouliKnowledge;
        }
    }

    // whe config is updated, patchouliFlags must be configured again:

    // some descriptions must change based on the config
    // all my flags will go here if I have more
    public static void configurePatchouliFlags(FishcastingConfig config) {
        /*
            fallback should always be false. Used for making items point
            SOMEWHERE if there's a page with branching options based on config
         */
        configurePatchouliFlag("fallback", false);
        configurePatchouliFlag("momentary_casting", config.gameplay.castingIsMomentary());
        configurePatchouliFlag("length_purification_only_fish", config.gameplay.isLengthPurificationOnlyFish());
        configurePatchouliFlag("forbidden_patchouli_knowledge", config.client.isShowForbiddenPatchouliKnowledge());
    }

    private static void configurePatchouliFlag(String name, boolean bool) {
        PatchouliAPI.get().setConfigFlag(MOD_ID + ":" + name, bool);
    }
}