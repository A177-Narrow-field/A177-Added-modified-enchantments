package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class FireProtectionEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("fire_protection");
    }
    
    public FireProtectionEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
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
        // 可以附在任何可附魔的物品上
        return true;
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

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // 检查是否是玩家受到攻击
        if (!(event.getEntity() instanceof Player player)) return;
        
        // 检查玩家是否在所有盔甲槽位都装备了带有焚火庇护附魔的盔甲
        boolean hasFullFireProtection = true;
        
        // 检查所有盔甲槽位
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armor = player.getItemBySlot(slot);
                int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_PROTECTION.get(), armor);
                if (level <= 0) {
                    hasFullFireProtection = false;
                    break;
                }
            }
        }
        
        // 如果没有在所有盔甲槽位都装备带有焚火庇护附魔的盔甲，则不处理
        if (!hasFullFireProtection) return;
        
        // 获取伤害源
        DamageSource source = event.getSource();

        // 使用 Minecraft 推荐的标签方式判断是否属于火焰伤害
        var damageSources = player.damageSources();
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) ||
            source == damageSources.lava() ||
            source == damageSources.inFire() ||
            source == damageSources.onFire() ||
            source == damageSources.hotFloor()) {
            // 取消火焰伤害
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查是否是玩家受到攻击
        if (!(event.getEntity() instanceof Player player)) return;
        
        // 检查玩家是否在所有盔甲槽位都装备了带有焚火庇护附魔的盔甲
        boolean hasFullFireProtection = true;
        
        // 检查所有盔甲槽位
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armor = player.getItemBySlot(slot);
                int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_PROTECTION.get(), armor);
                if (level <= 0) {
                    hasFullFireProtection = false;
                    break;
                }
            }
        }
        
        // 如果没有在所有盔甲槽位都装备带有焚火庇护附魔的盔甲，则不处理
        if (!hasFullFireProtection) return;
        
        // 如果玩家着火，减少10%伤害
        if (player.getRemainingFireTicks() > 0) {
            event.setAmount(event.getAmount() * 0.9f);
        }
    }
}