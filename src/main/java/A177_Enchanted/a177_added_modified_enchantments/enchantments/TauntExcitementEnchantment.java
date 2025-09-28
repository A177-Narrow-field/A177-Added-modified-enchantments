package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class TauntExcitementEnchantment extends Enchantment {
    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_TAUNT_EXCITEMENT_CACHE = new WeakHashMap<>();
    
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Long> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    // 检查间隔 (10 ticks = 0.5秒)
    private static final int CHECK_INTERVAL = 10;
    
    // 效果范围半径
    private static final double EFFECT_RADIUS = 8.0;
    
    // 效果持续时间（tick）
    private static final int EFFECT_DURATION = 60; // 3秒 (3*20)
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("taunt_excitement");
    }

    public TauntExcitementEnchantment() {
        super(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在靴子上
        return EnchantmentCategory.ARMOR_FEET.canEnchant(stack.getItem());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }

        // 检查是否到了更新时间
        long currentTime = player.level().getGameTime();
        Long nextCheckTime = PLAYER_NEXT_CHECK_TIME.get(player);
        if (nextCheckTime != null && currentTime < nextCheckTime) {
            return;
        }

        // 更新下次检查时间
        PLAYER_NEXT_CHECK_TIME.put(player, currentTime + CHECK_INTERVAL);

        // 检查玩家是否穿着附魔靴子
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int enchantmentLevel = 0;
        if (!boots.isEmpty() && boots.isEnchanted() && boots.getEnchantmentLevel(ModEnchantments.TAUNT_EXCITEMENT.get()) > 0) {
            enchantmentLevel = boots.getEnchantmentLevel(ModEnchantments.TAUNT_EXCITEMENT.get());
        }

        // 更新缓存
        PLAYER_TAUNT_EXCITEMENT_CACHE.put(player, enchantmentLevel);
        
        // 检查玩家是否在疾跑
        if (enchantmentLevel > 0 && player.isSprinting()) {
            // 获取8格范围内的敌对生物
            AABB boundingBox = player.getBoundingBox().inflate(EFFECT_RADIUS, EFFECT_RADIUS/2, EFFECT_RADIUS);
            
            // 给范围内的敌对生物施加急速效果
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, boundingBox)) {
                // 检查实体是否为敌对生物且不是玩家自己
                if (entity instanceof net.minecraft.world.entity.monster.Enemy && !entity.equals(player)) {
                    // 施加急速III效果，持续3秒
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION, 2)); // 急速III
                }
            }
        }
    }
}