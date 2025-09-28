package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 添加Enemy接口导入
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;

@Mod.EventBusSubscriber
public class SoulRepulsionEnchantment extends Enchantment {
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("soul_repulsion");
    }

    public SoulRepulsionEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        return EnchantmentCategory.ARMOR_CHEST.canEnchant(stack.getItem());
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.STRONG_THORNS.get();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为玩家
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 检查玩家是否装备了胸甲
        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestItem.isEmpty()) {
            return;
        }

        // 检查胸甲是否有斥魂爆震附魔
        int level = chestItem.getEnchantmentLevel(ModEnchantments.SOUL_REPULSION.get());
        if (level <= 0) {
            return;
        }

        // 检查伤害来源是否为实体攻击
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity == null) {
            return;
        }

        // 计算反伤值（每级增加50%伤害）
        float reflectedDamage = event.getAmount() * (0.5f * level);

        // 获取玩家周围更大的范围内的所有实体（范围随附魔等级增加）
        double range = 1.0 + (level * 1.1); // 基础3格范围，每级增加1格
        AABB boundingBox = player.getBoundingBox().inflate(range, range/2, range);
        
        // 播放爆炸音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

        // 对范围内的敌人造成伤害和击退
        for (Entity entity : player.level().getEntities(player, boundingBox)) {
            // 检查实体是否为生物且不是玩家自己，且是敌对生物
            if (entity instanceof LivingEntity livingEntity && !entity.equals(player) && livingEntity instanceof Enemy) {
                // 对敌人造成反伤
                livingEntity.hurt(player.damageSources().playerAttack(player), reflectedDamage);
                
                // 使用与InfernalArmorEnchantment相同的击退方法（力度减半）
                double knockbackStrength = (0.4 + (0.2 * level)) * 0.5;
                livingEntity.knockback(knockbackStrength, player.getX() - livingEntity.getX(), player.getZ() - livingEntity.getZ());

                // 添加垂直方向的击退力（力度减半）
                livingEntity.setDeltaMovement(
                    livingEntity.getDeltaMovement().x,
                    livingEntity.getDeltaMovement().y + (0.2 * level * 0.5),
                    livingEntity.getDeltaMovement().z
                );
                
                // 强制更新实体状态
                livingEntity.hurtMarked = true;
            }
        }
    }
}