package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class LeapForceEnchantment extends Enchantment {

    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("leap_force");
    }

    public LeapForceEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_LEGS, 
              new EquipmentSlot[]{EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 10 + 20 * (enchantmentLevel - 1);
    }
    
    @Override
    public int getMaxCost(int enchantmentLevel) {
        return super.getMinCost(enchantmentLevel) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在护腿和靴子上
        return EnchantmentCategory.ARMOR_LEGS.canEnchant(stack.getItem()) || 
               EnchantmentCategory.ARMOR_FEET.canEnchant(stack.getItem());
    }
    
    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }//可以在附魔台


    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        // 检查实体是否为玩家
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否穿戴带有跃力附魔的护腿或靴子
            ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
            int level = Math.max(
                legs.getEnchantmentLevel(ModEnchantments.LEAP_FORCE.get()),
                feet.getEnchantmentLevel(ModEnchantments.LEAP_FORCE.get())
            );
            
            if (level > 0) {
                // 增加跳跃高度，每级增加0.15倍
                player.setDeltaMovement(player.getDeltaMovement().add(0, level * 0.15, 0));
            }
        }
    }
}