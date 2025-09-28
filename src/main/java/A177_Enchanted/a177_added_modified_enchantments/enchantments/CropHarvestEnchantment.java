package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.CropHarvestConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@Mod.EventBusSubscriber
public class CropHarvestEnchantment extends Enchantment {

    public CropHarvestEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.create("HOE", item -> item instanceof HoeItem), new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.CROP_HARVEST.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.CROP_HARVEST.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.CROP_HARVEST.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player != null && !player.level().isClientSide()) {
            // 检查破坏的方块是否为农作物
            BlockState state = event.getState();
            BlockPos pos = event.getPos();
            Level world = player.level();
            
            if (isConfiguredCrop(state, world, pos) && isCropMaxAge(state, world, pos)) {
                // 检查玩家主手物品
                ItemStack heldItem = player.getMainHandItem();
                if (!heldItem.isEmpty() && heldItem.isEnchanted()) {
                    // 检查作物收割附魔等级
                    int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CROP_HARVEST.get(), heldItem);
                    
                    // 如果有任何作物收割附魔，增加掉落物
                    if (level > 0) {
                        // 每级增加一倍掉落物（即乘以2的等级次方）
                        int multiplier = (int) Math.pow(2.0, level) - 1; // 减1是因为原本就会掉落一次
                        
                        // 获取作物的掉落物
                        LootParams.Builder lootParams = new LootParams.Builder((ServerLevel) world)
                                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                                .withParameter(LootContextParams.TOOL, heldItem);
                        List<ItemStack> drops = state.getDrops(lootParams);
                        
                        // 增加额外的掉落物
                        for (int i = 0; i < multiplier; i++) {
                            for (ItemStack drop : drops) {
                                if (!drop.isEmpty()) {
                                    // 创建新的物品堆叠，避免修改原始堆叠
                                    ItemStack extraDrop = drop.copy();
                                    // 在作物位置附近掉落额外物品
                                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, extraDrop);
                                    world.addFreshEntity(itemEntity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 检查方块是否为配置文件中定义的农作物
     * @param state 方块状态
     * @param world 世界
     * @param pos 位置
     * @return 是否为配置的农作物
     */
    private static boolean isConfiguredCrop(BlockState state, Level world, BlockPos pos) {
        // 获取方块的注册名
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId != null) {
            // 检查是否在配置文件列表中
            return CropHarvestConfig.Common.cropHarvestCrops.contains(blockId.toString());
        }
        return false;
    }

    /**
     * 检查农作物是否成熟
     * @param state 方块状态
     * @param world 世界
     * @param pos 位置
     * @return 是否成熟
     */
    private static boolean isCropMaxAge(BlockState state, Level world, BlockPos pos) {
        // 检查普通作物（小麦等）
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        }
        
        // 检查地狱疣
        if (state.getBlock() instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        
        // 对于其他类型的作物，默认返回true
        return true;
    }
}