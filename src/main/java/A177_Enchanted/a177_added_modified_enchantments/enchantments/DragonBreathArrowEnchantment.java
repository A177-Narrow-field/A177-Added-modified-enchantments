package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class DragonBreathArrowEnchantment extends Enchantment {

    public DragonBreathArrowEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("dragon_breath_arrow");
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

                // 检查武器是否有龙息矢附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.DRAGON_BREATH_ARROW.get());
                if (level > 0) {
                    // 检查撞击结果是否为实体
                    if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
                        // 检查被击中的实体是否为生物实体
                        if (entityHitResult.getEntity() instanceof LivingEntity target && !(target instanceof Player)) {
                            // 在目标位置创建龙息效果云
                            createDragonBreathEffect(target);
                        }
                    } else {
                        // 射中方块时也触发龙息效果
                        createDragonBreathEffectAtBlock(arrow);
                    }
                }
            }
        }
    }

    /**
     * 在箭射中的方块位置创建龙息效果
     * @param arrow 箭
     */
    private static void createDragonBreathEffectAtBlock(AbstractArrow arrow) {
        // 在箭的位置创建区域效果云（龙息）
        AreaEffectCloud areaEffectCloud = new AreaEffectCloud(arrow.level(), arrow.getX(), arrow.getY(), arrow.getZ());
        areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
        areaEffectCloud.setRadius(3.0F); // 设置半径为3格
        areaEffectCloud.setDuration(200); // 持续时间10秒(200 ticks)
        areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / (float)areaEffectCloud.getDuration()); // 逐渐缩小
        areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0)); // 添加伤害效果
        
        // 设置龙息特有的属性
        if (arrow.getOwner() instanceof LivingEntity) {
            areaEffectCloud.setOwner((LivingEntity) arrow.getOwner()); // 设置所有者
        }
        
        // 在世界中添加效果云
        arrow.level().addFreshEntity(areaEffectCloud);
        
        // 播放龙息声音
        arrow.level().playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(), 
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    /**
     * 在目标位置创建龙息效果
     * @param target 被击中的目标
     */
    private static void createDragonBreathEffect(LivingEntity target) {
        // 创建区域效果云（龙息）
        AreaEffectCloud areaEffectCloud = new AreaEffectCloud(target.level(), target.getX(), target.getY(), target.getZ());
        areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
        areaEffectCloud.setRadius(3.0F); // 设置半径为3格
        areaEffectCloud.setDuration(200); // 持续时间10秒(200 ticks)
        areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / (float)areaEffectCloud.getDuration()); // 逐渐缩小
        areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0)); // 添加伤害效果
        
        // 设置龙息特有的属性
        areaEffectCloud.setOwner(target); // 设置所有者
        
        // 在世界中添加效果云
        target.level().addFreshEntity(areaEffectCloud);
        
        // 播放龙息声音
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(), 
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
    }
}