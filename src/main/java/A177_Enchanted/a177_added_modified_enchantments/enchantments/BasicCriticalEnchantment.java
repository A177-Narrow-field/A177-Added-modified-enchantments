package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.Random;

@Mod.EventBusSubscriber
public class BasicCriticalEnchantment extends Enchantment {
    private static final Random RANDOM = new Random();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("basic_critical");
    }

    public BasicCriticalEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 2;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 10;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在工具和武器上
        return EnchantmentCategory.WEAPON.canEnchant(stack.getItem()) || 
               EnchantmentCategory.DIGGER.canEnchant(stack.getItem());
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // 与其他暴击附魔冲突
        return super.checkCompatibility(other) && 
               other != ModEnchantments.CRITICAL_CHANCE.get() && 
               other != ModEnchantments.FOCUSED_CRITICAL.get();
    }

    /**
     * 检查是否触发暴击
     * @param level 附魔等级
     * @return 是否暴击
     */
    public static boolean shouldCriticalHit(int level) {
        if (level <= 0) return false;
        // 每级增加5%暴击概率，最高5级25%
        double chance = level * 0.05;
        return RANDOM.nextDouble() < chance;
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
            int level = weapon.getEnchantmentLevel(ModEnchantments.BASIC_CRITICAL.get());

            if (level > 0 && shouldCriticalHit(level)) {
                // 触发原版暴击效果（特效和音效）
                player.crit(event.getEntity());
                // 暴击伤害为原来的1.5倍
                event.setAmount(event.getAmount() * 1.5f);
                // 直接应用暴击伤害增强效果
                applyCriticalDamageEnhancements(weapon, event);
            }
        }
    }
}