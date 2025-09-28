package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class MassacreEnchantment extends Enchantment {
    // 攻击伤害增加的UUID
    public static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("C1D2E3F4-A5B6-C7D8-E9F0-1234567890AB");
    // 攻击速度增加的UUID
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("D2E3F4A5-B6C7-D8E9-F012-34567890ABCD");

    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_MASSACRE_CACHE = new WeakHashMap<>();
    
    // 更新间隔（游戏刻）
    private static final int UPDATE_INTERVAL = 20; // 每秒更新一次 (20 ticks = 1 second)
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Integer> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("massacre");
    }

    public MassacreEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }
    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在剑上
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // 与独斗附魔冲突
        return super.checkCompatibility(other) && other != ModEnchantments.SOLO_COMBAT.get();
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

        // 检查是否到了更新时间
        int currentTick = player.tickCount;
        Integer nextCheckTick = PLAYER_NEXT_CHECK_TIME.get(player);
        if (nextCheckTick != null && currentTick < nextCheckTick) {
            return;
        }
        
        // 更新下次检查时间
        PLAYER_NEXT_CHECK_TIME.put(player, currentTick + UPDATE_INTERVAL);

        ItemStack weapon = player.getMainHandItem();
        int currentLevel = 0;
        if (!weapon.isEmpty() && weapon.isEnchanted() && weapon.getEnchantmentLevel(ModEnchantments.MASSACRE.get()) > 0) {
            currentLevel = weapon.getEnchantmentLevel(ModEnchantments.MASSACRE.get());
        }

        // 更新玩家属性
        updatePlayerAttributes(player, currentLevel);
        PLAYER_MASSACRE_CACHE.put(player, currentLevel);
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.MAINHAND) {
            return;
        }

        // 当主手装备变更时，立即清除缓存并更新属性
        PLAYER_MASSACRE_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
        updatePlayerAttributes(player, 0); // 先清除修饰符
    }

    private static void updatePlayerAttributes(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }
        if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 计算周围的敌对实体数量
            int enemyCount = getNearbyEnemiesCount(player);
            
            // 最多计算5个敌人
            enemyCount = Math.min(enemyCount, 5);
            
            // 每个敌人增加10%伤害和攻击速度
            double damageBonus = enemyCount * 0.1 * level;
            double speedBonus = enemyCount * 0.1 * level;

            // 添加攻击伤害修饰符
            if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null && damageBonus > 0) {
                player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
                        new AttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Massacre attack damage", damageBonus, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }

            // 添加攻击速度修饰符
            if (player.getAttribute(Attributes.ATTACK_SPEED) != null && speedBonus > 0) {
                player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                        new AttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Massacre attack speed", speedBonus, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
    }

    private static int getNearbyEnemiesCount(Player player) {
        // 获取玩家周围10格范围内的敌对实体
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(10.0D),
                entity -> entity != player && 
                         entity.isAlive() && 
                         entity instanceof Enemy &&
                         entity.hasLineOfSight(player)  // 确保实体在玩家视线内
        );
        
        // 过滤掉友好实体和玩家自己
        return (int) nearbyEntities.stream()
                .filter(entity -> !(entity instanceof Player))
                .filter(entity -> !(entity instanceof NeutralMob) || ((NeutralMob) entity).isAngry())
                .count();
    }
}