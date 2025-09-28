package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.List;

@Mod.EventBusSubscriber
public class SweepingFireEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("sweeping_fire");
    }

    public SweepingFireEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在剑上
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 可以在附魔台附魔，但仅限于剑类武器
        return canEnchant(stack) && isDiscoverable();
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

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        // 检查攻击者是否为玩家
        Player player = event.getEntity();
        ItemStack mainHandItem = player.getMainHandItem();
        
        // 检查主手物品是否有火之横扫附魔
        if (mainHandItem.isEnchanted()) {
            int level = mainHandItem.getEnchantmentLevel(ModEnchantments.SWEEPING_FIRE.get());
            if (level > 0) {
                // 获取附近的实体
                List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                    LivingEntity.class, 
                    player.getBoundingBox().inflate(3.0D), 
                    entity -> entity != player && entity.isAlive() && !entity.fireImmune()
                );
                
                // 给每个附近的实体添加燃烧效果
                for (LivingEntity entity : nearbyEntities) {
                    // 每级增加2秒燃烧时间
                    int fireDuration = level * 2 * 20; // 转换为tick（1秒=20tick）
                    entity.setSecondsOnFire(fireDuration / 20);
                }
            }
        }
    }
}