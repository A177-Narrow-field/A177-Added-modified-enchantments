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

@Mod.EventBusSubscriber
public class SoloCombatEnchantment extends Enchantment {
    // 攻击伤害增加的UUID
    public static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("A1B2C3D4-E5F6-7890-ABCD-EF1234567897");
    // 攻击速度增加的UUID
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("B2C3D4E5-F6A7-8901-BCDE-F01234567898");

    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_SOLO_COMBAT_CACHE = new WeakHashMap<>();
    
    // 更新间隔（游戏刻）
    private static final int UPDATE_INTERVAL = 20; // 每秒更新一次 (20 ticks = 1 second)
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Integer> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("solo_combat");
    }

    public SoloCombatEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
    protected boolean checkCompatibility(Enchantment other) {
        // 与群戮附魔冲突
        return super.checkCompatibility(other) && other != ModEnchantments.MASSACRE.get();
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
        if (!weapon.isEmpty() && weapon.isEnchanted() && weapon.getEnchantmentLevel(ModEnchantments.SOLO_COMBAT.get()) > 0) {
            currentLevel = weapon.getEnchantmentLevel(ModEnchantments.SOLO_COMBAT.get());
        }

        // 更新玩家属性
        updatePlayerAttributes(player, currentLevel);
        PLAYER_SOLO_COMBAT_CACHE.put(player, currentLevel);
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.MAINHAND) {
            return;
        }

        // 当主手装备变更时，立即清除缓存并更新属性
        PLAYER_SOLO_COMBAT_CACHE.remove(player);
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
            
            // 最多计算5个敌人（与范围对应）
            enemyCount = Math.min(enemyCount, 5);
            
            // 计算缺少的敌人数量（范围内没有敌人时获得最大加成）
            int missingEnemies = 5 - enemyCount;
            
            // 每缺少一个敌人增加10%伤害和攻击速度
            double damageBonus = missingEnemies * 0.1 * level;
            double speedBonus = missingEnemies * 0.1 * level;

            // 添加攻击伤害修饰符
            if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null && damageBonus > 0) {
                player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
                        new AttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Solo combat attack damage", damageBonus, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }

            // 添加攻击速度修饰符
            if (player.getAttribute(Attributes.ATTACK_SPEED) != null && speedBonus > 0) {
                player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                        new AttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Solo combat attack speed", speedBonus, AttributeModifier.Operation.MULTIPLY_TOTAL)
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