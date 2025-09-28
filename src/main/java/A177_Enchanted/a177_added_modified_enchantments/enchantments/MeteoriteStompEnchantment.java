package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class MeteoriteStompEnchantment extends Enchantment {

    private static final Random RANDOM = new Random();
    private static final float BASE_DAMAGE_PER_BLOCK = 0.5f;
    private static final float MAX_DAMAGE = 100.0f;
    private static final float KNOCKBACK_STRENGTH = 2.0f;
    private static final int MIN_FALL_HEIGHT = 4;
    private static final float FALL_DAMAGE_REDUCTION = 0.9f;
    private static final int STOMP_COOLDOWN = 10; // 0.5秒冷却

    // 存储玩家状态信息（服务器端）
    private static final Map<UUID, Double> LAST_Y_POSITIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> FALL_DISTANCES = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> WAS_IN_AIR = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_STOMP_TIME = new ConcurrentHashMap<>();

    public MeteoriteStompEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMinCost(int level) {
        return 25;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.METEORITE_STOMP.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.METEORITE_STOMP.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.METEORITE_STOMP.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && 
               AllEnchantmentsConfig.METEORITE_STOMP.isDiscoverable.get();
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        // 只在服务端执行逻辑
        if (entity.level().isClientSide()) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUUID();
        boolean isOnGround = player.onGround();
        boolean wasInAir = WAS_IN_AIR.getOrDefault(playerId, false);
        long currentTime = player.level().getGameTime();

        // 防止重复触发
        Long lastStompTime = LAST_STOMP_TIME.get(playerId);
        if (lastStompTime != null && currentTime - lastStompTime < STOMP_COOLDOWN) {
            return;
        }

        // 跟踪摔落距离
        if (!isOnGround) {
            double currentY = player.getY();
            Double lastY = LAST_Y_POSITIONS.get(playerId);
            
            if (lastY != null) {
                // 只有在下降时才增加摔落距离
                if (currentY < lastY) {
                    float fallDistance = FALL_DISTANCES.getOrDefault(playerId, 0f);
                    fallDistance += (float)(lastY - currentY);
                    FALL_DISTANCES.put(playerId, fallDistance);
                }
            }
            
            LAST_Y_POSITIONS.put(playerId, currentY);
            WAS_IN_AIR.put(playerId, true);
        } else if (wasInAir) {
            // 玩家刚从空中落地
            float fallDistance = FALL_DISTANCES.getOrDefault(playerId, 0f);

            // 检查鞋子是否有陨铁踏附魔
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.METEORITE_STOMP.get(), boots);

            // 确保条件满足，必须有足够的摔落高度
            if (enchantmentLevel > 0 && fallDistance >= MIN_FALL_HEIGHT) {
                // 播放音效（客户端和服务器端都播放）
                playStompSound(player);

                // 只在服务器端触发伤害效果
                boolean damageDealt = triggerMeteoriteStomp(player, fallDistance);
                if (damageDealt) {
                    LAST_STOMP_TIME.put(playerId, currentTime);
                }
            }

            // 重置状态
            resetPlayerState(playerId);
        } else if (isOnGround) {
            // 确保在地面上时状态正确
            resetPlayerState(playerId);
        }
    }


    private static boolean triggerMeteoriteStomp(Player player, float fallDistance) {
        // 计算伤害
        float damage = Math.min(fallDistance * BASE_DAMAGE_PER_BLOCK, MAX_DAMAGE);
        damage = Math.max(1.0f, damage);

        // 碰撞检测范围
        AABB area = new AABB(
                player.getX() - 4.0, player.getY() - 2.0, player.getZ() - 4.0,
                player.getX() + 4.0, player.getY() + 3.0, player.getZ() + 4.0
        );

        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class, area, e -> e != player && e.isAlive() && !e.isInvulnerable()
        );

        boolean damageDealt = false;

        for (LivingEntity target : entities) {
            if (target instanceof Player targetPlayer &&
                    (targetPlayer.isCreative() || targetPlayer.isSpectator())) {
                continue;
            }

            // 造成伤害
            target.hurt(player.damageSources().playerAttack(player), damage);
            damageDealt = true;

            // 计算击退
            double distance = target.distanceTo(player);
            double distanceFactor = Math.max(0.1, 1.0 - (distance / 8.0));

            Vec3 direction = target.position().subtract(player.position())
                    .normalize()
                    .multiply(KNOCKBACK_STRENGTH * 0.3 * distanceFactor,
                            0.05,
                            KNOCKBACK_STRENGTH * 0.3 * distanceFactor);

            // 应用击退
            target.setDeltaMovement(
                    target.getDeltaMovement().x + direction.x,
                    Math.min(0.3, target.getDeltaMovement().y + direction.y),
                    target.getDeltaMovement().z + direction.z
            );
        }

        // 如果造成了伤害，则消耗靴子5%的耐久度
        if (damageDealt && !player.isCreative()) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            int maxDamage = boots.getMaxDamage();
            int damageToApply = Math.max(1, (int) (maxDamage * 0.05)); // 至少消耗1点耐久
            
            // 只在服务端执行耐久度扣除
            if (!player.level().isClientSide()) {
                boots.setDamageValue(boots.getDamageValue() + damageToApply);
                
                // 如果物品损坏，则破坏它
                if (boots.getDamageValue() >= maxDamage) {
                    player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                }
            }
        }

        return damageDealt;
    }

    private static void playStompSound(Player player) {
        // 客户端和服务器端都播放音效
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ANVIL_LAND,
                SoundSource.PLAYERS,
                0.6f,
                0.5f + RANDOM.nextFloat() * 0.2f
        );
    }

    private static void resetPlayerState(UUID playerId) {
        FALL_DISTANCES.put(playerId, 0f);
        WAS_IN_AIR.put(playerId, false);
        LAST_Y_POSITIONS.remove(playerId);
    }

    // 清理玩家数据的方法
    private static void cleanupPlayerData(UUID playerId) {
        LAST_Y_POSITIONS.remove(playerId);
        FALL_DISTANCES.remove(playerId);
        WAS_IN_AIR.remove(playerId);
        LAST_STOMP_TIME.remove(playerId);
    }

    // 清理缓存的方法
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // 只在服务端执行清理
        if (!event.getEntity().level().isClientSide()) {
            cleanupPlayerData(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // 只在服务端执行清理
        if (!event.getEntity().level().isClientSide()) {
            cleanupPlayerData(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 只在服务端执行清理
        if (!event.getEntity().level().isClientSide()) {
            cleanupPlayerData(event.getEntity().getUUID());
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 只在服务端执行清理
        if (!event.getEntity().level().isClientSide()) {
            cleanupPlayerData(event.getEntity().getUUID());
        }
    }
}