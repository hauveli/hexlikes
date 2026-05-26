package hauveli.hexlikes.common.paraphernalia;

import at.petrak.hexcasting.common.items.storage.ItemFocus;
import com.li64.tide.Tide;
import com.li64.tide.data.rods.AccessoryData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class TideyFocusItem extends ItemFocus {
    // https://github.com/SuperKnux/HexMod/blob/indev/1.21.1/Common/src/main/java/at/petrak/hexcasting/common/items/magic/ItemPackagedHex.java
    public static final String TAG_PROGRAM = "patterns";
    public static final String TAG_PIGMENT = "pigment";
    public static final ResourceLocation HAS_PATTERNS_PRED = modLoc("has_patterns");

    public TideyFocusItem(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }
}
