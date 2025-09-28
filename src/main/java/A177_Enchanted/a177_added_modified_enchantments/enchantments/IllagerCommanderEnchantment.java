package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.List;

@Mod.EventBusSubscriber
public class IllagerCommanderEnchantment extends Enchantment {
    // 保护范围（方块）
    private static final double GUARD_RADIUS = 16.0;
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("illager_commander");
    }

    public IllagerCommanderEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 是否可发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return enchantment != ModEnchantments.UNDEAD_COMMANDER.get() &&
               enchantment != ModEnchantments.COMMANDER.get() &&
               super.checkCompatibility(enchantment);
    }

    /**
     * 阻止灾厄生物将穿戴此附魔的玩家作为攻击目标
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player player) {
            // 检查玩家是否装备了灾厄统领附魔头盔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLAGER_COMMANDER.get(), helmet) > 0) {
                // 如果攻击者是灾厄生物，则取消攻击目标
                LivingEntity attacker = event.getEntity();
                if (attacker instanceof Raider) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * 当穿戴此附魔的玩家受到攻击时，召唤附近的灾厄生物攻击攻击者
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否装备了灾厄统领附魔头盔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLAGER_COMMANDER.get(), helmet) > 0) {
                // 获取攻击者
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity livingAttacker) {
                    // 查找附近的灾厄生物
                    AABB boundingBox = player.getBoundingBox().inflate(GUARD_RADIUS);
                    List<Monster> nearbyIllagers = player.level().getNearbyEntities(
                            Monster.class,
                            TargetingConditions.forCombat(),
                            player,
                            boundingBox
                    ).stream().filter(entity -> entity instanceof Raider).toList();

                    // 让附近的灾厄生物攻击攻击者
                    for (Monster illager : nearbyIllagers) {
                        // 确保灾厄生物不是攻击者自己
                        if (illager != livingAttacker) {
                            illager.setTarget(livingAttacker);
                        }
                    }
                }
            }
        }
    }
}