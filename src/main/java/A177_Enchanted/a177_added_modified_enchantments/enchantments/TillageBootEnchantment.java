package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.TillageBootConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.lang.reflect.Field;
import java.util.Map;

@Mod.EventBusSubscriber
public class TillageBootEnchantment extends Enchantment {

    public TillageBootEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 15;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 10;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 该附魔只能应用于锄头类工具
        return this.category.canEnchant(stack.getItem()) && stack.getItem() instanceof HoeItem;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.TILLAGE_BOOT.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.TILLAGE_BOOT.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.TILLAGE_BOOT.isTradeable.get();
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        // 与除草附魔和作物收获附魔冲突
        return super.checkCompatibility(enchantment) 
            && enchantment != ModEnchantments.WEED_REMOVAL_BOOT.get()
            && enchantment != ModEnchantments.CROP_HARVEST.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (!player.level().isClientSide) {
            // 检查配置是否启用
            if (!TillageBootConfig.enabled.get()) {
                return;
            }
            
            // 直接获取附魔等级，不使用缓存
            ItemStack heldItem = player.getMainHandItem();
            int tillageBootLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TILLAGE_BOOT.get(), heldItem);

            // 只有在有附魔时才处理效果
            if (tillageBootLevel > 0) {
                tryHarvestAndPlantCrops(player);
            } else {
                // 检查副手
                heldItem = player.getOffhandItem();
                tillageBootLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TILLAGE_BOOT.get(), heldItem);
                if (tillageBootLevel > 0) {
                    tryHarvestAndPlantCrops(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.isShiftKeyDown()) {
            // 检查配置是否启用
            if (!TillageBootConfig.enabled.get()) {
                return;
            }

            // 检查玩家是否手持附魔的锄头
            ItemStack heldItem = player.getMainHandItem();
            int tillageBootLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TILLAGE_BOOT.get(), heldItem);

            if (tillageBootLevel > 0 && heldItem.getItem() instanceof HoeItem) {
                // 执行范围破坏作物操作
                breakCropsInRange(player, event.getPos());
                // 取消事件以防止正常的挖掘操作
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && !player.isShiftKeyDown()) { // 确保不是Shift键（避免与左击冲突）
            // 检查配置是否启用
            if (!TillageBootConfig.enabled.get()) {
                return;
            }

            // 检查玩家是否手持附魔的锄头
            ItemStack heldItem = player.getMainHandItem();
            int tillageBootLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TILLAGE_BOOT.get(), heldItem);

            if (tillageBootLevel > 0 && heldItem.getItem() instanceof HoeItem) {
                // 执行范围耕地操作
                tillGroundInRange(player, event.getPos(), event.getFace());
            }
        }
    }

    /**
     * 在指定范围内破坏作物（无视成熟度）
     *
     * @param player 玩家实体
     * @param centerPos 中心位置
     */
    private static void breakCropsInRange(Player player, BlockPos centerPos) {
        Level level = player.level();
        int range = TillageBootConfig.range.get();

        // 获取玩家手持的工具
        ItemStack heldItem = player.getMainHandItem();
        // 获取时运附魔等级
        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem);

        // 定义工作区域
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    BlockState checkBlockState = level.getBlockState(checkPos);

                    // 破坏农作物方块（无视成熟度）
                    if (checkBlockState.getBlock() instanceof CropBlock) {
                        // 收获农作物，考虑时运附魔
                        Block.dropResources(checkBlockState, level, checkPos, null, player, heldItem);
                        level.destroyBlock(checkPos, false);
                    }
                }
            }
        }
    }

    /**
     * 处理玩家脚位置的成熟农作物收获和种植操作
     *
     * @param player 玩家实体
     */
    private static void tryHarvestAndPlantCrops(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // 检查玩家是否手持锄类工具
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        boolean isHoldingHoe = mainHandItem.getItem() instanceof HoeItem || 
                              offHandItem.getItem() instanceof HoeItem;
                              
        // 检查玩家是否持有种子
        boolean isHoldingSeed = isSeedItem(mainHandItem.getItem()) || isSeedItem(offHandItem.getItem());

        // 确定玩家手上持有的种子类型
        Item heldSeedItem = null;
        if (isSeedItem(mainHandItem.getItem())) {
            heldSeedItem = mainHandItem.getItem();
        } else if (isSeedItem(offHandItem.getItem())) {
            heldSeedItem = offHandItem.getItem();
        }

        // 获取配置的范围值
        int range = TillageBootConfig.range.get();

        // 获取玩家手持的工具（用于时运附魔）
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof HoeItem)) {
            heldItem = player.getOffhandItem();
        }

        // 定义工作区域
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                BlockPos checkPos = playerPos.offset(x, 1, z); // 检查玩家上方位置
                BlockState checkBlockState = level.getBlockState(checkPos);
                
                // 只有在手持锄头时才进行收获和种植
                if (isHoldingHoe) {
                    // 收获成熟农作物
                    if (checkBlockState.getBlock() instanceof CropBlock cropBlock) {
                        // 检查农作物是否成熟
                        if (cropBlock.isMaxAge(checkBlockState)) {
                            // 收获农作物，考虑时运附魔
                            Block.dropResources(checkBlockState, level, checkPos, null, player, heldItem);
                            level.destroyBlock(checkPos, false);
                        }
                    }
                    
                    // 只有在同时持有种子时才进行种植
                    if (isHoldingSeed && heldSeedItem != null) {
                        // 检查当前位置下方是否为耕地
                        BlockPos belowPos = checkPos.below();
                        BlockState belowBlockState = level.getBlockState(belowPos);
                        if (belowBlockState.getBlock() instanceof FarmBlock) {
                            // 检查当前位置是否是空气方块（可以种植）
                            if (checkBlockState.isAir()) {
                                // 查找玩家拥有的可种植农作物（只查找与手上种子类型相同的）
                                ItemStack seedItemStack = findSameTypeSeedItemStack(player, heldSeedItem);
                                if (!seedItemStack.isEmpty()) {
                                    // 获取种子对应的农作物
                                    Block cropBlock = getCropBlockFromSeed(seedItemStack.getItem());
                                    if (cropBlock != null && canPlantOnBlock(cropBlock, belowBlockState.getBlock())) {
                                        // 消耗种子（创造模式玩家不消耗种子）
                                        if (!player.getAbilities().instabuild) {
                                            consumeSeedItem(player, seedItemStack.getItem());
                                        }
                                        
                                        // 种植农作物
                                        level.setBlock(checkPos, cropBlock.defaultBlockState(), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 查找玩家拥有的与指定类型相同的种子物品
     *
     * @param player 玩家实体
     * @param seedType 要查找的种子类型
     * @return 种子物品堆，如果找不到则返回空物品堆
     */
    private static ItemStack findSameTypeSeedItemStack(Player player, Item seedType) {
        // 首先检查背包中的同类型种子
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == seedType) {
                return stack;
            }
        }
        
        // 然后检查主手是否是指定类型的种子（即使是最后一个）
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() == seedType) {
            return mainHandItem;
        }

        // 最后检查副手是否是指定类型的种子（即使是最后一个）
        ItemStack offHandItem = player.getOffhandItem();
        if (offHandItem.getItem() == seedType) {
            return offHandItem;
        }

        // 如果没有找到可用的指定类型种子，返回空
        return ItemStack.EMPTY;
    }

    /**
     * 查找玩家拥有的种子物品
     *
     * @param player 玩家实体
     * @return 种子物品堆，如果找不到则返回空物品堆
     */
    private static ItemStack findSeedItemStack(Player player) {
        // 首先检查背包中的种子
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isSeedItem(stack.getItem())) {
                return stack;
            }
        }
        
        // 然后检查主手是否是种子（即使是最后一个）
        ItemStack mainHandItem = player.getMainHandItem();
        if (isSeedItem(mainHandItem.getItem())) {
            return mainHandItem;
        }

        // 最后检查副手是否是种子（即使是最后一个）
        ItemStack offHandItem = player.getOffhandItem();
        if (isSeedItem(offHandItem.getItem())) {
            return offHandItem;
        }

        // 如果没有找到可用的种子，返回空
        return ItemStack.EMPTY;
    }

    /**
     * 判断物品是否为种子
     *
     * @param item 物品
     * @return 是否为种子
     */
    private static boolean isSeedItem(Item item) {
        // 检查配置文件中的种子ID列表
        for (String seedId : TillageBootConfig.cropMappings.get()) {
            Item seedItem = ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse(seedId));
            if (seedItem != null && seedItem == item) {
                return true;
            }
        }
        
        // 如果配置中没有找到，尝试检查物品是否属于常见的种子类型
        // 这可以提高对其他模组农作物的支持
        return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof CropBlock;
    }

    /**
     * 根据种子获取对应的农作物方块
     *
     * @param seedItem 种子物品
     * @return 农作物方块
     */
    private static Block getCropBlockFromSeed(Item seedItem) {
        // 通过配置文件查找种子对应的农作物
        for (String seedId : TillageBootConfig.cropMappings.get()) {
            Item configSeedItem = ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse(seedId));
            if (configSeedItem != null && configSeedItem == seedItem) {
                // 根据种子ID推断作物ID
                String cropId = getCropIdFromSeedId(seedId);
                Block cropBlock = ForgeRegistries.BLOCKS.getValue(net.minecraft.resources.ResourceLocation.tryParse(cropId));
                return cropBlock;
            }
        }
        
        // 如果在配置中没有找到，尝试通过其他方式获取作物方块
        // 这可以提高对其他模组农作物的支持
        if (seedItem instanceof BlockItem) {
            Block block = ((BlockItem) seedItem).getBlock();
            // 如果种子本身就是作物方块，则直接返回
            if (block instanceof CropBlock) {
                return block;
            }
        }
        
        return null;
    }

    /**
     * 根据种子ID推断作物ID
     *
     * @param seedId 种子ID
     * @return 作物ID
     */
    private static String getCropIdFromSeedId(String seedId) {
        // 处理特殊情况
        switch (seedId) {
            case "minecraft:carrot":
                return "minecraft:carrots";
            case "minecraft:potato":
                return "minecraft:potatoes";
            case "minecraft:wheat_seeds":
                return "minecraft:wheat";
            case "minecraft:beetroot_seeds":
                return "minecraft:beetroots";
            case "minecraft:melon_seeds":
                return "minecraft:melon_stem";
            case "minecraft:pumpkin_seeds":
                return "minecraft:pumpkin_stem";
            default:
                // 对于一般情况，尝试多种推断方式
                // 1. 尝试移除"_seeds"后缀
                if (seedId.endsWith("_seeds")) {
                    return seedId.substring(0, seedId.length() - 6); // 移除"_seeds"
                }
                // 2. 尝试添加"s"后缀（处理carrot->carrots这样的情况）
                if (!seedId.endsWith("s")) {
                    return seedId + "s";
                }
                // 3. 如果以上都不匹配，则直接返回种子ID（假设种子和作物同名）
                return seedId;
        }
    }

    /**
     * 检查农作物是否可以种植在指定方块上
     *
     * @param cropBlock 农作物方块
     * @param groundBlock 地面方块
     * @return 是否可以种植
     */
    private static boolean canPlantOnBlock(Block cropBlock, Block groundBlock) {
        // 通过配置文件查找农作物可以种植的方块
        for (String seedId : TillageBootConfig.cropMappings.get()) {
            String cropId = getCropIdFromSeedId(seedId);
            
            Block configCropBlock = ForgeRegistries.BLOCKS.getValue(net.minecraft.resources.ResourceLocation.tryParse(cropId));
            if (configCropBlock != null && configCropBlock == cropBlock) {
                // 找到了农作物的配置，检查地面方块是否匹配
                // 目前简化处理，只检查是否为耕地
                return groundBlock instanceof FarmBlock;
            }
        }
        // 默认情况下，农作物可以种植在耕地上
        return groundBlock instanceof FarmBlock;
    }

    /**
     * 消耗玩家的种子物品
     *
     * @param player 玩家实体
     * @param seedItem 要消耗的种子物品
     */
    private static void consumeSeedItem(Player player, Item seedItem) {
        // 优先消耗背包中的种子
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == seedItem) {
                stack.shrink(1);
                return;
            }
        }
        
        // 如果背包中没有，消耗手上的种子
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() == seedItem) {
            mainHandItem.shrink(1);
            return;
        }
        
        ItemStack offHandItem = player.getOffhandItem();
        if (offHandItem.getItem() == seedItem) {
            offHandItem.shrink(1);
        }
    }

    /**
     * 在指定范围内耕地
     *
     * @param player 玩家实体
     * @param centerPos 中心位置
     * @param face 点击的面
     */
    private static void tillGroundInRange(Player player, BlockPos centerPos, Direction face) {
        Level level = player.level();
        int range = TillageBootConfig.range.get();

        // 只有点击的是上表面才进行范围耕地
        if (face != Direction.UP) {
            return;
        }

        boolean tilled = false;
        // 定义工作区域
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                BlockPos checkPos = centerPos.offset(x, 0, z);
                BlockState checkBlockState = level.getBlockState(checkPos);
                
                // 检查是否可以耕地（例如草方块、泥土等）
                BlockState tilledState = getTilledState(checkBlockState);
                if (tilledState != null) {
                    // 耕地
                    level.setBlock(checkPos, tilledState, 3);
                    tilled = true;
                }
            }
        }
        
        // 如果至少耕了一块地，则消耗锄头耐久
        if (tilled) {
            ItemStack heldItem = player.getMainHandItem();
            heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }
    }
    
    /**
     * 获取耕地后的状态
     * 
     * @param state 原始方块状态
     * @return 耕地后的状态，如果不能耕地则返回null
     */
    private static BlockState getTilledState(BlockState state) {
        Block block = state.getBlock();
        
        try {
            // 使用反射访问HoeItem的TILLABLES字段
            Field tillablesField = HoeItem.class.getDeclaredField("TILLABLES");
            tillablesField.setAccessible(true);
            Map<Block, Object> tillables = (Map<Block, Object>) tillablesField.get(null);
            
            // 检查该方块是否可以在HoeItem的可耕地列表中找到
            if (tillables.containsKey(block)) {
                // 获取耕地后的状态
                Object tillable = tillables.get(block);
                Field turnedStateField = tillable.getClass().getDeclaredField("turnedState");
                turnedStateField.setAccessible(true);
                return (BlockState) turnedStateField.get(tillable);
            }
        } catch (Exception e) {
            // 如果反射失败，回退到手动检查
            if (block instanceof GrassBlock || block == Blocks.DIRT || block == Blocks.COARSE_DIRT) {
                return Blocks.FARMLAND.defaultBlockState();
            }
        }
        
        return null;
    }

}