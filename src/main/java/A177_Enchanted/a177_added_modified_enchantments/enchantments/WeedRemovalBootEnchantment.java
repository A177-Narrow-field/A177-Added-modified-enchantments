package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.A177_added_modified_enchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.WeedRemovalConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class WeedRemovalBootEnchantment extends Enchantment {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeedRemovalBootEnchantment.class);

    // 定义需要被清除的杂草方块集合
    private static final Set<Block> WEED_BLOCKS = new HashSet<>();
    
    static {
        // 初始化默认的杂草方块集合
        initWeedBlocks();
    }

    public WeedRemovalBootEnchantment() {
        // 修改为只能应用于锄头类工具
        super(Rarity.COMMON, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
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
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 修改为只能应用于锄头类工具
        return stack.getItem() instanceof HoeItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack) && isDiscoverable();
    }//可以正确的出现在附魔台
    
    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.WEED_REMOVAL_BOOT.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.WEED_REMOVAL_BOOT.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.WEED_REMOVAL_BOOT.isTradeable.get();
    }

    /**
     * 初始化杂草方块集合
     */
    private static void initWeedBlocks() {
        WEED_BLOCKS.clear();
        
        // 尝试从配置文件加载方块列表
        try {
            if (WeedRemovalConfig.weedBlockList != null && !WeedRemovalConfig.weedBlockList.get().isEmpty()) {
                for (String blockId : WeedRemovalConfig.weedBlockList.get()) {
                    ResourceLocation resourceLocation = ResourceLocation.tryParse(blockId);
                    if (resourceLocation != null) {
                        Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                        if (block != null && block != Blocks.AIR) {
                            WEED_BLOCKS.add(block);
                        }
                    }
                }
            }
        } catch (IllegalStateException e) {
            // 配置尚未加载，稍后会重新初始化
            LOGGER.warn("WeedRemovalConfig not yet loaded, will retry later");
        }
    }
    
    // 添加一个公共方法，用于重新加载配置
    public static void reloadConfig() {
        initWeedBlocks();
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // 获取破坏方块的玩家
        Player player = event.getPlayer();
        
        // 检查玩家是否蹲下
        if (!player.isCrouching()) {
            return;
        }

        // 检查玩家是否手持锄头
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        boolean isHoldingHoe = mainHandItem.getItem() instanceof HoeItem || offHandItem.getItem() instanceof HoeItem;
        
        if (!isHoldingHoe) {
            return;
        }

        // 检查锄头是否具有精准采集附魔
        boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, mainHandItem) > 0 || 
                              EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, offHandItem) > 0;

        // 获取玩家手持的锄头附魔等级
        int weedRemovalLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WEED_REMOVAL_BOOT.get(), mainHandItem);
        ItemStack hoeItem = mainHandItem;

        // 只有在有附魔时才处理效果
        if (weedRemovalLevel > 0) {
            tryRemoveWeeds(player, hasSilkTouch, weedRemovalLevel, event.getPos(), hoeItem);
        } else {
            // 检查副手
            weedRemovalLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WEED_REMOVAL_BOOT.get(), offHandItem);
            if (weedRemovalLevel > 0) {
                hoeItem = offHandItem;
                tryRemoveWeeds(player, hasSilkTouch, weedRemovalLevel, event.getPos(), hoeItem);
            }
        }
    }

    /**
     * 处理玩家周围的草类植被清除
     *
     * @param player 玩家实体
     * @param hasSilkTouch 剪刀是否具有精准采集附魔
     * @param level 附魔等级
     * @param hoeItem 使用的锄头物品
     */
    private static void tryRemoveWeeds(Player player, boolean hasSilkTouch, int level, BlockPos centerPos, ItemStack hoeItem) {
        Level levelObj = player.level();
        if (!(levelObj instanceof ServerLevel)) {
            return;
        }

        // 计算范围：默认2格，每级增加2格
        int range = 2 + (level - 1) * 2;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    BlockState checkBlockState = levelObj.getBlockState(checkPos);
                    
                    // 检查是否为需要清除的草类植被方块
                    if (WEED_BLOCKS.contains(checkBlockState.getBlock())) {
                        // 根据是否具有精准采集附魔来决定破坏方式
                        if (hasSilkTouch) {
                            // 如果有精准采集，则掉落方块本身
                            Block.popResource(levelObj, checkPos, new ItemStack(checkBlockState.getBlock()));
                            levelObj.removeBlock(checkPos, false);
                        } else {
                            // 破坏方块并掉落物品（默认方式）
                            levelObj.destroyBlock(checkPos, true);
                        }
                    }
                }
            }
        }
        
        // 消耗锄头5%的耐久度
        if (hoeItem.isDamageableItem()) {
            int damage = Math.max(1, hoeItem.getMaxDamage() / 20); // 5%耐久度，至少消耗1点
            hoeItem.hurtAndBreak(damage, player, (p) -> {
                p.broadcastBreakEvent(hoeItem.equals(p.getMainHandItem()) ? 
                    EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            });
        }
    }
}