package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class DesireFlameEnchantment extends Enchantment {
    // 存储玩家上次修复时间
    private static final Map<UUID, Long> lastRepairTimeMap = new HashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("desire_flame");
    }
    
    public DesireFlameEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在有耐久度的物品上
        return stack.isDamageableItem();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
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
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Player player = event.player;
        // 检查玩家是否正在燃烧
        if (player.getRemainingFireTicks() <= 0) return;
        
        // 检查修复冷却时间 (0.1秒 = 2 ticks)
        UUID playerId = player.getUUID();
        long currentTime = player.level().getGameTime();
        long lastRepairTime = lastRepairTimeMap.getOrDefault(playerId, 0L);
        
        if (currentTime - lastRepairTime < 2) return;
        
        // 遍历玩家所有物品栏寻找带有欲火神修附魔的物品
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESIRE_FLAME.get(), stack) > 0) {
                // 修复1点耐久度
                if (stack.getDamageValue() > 0) {
                    stack.setDamageValue(stack.getDamageValue() - 1);
                    // 更新上次修复时间
                    lastRepairTimeMap.put(playerId, currentTime);
                    break; // 每次tick只修复一个物品
                }
            }
        }
        
        // 检查玩家装备的物品
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESIRE_FLAME.get(), stack) > 0) {
                // 修复1点耐久度
                if (stack.getDamageValue() > 0) {
                    stack.setDamageValue(stack.getDamageValue() - 1);
                    // 更新上次修复时间
                    lastRepairTimeMap.put(playerId, currentTime);
                    break; // 每次tick只修复一个物品
                }
            }
        }
    }
}