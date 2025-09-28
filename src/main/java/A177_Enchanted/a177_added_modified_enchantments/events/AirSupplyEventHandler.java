package A177_Enchanted.a177_added_modified_enchantments.events;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.tags.FluidTags;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class AirSupplyEventHandler {
    // 存储玩家自定义氧气值
    private static final Map<UUID, Integer> customAirSupplies = new HashMap<>();
    
    // 存储玩家窒息计时器
    private static final Map<UUID, Integer> suffocationTimers = new HashMap<>();
    
    // 最大氧气值（与水下相同）
    private static final int MAX_AIR_SUPPLY = 300;
    
    // 氧气恢复速度（与原版相同）
    private static final int AIR_RECOVERY_RATE = 1;
    
    // 窒息伤害间隔 (20 ticks = 1秒)
    private static final int SUFFOCATION_INTERVAL = 20;
    
    // 窒息伤害值
    private static final float SUFFOCATION_DAMAGE = 2.0f;
    
    // 各种附魔的氧气消耗值
    private static final int HOLD_BREATH_CONSUMPTION = 2; // 憋气附魔每tick消耗
    private static final int SHORT_BREATH_SPRINT_CONSUMPTION = 1; // 气短附魔疾跑消耗
    private static final int HIGH_ALTITUDE_OXYGEN_DEFICIENCY_CONSUMPTION = 1; // 高原缺氧附魔每秒消耗
    private static final int OXYGEN_DEPLETING_SPRINT_CONSUMPTION = 2; // 耗氧冲刺附魔每秒消耗
    private static final int DEEP_PRESSURE_CONSUMPTION = 1; // 深压附魔每秒消耗
    private static final int HOVER_CONSUMPTION = 2; // 悬停附魔每tick消耗

    public static int getCustomAirSupply(UUID playerId) {
        return customAirSupplies.getOrDefault(playerId, -1);
    }

    public static void setCustomAirSupply(UUID playerId, int airSupply) {
        customAirSupplies.put(playerId, airSupply);
    }

    public static void removeCustomAirSupply(UUID playerId) {
        customAirSupplies.remove(playerId);
        suffocationTimers.remove(playerId);
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        UUID playerId = player.getUUID();
        
        // 检查玩家是否在水中（水中使用原版机制）
        boolean inWater = player.isEyeInFluid(FluidTags.WATER);
        
        if (inWater) {
            // 在水中时清理自定义氧气值
            if (customAirSupplies.containsKey(playerId)) {
                customAirSupplies.remove(playerId);
                suffocationTimers.remove(playerId);
            }
            return;
        }

        // 检查玩家是否拥有任何与氧气相关的附魔
        boolean hasAnyAirEnchantment = false;
        
        // 检查头盔附魔
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean hasDeepPressureEnchantment = false;
        if (!helmet.isEmpty()) {
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOLD_BREATH.get(), helmet) > 0 ||
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHORT_BREATH.get(), helmet) > 0 ||
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HIGH_ALTITUDE_OXYGEN_DEFICIENCY.get(), helmet) > 0) {
                hasAnyAirEnchantment = true;
            }
            
            // 检查深压附魔
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DEEP_PRESSURE.get(), helmet) > 0) {
                hasDeepPressureEnchantment = true;
                hasAnyAirEnchantment = true;
            }
        }
        
        // 检查靴子附魔
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.isEmpty()) {
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OXYGEN_DEPLETING_SPRINT.get(), boots) > 0 ||
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOVER.get(), boots) > 0) {
                hasAnyAirEnchantment = true;
            }
        }

        // 获取当前自定义氧气值（如果存在）
        Integer customAir = customAirSupplies.get(playerId);

        // 如果玩家拥有任何氧气相关附魔
        if (hasAnyAirEnchantment) {
            // 初始化或获取自定义氧气值
            if (customAir == null) {
                customAir = player.getAirSupply(); // 使用当前氧气值作为起点
                customAirSupplies.put(playerId, customAir);
            }

            // 计算氧气变化
            int airChange = 0;
            
            // 处理憋气附魔
            if (!helmet.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOLD_BREATH.get(), helmet) > 0) {
                if (player.isCrouching()) {
                    // 蹲下时消耗氧气
                    airChange -= HOLD_BREATH_CONSUMPTION;
                }
            }
            
            // 处理气短附魔
            if (!helmet.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHORT_BREATH.get(), helmet) > 0) {
                if (player.isSprinting()) {
                    // 疾跑时消耗氧气
                    airChange -= SHORT_BREATH_SPRINT_CONSUMPTION;
                }
            }
            
            // 处理高原缺氧附魔
            if (!helmet.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HIGH_ALTITUDE_OXYGEN_DEFICIENCY.get(), helmet) > 0) {
                // 当玩家在高于Y坐标100的位置时，每秒消耗氧气
                if (player.blockPosition().getY() > 100) {
                    // 每20 ticks（1秒）消耗1点氧气
                    airChange -= HIGH_ALTITUDE_OXYGEN_DEFICIENCY_CONSUMPTION;
                }
            }
            
            // 处理深压附魔
            if (hasDeepPressureEnchantment) {
                // 当玩家在Y坐标0层以下时，每秒消耗氧气
                if (player.blockPosition().getY() < 0) {
                    // 每20 ticks（1秒）消耗1点氧气
                    airChange -= DEEP_PRESSURE_CONSUMPTION;
                }
            }
            
            // 处理耗氧冲刺附魔
            if (!boots.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OXYGEN_DEPLETING_SPRINT.get(), boots) > 0) {
                // 当玩家疾跑时，每秒消耗氧气
                if (player.isSprinting()) {
                    // 每20 ticks（1秒）消耗1点氧气
                    airChange -= OXYGEN_DEPLETING_SPRINT_CONSUMPTION;

                }
            }
            
            // 处理悬停附魔
            if (!boots.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOVER.get(), boots) > 0) {
                // 当玩家蹲下时消耗氧气
                if (player.isCrouching()) {
                    // 蹲下时每tick消耗氧气
                    airChange -= HOVER_CONSUMPTION;
                }
            }
            
            // 如果没有消耗氧气，则恢复氧气
            if (airChange >= 0) {
                airChange += AIR_RECOVERY_RATE;
            }
            
            // 应用氧气变化
            customAir += airChange;

            // 限制氧气值范围
            if (customAir < -20) {
                customAir = -20;
            } else if (customAir > MAX_AIR_SUPPLY) {
                customAir = MAX_AIR_SUPPLY;
            }

            // 处理窒息伤害
            if (customAir <= -20) {
                // 获取窒息计时器
                int timer = suffocationTimers.getOrDefault(playerId, 0);
                timer++;
                
                // 每隔一定时间造成窒息伤害
                if (timer >= SUFFOCATION_INTERVAL) {
                    player.hurt(player.damageSources().drown(), SUFFOCATION_DAMAGE);
                    timer = 0; // 重置计时器
                }
                
                // 更新计时器
                suffocationTimers.put(playerId, timer);
            } else {
                // 氧气值正常时清除窒息计时器
                suffocationTimers.remove(playerId);
            }

            // 更新氧气值
            customAirSupplies.put(playerId, customAir);
            player.setAirSupply(customAir);

            // 如果氧气已满且不需要消耗氧气，清除自定义氧气记录
            if (customAir >= MAX_AIR_SUPPLY && airChange >= 0) {
                customAirSupplies.remove(playerId);
                suffocationTimers.remove(playerId);
            }
            return;
        }

        // 玩家没有佩戴任何氧气相关附魔
        if (customAir != null) {
            // 恢复氧气
            customAir += AIR_RECOVERY_RATE;

            // 限制不超过最大氧气值
            if (customAir > MAX_AIR_SUPPLY) {
                customAir = MAX_AIR_SUPPLY;
            }

            // 更新氧气值
            customAirSupplies.put(playerId, customAir);
            player.setAirSupply(customAir);

            // 如果氧气已满，清除自定义氧气记录
            if (customAir >= MAX_AIR_SUPPLY) {
                customAirSupplies.remove(playerId);
                suffocationTimers.remove(playerId);
            }
        }
    }
    
    /**
     * 当玩家重新进入游戏时，清理可能存在的窒息计时器
     * 防止因游戏暂停导致的持续窒息问题
     */
    public static void onPlayerJoin(UUID playerId) {
        // 清理窒息计时器，防止玩家重新进入游戏时持续窒息
        suffocationTimers.remove(playerId);
    }
    
    /**
     * 玩家登录游戏时调用，清理可能存在的窒息计时器
     * 防止因游戏暂停导致的持续窒息问题
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            onPlayerJoin(player.getUUID());
        }
    }
    
    /**
     * 玩家登出游戏时调用，清理玩家数据
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            removeCustomAirSupply(player.getUUID());
        }
    }
    
    /**
     * 玩家改变维度时调用，清理可能存在的窒息计时器
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            onPlayerJoin(player.getUUID());
        }
    }
}