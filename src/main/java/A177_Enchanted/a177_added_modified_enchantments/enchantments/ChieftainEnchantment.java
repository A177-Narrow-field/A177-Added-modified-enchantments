package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class ChieftainEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("chieftain");
    }

    public ChieftainEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在头盔上
        return EnchantmentCategory.ARMOR_HEAD.canEnchant(stack.getItem());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 当玩家杀死生物时触发
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
            // 检查玩家是否拥有匪首附魔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int enchantmentLevel = helmet.getEnchantmentLevel(ModEnchantments.CHIEFTAIN.get());
            
            if (enchantmentLevel > 0) {
                healNearbyPlayers(player.level(), player, enchantmentLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        // 当玩家受到伤害时触发
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            // 检查玩家是否拥有匪首附魔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int enchantmentLevel = helmet.getEnchantmentLevel(ModEnchantments.CHIEFTAIN.get());
            
            if (enchantmentLevel > 0) {
                // 应用30%免伤
                float damageReduction = 0.3f;
                float reducedDamage = event.getAmount() * (1 - damageReduction);
                event.setAmount(reducedDamage);
                
                // 对附近玩家造成相同伤害
                damageNearbyPlayers(player.level(), player, event.getAmount());
            }
        }
    }

    /**
     * 回复附近玩家的生命值
     * @param level 世界
     * @param player 玩家
     * @param enchantmentLevel 附魔等级
     */
    private static void healNearbyPlayers(Level level, Player player, int enchantmentLevel) {
        if (level.isClientSide) return;

        // 计算范围（10格）
        AABB boundingBox = new AABB(player.blockPosition()).inflate(10);
        List<Player> players = level.getEntitiesOfClass(Player.class, boundingBox);
        
        // 计算回复量（每级10%）
        float healAmount = 0.1f * enchantmentLevel;

        // 为附近玩家回复生命值
        for (Player targetPlayer : players) {
            if (targetPlayer != player && targetPlayer.getHealth() < targetPlayer.getMaxHealth()) {
                float healthToHeal = targetPlayer.getMaxHealth() * healAmount;
                targetPlayer.heal(healthToHeal);
            }
        }
    }

    /**
     * 对附近玩家造成伤害
     * @param level 世界
     * @param player 玩家
     * @param damage 伤害值
     */
    private static void damageNearbyPlayers(Level level, Player player, float damage) {
        if (level.isClientSide) return;

        // 计算范围（10格）
        AABB boundingBox = new AABB(player.blockPosition()).inflate(10);
        List<Player> players = level.getEntitiesOfClass(Player.class, boundingBox);

        // 对附近玩家造成相同伤害
        for (Player targetPlayer : players) {
            if (targetPlayer != player) {
                targetPlayer.hurt(player.damageSources().magic(), damage);
            }
        }
    }
}