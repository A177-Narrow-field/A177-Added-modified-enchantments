package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class TimeAxisDecayEnchantment extends Enchantment {
    // 消耗间隔（tick）
    private static final int DECAY_INTERVAL = 200; // 10秒 = 200 ticks
    // 消耗百分比
    private static final double DECAY_PERCENTAGE = 0.01; // 1%

    // 存储物品数据（使用WeakHashMap自动清理无引用的物品）
    private static final WeakHashMap<ItemStack, Integer> itemTickMap = new WeakHashMap<>();

    public TimeAxisDecayEnchantment() {
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
    public boolean canEnchant(ItemStack stack) {
        // 可以附魔在有耐久的物品上
        return stack.isDamageableItem();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.TIME_AXIS_DECAY.isTreasureOnly.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.TIME_AXIS_DECAY.isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.TIME_AXIS_DECAY.isDiscoverable.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player != null && !event.player.level().isClientSide) {
            // 遍历玩家物品栏中的所有物品
            for (ItemStack stack : event.player.getInventory().items) {
                processItemStack(stack, event.player.tickCount);
            }
            
            // 检查玩家装备的物品
            for (ItemStack stack : event.player.getInventory().armor) {
                processItemStack(stack, event.player.tickCount);
            }
            
            // 检查玩家副手物品
            ItemStack offhandStack = event.player.getInventory().offhand.get(0);
            processItemStack(offhandStack, event.player.tickCount);
            
            // 检查玩家主手物品
            ItemStack mainHandStack = event.player.getMainHandItem();
            processItemStack(mainHandStack, event.player.tickCount);
        }
    }

    private static void processItemStack(ItemStack stack, int tickCount) {
        if (!stack.isEmpty()) {
            int decayLevel = stack.getEnchantmentLevel(ModEnchantments.TIME_AXIS_DECAY.get());
            
            if (decayLevel > 0) {
                // 获取物品的tick计数器
                Integer lastTickCount = itemTickMap.get(stack);
                if (lastTickCount == null) {
                    lastTickCount = tickCount;
                    itemTickMap.put(stack, lastTickCount);
                }

                // 检查是否达到消耗间隔
                if (tickCount - lastTickCount >= DECAY_INTERVAL) {
                    // 消耗耐久
                    decayItem(stack);
                    // 更新tick计数器
                    itemTickMap.put(stack, tickCount);
                }
            }
        }
    }

    private static void decayItem(ItemStack stack) {
        // 不需要检查UNBREAKING附魔，直接处理已应用本附魔的物品

        // 获取物品当前耐久信息
        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamageValue();
        int remainingDurability = maxDamage - currentDamage;

        // 如果物品仅剩1%耐久或者10点耐久，就不再消耗
        if (remainingDurability <= Math.max(1, maxDamage / 100) || remainingDurability <= 10) {
            return;
        }

        // 计算要消耗的耐久值（最少1点）
        int decayAmount = Math.max(1, (int) (maxDamage * DECAY_PERCENTAGE));

        // 应用耐久消耗
        stack.setDamageValue(currentDamage + decayAmount);
    }
}