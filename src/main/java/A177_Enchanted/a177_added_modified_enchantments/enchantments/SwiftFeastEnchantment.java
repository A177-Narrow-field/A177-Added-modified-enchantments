package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class SwiftFeastEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("swift_feast");
    }

    public SwiftFeastEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }
    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 50 + (level - 1) * 50;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在头盔上
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是头盔时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }//确保在附魔台中可以正确应用

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 不是宝藏附魔

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 可以通过交易获得

    
    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() instanceof Player player && (event.getItem().getUseAnimation() == UseAnim.EAT || event.getItem().getUseAnimation() == UseAnim.DRINK)) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.isEnchanted() && helmet.getEnchantmentLevel(ModEnchantments.SWIFT_FEAST.get()) > 0) {
                int level = helmet.getEnchantmentLevel(ModEnchantments.SWIFT_FEAST.get());
                // 等级越高，每次减少的持续时间越多，食用速度越快
                event.setDuration(event.getDuration() - Math.max(1, (int)(level * 1.2)));
            }
        }
    }
}