package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mod.EventBusSubscriber
public class KnightEnchantment extends Enchantment {
    // 每级伤害增加百分比 (15%)
    private static final double DAMAGE_BONUS_PER_LEVEL = 0.15;
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("knight");
    }

    public KnightEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器和工具上
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof TieredItem;
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            // 检查玩家是否正在骑乘生物
            if (player.isPassenger() && player.getVehicle() != null) {
                // 获取玩家主手物品上的骑士附魔等级
                ItemStack mainHandItem = player.getMainHandItem();
                int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.KNIGHT.get(), mainHandItem);
                
                // 只在玩家拥有附魔时处理效果
                if (enchantmentLevel > 0) {
                    // 检查玩家是否有有效的武器
                    if (mainHandItem.isEmpty() || !(mainHandItem.getItem() instanceof SwordItem) || mainHandItem.getDamageValue() >= mainHandItem.getMaxDamage()) {
                        return; // 如果没有有效武器，则不增加伤害
                    }
                    
                    // 计算伤害加成 (每级30%)
                    float bonusDamage = event.getAmount() * (float) (enchantmentLevel * DAMAGE_BONUS_PER_LEVEL);
                    
                    // 增加伤害
                    event.setAmount(event.getAmount() + bonusDamage);
                }
            }
        }
    }
}