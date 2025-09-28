package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class DiamondObsidianFistChestplateEnchantment extends Enchantment {
    // 攻击伤害增加的UUID
    public static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("B1C2D3E4-F5A6-7890-ABCD-EF1234567890");
    // 挖掘距离增加的UUID
    public static final UUID DIG_REACH_MODIFIER_UUID = UUID.fromString("D3E4F5A6-B7C8-9012-DEFA-234567890123");

    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_DIAMOND_OBSIDIAN_FIST_CACHE = new WeakHashMap<>();

    // 更新间隔（游戏刻）
    private static final int UPDATE_INTERVAL = 10; // 每0.5秒更新一次 (10 ticks = 0.5 second)
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Integer> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("diamond_obsidian_fist_chestplate");
    }

    public DiamondObsidianFistChestplateEnchantment() {
        // 恢复为硬编码的稀有度，不再从配置文件读取
        super(Enchantment.Rarity.VERY_RARE, 
              EnchantmentCategory.ARMOR_CHEST, 
              new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 10;
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
        // 只有当配置允许且物品是胸甲时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.CHEST) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否空手
        ItemStack mainHandItem = player.getMainHandItem();
        boolean isEmptyHanded = mainHandItem.isEmpty();

        // 获取旧装备和新装备的附魔等级
        int oldLevel = event.getFrom().getEnchantmentLevel(ModEnchantments.DIAMOND_OBSIDIAN_FIST_CHESTPLATE.get());
        int newLevel = event.getTo().getEnchantmentLevel(ModEnchantments.DIAMOND_OBSIDIAN_FIST_CHESTPLATE.get());

        // 只有在玩家空手且附魔等级发生变化时才更新玩家属性
        if (isEmptyHanded && oldLevel != newLevel) {
            // 移除旧的效果
            updatePlayerAttributes(player, 0);
            // 如果新等级大于0，应用新效果
            if (newLevel > 0) {
                updatePlayerAttributes(player, newLevel);
            }
        }

        // 更新下次检查时间
        PLAYER_NEXT_CHECK_TIME.put(player, player.tickCount + UPDATE_INTERVAL);
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

        // 检查玩家是否空手
        ItemStack mainHandItem = player.getMainHandItem();
        boolean isEmptyHanded = mainHandItem.isEmpty();

        // 获取胸甲上的钻曜拳甲附魔等级
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DIAMOND_OBSIDIAN_FIST_CHESTPLATE.get(), chestplate);

        // 只有在玩家空手且胸甲有附魔时才应用效果
        if (isEmptyHanded && enchantmentLevel > 0) {
            // 更新玩家属性
            updatePlayerAttributes(player, enchantmentLevel);
        } else {
            // 移除玩家属性修饰符
            updatePlayerAttributes(player, 0);
        }
    }


    @SubscribeEvent
    public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时移除属性修饰符
        Player player = event.getEntity();
        updatePlayerAttributes(player, 0);

        // 清除缓存
        PLAYER_DIAMOND_OBSIDIAN_FIST_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时移除属性修饰符
        Player player = event.getEntity();
        updatePlayerAttributes(player, 0);

        // 清除缓存
        PLAYER_DIAMOND_OBSIDIAN_FIST_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时移除属性修饰符
        Player player = event.getEntity();
        updatePlayerAttributes(player, 0);

        // 清除缓存
        PLAYER_DIAMOND_OBSIDIAN_FIST_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
    }

    private static void updatePlayerAttributes(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }
        if (player.getAttribute(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get()) != null) {
            player.getAttribute(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get()).removeModifier(DIG_REACH_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 增加6点攻击伤害
            double damageBonus = level * 6.0;
            AttributeModifier attackDamageModifier = new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER_UUID,
                    "Diamond obsidian fist attack damage bonus",
                    damageBonus,
                    AttributeModifier.Operation.ADDITION
            );

            // 增加2格挖掘距离
            double reachBonus = level * 2.0;
            AttributeModifier digReachModifier = new AttributeModifier(
                    DIG_REACH_MODIFIER_UUID,
                    "Diamond obsidian fist dig reach bonus",
                    reachBonus,
                    AttributeModifier.Operation.ADDITION
            );

            // 添加攻击伤害修饰符
            if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(attackDamageModifier);
            }

            // 添加挖掘距离修饰符
            if (player.getAttribute(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get()) != null) {
                player.getAttribute(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get()).addTransientModifier(digReachModifier);
            }
        }
    }
}