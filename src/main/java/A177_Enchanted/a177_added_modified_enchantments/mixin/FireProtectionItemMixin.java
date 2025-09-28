package A177_Enchanted.a177_added_modified_enchantments.mixin;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class FireProtectionItemMixin {
    
    /**
     * 修改物品实体受到火焰伤害时的行为，如果物品拥有焚火庇护附魔，则完全免疫伤害
     */
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity itemEntity = (ItemEntity)(Object)this;
        ItemStack itemStack = itemEntity.getItem();
        
        // 检查物品是否拥有焚火庇护附魔
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_PROTECTION.get(), itemStack);
        
        // 如果物品拥有焚火庇护附魔，则完全免疫火焰伤害
        if (level > 0) {
            // 检查是否是火焰伤害
            if (source.is(DamageTypeTags.IS_FIRE)) {
                // 取消伤害并返回false表示未受伤
                cir.cancel();
                cir.setReturnValue(false);
            }
        }
    }
    
    /**
     * 修改物品实体在火中的行为，如果物品拥有焚火庇护附魔，则免疫燃烧
     */
    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void onFireImmune(CallbackInfoReturnable<Boolean> cir) {
        ItemEntity itemEntity = (ItemEntity)(Object)this;
        ItemStack itemStack = itemEntity.getItem();
        
        // 检查物品是否拥有焚火庇护附魔
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_PROTECTION.get(), itemStack);
        
        // 如果物品拥有焚火庇护附魔，则完全免疫火焰
        if (level > 0) {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}