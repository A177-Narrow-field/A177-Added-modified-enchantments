package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
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

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.events.AirSupplyEventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HypoxiaSprintEnchantment extends Enchantment {
    // 移动速度修饰符UUID
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("B2C3D4E5-F6A7-8901-CDAB-EF1234567890");
    
    // 存储玩家的附魔等级
    private static final Map<UUID, Integer> PLAYER_ENCHANTMENT_LEVELS = new HashMap<>();
    
    // 每格氧气增加的移速百分比
    private static final double SPEED_PER_AIR_LEVEL = 1.0;
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("hypoxia_sprint");
    }

    public HypoxiaSprintEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
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
        return this.getMinCost(level) + 20;
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.FEET) {
            return;
        }

        // 获取玩家的移动速度属性
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        // 移除旧的修饰符
        if (speedAttribute.getModifier(SPEED_MODIFIER_UUID) != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
        }

        // 检查新的装备是否具有缺氧急行附魔
        ItemStack newItem = event.getTo();
        int level = newItem.getEnchantmentLevel(ModEnchantments.HYPOXIA_SPRINT.get());
        
        // 更新玩家的附魔等级缓存
        UUID playerId = player.getUUID();
        if (level > 0) {
            PLAYER_ENCHANTMENT_LEVELS.put(playerId, level);
        } else {
            PLAYER_ENCHANTMENT_LEVELS.remove(playerId);
        }
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
        // 检查玩家是否装备了缺氧急行附魔
        Integer level = PLAYER_ENCHANTMENT_LEVELS.get(playerId);
        
        if (level != null && level > 0) {
            // 获取玩家的移动速度属性
            var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute == null) return;
            
            // 从统一系统获取当前氧气值
            int currentAir = AirSupplyEventHandler.getCustomAirSupply(playerId);
            if (currentAir == -1) {
                // 如果没有自定义氧气值，使用玩家当前氧气值
                currentAir = player.getAirSupply();
            }
            
            // 计算速度加成：氧气越少移速越快
            // 最大氧气值为300，当前氧气值越低，速度加成越高
            // 修正公式：使用(300 - currentAir)作为基础值，每格氧气增加速度
            double speedBonus = (300.0 - currentAir) * SPEED_PER_AIR_LEVEL * level / 300.0;
            
            // 移除旧的修饰符
            if (speedAttribute.getModifier(SPEED_MODIFIER_UUID) != null) {
                speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
            }
            
            // 添加新的速度修饰符
            if (speedBonus > 0) {
                AttributeModifier speedModifier = new AttributeModifier(
                        SPEED_MODIFIER_UUID,
                        "Hypoxia sprint speed boost",
                        speedBonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                speedAttribute.addTransientModifier(speedModifier);
            }
        }
    }
}