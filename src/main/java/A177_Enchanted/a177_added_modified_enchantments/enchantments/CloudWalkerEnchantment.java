package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModBlocks;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class CloudWalkerEnchantment extends Enchantment {
    // 缓存玩家的附魔等级
    private static final Map<UUID, Integer> PLAYER_ENCHANTMENT_CACHE = new HashMap<>();
    
    // 缓存玩家的下次生成时间
    private static final Map<UUID, Long> PLAYER_NEXT_GENERATE_TIME = new HashMap<>();
    
    // 不同等级的生成间隔（ticks）
    private static final int[] GENERATE_INTERVALS = {0, 15, 10, 1}; // 索引0未使用，1-3级对应值

    public CloudWalkerEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
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
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.CLOUD_WALKER.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.CLOUD_WALKER.isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.CLOUD_WALKER.isTradeable.get();
    }//可通过交易获得

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && 
               AllEnchantmentsConfig.CLOUD_WALKER.isDiscoverable.get();}//可以正确的出现在附魔台

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return enchantment != ModEnchantments.FOOT_BLOCK.get() && 
               enchantment != ModEnchantments.RANGE_FOOT_BLOCK.get() && 
               super.checkCompatibility(enchantment);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        Player player = event.player;
        UUID playerId = player.getUUID();
        Level level = player.level();
        long currentTime = level.getGameTime();
        
        // 获取缓存的附魔等级，如果不存在则计算并缓存
        int cloudWalkerLevel = PLAYER_ENCHANTMENT_CACHE.getOrDefault(playerId, -1);
        if (cloudWalkerLevel == -1) {
            cloudWalkerLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.CLOUD_WALKER.get(), player);
            PLAYER_ENCHANTMENT_CACHE.put(playerId, cloudWalkerLevel);
        }
        
        // 只有在有附魔时才处理效果
        if (cloudWalkerLevel > 0) {
            // 检查是否需要生成云
            Long nextGenerateTime = PLAYER_NEXT_GENERATE_TIME.get(playerId);
            if (nextGenerateTime == null || currentTime >= nextGenerateTime) {
                applyCloudWalkerEffect(player, cloudWalkerLevel);
                PLAYER_NEXT_GENERATE_TIME.put(playerId, currentTime + GENERATE_INTERVALS[cloudWalkerLevel]);
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        // 当玩家装备的靴子发生变化时更新缓存
        if (event.getEntity() instanceof Player player && event.getSlot() == net.minecraft.world.entity.EquipmentSlot.FEET) {
            updatePlayerEnchantmentCache(player);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存
        Player player = event.getEntity();
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_NEXT_GENERATE_TIME.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存
        Player player = event.getEntity();
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_NEXT_GENERATE_TIME.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时清理缓存
        Player player = event.getEntity();
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_NEXT_GENERATE_TIME.remove(playerId);
    }
    
    /**
     * 应用踏云效果
     * 在玩家脚下生成云块
     * @param player 玩家实体
     * @param level 附魔等级
     */
    private static void applyCloudWalkerEffect(Player player, int level) {
        // 玩家蹲下时不生成
        if (player.isCrouching()) {
            return;
        }
        
        Level levelObj = player.level();
        
        // 检查维度和高度条件
        if (!canGenerateCloudAt(levelObj, player.blockPosition(), player)) {
            return;
        }
        
        // 确定云块生成位置（玩家脚下）
        BlockPos playerPos = player.blockPosition().below();
        
        // 生成4x4的云平台
        generateCloudPlatform(levelObj, playerPos, level);
    }
    
    /**
     * 判断是否可以在当前位置生成云
     * @param level 世界
     * @param pos 位置
     * @param player 玩家
     * @return 是否可以生成
     */
    private static boolean canGenerateCloudAt(Level level, BlockPos pos, Player player) {
        // 检查玩家是否手持下界之星
        boolean isHoldingNetherStar = isPlayerHoldingNetherStar(player);
        
        // 在所有维度中，只要高度在195层以上，或者玩家手持下界之星，就可以生成云块
        if (pos.getY() >= 195 || isHoldingNetherStar) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查玩家是否手持下界之星
     * @param player 玩家
     * @return 是否手持下界之星
     */
    private static boolean isPlayerHoldingNetherStar(Player player) {
        // 检查主手
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() == Items.NETHER_STAR) {
            return true;
        }
        
        // 检查副手
        ItemStack offHandItem = player.getOffhandItem();
        if (offHandItem.getItem() == Items.NETHER_STAR) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 生成云平台
     * @param level 世界
     * @param centerPos 中心位置
     * @param enchantmentLevel 附魔等级
     */
    private static void generateCloudPlatform(Level level, BlockPos centerPos, int enchantmentLevel) {
        if (!(level instanceof ServerLevel)) {
            return; // 只在服务端生成方块
        }
        
        int radius = 1; // 3x3平台，半径为1
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos pos = centerPos.offset(x, 0, z);
                
                // 只检查位置是否为空气
                if (level.isEmptyBlock(pos)) {
                    // 生成云块
                    level.setBlock(pos, ModBlocks.CLOUD_BLOCK.get().defaultBlockState(), 3);
                }
            }
        }
    }
    
    /**
     * 当玩家装备物品发生变化时调用此方法来更新缓存
     * @param player 玩家实体
     */
    public static void updatePlayerEnchantmentCache(Player player) {
        UUID playerId = player.getUUID();
        int cloudWalkerLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.CLOUD_WALKER.get(), player);
        PLAYER_ENCHANTMENT_CACHE.put(playerId, cloudWalkerLevel);
        
        // 更新下次生成时间
        if (cloudWalkerLevel > 0) {
            PLAYER_NEXT_GENERATE_TIME.put(playerId, player.level().getGameTime() + GENERATE_INTERVALS[cloudWalkerLevel]);
        } else {
            PLAYER_NEXT_GENERATE_TIME.remove(playerId);
        }
    }
}