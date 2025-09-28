package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class SwiftStepEnchantment extends Enchantment {
    public static final UUID SPEED_UUID = UUID.fromString("3a5f8cf0-7e4a-4f59-9f25-eb84c0e3b3a2");
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("swift_step");
    }
    
    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_SWIFT_STEP_CACHE = new WeakHashMap<>();
    
    // 更新间隔（ticks）
    private static final int UPDATE_INTERVAL = 20; // 1秒 (20 ticks = 1秒)
    
    // 加速效果持续时间（ticks）
    private static final int SPEED_DURATION = 40; // 2秒 (40 ticks = 2秒)
    
    // 缓存玩家的下次更新时间
    private static final WeakHashMap<Player, Long> PLAYER_NEXT_UPDATE_TIME = new WeakHashMap<>();
    
    // 玩家加速效果的到期时间缓存
    private static final WeakHashMap<Player, Long> PLAYER_SPEED_END_TIME = new WeakHashMap<>();

    /**
     * SwiftStepEnchantment类表示迅捷步伐附魔，为玩家提供移动速度加成。
     * 默认构造函数设置稀有度为RARE，附魔类别为ARMOR_FEET，装备槽位为FEET。
     */

    public SwiftStepEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }
    @Override
    public int getMaxLevel() {
        return 5;
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
    }// 是否可交易

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());}//确保在附魔台中可以正确应用

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能应用于鞋子
        return (stack.getItem() instanceof ArmorItem && 
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET) &&
                super.canEnchant(stack);
    }

    public static AttributeModifier getSpeedModifier(int level) {
        double amount = level * 0.025;
        return new AttributeModifier(SPEED_UUID, "Swift step speed bonus", amount, AttributeModifier.Operation.ADDITION);    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        
        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }
        
        long currentTime = player.level().getGameTime();
        
        // 检查加速效果是否应该结束
        Long speedEndTime = PLAYER_SPEED_END_TIME.get(player);
        if (speedEndTime != null && currentTime >= speedEndTime) {
            removeSwiftStepEffects(player);
            PLAYER_SPEED_END_TIME.remove(player);
        }

        // 检查是否需要更新效果
        Long nextUpdateTime = PLAYER_NEXT_UPDATE_TIME.get(player);
        if (nextUpdateTime == null || currentTime >= nextUpdateTime) {
            updatePlayerSwiftStepEffect(player);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || 
            event.getSlot() != EquipmentSlot.FEET) {
            return;
        }
        
        // 清除缓存，强制重新计算
        PLAYER_SWIFT_STEP_CACHE.remove(player);
        PLAYER_NEXT_UPDATE_TIME.remove(player);
        PLAYER_SPEED_END_TIME.remove(player);
        
        // 立即更新效果
        updatePlayerSwiftStepEffect(player);
    }
    
    /**
     * 更新玩家的迅捷步伐效果
     * @param player 玩家实体
     */
    private static void updatePlayerSwiftStepEffect(Player player) {
        long currentTime = player.level().getGameTime();
        
        // 检查玩家脚部装备
        int totalLevel = 0;

        ItemStack feetStack = player.getItemBySlot(EquipmentSlot.FEET);
        if (!feetStack.isEmpty()) {
            totalLevel += feetStack.getEnchantmentLevel(ModEnchantments.SWIFT_STEP.get());
        }

        // 检查缓存中是否已有该等级
        Integer cachedLevel = PLAYER_SWIFT_STEP_CACHE.get(player);
        if (cachedLevel == null || cachedLevel != totalLevel) {
            // 更新缓存
            PLAYER_SWIFT_STEP_CACHE.put(player, totalLevel);

            // 应用效果（如果有等级）
            if (totalLevel > 0) {
                applySwiftStepEffects(player, totalLevel);
                // 设置加速效果的到期时间
                PLAYER_SPEED_END_TIME.put(player, currentTime + SPEED_DURATION);
            } else {
                // 移除加速效果
                removeSwiftStepEffects(player);
                PLAYER_SPEED_END_TIME.remove(player);
            }
        } else if (totalLevel > 0) {
            // 即使等级未变化，也要更新效果的到期时间
            PLAYER_SPEED_END_TIME.put(player, currentTime + SPEED_DURATION);
        }
        
        // 更新下次检查时间
        PLAYER_NEXT_UPDATE_TIME.put(player, currentTime + UPDATE_INTERVAL);
    }
    
    /**
     * 应用迅捷步伐附魔效果
     * @param player 玩家实体
     * @param level 附魔等级
     */
    private static void applySwiftStepEffects(Player player, int level) {
        // 获取玩家移动速度属性实例
        net.minecraft.world.entity.ai.attributes.AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        // 移除旧的修饰符
        if (speedAttribute.getModifier(SPEED_UUID) != null) {
            speedAttribute.removeModifier(SPEED_UUID);
        }

        // 添加新的修饰符
        speedAttribute.addTransientModifier(getSpeedModifier(level));
    }
    
    /**
     * 移除迅捷步伐附魔效果
     * @param player 玩家实体
     */
    private static void removeSwiftStepEffects(Player player) {
        net.minecraft.world.entity.ai.attributes.AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;
        
        // 移除修饰符（如果存在）
        if (speedAttribute.getModifier(SPEED_UUID) != null) {
            speedAttribute.removeModifier(SPEED_UUID);
        }
    }
}