package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class WaterArrowEnchantment extends Enchantment {
    public WaterArrowEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("water_arrow");
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
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有水矢附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.WATER_ARROW.get());
                if (level > 0) {
                    HitResult hitResult = event.getRayTraceResult();
                    
                    // 处理实体击中事件
                    if (hitResult instanceof EntityHitResult entityHitResult) {
                        Entity targetEntity = entityHitResult.getEntity();
                        if (targetEntity instanceof LivingEntity target) {
                            // 熄灭目标身上的火焰
                            target.clearFire();
                            
                            // 对特定生物增加50%伤害
                            if (target instanceof Blaze || target instanceof MagmaCube || 
                                target instanceof SnowGolem) {
                                // 增加50%伤害，通过修改箭的伤害属性实现
                                arrow.setBaseDamage(arrow.getBaseDamage() * 1.5);
                            }
                        }
                    }
                    // 处理方块击中事件
                    else if (hitResult instanceof BlockHitResult blockHitResult) {
                        // 只在非下界维度生成水
                        Level levelObj = arrow.level();
                        if (levelObj.dimension() != Level.NETHER) {
                            BlockPos blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
                            BlockState blockState = levelObj.getBlockState(blockPos);
                            
                            // 检查位置是否为空气或可替换方块
                            if (blockState.isAir() || blockState.canBeReplaced()) {
                                // 在目标位置生成水
                                levelObj.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);

                                // 播放水生成声音
                                levelObj.playSound(null, blockPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }
}