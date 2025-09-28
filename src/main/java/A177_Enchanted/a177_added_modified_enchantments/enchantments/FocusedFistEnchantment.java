package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class FocusedFistEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("focused_fist");
    }

    public FocusedFistEnchantment() {
        // 恢复为硬编码的稀有度，不再从配置文件读取
        super(Enchantment.Rarity.VERY_RARE, 
              EnchantmentCategory.ARMOR_CHEST, 
              new EquipmentSlot[]{EquipmentSlot.CHEST});
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
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是胸甲时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            // 检查玩家是否空手
            ItemStack mainHandItem = player.getMainHandItem();
            boolean isFistFighting = mainHandItem.isEmpty();

            // 检查玩家是否蹲下
            boolean isCrouching = player.isCrouching();

            // 检查玩家胸甲是否有专注拳甲附魔
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FOCUSED_FIST.get(), chestplate);

            // 只有在玩家空手、蹲下且胸甲有附魔时才应用效果
            if (isFistFighting && isCrouching && enchantmentLevel > 0) {
                // 伤害增加200%
                float damageMultiplier = 1.0f + (enchantmentLevel * 2.0f);
                event.setAmount(event.getAmount() * damageMultiplier);
            }
        }
    }
    
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        
        // 检查玩家是否空手
        ItemStack mainHandItem = player.getMainHandItem();
        boolean isEmptyHanded = mainHandItem.isEmpty();
        
        // 检查玩家是否蹲下
        boolean isCrouching = player.isCrouching();
        
        // 获取玩家胸甲上的专注拳甲附魔等级
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = 0;
        
        // 只有当玩家装备了胸甲、空手且蹲下时才应用附魔效果
        if (!chestplate.isEmpty() && isEmptyHanded && isCrouching) {
            enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FOCUSED_FIST.get(), chestplate);
        }
        
        // 如果有附魔等级，增加挖掘速度（增加500%）
        if (enchantmentLevel > 0) {
            // 增加150%挖掘速度
            event.setNewSpeed(event.getOriginalSpeed() * 1.5f);
        }
    }
}