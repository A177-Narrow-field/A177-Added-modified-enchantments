package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class ReachPunchChestplateEnchantment extends Enchantment {
    // 挖掘距离增加的UUID
    public static final UUID BLOCK_REACH_MODIFIER_UUID = UUID.fromString("D1E2F3A4-B5C6-7890-EF12-345678901234");
    // 攻击距离增加的UUID
    public static final UUID ENTITY_REACH_MODIFIER_UUID = UUID.fromString("E2F3A4B5-C6D7-8901-F234-567890123456");
    
    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_REACH_PUNCH_CACHE = new WeakHashMap<>();
    
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Integer> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("reach_punch_chestplate");
    }

    public ReachPunchChestplateEnchantment() {
        // 恢复为硬编码的稀有度，不再从配置文件读取
        super(Enchantment.Rarity.RARE, 
              EnchantmentCategory.ARMOR_CHEST, 
              new EquipmentSlot[]{EquipmentSlot.CHEST});
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
        return this.getMinCost(level) + 20;
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
        PLAYER_NEXT_CHECK_TIME.put(player, currentTick + 10);

        // 检查玩家是否空手
        ItemStack mainHandItem = player.getMainHandItem();
        boolean isEmptyHanded = mainHandItem.isEmpty();

        // 获取胸甲上的直延拳甲附魔等级
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = 0;
        
        // 只有在玩家空手时才应用效果
        if (isEmptyHanded && !chestplate.isEmpty()) {
            enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.REACH_PUNCH_CHESTPLATE.get(), chestplate);
        }

        // 获取缓存的附魔等级
        int cachedLevel = PLAYER_REACH_PUNCH_CACHE.getOrDefault(player, -1);
        
        // 如果等级发生变化，更新属性修饰符
        if (cachedLevel != enchantmentLevel) {
            updatePlayerAttributes(player, enchantmentLevel);
            PLAYER_REACH_PUNCH_CACHE.put(player, enchantmentLevel);
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 当任何装备变更时，立即更新属性
        ItemStack mainHandItem = player.getMainHandItem();
        boolean isEmptyHanded = mainHandItem.isEmpty();

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = 0;
        
        // 只有在玩家空手时才应用效果
        if (isEmptyHanded && !chestplate.isEmpty()) {
            enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.REACH_PUNCH_CHESTPLATE.get(), chestplate);
        }

        // 更新玩家属性
        updatePlayerAttributes(player, enchantmentLevel);
        
        // 更新缓存
        PLAYER_REACH_PUNCH_CACHE.put(player, enchantmentLevel);

        // 更新下次检查时间
        PLAYER_NEXT_CHECK_TIME.put(player, player.tickCount + 10);
    }

    private static void updatePlayerAttributes(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null) {
            player.getAttribute(ForgeMod.BLOCK_REACH.get()).removeModifier(BLOCK_REACH_MODIFIER_UUID);
        }
        if (player.getAttribute(ForgeMod.ENTITY_REACH.get()) != null) {
            player.getAttribute(ForgeMod.ENTITY_REACH.get()).removeModifier(ENTITY_REACH_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 每级增加1个方块的距离
            double reachBonus = level * 1.0; // 总增加量等于等级数

            AttributeModifier blockReachModifier = new AttributeModifier(
                    BLOCK_REACH_MODIFIER_UUID,
                    "Reach punch chestplate block reach bonus",
                    reachBonus,
                    AttributeModifier.Operation.ADDITION
            );

            AttributeModifier entityReachModifier = new AttributeModifier(
                    ENTITY_REACH_MODIFIER_UUID,
                    "Reach punch chestplate entity reach bonus",
                    reachBonus,
                    AttributeModifier.Operation.ADDITION
            );

            // 添加挖掘距离修饰符
            if (player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null) {
                player.getAttribute(ForgeMod.BLOCK_REACH.get()).addTransientModifier(blockReachModifier);
            }

            // 添加攻击距离修饰符
            if (player.getAttribute(ForgeMod.ENTITY_REACH.get()) != null) {
                player.getAttribute(ForgeMod.ENTITY_REACH.get()).addTransientModifier(entityReachModifier);
            }
        }
    }
}