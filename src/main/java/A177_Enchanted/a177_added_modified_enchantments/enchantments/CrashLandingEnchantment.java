package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

public class CrashLandingEnchantment extends Enchantment {
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("crash_landing");
    }
    
    public CrashLandingEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public int getMinCost(int level) {
        return 15;
    }
    
    @Override
    public int getMaxCost(int level) {
        return 50;
    }
    
    @Override
    public int getMaxLevel() {
        return 1; // 只有1级
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get(); // 使用配置文件设置
    }
    
    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get(); // 使用配置文件设置
    }
    
    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get(); // 使用配置文件设置
    }
    
    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在靴子上
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack) && isDiscoverable();
    }
    
    // 处理坠落事件，增加坠落伤害
    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // 检查玩家是否穿着带有坠机附魔的靴子
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int level = boots.getEnchantmentLevel(this);
        
        // 如果有坠机附魔，则增加坠落伤害
        if (level > 0) {
            // 增加2.4倍坠落伤害（原本是1倍，现在是2.4倍伤害）
            event.setDistance(event.getDistance() * 2.4F);
        }
    }
}