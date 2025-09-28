package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.RangeFootBlockConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class RangeFootBlockEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("range_foot_block");
    }

    public RangeFootBlockEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
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
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return enchantment != ModEnchantments.CLOUD_WALKER.get() &&
                enchantment != ModEnchantments.FOOT_BLOCK.get() &&
                super.checkCompatibility(enchantment);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (!player.level().isClientSide) {
            // 直接获取附魔等级，不使用缓存
            int rangeFootBlockLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.RANGE_FOOT_BLOCK.get(), player);

            // 只有在有附魔时才处理效果
            if (rangeFootBlockLevel > 0) {
                tryPlaceBlockUnderPlayer(player);
            }
        }
    }

    /**
     * 尝试在玩家脚下指定范围放置方块
     *
     * @param player 玩家实体
     */
    private static void tryPlaceBlockUnderPlayer(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition().below();

        // 检查玩家是否手持镐类工具
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        boolean isHoldingPickaxe = mainHandItem.getItem() instanceof PickaxeItem ||
                offHandItem.getItem() instanceof PickaxeItem;

        // 如果没有手持镐类工具，则不能放置方块
        if (!isHoldingPickaxe) {
            return;
        }

        // 查找玩家背包中的方块和索引位置
        Object[] result = findBlockItemStackWithIndex(player);
        ItemStack blockItemStack = (ItemStack) result[0];
        int blockItemIndex = (Integer) result[1];

        if (blockItemStack.isEmpty()) {
            return;
        }

        // 获取方块类型
        Block targetBlock = ((BlockItem) blockItemStack.getItem()).getBlock();

        // 使用配置文件中的范围值
        int range = RangeFootBlockConfig.range.get();

        // 收集指定范围内可替换的空气方块位置
        List<BlockPos> replaceablePositions = new ArrayList<>();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                BlockPos pos = playerPos.offset(x, 0, z);
                BlockState blockState = level.getBlockState(pos);
                if (blockState.isAir() || blockState.canBeReplaced()) {
                    replaceablePositions.add(pos);
                }
            }
        }

        // 如果没有可替换的位置，直接返回
        if (replaceablePositions.isEmpty()) {
            return;
        }

        // 计算玩家拥有的方块总数
        int totalBlockCount = getTotalBlockCount(player, targetBlock);

        // 确定可以放置的方块数量（不超过可替换位置数和拥有方块数的最小值）
        int placeCount = Math.min(replaceablePositions.size(), totalBlockCount);

        // 如果没有方块可以放置，直接返回
        if (placeCount <= 0) {
            return;
        }

        // 消耗方块（创造模式玩家不消耗方块）
        if (!player.getAbilities().instabuild) {
            consumeBlockItems(player, placeCount, targetBlock);
        }

        // 放置方块
        for (int i = 0; i < placeCount; i++) {
            BlockPos pos = replaceablePositions.get(i);
            level.setBlock(pos, targetBlock.defaultBlockState(), 3);
        }
    }

    /**
     * 查找玩家背包中的方块物品和索引位置
     *
     * @param player 玩家实体
     * @return 包含方块物品堆和索引位置的数组，如果找不到则返回空物品堆和-1
     */
    private static Object[] findBlockItemStackWithIndex(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        // 确定手上拿的方块类型
        Block targetBlock = null;
        if (mainHandItem.getItem() instanceof BlockItem) {
            targetBlock = ((BlockItem) mainHandItem.getItem()).getBlock();
        } else if (offHandItem.getItem() instanceof BlockItem) {
            targetBlock = ((BlockItem) offHandItem.getItem()).getBlock();
        } else {
            // 手上没有方块，无法确定要放置什么方块
            return new Object[]{ItemStack.EMPTY, -1};
        }

        // 首先检查物品栏中是否有相同类型的方块
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) stack.getItem()).getBlock();
                if (block == targetBlock) {
                    return new Object[]{stack, i};
                }
            }
        }

        // 然后检查主手是否是方块（即使是最后一个）
        if (mainHandItem.getItem() instanceof BlockItem) {
            Block block = ((BlockItem) mainHandItem.getItem()).getBlock();
            if (block == targetBlock) {
                return new Object[]{mainHandItem, -1};
            }
        }

        // 最后检查副手是否是方块（即使是最后一个）
        if (offHandItem.getItem() instanceof BlockItem) {
            Block block = ((BlockItem) offHandItem.getItem()).getBlock();
            if (block == targetBlock) {
                return new Object[]{offHandItem, -1};
            }
        }

        // 如果没有找到可用的方块，返回空
        return new Object[]{ItemStack.EMPTY, -1};
    }

    /**
     * 计算玩家拥有的指定类型方块总数
     *
     * @param player 玩家实体
     * @param targetBlock 目标方块类型
     * @return 玩家拥有的方块总数
     */
    private static int getTotalBlockCount(Player player, Block targetBlock) {
        int totalCount = 0;

        // 检查背包中的方块
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) stack.getItem()).getBlock();
                if (block == targetBlock) {
                    totalCount += stack.getCount();
                }
            }
        }

        // 检查主手（排除在背包中已经计算过的情况）
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() instanceof BlockItem) {
            Block block = ((BlockItem) mainHandItem.getItem()).getBlock();
            if (block == targetBlock) {
                // 检查主手物品是否已经在背包中计算过了
                boolean alreadyCounted = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack == mainHandItem) {
                        alreadyCounted = true;
                        break;
                    }
                }
                if (!alreadyCounted) {
                    totalCount += mainHandItem.getCount();
                }
            }
        }

        // 检查副手（排除在背包中已经计算过的情况）
        ItemStack offHandItem = player.getOffhandItem();
        if (offHandItem.getItem() instanceof BlockItem) {
            Block block = ((BlockItem) offHandItem.getItem()).getBlock();
            if (block == targetBlock) {
                // 检查副手物品是否已经在背包中计算过了
                boolean alreadyCounted = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack == offHandItem) {
                        alreadyCounted = true;
                        break;
                    }
                }
                if (!alreadyCounted) {
                    totalCount += offHandItem.getCount();
                }
            }
        }

        return totalCount;
    }

    /**
     * 消耗指定数量的方块物品
     *
     * @param player 玩家实体
     * @param count 需要消耗的数量
     * @param targetBlock 目标方块类型
     */
    private static void consumeBlockItems(Player player, int count, Block targetBlock) {
        int remainingCount = count;

        // 优先消耗背包中的方块
        for (int i = 0; i < player.getInventory().getContainerSize() && remainingCount > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) stack.getItem()).getBlock();
                if (block == targetBlock) {
                    int consumeCount = Math.min(stack.getCount(), remainingCount);
                    stack.shrink(consumeCount);
                    remainingCount -= consumeCount;
                }
            }
        }

        // 如果背包中的方块不够，消耗手上的方块
        if (remainingCount > 0) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) mainHandItem.getItem()).getBlock();
                if (block == targetBlock) {
                    // 检查主手物品是否已经在背包中处理过了
                    boolean alreadyConsumed = false;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack == mainHandItem) {
                            alreadyConsumed = true;
                            break;
                        }
                    }
                    if (!alreadyConsumed) {
                        int consumeCount = Math.min(mainHandItem.getCount(), remainingCount);
                        mainHandItem.shrink(consumeCount);
                        remainingCount -= consumeCount;
                    }
                }
            }
        }

        if (remainingCount > 0) {
            ItemStack offHandItem = player.getOffhandItem();
            if (offHandItem.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) offHandItem.getItem()).getBlock();
                if (block == targetBlock) {
                    // 检查副手物品是否已经在背包中处理过了
                    boolean alreadyConsumed = false;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack == offHandItem) {
                            alreadyConsumed = true;
                            break;
                        }
                    }
                    if (!alreadyConsumed) {
                        int consumeCount = Math.min(offHandItem.getCount(), remainingCount);
                        offHandItem.shrink(consumeCount);
                        remainingCount -= consumeCount;
                    }
                }
            }
        }
    }
}