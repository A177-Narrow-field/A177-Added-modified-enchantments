package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class FastFallEnchantment extends Enchantment {
    // 存储玩家状态
    private static final Map<UUID, Boolean> WAS_SNEAKING_MAP = new HashMap<>();
    private static final Map<UUID, Integer> PAUSE_TICKS_MAP = new HashMap<>();

    // 配置常量 - 便于调整
    private static final int PAUSE_DURATION = 5;
    private static final float GRAVITY_BOOST = 0.24f; // 三倍重力
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("fast_fall");
    }

    public FastFallEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 5;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 20;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//可以正确的出现在附魔台

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // 与悬停附魔冲突
        return super.checkCompatibility(other) && other != ModEnchantments.HOVER.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;
        UUID playerId = player.getUUID();

        // 检查靴子是否附魔
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        boolean hasEnchant = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FAST_FALL.get(), boots) > 0;

        // 如果没有附魔，清除状态并返回
        if (!hasEnchant) {
            WAS_SNEAKING_MAP.remove(playerId);
            PAUSE_TICKS_MAP.remove(playerId);
            return;
        }

        boolean isSneaking = player.isShiftKeyDown();
        boolean wasSneaking = WAS_SNEAKING_MAP.getOrDefault(playerId, false);
        int pauseTicks = PAUSE_TICKS_MAP.getOrDefault(playerId, 0);

        // 处理松开蹲下键的瞬间
        if (wasSneaking && !isSneaking && !player.onGround() && !player.isInWater() && !player.isFallFlying()) {
            // 设置暂停20tick
            PAUSE_TICKS_MAP.put(playerId, PAUSE_DURATION);
            pauseTicks = PAUSE_DURATION;
        }

        // 应用效果
        if (!player.onGround() && !player.isInWater() && !player.isFallFlying()) {
            if (isSneaking) {
                // 蹲下时增加重力（3倍）
                player.setDeltaMovement(
                        player.getDeltaMovement().x,
                        player.getDeltaMovement().y - GRAVITY_BOOST,
                        player.getDeltaMovement().z
                );
            } else if (pauseTicks > 0) {
                // 松开蹲下后暂停期间完全停滞
                player.setDeltaMovement(
                        player.getDeltaMovement().x,
                        0, // 垂直速度设为0
                        player.getDeltaMovement().z
                );
                // 减少暂停计数
                PAUSE_TICKS_MAP.put(playerId, pauseTicks - 1);
            }
        }

        // 更新状态
        WAS_SNEAKING_MAP.put(playerId, isSneaking);

        // 如果玩家落地，清除暂停状态
        if (player.onGround() && pauseTicks > 0) {
            PAUSE_TICKS_MAP.remove(playerId);
        }
    }
}