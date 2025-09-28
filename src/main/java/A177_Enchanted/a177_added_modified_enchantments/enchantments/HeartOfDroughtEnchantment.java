package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HeartOfDroughtEnchantment extends Enchantment {
    // 冷却时间记录，使用玩家UUID作为键
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 20; // 1秒冷却时间（单位为tick）

    public HeartOfDroughtEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 25;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("heart_of_drought");
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
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench) && ench != ModEnchantments.HEART_OF_TIDE.get();
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        // 检查是否是玩家
        if (!(event.getEntity() instanceof Player player)) return;
        
        // 只在服务端执行逻辑
        if (player.level().isClientSide()) return;

        // 检查玩家是否装备了干涸之心附魔的胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = EnchantmentHelper.getItemEnchantmentLevel(
                A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments.HEART_OF_DROUGHT.get(), 
                chestplate);

        // 如果没有装备或者附魔等级为0，则不处理
        if (level <= 0) return;

        // 检查玩家是否蹲下，只有蹲下时才能清除水
        if (!player.isCrouching()) return;

        // 检查冷却时间
        UUID playerUUID = player.getUUID();
        long currentTime = player.level().getGameTime();
        
        if (cooldowns.containsKey(playerUUID) && currentTime < cooldowns.get(playerUUID)) {
            return; // 冷却中，不处理
        }

        // 清除玩家周围6格内的水
        boolean removedWater = false;
        BlockPos playerPos = player.blockPosition();
        
        // 遍历玩家周围6格范围
        for (int x = -6; x <= 6; x++) {
            for (int y = -6; y <= 6; y++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockState blockState = player.level().getBlockState(pos);
                    FluidState fluidState = player.level().getFluidState(pos);
                    
                    // 检查是否是水方块
                    if (blockState.getBlock() == Blocks.WATER) {
                        // 移除水方块
                        player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        removedWater = true;
                    } 
                    // 检查是否是含水方块（如含水方块）
                    else if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && 
                             blockState.getValue(BlockStateProperties.WATERLOGGED)) {
                        // 将含水方块设置为不含水状态
                        player.level().setBlock(pos, blockState.setValue(BlockStateProperties.WATERLOGGED, false), 3);
                        removedWater = true;
                    }
                    // 检查是否是含水的植物方块（如海草、海带等）
                    else if (blockState.getBlock() == Blocks.SEAGRASS || 
                             blockState.getBlock() == Blocks.TALL_SEAGRASS ||
                             blockState.getBlock() == Blocks.KELP || 
                             blockState.getBlock() == Blocks.KELP_PLANT) {
                        // 移除含水植物方块
                        player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        removedWater = true;
                    }
                }
            }
        }

        // 如果成功移除了水，则恢复玩家生命值并设置冷却时间
        if (removedWater) {
            // 恢复1格生命值
            player.heal(1.0F);
            
            // 设置冷却时间
            cooldowns.put(playerUUID, currentTime + COOLDOWN_TIME);
        }
    }
}