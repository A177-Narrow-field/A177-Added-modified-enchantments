package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FoodPowerStrikeEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("food_power_strike");
    }
    
    public FoodPowerStrikeEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }//确保在附魔台中可以正确应用

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return enchantment != ModEnchantments.BITE.get() && super.checkCompatibility(enchantment);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack weapon = player.getMainHandItem();
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FOOD_POWER_STRIKE.get(), weapon);

        // 如果武器有食力斩附魔
        if (level > 0) {
            // 消耗4点饥饿度（食物值）
            if (player.getFoodData().getFoodLevel() >= 4) {
                player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 4);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手装备是否有食力斩附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FOOD_POWER_STRIKE.get(), mainHandItem);

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 获取玩家当前饥饿值
                int foodLevel = player.getFoodData().getFoodLevel();
                
                // 每1点饥饿值增加10%伤害
                float damageMultiplier = 1.0f + (foodLevel * 0.1f);
                event.setAmount(event.getAmount() * damageMultiplier);
            }
        }
    }
}