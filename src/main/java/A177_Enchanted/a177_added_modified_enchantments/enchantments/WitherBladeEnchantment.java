package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class WitherBladeEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("wither_blade");
    }
    
    public WitherBladeEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是剑时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public boolean isTreasureOnly() {
        // 从配置文件读取是否为宝藏附魔
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : true;
    }// 宝藏附魔

    @Override
    public boolean isTradeable() {
        // 从配置文件读取是否可交易
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTradeable.get() : false;
    }// 不可通过交易获得

    @Override
    public boolean isDiscoverable() {
        // 从配置文件读取是否可发现
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : false;
    }// 不可在附魔书中发现

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) &&
               other != ModEnchantments.POISON_FANG.get() &&
               other != ModEnchantments.POISON_SCAR.get();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            int level = weapon.getEnchantmentLevel(ModEnchantments.WITHER_BLADE.get());

            if (level > 0 && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = event.getEntity();

                // 如果目标已经有凋零效果，增加伤害50%
                if (target.hasEffect(MobEffects.WITHER)) {
                    float newDamage = event.getAmount() * 1.5f;

                    // 如果目标有盔甲，再增加50%伤害
                    if (target.getArmorValue() > 0) {
                        newDamage *= 1.5f;
                    }

                    event.setAmount(newDamage);
                }
            }
        }
    }
}