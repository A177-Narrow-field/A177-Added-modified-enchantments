package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.List;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class LootAttractionEnchantment extends Enchantment {
    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_LOOT_ATTRACTION_CACHE = new WeakHashMap<>();
    
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Long> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    // 检查间隔 (1 ticks = 0.05秒)
    private static final int CHECK_INTERVAL = 1;
    
    // 基础拾取范围(默认Minecraft是1.5格)
    private static final double BASE_PICKUP_RANGE = 1.5;
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("loot_attraction");
    }

    public LootAttractionEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 5;
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        Player player = event.player;
        long currentTime = player.level().getGameTime();
        
        // 获取玩家的下次检查时间
        Long nextCheckTime = PLAYER_NEXT_CHECK_TIME.get(player);
        if (nextCheckTime != null && currentTime < nextCheckTime) {
            return;
        }
        
        // 设置下次检查时间为0.05秒后（1个tick）
        PLAYER_NEXT_CHECK_TIME.put(player, currentTime + CHECK_INTERVAL);
        
        // 获取缓存的附魔等级
        int cachedLevel = PLAYER_LOOT_ATTRACTION_CACHE.getOrDefault(player, -1);
        int currentLevel = 0;
        
        // 检查胸甲是否有掉落吸引附魔
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestplate.isEmpty() && chestplate.isEnchanted() && chestplate.getEnchantmentLevel(ModEnchantments.LOOT_ATTRACTION.get()) > 0) {
            currentLevel = chestplate.getEnchantmentLevel(ModEnchantments.LOOT_ATTRACTION.get());
        }
        
        // 如果等级发生变化，更新缓存
        if (cachedLevel != currentLevel) {
            PLAYER_LOOT_ATTRACTION_CACHE.put(player, currentLevel);
        }
        
        // 如果有附魔等级，且玩家正在蹲下，处理物品吸引逻辑
        if (currentLevel > 0 && player.isCrouching()) {
            attractItemsToPlayer(player, currentLevel);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.CHEST) {
            return;
        }
        
        // 当胸甲装备变更时，立即清除缓存
        PLAYER_LOOT_ATTRACTION_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
        
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int currentLevel = 0;
        if (!chestplate.isEmpty() && chestplate.isEnchanted() && chestplate.getEnchantmentLevel(ModEnchantments.LOOT_ATTRACTION.get()) > 0) {
            currentLevel = chestplate.getEnchantmentLevel(ModEnchantments.LOOT_ATTRACTION.get());
        }
        
        PLAYER_LOOT_ATTRACTION_CACHE.put(player, currentLevel);
    }
    
    /**
     * 将范围内的物品吸引到玩家身边
     * @param player 玩家
     * @param level 附魔等级
     */
    private static void attractItemsToPlayer(Player player, int level) {
        // 计算增加的拾取范围（每级增加3格）
        double additionalRange = level * 3;
        double totalRange = BASE_PICKUP_RANGE + additionalRange;
        
        // 创建一个包围盒，覆盖玩家周围的区域
        AABB boundingBox = player.getBoundingBox().inflate(totalRange, totalRange, totalRange);
        
        // 获取范围内的所有物品实体
        List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, boundingBox);
        
        // 将物品吸引到玩家身边
        for (ItemEntity item : items) {
            // 检查物品是否可以被拾取
            if (!item.isAlive() || item.hasPickUpDelay()) {
                continue;
            }
            
            // 计算物品到玩家的距离
            double distance = item.distanceTo(player);
            
            // 如果物品在玩家的增加拾取范围内，则吸引它
            if (distance <= totalRange && distance > BASE_PICKUP_RANGE) {
                // 计算吸引方向（从物品指向玩家）
                double dx = player.getX() - item.getX();
                double dy = player.getY() + player.getEyeHeight() - item.getY();
                double dz = player.getZ() - item.getZ();
                
                // 归一化向量并乘以吸引速度
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (length > 0) {
                    double speed = 0.5; // 吸引速度
                    dx = (dx / length) * speed;
                    dy = (dy / length) * speed;
                    dz = (dz / length) * speed;
                    
                    // 设置物品的速度，使其向玩家移动
                    item.setDeltaMovement(dx, dy, dz);
                }
            }
        }
    }
}