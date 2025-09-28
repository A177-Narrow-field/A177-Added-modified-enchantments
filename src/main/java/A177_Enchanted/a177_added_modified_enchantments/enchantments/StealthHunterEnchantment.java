package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class StealthHunterEnchantment extends Enchantment {
    // 用于属性修饰符的UUID
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID INVISIBILITY_DAMAGE_MODIFIER_UUID = UUID.fromString("22222222-3333-4444-5555-666666666666");
    private static final UUID GLOWING_DAMAGE_MODIFIER_UUID = UUID.fromString("33333333-4444-5555-6666-777777777777");

    // 存储玩家蹲下开始时间
    private static final Map<UUID, Long> CROUCH_START_TIME = new HashMap<>();
    // 存储玩家隐身效果结束时间
    private static final Map<UUID, Long> INVISIBILITY_END_TIME = new HashMap<>();
    // 存储玩家隐身冷却时间
    private static final Map<UUID, Long> INVISIBILITY_COOLDOWN = new HashMap<>();
    // 存储玩家是否隐身的状态
    private static final Map<UUID, Boolean> PLAYER_INVISIBILITY_STATUS = new HashMap<>();
    // 存储上次取消锁定的时间，避免每帧都执行
    private static final Map<UUID, Long> LAST_TARGET_CLEAR_TIME = new HashMap<>();

    // 时间设置（单位：tick）
    private static final int CROUCH_TIME = 10; // 蹲下0.5秒（10 ticks）
    private static final int INVISIBILITY_DURATION = 240; // 隐身12秒（240 ticks）
    private static final int COOLDOWN_TIME = 60; // 冷却3秒（60 ticks）
    private static final int TARGET_CLEAR_INTERVAL = 5; // 每0.25秒清除一次目标

    public StealthHunterEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("stealth_hunter");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 30;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem;
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench) && ench != ModEnchantments.ARMY_SUMMON.get();
    }

    /**
     * 阻止敌对生物将隐身状态的玩家作为攻击目标
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player player) {
            // 检查玩家是否拥有隐身效果
            if (player.hasEffect(MobEffects.INVISIBILITY)) {
                // 检查攻击者是否为敌对生物
                LivingEntity attacker = event.getEntity();
                if (!attacker.getType().getCategory().isFriendly()) {
                    // 取消攻击目标
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return; // 只在服务端运行
        }

        // 检查玩家是否持有带有匿猎附魔的武器
        ItemStack weapon = player.getMainHandItem();
        int level = weapon.getEnchantmentLevel(ModEnchantments.STEALTH_HUNTER.get());

        UUID playerUUID = player.getUUID();
        long currentTime = player.level().getGameTime();

        // 检查冷却是否结束（无论是否持有附魔武器都要检查）
        Long cooldownEndTime = INVISIBILITY_COOLDOWN.get(playerUUID);
        if (cooldownEndTime != null && currentTime >= cooldownEndTime) {
            INVISIBILITY_COOLDOWN.remove(playerUUID);
            // 播放冷却完成提示音
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (level <= 0) {
            // 如果没有附魔，确保移除所有效果
            clearPlayerData(playerUUID);
            return;
        }

        // 检查隐身效果是否应该结束
        Long invisibilityEndTime = INVISIBILITY_END_TIME.get(playerUUID);
        if (invisibilityEndTime != null && currentTime >= invisibilityEndTime) {
            // 移除隐身效果
            player.removeEffect(MobEffects.INVISIBILITY);
            clearPlayerData(playerUUID);
        }

        // 检查玩家是否在蹲下
        if (player.isCrouching()) {
            // 如果玩家之前没有记录蹲下时间，则记录当前时间
            if (!CROUCH_START_TIME.containsKey(playerUUID)) {
                CROUCH_START_TIME.put(playerUUID, currentTime);
            }
            // 需要蹲下0.5秒(10 ticks)才能获得隐身效果
            else if (currentTime - CROUCH_START_TIME.get(playerUUID) >= CROUCH_TIME) {
                // 检查是否在冷却时间内
                boolean isOnCooldown = isOnCooldown(player);
                if (!isOnCooldown) {
                    // 触发隐身效果
                    triggerInvisibility(player, currentTime);
                    // 清除蹲下时间记录，需要重新蹲下才能再次触发
                    CROUCH_START_TIME.remove(playerUUID);
                }
            }
        } else {
            // 如果玩家没有蹲下，清除蹲下时间记录
            CROUCH_START_TIME.remove(playerUUID);
        }

        // 在隐身期间持续让敌人丢失目标
        Boolean isInvisible = PLAYER_INVISIBILITY_STATUS.get(playerUUID);
        if (isInvisible != null && isInvisible) {
            Long lastClearTime = LAST_TARGET_CLEAR_TIME.get(playerUUID);
            if (lastClearTime == null || currentTime - lastClearTime >= TARGET_CLEAR_INTERVAL) {
                makeEnemiesLoseTarget(player);
                LAST_TARGET_CLEAR_TIME.put(playerUUID, currentTime);
            }
        }
    }

    /**
     * 触发隐身效果
     */
    private static void triggerInvisibility(Player player, long currentTime) {
        UUID playerUUID = player.getUUID();

        // 给玩家添加隐身效果，持续10秒，不显示粒子效果但保留图标
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_DURATION, 0, false, false, true));

        // 记录隐身效果结束时间和状态
        INVISIBILITY_END_TIME.put(playerUUID, currentTime + INVISIBILITY_DURATION);
        PLAYER_INVISIBILITY_STATUS.put(playerUUID, true);

        // 立即让敌人丢失目标
        makeEnemiesLoseTarget(player);
        LAST_TARGET_CLEAR_TIME.put(playerUUID, currentTime);

        // 设置冷却时间（6秒）
        INVISIBILITY_COOLDOWN.put(playerUUID, currentTime + COOLDOWN_TIME);

        // 设置物品冷却效果（冷却6秒）
        player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), COOLDOWN_TIME);
    }

    /**
     * 让附近的敌人永久丢失玩家目标
     */
    private static void makeEnemiesLoseTarget(Player player) {
        double range = 16.0; // 16格范围

        // 获取玩家周围一定范围内的所有生物
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range))) {
            if (entity instanceof Mob mob) {
                // 如果生物以玩家为目标，让其丢失目标
                if (mob.getTarget() == player) {
                    mob.setTarget(null);
                }

                // 让生物短暂停止行动
                mob.setNoActionTime(30); // 停止行动1.5秒
            }
        }
    }

    /**
     * 清理玩家数据
     */
    private static void clearPlayerData(UUID playerUUID) {
        INVISIBILITY_END_TIME.remove(playerUUID);
        PLAYER_INVISIBILITY_STATUS.remove(playerUUID);
        LAST_TARGET_CLEAR_TIME.remove(playerUUID);
    }

    /**
     * 检查玩家是否在冷却中
     */
    private static boolean isOnCooldown(Player player) {
        UUID playerUUID = player.getUUID();
        Long cooldownEndTime = INVISIBILITY_COOLDOWN.get(playerUUID);

        if (cooldownEndTime == null) {
            return false;
        }

        long currentTime = player.level().getGameTime();
        boolean onCooldown = currentTime < cooldownEndTime;

        if (!onCooldown) {
            INVISIBILITY_COOLDOWN.remove(playerUUID);
        }

        return onCooldown;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return; // 只在服务端运行
        }

        // 当玩家受到伤害时，移除隐身效果
        if (event.getEntity() instanceof Player player) {
            UUID playerUUID = player.getUUID();

            // 移除隐身效果
            player.removeEffect(MobEffects.INVISIBILITY);
            clearPlayerData(playerUUID);

            return;
        }

        // 当玩家造成伤害时，应用额外伤害并移除隐身效果
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            // 检查玩家是否持有带有匿猎附魔的武器
            ItemStack weapon = player.getMainHandItem();
            int level = weapon.getEnchantmentLevel(ModEnchantments.STEALTH_HUNTER.get());

            if (level <= 0) {
                return;
            }

            UUID playerUUID = player.getUUID();
            LivingEntity target = (LivingEntity) event.getEntity();

            // 检查玩家是否隐身
            Boolean isInvisible = PLAYER_INVISIBILITY_STATUS.get(playerUUID);
            if (isInvisible != null && isInvisible) {
                // 隐身时增加40%伤害
                event.setAmount(event.getAmount() * 1.4f);
                // 移除隐身效果
                player.removeEffect(MobEffects.INVISIBILITY);
                clearPlayerData(playerUUID);
            }

            // 检查目标是否有发光效果
            if (target.hasEffect(MobEffects.GLOWING)) {
                // 对发光目标造成额外50%伤害
                event.setAmount(event.getAmount() * 1.5f);
            }
        }
    }

    /**
     * 获取玩家当前的冷却进度（用于UI显示等）
     */
    public static float getCooldownProgress(Player player) {
        if (player.level().isClientSide()) {
            // 客户端使用原版冷却系统
            ItemStack weapon = player.getMainHandItem();
            return player.getCooldowns().getCooldownPercent(weapon.getItem(), 0.0f);
        }

        // 服务端计算冷却进度
        UUID playerUUID = player.getUUID();
        Long cooldownEndTime = INVISIBILITY_COOLDOWN.get(playerUUID);

        if (cooldownEndTime == null) {
            return 0.0f;
        }

        long currentTime = player.level().getGameTime();
        long totalCooldown = COOLDOWN_TIME;

        if (currentTime >= cooldownEndTime) {
            INVISIBILITY_COOLDOWN.remove(playerUUID);
            return 0.0f;
        }

        long remaining = cooldownEndTime - currentTime;
        return Math.min(1.0f, (float) remaining / totalCooldown);
    }

    /**
     * 检查玩家是否在冷却中（公开方法）
     */
    public static boolean isPlayerOnCooldown(Player player) {
        if (player.level().isClientSide()) {
            // 客户端使用原版冷却系统
            ItemStack weapon = player.getMainHandItem();
            return player.getCooldowns().isOnCooldown(weapon.getItem());
        }

        // 服务端检查冷却
        return isOnCooldown(player);
    }
}