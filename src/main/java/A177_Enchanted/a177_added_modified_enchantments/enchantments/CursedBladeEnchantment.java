package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class CursedBladeEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("cursed_blade");
    }
    
    public CursedBladeEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 30;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器上（包括剑和其他武器类型）
        return stack.getItem() instanceof SwordItem || EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是武器时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public boolean isTreasureOnly() {
        // 从配置文件读取是否为宝藏附魔
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : false;
    }

    @Override
    public boolean isDiscoverable() {
        // 从配置文件读取是否可发现
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : true;
    }

    @Override
    public boolean isTradeable() {
        // 从配置文件读取是否可交易
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTradeable.get() : true;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // 处理诅咒之刃的伤害加成
        if (event.getSource().getDirectEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (!mainHandItem.isEmpty() && mainHandItem.getEnchantmentLevel(ModEnchantments.CURSED_BLADE.get()) > 0) {
                // 提升10倍伤害
                event.setAmount(event.getAmount() * 10.0f);
            }
        }
        
        // 当玩家受到伤害时检查是否携带诅咒之刃附魔的物品
        if (event.getEntity() instanceof Player player) {
            // 检查玩家物品栏中是否有带诅咒之刃附魔的物品
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.CURSED_BLADE.get()) > 0) {
                    // 立即清空玩家血量
                    player.setHealth(0);
                    break; // 只需要触发一次
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        // 当玩家死亡时，移除其携带的诅咒之刃物品
        if (event.getEntity() instanceof Player player) {
            // 检查玩家物品栏中的所有物品
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.CURSED_BLADE.get()) > 0) {
                    // 清除这个物品
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
            
            // 检查装备槽
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.CURSED_BLADE.get()) > 0) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
            
            // 检查主手和副手
            if (!player.getMainHandItem().isEmpty() && 
                player.getMainHandItem().getEnchantmentLevel(ModEnchantments.CURSED_BLADE.get()) > 0) {
                player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
            }

            if (!player.getOffhandItem().isEmpty() && 
                player.getOffhandItem().getEnchantmentLevel(ModEnchantments.CURSED_BLADE.get()) > 0) {
                player.getInventory().offhand.set(0, ItemStack.EMPTY);
            }
        }
    }
}