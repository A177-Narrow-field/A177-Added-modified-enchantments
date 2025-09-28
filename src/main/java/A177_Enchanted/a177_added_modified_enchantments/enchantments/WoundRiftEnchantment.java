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
public class WoundRiftEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("wound_rift");
    }

    public WoundRiftEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
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
            // 计算所有盔甲上的伤裂附魔等级总和
            int totalLevel = 0;
            
            // 检查头盔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && helmet.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WOUND_RIFT.get(), helmet);
            }
            
            // 检查胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!chestplate.isEmpty() && chestplate.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WOUND_RIFT.get(), chestplate);
            }
            
            // 检查护腿
            ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
            if (!leggings.isEmpty() && leggings.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WOUND_RIFT.get(), leggings);
            }
            
            // 检查靴子
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (!boots.isEmpty() && boots.isEnchanted()) {
                totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WOUND_RIFT.get(), boots);
            }
            
            // 如果有任何伤裂附魔，根据等级增加受到的伤害
            if (totalLevel > 0) {
                // 每级增加20%伤害
                float multiplier = 1.0f + (0.2f * totalLevel);
                event.setAmount(event.getAmount() * multiplier);
            }
        }
    }
}