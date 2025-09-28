package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class PointShootEnchantment extends Enchantment {
    public PointShootEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("point_shoot");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
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

    // 添加与穿体附魔的冲突规则
    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) 
                && enchantment != ModEnchantments.PIERCING.get();
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        // 检查使用的武器是否有点射附魔
        Player player = event.getEntity();
        ItemStack weapon = event.getBow();
        
        if (weapon != null && weapon.getItem() instanceof BowItem) {
            int level = weapon.getEnchantmentLevel(ModEnchantments.POINT_SHOOT.get());
            if (level > 0) {
                // 增加800%的飞行速度
                int additionalCharge = (int)(event.getCharge() * 80.0);
                event.setCharge(Math.min(event.getCharge() + additionalCharge, 20));
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查箭是否由附魔武器射出
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有点射附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.POINT_SHOOT.get());
                if (level > 0) {
                    // 减少80%伤害
                    arrow.setBaseDamage(arrow.getBaseDamage() * 0.2);
                    
                    // 只有在击中生物实体时才消除箭矢，避免击中方块时也消失
                    if (event.getRayTraceResult() != null && event.getRayTraceResult().getType() == HitResult.Type.ENTITY) {
                        // 设置箭矢不穿透，防止回弹问题
                        arrow.setPierceLevel((byte) 0);
                        
                        // 设置箭矢在击中目标后不再存在，彻底解决回弹问题
                        arrow.discard();
                    }
                }
            }
        }
    }
}