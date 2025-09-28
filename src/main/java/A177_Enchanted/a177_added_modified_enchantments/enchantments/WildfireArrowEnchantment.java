package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class WildfireArrowEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("wildfire_arrow");
    }

    public WildfireArrowEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
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
            // 检查撞击结果是否为实体
            if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
                // 检查被击中的实体是否为生物实体
                Entity targetEntity = entityHitResult.getEntity();
                if (targetEntity instanceof LivingEntity target && !(target instanceof Player)) {
                    // 检查箭是否由玩家射出
                    if (arrow.getOwner() instanceof Player player) {
                        // 获取玩家使用的武器（弓）
                        ItemStack weapon = player.getMainHandItem();
                        if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem)) {
                            weapon = player.getOffhandItem();
                        }

                        // 检查武器是否有燎原矢附魔
                        int level = weapon.getEnchantmentLevel(ModEnchantments.WILDFIRE_ARROW.get());
                        if (level > 0) {
                            // 先让被直接命中的目标着火
                            target.setSecondsOnFire(4);
                            
                            // 在目标周围6格范围内造成火焰效果
                            Level levelObj = target.level();
                            double x = target.getX();
                            double y = target.getY();
                            double z = target.getZ();
                            
                            // 获取范围内的所有实体
                            AABB boundingBox = new AABB(x - 6, y - 6, z - 6, x + 6, y + 6, z + 6);
                            for (Entity entity : levelObj.getEntities(target, boundingBox)) {
                                if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player)) {
                                    // 点燃实体6秒
                                    livingEntity.setSecondsOnFire(6);
                                    
                                    // 在服务端生成火焰粒子效果
                                    if (levelObj instanceof ServerLevel serverLevel) {
                                        serverLevel.sendParticles(
                                            ParticleTypes.FLAME,
                                            entity.getX(),
                                            entity.getY() + entity.getBbHeight() / 2,
                                            entity.getZ(),
                                            10,
                                            entity.getBbWidth() / 2,
                                            entity.getBbHeight() / 2,
                                            entity.getBbWidth() / 2,
                                            0.1
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}