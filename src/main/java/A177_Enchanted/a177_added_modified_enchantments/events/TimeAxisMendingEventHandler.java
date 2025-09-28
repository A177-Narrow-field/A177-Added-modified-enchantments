package A177_Enchanted.a177_added_modified_enchantments.events;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class TimeAxisMendingEventHandler {
    
    // 记录每个玩家的下次更新时间（游戏刻）- 高级时针轴修补
    private static final Map<UUID, Long> ADVANCED_TIME_AXIS_NEXT_UPDATE_TIME = new HashMap<>();
    
    // 记录每个玩家的下次更新时间（游戏刻）- 时针修补
    private static final Map<UUID, Long> TIME_AXIS_NEXT_UPDATE_TIME = new HashMap<>();
    
    // 记录每个玩家的下次更新时间（游戏刻）- 初级时针修补
    private static final Map<UUID, Long> PRIMITIVE_TIME_AXIS_NEXT_UPDATE_TIME = new HashMap<>();
    
    // 缓存玩家是否拥有附魔的状态，避免每tick都检查
    private static final Map<UUID, Boolean> PLAYER_HAS_MENDING_ENCHANTMENTS = new HashMap<>();
    
    // 更新间隔（ticks）- 高级时针轴修补 = 20 ticks = 1秒
    private static final int ADVANCED_TIME_AXIS_UPDATE_INTERVAL = 20;
    
    // 更新间隔（ticks）- 时针修补 = 60 ticks = 3秒
    private static final int TIME_AXIS_UPDATE_INTERVAL = 60;
    
    // 更新间隔（ticks）- 初级时针修补 = 160 ticks = 8秒
    private static final int PRIMITIVE_TIME_AXIS_UPDATE_INTERVAL = 160;
    
    // 附魔等级对应的耐久修复概率 - 高级时针轴修补
    private static final double[] ADVANCED_TIME_AXIS_REPAIR_CHANCES = {0.0, 0.15, 0.25, 0.35, 0.45, 0.60};
    
    // 附魔等级对应的耐久修复概率 - 时针修补
    private static final double[] TIME_AXIS_REPAIR_CHANCES = {0.0, 0.10, 0.20, 0.30, 0.40, 0.50};
    
    // 附魔等级对应的耐久修复概率 - 初级时针修补
    private static final double[] PRIMITIVE_TIME_AXIS_REPAIR_CHANCES = {0.0, 0.05, 0.10, 0.15, 0.20, 0.25};
    
    // 最大附魔等级
    private static final int MAX_ENCHANTMENT_LEVEL = 5;
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        
        Player player = event.player;
        UUID playerId = player.getUUID();
        
        // 检查玩家是否拥有任何时针修补类附魔，如果没有则跳过处理
        if (!doesPlayerHaveMendingEnchantments(player)) {
            // 清除该玩家的缓存数据以节省内存
            clearPlayerData(playerId);
            return;
        }

        long currentTime = player.level().getGameTime();

        // 处理高级时针轴修补附魔
        handleAdvancedTimeAxisMending(player, playerId, currentTime);

        // 处理时针修补附魔（高级）
        handleTimeAxisMending(player, playerId, currentTime);

        // 处理初级时针修补附魔
        handlePrimitiveTimeAxisMending(player, playerId, currentTime);
    }
    
    /**
     * 玩家登出时清除其缓存数据
     * @param event 玩家登出事件
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        clearPlayerData(playerId);
    }
    
    /**
     * 玩家物品变更时更新附魔缓存
     * @param event 玩家物品变更事件
     */
    @SubscribeEvent
    public static void onPlayerItemChange(PlayerEvent.ItemPickupEvent event) {
        UUID playerId = event.getEntity().getUUID();
        // 移除玩家附魔状态缓存，下次检查时会重新计算
        PLAYER_HAS_MENDING_ENCHANTMENTS.remove(playerId);
    }
    
    /**
     * 玩家使用物品时更新附魔缓存（可能与附魔台交互）
     * @param event 玩家右键点击事件
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        UUID playerId = event.getEntity().getUUID();
        // 移除玩家附魔状态缓存，下次检查时会重新计算
        PLAYER_HAS_MENDING_ENCHANTMENTS.remove(playerId);
    }
    
    /**
     * 玩家物品变更时更新附魔缓存
     * @param event 玩家物品变更事件
     */
    @SubscribeEvent
    public static void onPlayerItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        UUID playerId = event.getEntity().getUUID();
        // 移除玩家附魔状态缓存，下次检查时会重新计算
        PLAYER_HAS_MENDING_ENCHANTMENTS.remove(playerId);
    }
    
    /**
     * 检查玩家是否拥有任何时针修补类附魔
     * @param player 玩家
     * @return 是否拥有附魔
     */
    private static boolean doesPlayerHaveMendingEnchantments(Player player) {
        UUID playerId = player.getUUID();
        
        // 首先检查缓存
        if (PLAYER_HAS_MENDING_ENCHANTMENTS.containsKey(playerId)) {
            Boolean result = PLAYER_HAS_MENDING_ENCHANTMENTS.get(playerId);
            // 处理可能的null值
            if (result != null) {
                return result;
            }
            // 如果结果为null，继续执行检查逻辑
        }
        
        // 检查玩家物品是否拥有附魔
        boolean hasEnchantments = checkPlayerItemsForEnchantments(player);
        
        // 缓存结果
        PLAYER_HAS_MENDING_ENCHANTMENTS.put(playerId, hasEnchantments);
        
        return hasEnchantments;
    }
    
    /**
     * 检查玩家物品是否拥有时针修补类附魔
     * @param player 玩家
     * @return 是否拥有附魔
     */
    private static boolean checkPlayerItemsForEnchantments(Player player) {
        // 检查主手物品
        if (hasAnyMendingEnchantment(player.getMainHandItem())) {
            return true;
        }
        
        // 检查副手物品
        if (hasAnyMendingEnchantment(player.getOffhandItem())) {
            return true;
        }
        
        // 检查装备的物品
        for (ItemStack stack : player.getArmorSlots()) {
            if (hasAnyMendingEnchantment(stack)) {
                return true;
            }
        }
        
        // 检查背包中的物品
        for (ItemStack stack : player.getInventory().items) {
            if (hasAnyMendingEnchantment(stack)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查物品是否拥有任何时针修补类附魔
     * @param stack 物品堆
     * @return 是否拥有附魔
     */
    private static boolean hasAnyMendingEnchantment(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 检查是否拥有高级时针轴修补附魔
        if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.ADVANCED_TIME_AXIS_MENDING.get(), stack) > 0) {
            return true;
        }
        
        // 检查是否拥有时针修补附魔
        if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.TIME_AXIS_MENDING.get(), stack) > 0) {
            return true;
        }
        
        // 检查是否拥有初级时针修补附魔
        if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.PRIMITIVE_TIME_AXIS_MENDING.get(), stack) > 0) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 清除玩家的缓存数据以节省内存
     * @param playerId 玩家UUID
     */
    private static void clearPlayerData(UUID playerId) {
        ADVANCED_TIME_AXIS_NEXT_UPDATE_TIME.remove(playerId);
        TIME_AXIS_NEXT_UPDATE_TIME.remove(playerId);
        PRIMITIVE_TIME_AXIS_NEXT_UPDATE_TIME.remove(playerId);
        PLAYER_HAS_MENDING_ENCHANTMENTS.remove(playerId);
    }
    
    /**
     * 检查并修复高级时针轴修补附魔的物品
     * @param stack 物品堆
     */
    private static void checkAndRepairAdvancedTimeAxisItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.isDamaged()) {
            int level = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ADVANCED_TIME_AXIS_MENDING.get(), stack);
            if (level > 0) {
                // 计算应该恢复的耐久度（每级+5%耐久，最少回复5点）
                int maxDamage = stack.getMaxDamage();
                int restoreAmount = Math.max(5, (int) (maxDamage * level * 0.05));

                if (restoreAmount > 0) {
                    // 修复物品
                    stack.setDamageValue(Math.max(0, stack.getDamageValue() - restoreAmount));
                }
            }
        }
    }

    /**
     * 检查并修复时针修补附魔的物品
     * @param stack 物品堆
     */
    private static void checkAndRepairTimeAxisItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.isDamaged()) {
            int level = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TIME_AXIS_MENDING.get(), stack);
            if (level > 0) {
                // 计算应该恢复的耐久度（每级+1%耐久，最少回复1点）
                int maxDamage = stack.getMaxDamage();
                int restoreAmount = Math.max(1, (int) (maxDamage * level * 0.01));

                if (restoreAmount > 0) {
                    // 修复物品
                    stack.setDamageValue(Math.max(0, stack.getDamageValue() - restoreAmount));
                }
            }
        }
    }

    /**
     * 检查并修复初级时针修补附魔的物品
     * @param stack 物品堆
     */
    private static void checkAndRepairPrimitiveTimeAxisItem(ItemStack stack) {
        if (!stack.isEmpty() && stack.isDamaged()) {
            int level = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PRIMITIVE_TIME_AXIS_MENDING.get(), stack);
            if (level > 0) {
                // 计算应该恢复的耐久度（每级+1%耐久，最少回复1点）
                int maxDamage = stack.getMaxDamage();
                int restoreAmount = Math.max(1, (int) (maxDamage * level * 0.01));

                if (restoreAmount > 0) {
                    // 修复物品
                    stack.setDamageValue(Math.max(0, stack.getDamageValue() - restoreAmount));
                }
            }
        }
    }
    
    /**
     * 检查并修复高级时针轴修补附魔的物品（包括背包内的物品）
     * @param player 玩家
     * @param playerId 玩家UUID
     * @param currentTime 当前游戏时间
     */
    private static void handleAdvancedTimeAxisMending(Player player, UUID playerId, long currentTime) {
        // 检查是否到了更新时间
        long nextUpdateTime = ADVANCED_TIME_AXIS_NEXT_UPDATE_TIME.getOrDefault(playerId, 0L);
        if (currentTime < nextUpdateTime) {
            return;
        }

        // 更新下次更新时间
        ADVANCED_TIME_AXIS_NEXT_UPDATE_TIME.put(playerId, currentTime + ADVANCED_TIME_AXIS_UPDATE_INTERVAL);

        // 检查玩家身上的所有物品
        // 检查主手和副手物品
        checkAndRepairAdvancedTimeAxisItem(player.getMainHandItem());
        checkAndRepairAdvancedTimeAxisItem(player.getOffhandItem());

        // 检查穿戴的装备
        player.getArmorSlots().forEach(TimeAxisMendingEventHandler::checkAndRepairAdvancedTimeAxisItem);
        
        // 检查背包中的物品
        player.getInventory().items.forEach(TimeAxisMendingEventHandler::checkAndRepairAdvancedTimeAxisItem);
    }

    /**
     * 处理时针修补附魔
     * @param player 玩家
     * @param playerId 玩家UUID
     * @param currentTime 当前游戏时间
     */
    private static void handleTimeAxisMending(Player player, UUID playerId, long currentTime) {
        // 检查是否到了更新时间
        long nextUpdateTime = TIME_AXIS_NEXT_UPDATE_TIME.getOrDefault(playerId, 0L);
        if (currentTime < nextUpdateTime) {
            return;
        }

        // 更新下次更新时间
        TIME_AXIS_NEXT_UPDATE_TIME.put(playerId, currentTime + TIME_AXIS_UPDATE_INTERVAL);

        // 检查玩家身上的所有物品
        // 检查主手和副手物品
        checkAndRepairTimeAxisItem(player.getMainHandItem());
        checkAndRepairTimeAxisItem(player.getOffhandItem());

        // 检查穿戴的装备
        player.getArmorSlots().forEach(TimeAxisMendingEventHandler::checkAndRepairTimeAxisItem);
        
        // 检查背包中的物品
        player.getInventory().items.forEach(TimeAxisMendingEventHandler::checkAndRepairTimeAxisItem);
    }

    /**
     * 处理初级时针修补附魔
     * @param player 玩家
     * @param playerId 玩家UUID
     * @param currentTime 当前游戏时间
     */
    private static void handlePrimitiveTimeAxisMending(Player player, UUID playerId, long currentTime) {
        // 检查是否到了更新时间
        long nextUpdateTime = PRIMITIVE_TIME_AXIS_NEXT_UPDATE_TIME.getOrDefault(playerId, 0L);
        if (currentTime < nextUpdateTime) {
            return;
        }

        // 更新下次更新时间
        PRIMITIVE_TIME_AXIS_NEXT_UPDATE_TIME.put(playerId, currentTime + PRIMITIVE_TIME_AXIS_UPDATE_INTERVAL);

        // 检查玩家身上的所有物品
        // 检查主手和副手物品
        checkAndRepairPrimitiveTimeAxisItem(player.getMainHandItem());
        checkAndRepairPrimitiveTimeAxisItem(player.getOffhandItem());

        // 检查穿戴的装备
        player.getArmorSlots().forEach(TimeAxisMendingEventHandler::checkAndRepairPrimitiveTimeAxisItem);
        
        // 检查背包中的物品
        player.getInventory().items.forEach(TimeAxisMendingEventHandler::checkAndRepairPrimitiveTimeAxisItem);
    }
}