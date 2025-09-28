package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.List;

@Mod.EventBusSubscriber
public class HeadhunterEnchantment extends Enchantment {
    
    public HeadhunterEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("headhunter");
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
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器上
        return EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
    }

    @Override
    public boolean isTradeable() {
        // 不可通过交易获得
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        // 可在附魔台发现
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

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害源是否来自玩家
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            
            // 检查武器是否带有弑魁附魔
            int level = weapon.getEnchantmentLevel(ModEnchantments.HEADHUNTER.get());
            if (level > 0) {
                // 查找玩家10格范围内的敌人
                LivingEntity targetEnemy = findHighestHealthEnemy(player);
                
                if (targetEnemy != null) {
                    // 计算伤害加成：每10点生命值+1点伤害
                    float enemyHealth = targetEnemy.getHealth();
                    int damageBonus = (int) (enemyHealth / 10);
                    
                    // 限制伤害加成最大为40
                    damageBonus = Math.min(damageBonus, 40);
                    
                    // 增加伤害
                    event.setAmount(event.getAmount() + (damageBonus * level));
                }
            }
        }
    }
    
    /**
     * 查找玩家附近血量最高的敌人
     * @param player 玩家
     * @return 血量最高的敌人，如果不存在则返回null
     */
    private static LivingEntity findHighestHealthEnemy(Player player) {
        // 获取玩家周围10格范围内的敌对实体
        AABB boundingBox = player.getBoundingBox().inflate(10.0D);
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                boundingBox,
                entity -> entity != player && 
                         entity.isAlive() && 
                         entity instanceof Mob &&  // 敌对生物
                         entity.hasLineOfSight(player)  // 确保实体在玩家视线内
        );
        
        LivingEntity highestHealthEnemy = null;
        float highestHealth = 0;
        
        for (LivingEntity entity : nearbyEntities) {
            float health = entity.getHealth();
            if (health > highestHealth && health >= 10) {  // 只有血量大于等于10的敌人才计算
                highestHealth = health;
                highestHealthEnemy = entity;
            }
        }
        
        return highestHealthEnemy;
    }
}