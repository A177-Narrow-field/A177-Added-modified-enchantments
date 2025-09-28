package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ChestTightnessEnchantment extends Enchantment {
    // 移动速度修饰符UUID
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("12C445B4-7A6D-4E88-B3C3-8F9E1E7A2D4F");
    
    // 存储玩家疾跑开始时间
    private static final Map<UUID, Long> playerSprintingStartTimes = new HashMap<>();
    // 存储玩家虚弱效果应用时间（用于下雨天效果）
    private static final Map<UUID, Long> playerWeaknessApplyTimes = new HashMap<>();
    
    // 疾跑时间阈值（100 ticks = 5秒）
    private static final int SPRINT_THRESHOLD = 100;
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("chest_tightness");
    }

    public ChestTightnessEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 1;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 40;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.CHEST) {
            return;
        }
        
        // 获取玩家的移动速度属性
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;
        
        // 移除旧的修饰符
        if (speedAttribute.getModifier(SPEED_MODIFIER_UUID) != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
        }
        
        // 检查新的装备是否具有胸闷附魔
        ItemStack newItem = event.getTo();
        if (newItem == null) return;
        
        int level = newItem.getEnchantmentLevel(ModEnchantments.CHEST_TIGHTNESS.get());
        
        // 如果有附魔，应用速度减缓效果
        if (level > 0) {
            // 减少20%移动速度
            AttributeModifier speedModifier = new AttributeModifier(
                SPEED_MODIFIER_UUID, 
                "Chest tightness speed reduction", 
                -0.2, 
                AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            speedAttribute.addTransientModifier(speedModifier);
        }
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // 检查玩家胸甲是否有胸闷附魔
        ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestArmor == null) return;
        
        int level = chestArmor.getEnchantmentLevel(ModEnchantments.CHEST_TIGHTNESS.get());
        
        if (level > 0) {
            // 20%概率获得10秒反胃效果
            if (player.getRandom().nextDouble() < 0.2) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0)); // 10秒 = 200 ticks
            }
            
            // 40%概率获得30秒虚弱效果
            if (player.getRandom().nextDouble() < 0.4) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0)); // 30秒 = 600 ticks
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player == null || player.level().isClientSide()) {
            return;
        }

        UUID playerId = player.getUUID();
        
        // 检查玩家是否装备了带有胸闷附魔的胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate == null) return;
        
        int level = chestplate.getEnchantmentLevel(ModEnchantments.CHEST_TIGHTNESS.get());
        
        // 如果没有附魔，清理相关数据
        if (level <= 0) {
            playerSprintingStartTimes.remove(playerId);
            playerWeaknessApplyTimes.remove(playerId);
            return;
        }
        
        // 处理疾跑时间检查
        if (player.isSprinting()) {
            // 玩家正在疾跑
            long currentTime = player.level().getGameTime();
            playerSprintingStartTimes.putIfAbsent(playerId, currentTime);
            
            // 检查是否疾跑超过5秒（100 ticks）
            Long sprintStartTime = playerSprintingStartTimes.get(playerId);
            if (sprintStartTime != null && currentTime - sprintStartTime > SPRINT_THRESHOLD) {
                // 中断疾跑并施加虚弱效果
                player.setSprinting(false);
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0)); // 5秒虚弱I
                
                // 重置疾跑时间
                playerSprintingStartTimes.remove(playerId);
            }
        } else {
            // 玩家没有疾跑，重置时间
            playerSprintingStartTimes.remove(playerId);
        }
        
        // 处理下雨天效果
        if (player.level().isRainingAt(player.blockPosition())) {
            long currentTime = player.level().getGameTime();
            Long lastApplyTime = playerWeaknessApplyTimes.get(playerId);
            
            // 每秒应用一次（20 ticks）
            if (lastApplyTime == null || currentTime - lastApplyTime >= 20) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1)); // 6秒虚弱II
                playerWeaknessApplyTimes.put(playerId, currentTime);
            }
        } else {
            // 不在下雨，移除时间记录
            playerWeaknessApplyTimes.remove(playerId);
        }
    }
}