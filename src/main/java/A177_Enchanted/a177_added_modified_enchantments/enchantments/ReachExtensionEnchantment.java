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
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.utils.CuriosHelper;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class ReachExtensionEnchantment extends Enchantment {
    // 挖掘距离增加的UUID
    public static final UUID BLOCK_REACH_MODIFIER_UUID = UUID.fromString("A1B2C3D4-E5F6-7890-ABCD-EF1234567890");
    // 攻击距离增加的UUID
    public static final UUID ENTITY_REACH_MODIFIER_UUID = UUID.fromString("B2C3D4E5-F6A7-8901-BCDE-F01234567891");
    
    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_REACH_EXTENSION_CACHE = new WeakHashMap<>();
    
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Long> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("reach_extension");
    }

    public ReachExtensionEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return 50 + (level - 1) * 50;
    }// 获取附魔的最小等级

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 200;
    }//获取附魔的最大等级

    @Override
    public boolean isTreasureOnly() {
        // return true;
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : true;
    }//是宝藏附魔

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在工具和武器上
        return EnchantmentCategory.DIGGER.canEnchant(stack.getItem()) || 
               EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
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
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : true;
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
        // 确保只在服务端执行
        if (player.level().isClientSide()) {
            return;
        }
        
        long currentTime = player.level().getGameTime();
        
        // 获取玩家的下次检查时间
        Long nextCheckTime = PLAYER_NEXT_CHECK_TIME.get(player);
        if (nextCheckTime != null && currentTime < nextCheckTime) {
            return;
        }
        
        // 设置下次检查时间为1秒后（20个tick）
        PLAYER_NEXT_CHECK_TIME.put(player, currentTime + 40);
        
        // 获取缓存的附魔等级
        int cachedLevel = PLAYER_REACH_EXTENSION_CACHE.getOrDefault(player, -1);
        int currentLevel = calculateTotalEnchantmentLevel(player);
        
        // 如果等级发生变化，更新属性修饰符
        if (cachedLevel != currentLevel) {
            updatePlayerReachModifiers(player, currentLevel);
            PLAYER_REACH_EXTENSION_CACHE.put(player, currentLevel);
        }
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // 当玩家破坏方块后，消耗工具耐久度
        Player player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        
        // 检查工具是否附有ReachExtension附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.REACH_EXTENSION.get()) > 0) {
            int level = tool.getEnchantmentLevel(ModEnchantments.REACH_EXTENSION.get());
            // 消耗耐久度（每次使用消耗1%耐久度，最少1点）
            consumeToolDurability(tool, player, level);
        }
    }
    
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        // 当玩家攻击实体后，消耗工具耐久度
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        
        // 检查工具是否附有ReachExtension附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.REACH_EXTENSION.get()) > 0) {
            int level = tool.getEnchantmentLevel(ModEnchantments.REACH_EXTENSION.get());
            // 消耗耐久度（每次使用消耗1%耐久度，最少1点）
            consumeToolDurability(tool, player, level);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // 确保只在服务端执行
        if (player.level().isClientSide()) {
            return;
        }
        
        // 当任何装备变更时，立即清除缓存并更新属性
        PLAYER_REACH_EXTENSION_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
        int currentLevel = calculateTotalEnchantmentLevel(player);
        updatePlayerReachModifiers(player, currentLevel);
        PLAYER_REACH_EXTENSION_CACHE.put(player, currentLevel);
    }
    
    /**
     * 消耗工具耐久度
     * @param tool 工具物品
     * @param player 玩家实体
     * @param level ReachExtension附魔等级
     */
    private static void consumeToolDurability(ItemStack tool, Player player, int level) {
        // 确保只在服务端执行
        if (player.level().isClientSide()) {
            return;
        }
        
        // 计算需要消耗的耐久度（1%耐久度，最少1点）
        int maxDamage = tool.getMaxDamage();
        if (maxDamage > 0) {
            // 计算消耗值：1%的耐久度，最少为1点
            int consumeAmount = Math.max(1, maxDamage / 100);
            
            // 创造模式玩家不消耗耐久度
            if (!player.getAbilities().instabuild) {
                tool.hurtAndBreak(consumeAmount, player, (entity) -> {
                    entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                });
            }
        }
    }
    
    /**
     * 计算玩家主手工具和Curios饰品上的延申附魔等级总和
     * @param player 玩家实体
     * @return 附魔等级总和
     */
    private static int calculateTotalEnchantmentLevel(Player player) {
        // 确保只在服务端执行
        if (player.level().isClientSide()) {
            return 0;
        }
        
        int totalLevel = 0;
        
        // 检查主手工具
        ItemStack tool = player.getMainHandItem();
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.REACH_EXTENSION.get()) > 0) {
            totalLevel += tool.getEnchantmentLevel(ModEnchantments.REACH_EXTENSION.get());
        }
        
        // 检查Curios饰品
        if (CuriosHelper.CURIOS_LOADED) {
            // 使用数组包装totalLevel以便在lambda中修改
            final int[] levelWrapper = {totalLevel};
            CuriosHelper.hasCurioItem(player, stack -> {
                if (!stack.isEmpty() && stack.isEnchanted() && stack.getEnchantmentLevel(ModEnchantments.REACH_EXTENSION.get()) > 0) {
                    levelWrapper[0] += stack.getEnchantmentLevel(ModEnchantments.REACH_EXTENSION.get());
                    return true; // 继续检查其他物品
                }
                return false;
            });
            totalLevel = levelWrapper[0];
        }
        
        return totalLevel;
    }
    
    private static void updatePlayerReachModifiers(Player player, int level) {
        // 确保只在服务端执行
        if (player.level().isClientSide()) {
            return;
        }
        
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
            if (player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null) {
                player.getAttribute(ForgeMod.BLOCK_REACH.get()).addTransientModifier(
                    new AttributeModifier(BLOCK_REACH_MODIFIER_UUID, "Reach extension block reach", level, AttributeModifier.Operation.ADDITION)
                );
            }
            
            if (player.getAttribute(ForgeMod.ENTITY_REACH.get()) != null) {
                player.getAttribute(ForgeMod.ENTITY_REACH.get()).addTransientModifier(
                    new AttributeModifier(ENTITY_REACH_MODIFIER_UUID, "Reach extension entity reach", level, AttributeModifier.Operation.ADDITION)
                );
            }
        }
    }
}