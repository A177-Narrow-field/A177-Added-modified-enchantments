package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.events.AirSupplyEventHandler;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class BreathHealEnchantment extends Enchantment {
    // 每次回复的生命值
    private static final float HEALTH_PER_HEAL = 2.0f;
    
    // 回复间隔 (30 ticks = 1.5秒)
    private static final int HEAL_INTERVAL = 30;
    
    // 存储玩家的回复计时器
    private static final Map<UUID, Integer> healTimers = new HashMap<>();
    
    // 存储玩家上一次的氧气值，用于判断是否在恢复氧气
    private static final Map<UUID, Integer> lastAirSupplies = new HashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("breath_heal");
    }

    public BreathHealEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD;
    }
    
    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }


    @Override
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            UUID playerId = player.getUUID();
            
            // 获取玩家头盔上的呼吸回血附魔等级
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BREATH_HEAL.get(), helmet);
            
            // 只在玩家拥有附魔时处理效果
            if (enchantmentLevel > 0) {
                // 检查玩家是否有有效的头盔
                if (helmet.isEmpty() || !(helmet.getItem() instanceof ArmorItem) || helmet.getDamageValue() >= helmet.getMaxDamage()) {
                    return; // 如果没有头盔或头盔已损坏，则不触发效果
                }
                
                // 从统一系统获取当前氧气值
                int currentAir = AirSupplyEventHandler.getCustomAirSupply(playerId);
                if (currentAir == -1) {
                    // 如果没有自定义氧气值，使用玩家当前氧气值
                    currentAir = player.getAirSupply();
                }
                
                // 获取上一次的氧气值
                Integer lastAir = lastAirSupplies.get(playerId);
                if (lastAir == null) {
                    lastAir = currentAir;
                }
                
                // 更新计时器
                Integer timer = healTimers.get(playerId);
                if (timer == null) {
                    timer = 0;
                }
                timer++;
                
                // 只有在玩家氧气正在恢复时才回复生命值（当前氧气值大于上一次的氧气值）
                if (currentAir > lastAir) {
                    // 如果计时器达到间隔，则回复生命值
                    if (timer >= HEAL_INTERVAL) {
                        // 回复生命值
                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(HEALTH_PER_HEAL);
                        }
                        
                        // 重置计时器
                        timer = 0;
                    }
                } else {
                    // 如果氧气没有在恢复，重置计时器
                    timer = 0;
                }
                
                // 更新记录
                healTimers.put(playerId, timer);
                lastAirSupplies.put(playerId, currentAir);
            } else {
                // 如果玩家没有该附魔，移除其记录
                healTimers.remove(playerId);
                lastAirSupplies.remove(playerId);
            }
        }
    }
}