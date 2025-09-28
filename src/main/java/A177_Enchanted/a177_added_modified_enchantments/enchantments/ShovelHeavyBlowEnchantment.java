package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ShovelHeavyBlowEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("shovel_heavy_blow");
    }
    
    public ShovelHeavyBlowEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        // 只能附在铲上
        return stack.getItem() instanceof ShovelItem;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 可交易
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 限制在附魔台上只能对铲子进行附魔
        return stack.getItem() instanceof ShovelItem && isDiscoverable();
    }

    /**
     * 应用暴击伤害增强效果
     * @param weapon 武器
     * @param event 伤害事件
     */
    public static void applyCriticalDamageEnhancements(ItemStack weapon, LivingHurtEvent event) {
        // 处理普伤暴击附魔
        int damageCriticalLevel = weapon.getEnchantmentLevel(ModEnchantments.DAMAGE_CRITICAL.get());
        if (damageCriticalLevel > 0) {
            // 每级增加20%暴击伤害
            float bonusDamage = event.getAmount() * (damageCriticalLevel * 0.2f);
            event.setAmount(event.getAmount() + bonusDamage);
        }

        // 处理超频暴击附魔
        int criticalOverclockLevel = weapon.getEnchantmentLevel(ModEnchantments.CRITICAL_OVERCLOCK.get());
        if (criticalOverclockLevel > 0) {
            // 每级增加100%暴击伤害
            float bonusDamage = event.getAmount() * (criticalOverclockLevel * 1.0f);
            event.setAmount(event.getAmount() + bonusDamage);
        }

        // 处理低伤害暴击附魔
        int LowDamageCriticalEnchantment = weapon.getEnchantmentLevel(ModEnchantments.LOW_DAMAGE_CRITICAL.get());
        if (LowDamageCriticalEnchantment > 0) {
            // 每级增加10%暴击伤害
            float bonusDamage = event.getAmount() * (LowDamageCriticalEnchantment * 0.1f);
            event.setAmount(event.getAmount() + bonusDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            int level = weapon.getEnchantmentLevel(ModEnchantments.SHOVEL_HEAVY_BLOW.get());

            // 如果武器有铲重击附魔，则触发暴击
            if (level > 0) {
                // 触发原版暴击效果（特效和音效）
                player.crit(event.getEntity());
                // 触发暴击效果，伤害为原来的3倍
                event.setAmount(event.getAmount() * 3f);
                // 直接应用暴击伤害增强效果
                applyCriticalDamageEnhancements(weapon, event);
            }
        }
    }
}