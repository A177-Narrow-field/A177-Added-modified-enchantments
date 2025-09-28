package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class PrimitiveLeapForceEnchantment extends Enchantment {

    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("primitive_leap_force");
    }

    public PrimitiveLeapForceEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_LEGS, new EquipmentSlot[]{EquipmentSlot.LEGS, EquipmentSlot.FEET});// 设置附魔类型为护腿和靴子
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
        return 3;
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
    }//可以正确的出现在附魔台

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在护腿和靴子上
        return EnchantmentCategory.ARMOR_LEGS.canEnchant(stack.getItem()) ||
               EnchantmentCategory.ARMOR_FEET.canEnchant(stack.getItem());
    }

    // 事件处理方法
    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // 检查玩家是否穿戴带有初级跃力附魔的护腿或靴子
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        int level = Math.max(
            legs.getEnchantmentLevel(ModEnchantments.PRIMITIVE_LEAP_FORCE.get()),
            feet.getEnchantmentLevel(ModEnchantments.PRIMITIVE_LEAP_FORCE.get())
        );
        
        if (level > 0) {
            // 增加跳跃高度（每级增加0.09垂直速度）
            player.setDeltaMovement(player.getDeltaMovement().add(0, level * 0.09, 0));
            
            // 增加饥饿度消耗（每级增加1%）
            player.causeFoodExhaustion((float) (level * 0.01));
        }
    }
    
}