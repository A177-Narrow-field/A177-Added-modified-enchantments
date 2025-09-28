package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class FearEnchantment extends Enchantment {
    // 攻击速度减少的UUID
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("B1C2D3E4-F5A6-7890-ABCD-EF1234567892");

    // 缓存玩家当前的附魔等级和位置状态
    private static final WeakHashMap<Player, AttackSpeedState> PLAYER_FEAR_CACHE = new WeakHashMap<>();

    // 30%的概率使攻击无效
    private static final Random RANDOM = new Random();

    // 内部类，用于存储攻击速度状态
    private static class AttackSpeedState {
        final int level;
        final boolean isBelowZero;

        AttackSpeedState(int level, boolean isBelowZero) {
            this.level = level;
            this.isBelowZero = isBelowZero;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AttackSpeedState that = (AttackSpeedState) obj;
            return level == that.level && isBelowZero == that.isBelowZero;
        }
    }
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("fear");
    }

    public FearEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在所有类型的武器和工具上
        return EnchantmentCategory.WEAPON.canEnchant(stack.getItem()) || 
               EnchantmentCategory.DIGGER.canEnchant(stack.getItem()) || 
               EnchantmentCategory.CROSSBOW.canEnchant(stack.getItem()) || 
               EnchantmentCategory.TRIDENT.canEnchant(stack.getItem());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是武器或工具时才能在附魔台中应用
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }//可以在附魔台

    @Override
    public boolean isTradeable() {
        // return true;
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTradeable.get() : true;
    }
    
    @Override
    public boolean isDiscoverable() {
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : true;
    }
    
    @Override
    public boolean isTreasureOnly() {
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : false;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        // 当玩家挖掘方块时，根据是否拥有恐惧附魔减少挖掘速度
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();

        // 检查玩家是否拥有恐惧附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.FEAR.get()) > 0) {
            // 基础减少40%挖掘速度（保留60%）
            float newSpeed = event.getOriginalSpeed() * 0.6f;

            // 如果玩家在0层以下，再额外减少66.67%（总共减少80%，保留20%）
            if (player.position().y < 0) {
                newSpeed = event.getOriginalSpeed() * 0.2f;
            }

            event.setNewSpeed(newSpeed);
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.MAINHAND) {
            return;
        }

        // 当主手装备变更时，立即清除缓存并更新属性
        PLAYER_FEAR_CACHE.remove(player);
        updatePlayerAttackSpeedModifier(player, 0, false); // 先清除修饰符

        ItemStack tool = player.getMainHandItem();
        int currentLevel = 0;
        // 检查玩家是否拥有恐惧附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.FEAR.get()) > 0) {
            currentLevel = tool.getEnchantmentLevel(ModEnchantments.FEAR.get());
        }

        // 获取当前位置状态
        boolean isBelowZero = player.position().y < 0;

        // 更新攻击速度修饰符
        updatePlayerAttackSpeedModifier(player, currentLevel, isBelowZero);

        // 缓存新状态
        PLAYER_FEAR_CACHE.put(player, new AttackSpeedState(currentLevel, isBelowZero));
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        AttackSpeedState cachedState = PLAYER_FEAR_CACHE.get(player);
        int currentLevel = 0;
        boolean isBelowZero = player.position().y < 0;

        ItemStack tool = player.getMainHandItem();
        // 检查玩家是否拥有恐惧附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.FEAR.get()) > 0) {
            currentLevel = tool.getEnchantmentLevel(ModEnchantments.FEAR.get());
        }

        // 创建当前状态
        AttackSpeedState currentState = new AttackSpeedState(currentLevel, isBelowZero);

        // 如果缓存状态与当前状态不同，则更新属性修饰符
        if (cachedState == null || !cachedState.equals(currentState)) {
            updatePlayerAttackSpeedModifier(player, currentLevel, isBelowZero);
            PLAYER_FEAR_CACHE.put(player, currentState);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        // 检查玩家是否拥有恐惧附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.FEAR.get()) > 0) {
            // 计算攻击失败概率
            int chance = player.position().y < 0 ? 60 : 30;
            if (RANDOM.nextInt(100) < chance) {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存和属性修饰符
        Player player = event.getEntity();
        PLAYER_FEAR_CACHE.remove(player);
        removePlayerAttackSpeedModifier(player);
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存和属性修饰符
        Player player = event.getEntity();
        PLAYER_FEAR_CACHE.remove(player);
        removePlayerAttackSpeedModifier(player);
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时清理缓存和属性修饰符
        Player player = event.getEntity();
        PLAYER_FEAR_CACHE.remove(player);
        removePlayerAttackSpeedModifier(player);
    }

    private static void updatePlayerAttackSpeedModifier(Player player, int level, boolean isBelowZero) {
        // 移除旧的修饰符
        if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            double reduction;
            if (isBelowZero) {
                // 0层以下：减少40%攻击速度（保留60%）
                reduction = -0.4;
            } else {
                // 0层以上：减少50%攻击速度（保留70%）
                reduction = -0.3;
            }

            if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
                player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                        new AttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Fear attack speed",
                                reduction, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
    }
    
    private static void removePlayerAttackSpeedModifier(Player player) {
        // 移除攻击速度修饰符
        if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        }
    }
}