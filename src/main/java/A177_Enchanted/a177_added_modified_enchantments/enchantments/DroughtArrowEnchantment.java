package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.List;

@Mod.EventBusSubscriber
public class DroughtArrowEnchantment extends Enchantment {

    public DroughtArrowEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("drought_arrow");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 30;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
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
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.WATER_ARROW.get();//禁止与水矢一起使用
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只能应用于弓
        return EnchantmentCategory.BOW.canEnchant(stack.getItem());
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在弓上
        return stack.getItem() instanceof BowItem;
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否为箭的事件
        if (event.getEntity() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有干矢附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.DROUGHT_ARROW.get());
                if (level > 0) {
                    // 标记箭矢具有干矢效果
                    arrow.getPersistentData().putBoolean("HasDroughtEffect", true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否具有干矢效果
            if (arrow.getPersistentData().getBoolean("HasDroughtEffect")) {
                clearWaterAlongPath(arrow);
            }
        }
    }


    /**
     * 清除箭矢飞行路径上的水方块
     * @param arrow 箭矢实体
     */
    private static void clearWaterAlongPath(AbstractArrow arrow) {
        Level level = arrow.level();
        // 获取箭矢当前位置和前一位置
        BlockPos currentPos = arrow.blockPosition();
        BlockPos previousPos = new BlockPos(
            (int) (arrow.xo - arrow.getDeltaMovement().x),
            (int) (arrow.yo - arrow.getDeltaMovement().y),
            (int) (arrow.zo - arrow.getDeltaMovement().z)
        );

        // 计算两点之间的包围盒
        AABB pathBox = new AABB(currentPos, previousPos);
        
        // 扩大包围盒以覆盖更大的区域
        pathBox = pathBox.inflate(3, 3, 3);
        
        // 获取包围盒内的所有方块
        BlockPos.betweenClosedStream(pathBox).forEach(pos -> {
            BlockState blockState = level.getBlockState(pos);
            // 检查是否是水方块
            if (blockState.getBlock() == Blocks.WATER) {
                // 移除水方块
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                // 播放蒸发声音
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            } 
            // 检查是否是含水方块（如含水方块）
            else if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && 
                     blockState.getValue(BlockStateProperties.WATERLOGGED)) {
                // 将含水方块设置为不含水状态
                level.setBlock(pos, blockState.setValue(BlockStateProperties.WATERLOGGED, false), 3);
                // 播放蒸发声音
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            }
            // 检查是否是含水的植物方块（如海草、海带等）
            else if (blockState.getBlock() == Blocks.SEAGRASS || 
                     blockState.getBlock() == Blocks.TALL_SEAGRASS ||
                     blockState.getBlock() == Blocks.KELP || 
                     blockState.getBlock() == Blocks.KELP_PLANT) {
                // 移除含水植物方块
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                // 播放蒸发声音
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            }
        });
    }
}