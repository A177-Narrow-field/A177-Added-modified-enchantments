package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mod.EventBusSubscriber
public class ArmorShatterEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("armor_shatter");
    }

    public ArmorShatterEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 20;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());}//确保在附魔台中可以正确应用

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            // 计算所有盔甲上的盔碎附魔等级总和
            int totalLevel = 0;
            
            // 检查头盔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && helmet.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_SHATTER.get(), helmet);
            }
            
            // 检查胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!chestplate.isEmpty() && chestplate.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_SHATTER.get(), chestplate);
            }
            
            // 检查护腿
            ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
            if (!leggings.isEmpty() && leggings.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_SHATTER.get(), leggings);
            }
            
            // 检查靴子
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (!boots.isEmpty() && boots.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_SHATTER.get(), boots);
            }
            
            // 如果有任何盔碎附魔，直接消耗盔甲耐久
            if (totalLevel > 0) {
                // 每级增加5%耐久消耗
                float durabilityPercentage = 0.05f * totalLevel;
                
                // 消耗盔甲耐久
                damageArmor(helmet, durabilityPercentage, player);
                damageArmor(chestplate, durabilityPercentage, player);
                damageArmor(leggings, durabilityPercentage, player);
                damageArmor(boots, durabilityPercentage, player);
            }
        }
    }
    
    /**
     * 消耗盔甲指定百分比的耐久
     * @param armor 盔甲物品
     * @param percentage 耐久消耗百分比
     * @param player 玩家
     */
    private static void damageArmor(ItemStack armor, float percentage, Player player) {
        if (!armor.isEmpty() && armor.isEnchanted() 
                && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_SHATTER.get(), armor) > 0) {
            // 计算耐久消耗
            int maxDamage = armor.getMaxDamage();
            if (maxDamage > 0) {
                int damageToApply = Math.max(1, (int) (maxDamage * percentage)); // 指定百分比耐久，最少1点
                armor.hurtAndBreak(damageToApply, player, (p) -> {
                    // 当盔甲损坏时的回调，这里为空实现
                });
            }
        }
    }
}