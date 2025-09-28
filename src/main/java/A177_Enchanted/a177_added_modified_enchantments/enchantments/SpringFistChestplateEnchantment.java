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
import net.minecraftforge.common.ForgeMod;
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
public class SpringFistChestplateEnchantment extends Enchantment {
    // 攻击速度增加的UUID
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("F1A2B3C4-D5E6-4890-AB12-345678901234");

    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_SPRING_FIST_CACHE = new WeakHashMap<>();
    
    // 更新间隔（游戏刻）
    private static final int UPDATE_INTERVAL = 10; // 每0.5秒更新一次 (10 ticks = 0.5 second)
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Integer> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("spring_fist_chestplate");
    }

    public SpringFistChestplateEnchantment() {
        // 恢复为硬编码的稀有度，不再从配置文件读取
        super(Enchantment.Rarity.VERY_RARE, 
              EnchantmentCategory.ARMOR_CHEST, 
              new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 5;
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

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.CRUSHING_FIST_CHESTPLATE.get();
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

        // 获取胸甲上的咏春拳甲附魔等级
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SPRING_FIST_CHESTPLATE.get(), chestplate);

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
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SPRING_FIST_CHESTPLATE.get(), chestplate);

        // 只有在玩家空手且胸甲有附魔时才应用效果
        if (isEmptyHanded && enchantmentLevel > 0) {
            // 更新玩家属性
            updatePlayerAttributes(player, enchantmentLevel);
        } else {
            // 移除玩家属性修饰符
            updatePlayerAttributes(player, 0);
        }

        // 更新下次检查时间
        PLAYER_NEXT_CHECK_TIME.put(player, player.tickCount + UPDATE_INTERVAL);
    }
    
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        // 当玩家挖掘方块时，根据附魔等级增加挖掘速度
        Player player = event.getEntity();
        
        // 检查玩家是否空手
        ItemStack mainHandItem = player.getMainHandItem();
        boolean isEmptyHanded = mainHandItem.isEmpty();
        
        // 获取玩家胸甲上的咏春拳甲附魔等级
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = 0;
        
        // 只有当玩家装备了胸甲且空手时才应用附魔效果
        if (!chestplate.isEmpty() && isEmptyHanded) {
            enchantmentLevel = chestplate.getEnchantmentLevel(ModEnchantments.SPRING_FIST_CHESTPLATE.get());
        }
        
        // 如果有附魔等级，增加挖掘速度
        if (enchantmentLevel > 0) {
            // 每级增加150%挖掘速度
            float newSpeed = event.getOriginalSpeed() * (1.0f + enchantmentLevel * 1.5f);
            
            // 检查玩家是否还装备了钻曜拳甲附魔，如果是则再次翻倍挖掘速度
            ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
            int diamondObsidianFistLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DIAMOND_OBSIDIAN_FIST_CHESTPLATE.get(), chestItem);
            if (diamondObsidianFistLevel > 0) {
                newSpeed *= 5.0f; // 再次翻倍
            }
            
            event.setNewSpeed(newSpeed);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时移除属性修饰符
        Player player = event.getEntity();
        updatePlayerAttributes(player, 0);

        // 清除缓存
        PLAYER_SPRING_FIST_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时移除属性修饰符
        Player player = event.getEntity();
        updatePlayerAttributes(player, 0);

        // 清除缓存
        PLAYER_SPRING_FIST_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时移除属性修饰符
        Player player = event.getEntity();
        updatePlayerAttributes(player, 0);

        // 清除缓存
        PLAYER_SPRING_FIST_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
    }

    private static void updatePlayerAttributes(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        }
        
        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 每级增加100%攻击速度
            if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
                player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                    new AttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Spring fist chestplate attack speed", level, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
    }
}