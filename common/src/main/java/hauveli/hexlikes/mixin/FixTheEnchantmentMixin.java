package hauveli.hexlikes.mixin;

import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(Enchantment.class)
public class FixTheEnchantmentMixin {

    // Uhh the idea would be to prevent the shepherds casting rod from accepting the unbreaking and mending enchantments
    // this is because they do nothing for it.
    // oh well!
}