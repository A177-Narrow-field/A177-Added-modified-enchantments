package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
public class SuperStinkyFeetEnchantment extends Enchantment {
    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_SUPER_STINKY_FEET_CACHE = new WeakHashMap<>();
    
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Long> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    // 检查间隔 (20 ticks = 1秒)
    private static final int CHECK_INTERVAL = 20;
    
    // 基础范围半径
    private static final double BASE_RANGE = 3.0;
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("super_stinky_feet");
    }

    public SuperStinkyFeetEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 10;
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
        // 只能附在鞋子上
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
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
        
        // 设置下次检查时间为1秒后（20个tick）
        PLAYER_NEXT_CHECK_TIME.put(player, currentTime + CHECK_INTERVAL);
        
        // 获取缓存的附魔等级
        int cachedLevel = PLAYER_SUPER_STINKY_FEET_CACHE.getOrDefault(player, -1);
        int currentLevel = 0;
        
        // 检查鞋子是否有超级脚臭附魔
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.isEmpty() && boots.isEnchanted() && boots.getEnchantmentLevel(ModEnchantments.SUPER_STINKY_FEET.get()) > 0) {
            currentLevel = boots.getEnchantmentLevel(ModEnchantments.SUPER_STINKY_FEET.get());
        }
        
        // 如果等级发生变化，更新缓存
        if (cachedLevel != currentLevel) {
            PLAYER_SUPER_STINKY_FEET_CACHE.put(player, currentLevel);
        }
        
        // 如果有附魔等级，处理凋零效果逻辑
        if (currentLevel > 0) {
            applyWitherEffectToNearbyEntities(player, currentLevel);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.FEET) {
            return;
        }
        
        // 当鞋子装备变更时，立即清除缓存
        PLAYER_SUPER_STINKY_FEET_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
        
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int currentLevel = 0;
        if (!boots.isEmpty() && boots.isEnchanted() && boots.getEnchantmentLevel(ModEnchantments.SUPER_STINKY_FEET.get()) > 0) {
            currentLevel = boots.getEnchantmentLevel(ModEnchantments.SUPER_STINKY_FEET.get());
        }
        
        PLAYER_SUPER_STINKY_FEET_CACHE.put(player, currentLevel);
    }
    
    /**
     * 给玩家周围范围内的生物施加凋零效果
     * @param player 玩家
     * @param level 附魔等级
     */
    private static void applyWitherEffectToNearbyEntities(Player player, int level) {
        // 计算凋零效果持续时间（每级增加2秒，基础2秒）
        int duration = (2 + level * 2) * 20; // 转换为tick单位
        
        // 创建一个包围盒，覆盖玩家周围的区域
        AABB boundingBox = player.getBoundingBox().inflate(BASE_RANGE, BASE_RANGE, BASE_RANGE);
        
        // 获取范围内的所有生物实体
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, boundingBox);
        
        // 给范围内的生物施加凋零效果
        for (LivingEntity entity : entities) {
            // 跳过玩家自己（除非玩家正在蹲下）
            if (entity instanceof Player && !player.isCrouching()) {
                continue;
            }
            
            // 检查实体是否有效且不是玩家自己
            if (entity.isAlive() && entity != player) {
                // 施加凋零效果
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 0));
            }
        }
        
        // 如果玩家正在蹲下，也给自己施加凋零效果
        if (player.isCrouching()) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 0));
        }
    }
}