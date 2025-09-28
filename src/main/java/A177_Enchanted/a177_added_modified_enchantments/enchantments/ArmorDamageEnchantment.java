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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mod.EventBusSubscriber
public class ArmorDamageEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("armor_damage");
    }

    public ArmorDamageEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
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
    }// 是否可以通过交易获得

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
            // 计算所有盔甲上的盔损附魔等级总和
            int totalLevel = 0;
            
            // 检查头盔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && helmet.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_DAMAGE.get(), helmet);
            }
            
            // 检查胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!chestplate.isEmpty() && chestplate.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_DAMAGE.get(), chestplate);
            }
            
            // 检查护腿
            ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
            if (!leggings.isEmpty() && leggings.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_DAMAGE.get(), leggings);
            }
            
            // 检查靴子
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (!boots.isEmpty() && boots.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_DAMAGE.get(), boots);
            }
            
            // 如果有任何盔损附魔，根据概率消耗盔甲耐久
            if (totalLevel > 0) {
                RandomSource random = player.level().random;
                // 每级增加30%概率
                float probability = 0.3f * totalLevel;
                
                // 检查是否触发效果
                if (random.nextFloat() < probability) {
                    // 消耗盔甲5%的耐久
                    damageArmor(helmet, random, player);
                    damageArmor(chestplate, random, player);
                    damageArmor(leggings, random, player);
                    damageArmor(boots, random, player);
                }
            }
        }
    }
    
    /**
     * 消耗盔甲5%的耐久
     * @param armor 盔甲物品
     * @param random 随机数生成器
     * @param player 玩家
     */
    private static void damageArmor(ItemStack armor, RandomSource random, Player player) {
        if (!armor.isEmpty() && armor.isEnchanted() 
                && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_DAMAGE.get(), armor) > 0) {
            // 计算5%的耐久消耗
            int maxDamage = armor.getMaxDamage();
            if (maxDamage > 0) {
                int damageToApply = Math.max(1, maxDamage / 20); // 5%耐久，最少1点
                armor.hurtAndBreak(damageToApply, player, (p) -> {
                    // 当盔甲损坏时的回调，这里为空实现
                });
            }
        }
    }
}