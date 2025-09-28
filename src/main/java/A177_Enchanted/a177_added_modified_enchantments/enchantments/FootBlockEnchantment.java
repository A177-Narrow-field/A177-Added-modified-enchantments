package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
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

@Mod.EventBusSubscriber
public class FootBlockEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("foot_block");
    }

    public FootBlockEnchantment() {
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
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return enchantment != ModEnchantments.CLOUD_WALKER.get() && 
               enchantment != ModEnchantments.RANGE_FOOT_BLOCK.get() && 
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
            int footBlockLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.FOOT_BLOCK.get(), player);

            // 只有在有附魔时才处理效果
            if (footBlockLevel > 0) {
                tryPlaceBlockUnderPlayer(player);
            }
        }
    }

    /**
     * 尝试在玩家脚下放置方块
     *
     * @param player 玩家实体
     */
    private static void tryPlaceBlockUnderPlayer(Player player) {
        Level level = player.level();
        BlockPos blockPos = player.blockPosition().below();

        // 检查脚下是否是空气方块或者是可替换方块（如草、花等）
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.isAir() && !blockState.canBeReplaced()) {
            return;
        }

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

        // 放置方块
        if (blockItemStack.getItem() instanceof BlockItem blockItem) {
            // 消耗方块（创造模式玩家不消耗方块）
            if (!player.getAbilities().instabuild) {
                if (blockItemStack == mainHandItem) {
                    // 如果是主手的方块，即使是最后一个也消耗
                    mainHandItem.shrink(1);
                } else if (blockItemStack == offHandItem) {
                    // 如果是副手的方块，即使是最后一个也消耗
                    offHandItem.shrink(1);
                } else {
                    // 从背包中移除一个方块
                    ItemStack stack = player.getInventory().getItem(blockItemIndex);
                    stack.shrink(1);
                }
            }

            // 放置方块
            Block block = blockItem.getBlock();
            level.setBlock(blockPos, block.defaultBlockState(), 3);
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
     * 当玩家装备物品发生变化时调用此方法来更新缓存
     *
     * @param player 玩家实体
     */
}