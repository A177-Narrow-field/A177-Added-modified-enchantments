package A177_Enchanted.a177_added_modified_enchantments.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.List;

public class HardenedLavaBlock extends Block {
    private static final VoxelShape FULL_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    // 检测玩家的范围（2格）
    private static final int PLAYER_DETECTION_RANGE = 4;

    public HardenedLavaBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_SHAPE; // 实心碰撞体积
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F; // 不透明
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false; // 不透明方块，不传播天光
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 5; // 发出亮度为2的光
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // 只在服务端安排任务
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 安排5秒（100 ticks）后将方块设置为岩浆
            serverLevel.scheduleTick(pos, this, 100);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false; // 我们使用scheduleTick而不是随机刻
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 检查附近是否有穿戴熔岩行者附魔的玩家
        if (hasPlayerWithLavaWalkerEnchantment(level, pos)) {
            // 如果有，则重新安排任务，延迟方块消失
            level.scheduleTick(pos, this, 100);
        } else {
            // 当计划的时间到达时，将方块替换为岩浆
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        }
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 当方块被破坏时（且新状态不是同种方块），将方块替换为岩浆
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    /**
     * 检查指定位置附近是否有穿戴熔岩行者附魔的玩家
     * @param level 世界
     * @param pos 方块位置
     * @return 如果附近有穿戴熔岩行者附魔的玩家返回true，否则返回false
     */
    private boolean hasPlayerWithLavaWalkerEnchantment(ServerLevel level, BlockPos pos) {
        // 获取附近的玩家列表
        List<Player> players = level.getEntitiesOfClass(Player.class, 
            net.minecraft.world.phys.AABB.ofSize(pos.getCenter(), 
                PLAYER_DETECTION_RANGE * 2, 
                PLAYER_DETECTION_RANGE * 2, 
                PLAYER_DETECTION_RANGE * 2));
        
        // 检查每个玩家是否穿戴了熔岩行者附魔
        for (Player player : players) {
            // 检查玩家是否装备了熔岩行者附魔的靴子
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LAVA_WALKER.get(), 
                    player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET)) > 0) {
                return true;
            }
        }
        
        return false;
    }
}