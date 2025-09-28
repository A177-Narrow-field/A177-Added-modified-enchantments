package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class BurningTraumaEnchantment extends Enchantment {
    // 每级伤害增加百分比 (20%)
    private static final double DAMAGE_BONUS_PER_LEVEL = 0.2;

    public BurningTraumaEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("burning_trauma");
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack) && isDiscoverable();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家，受伤实体是否为生物实体
        if (event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = event.getEntity();
            
            // 检查目标是否着火
            if (target.isOnFire()) {
                // 检查玩家主手装备是否有烈火创伤附魔
                ItemStack mainHandItem = player.getMainHandItem();
                int level = mainHandItem.getEnchantmentLevel(ModEnchantments.BURNING_TRAUMA.get());

                // 如果有附魔且等级大于0
                if (level > 0) {
                    // 增加伤害（每级增加20%伤害）
                    float additionalDamage = event.getAmount() * (float) (DAMAGE_BONUS_PER_LEVEL * level);
                    event.setAmount(event.getAmount() + additionalDamage);
                }
            }
        }
    }
}