package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.List;

@Mod.EventBusSubscriber
public class ShieldShockEnchantment extends Enchantment {
    // 基础范围半径
    private static final double BASE_RANGE = 0.5;

    public ShieldShockEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.create("SHIELD", item -> item instanceof ShieldItem), new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    public AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.SHIELD_SHOCK;
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
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在盾牌上
        return stack.getItem() instanceof ShieldItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否持有盾牌
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();
            
            ItemStack shield = ItemStack.EMPTY;
            if (mainHandItem.getItem() instanceof ShieldItem) {
                shield = mainHandItem;
            } else if (offHandItem.getItem() instanceof ShieldItem) {
                shield = offHandItem;
            }
            
            // 如果玩家持有附魔盾牌
            if (!shield.isEmpty() && shield.isEnchanted() && shield.getEnchantmentLevel(ModEnchantments.SHIELD_SHOCK.get()) > 0) {
                int level = shield.getEnchantmentLevel(ModEnchantments.SHIELD_SHOCK.get());
                
                // 计算范围（基础0.5格 + 每级0.5格）
                double range = BASE_RANGE + level;
                
                // 创建一个包围盒，覆盖玩家周围的区域
                AABB boundingBox = player.getBoundingBox().inflate(range, range, range);
                
                // 获取范围内的所有生物实体
                List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, boundingBox, 
                    entity -> entity != player && entity.isAlive());
                
                // 击退范围内的生物（击退力度减半）
                for (LivingEntity entity : entities) {
                    // 计算击退方向（从玩家指向实体）
                    Vec3 knockbackDirection = entity.position().subtract(player.position()).normalize();
                    
                    // 应用击退效果（击退力度减半）
                    entity.knockback(0.3F + 0.1F * level, -knockbackDirection.x, -knockbackDirection.z);
                }
                
                // 消耗盾牌耐久度：每次触发成功消耗5%的耐久度，最少消耗5点
                int maxDamage = shield.getMaxDamage();
                if (maxDamage > 0) {
                    // 计算5%的耐久度（最少5点）
                    int damageByPercent = Math.max(5, maxDamage / 20);
                    // 确定盾牌所在的装备槽位
                    EquipmentSlot slot = shield.equals(mainHandItem) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    // 应用耐久度消耗
                    shield.hurtAndBreak(damageByPercent, player, (p) -> p.broadcastBreakEvent(slot));
                }
            }
        }
    }
}