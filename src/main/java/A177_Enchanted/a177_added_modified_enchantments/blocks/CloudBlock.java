package A177_Enchanted.a177_added_modified_enchantments.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CloudBlock extends Block {

    private static final VoxelShape FULL_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape EMPTY_SHAPE = Shapes.empty();
    // 检测玩家的范围（2格）
    private static final int PLAYER_DETECTION_RANGE = 2;

    public CloudBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 检查碰撞实体是否带有 CloudWalkerEnchantment
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                if (EnchantmentHelper.getEnchantmentLevel(ModEnchantments.CLOUD_WALKER.get(), livingEntity) > 0) {
                    return FULL_SHAPE; // 有附魔则返回完整碰撞箱
                }
            }
        }
        return EMPTY_SHAPE; // 否则无碰撞
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean isAir(BlockState state) {
        return false; // 必须返回 false，否则玩家会直接掉下去
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction direction) {
        return adjacentBlockState.is(this) || super.skipRendering(state, adjacentBlockState, direction);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // 只在服务端安排任务
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 安排4秒后将方块设置为空气
            serverLevel.scheduleTick(pos, this, 80);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false; // 我们使用scheduleTick而不是随机刻
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 检查附近是否有穿戴踏云附魔的玩家
        if (hasPlayerWithCloudWalkerEnchantment(level, pos)) {
            // 如果有，则重新安排任务，延迟方块消失
            level.scheduleTick(pos, this, 80);
        } else {
            // 当计划的时间到达时，将方块替换为空气
            level.removeBlock(pos, false);
        }
    }
    
    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        // 使实体在落在该方块上时免疫摔落伤害
        entity.resetFallDistance();
        super.updateEntityAfterFallOn(level, entity);
    }
    
    /**
     * 检查指定位置附近是否有穿戴踏云附魔的玩家
     * @param level 世界
     * @param pos 方块位置
     * @return 如果附近有穿戴踏云附魔的玩家返回true，否则返回false
     */
    private boolean hasPlayerWithCloudWalkerEnchantment(ServerLevel level, BlockPos pos) {
        // 获取附近的玩家列表
        List<Player> players = level.getEntitiesOfClass(Player.class, 
            net.minecraft.world.phys.AABB.ofSize(pos.getCenter(), 
                PLAYER_DETECTION_RANGE * 2, 
                PLAYER_DETECTION_RANGE * 2, 
                PLAYER_DETECTION_RANGE * 2));
        
        // 检查每个玩家是否穿戴了踏云附魔
        for (Player player : players) {
            // 检查玩家是否拥有踏云附魔
            if (EnchantmentHelper.getEnchantmentLevel(ModEnchantments.CLOUD_WALKER.get(), player) > 0) {
                return true;
            }
        }
        
        return false;
    }
}