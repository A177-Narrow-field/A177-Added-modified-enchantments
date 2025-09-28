package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
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
public class ShortDimensionEnchantment extends Enchantment {
    // 挖掘距离减少的UUID
    public static final UUID BLOCK_REACH_MODIFIER_UUID = UUID.fromString("D8A08E48-1D2B-4F3A-9B39-1234567890AB");
    // 攻击距离减少的UUID
    public static final UUID ENTITY_REACH_MODIFIER_UUID = UUID.fromString("E9B19F59-2E3C-5F4B-AC40-234567890BCD");
    
    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_SHORT_DIMENSION_CACHE = new WeakHashMap<>();
    
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Long> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("short_dimension");
    }

    public ShortDimensionEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        // 只能附在工具和武器上
        return EnchantmentCategory.DIGGER.canEnchant(stack.getItem()) || EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
    }

    @Override
    public boolean isTradeable() {
        // // 不可通过交易获得
        // return false;
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTradeable.get() : false;
    }
    @Override
    public boolean isDiscoverable() {
        // return true;
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : true;
    }// 可在附魔台发现
    
    @Override
    public boolean isTreasureOnly() {
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是工具或武器时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        long currentTime = player.level().getGameTime();
        
        // 获取玩家的下次检查时间
        Long nextCheckTime = PLAYER_NEXT_CHECK_TIME.get(player);
        if (nextCheckTime != null && currentTime < nextCheckTime) {
            return;
        }
        
        // 设置下次检查时间为1秒后（20个tick）
        PLAYER_NEXT_CHECK_TIME.put(player, currentTime + 40);
        
        ItemStack tool = player.getMainHandItem();
        
        // 获取缓存的附魔等级
        Integer cachedLevel = PLAYER_SHORT_DIMENSION_CACHE.get(player);
        if (cachedLevel == null) {
            cachedLevel = -1;
        }
        int currentLevel = 0;
        
        // 检查当前工具是否有短寸附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.SHORT_DIMENSION.get()) > 0) {
            currentLevel = tool.getEnchantmentLevel(ModEnchantments.SHORT_DIMENSION.get());
        }
        
        // 如果等级发生变化，更新属性修饰符
        if (cachedLevel != currentLevel) {
            updatePlayerReachModifiers(player, currentLevel);
            PLAYER_SHORT_DIMENSION_CACHE.put(player, currentLevel);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 当主手装备变更时，立即清除缓存并更新属性
        PLAYER_SHORT_DIMENSION_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
        updatePlayerReachModifiers(player, 0); // 先清除修饰符
        
        ItemStack tool = player.getMainHandItem();
        int currentLevel = 0;
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.SHORT_DIMENSION.get()) > 0) {
            currentLevel = tool.getEnchantmentLevel(ModEnchantments.SHORT_DIMENSION.get());
        }
        
        updatePlayerReachModifiers(player, currentLevel);
        PLAYER_SHORT_DIMENSION_CACHE.put(player, currentLevel);
    }
    
    private static void updatePlayerReachModifiers(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null) {
            player.getAttribute(ForgeMod.BLOCK_REACH.get()).removeModifier(BLOCK_REACH_MODIFIER_UUID);
        }
        if (player.getAttribute(ForgeMod.ENTITY_REACH.get()) != null) {
            player.getAttribute(ForgeMod.ENTITY_REACH.get()).removeModifier(ENTITY_REACH_MODIFIER_UUID);
        }
        
        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 每级减少1个方块的距离
            if (player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null) {
                player.getAttribute(ForgeMod.BLOCK_REACH.get()).addTransientModifier(
                    new AttributeModifier(BLOCK_REACH_MODIFIER_UUID, "Short dimension block reach", -level, AttributeModifier.Operation.ADDITION)
                );
            }
            
            if (player.getAttribute(ForgeMod.ENTITY_REACH.get()) != null) {
                player.getAttribute(ForgeMod.ENTITY_REACH.get()).addTransientModifier(
                    new AttributeModifier(ENTITY_REACH_MODIFIER_UUID, "Short dimension entity reach", -level, AttributeModifier.Operation.ADDITION)
                );
            }
        }
    }
}