package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mod.EventBusSubscriber
public class ExperienceHarvestEnchantment extends Enchantment {

    public ExperienceHarvestEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.create("HOE", item -> item instanceof HoeItem), new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.EXPERIENCE_HARVEST.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.EXPERIENCE_HARVEST.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.EXPERIENCE_HARVEST.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }
    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player != null && !player.level().isClientSide()) {
            // 检查玩家主手物品
            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.isEmpty() && heldItem.isEnchanted()) {
                // 检查经验收割附魔等级
                int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.EXPERIENCE_HARVEST.get(), heldItem);
                
                // 如果有任何经验收割附魔，增加掉落的经验
                if (level > 0) {
                    // 每级增加一倍经验（即乘以2的等级次方）
                    int originalExperience = event.getDroppedExperience();
                    int multiplier = (int) Math.pow(2.0, level);
                    event.setDroppedExperience(originalExperience * multiplier);
                }
            }
        }
    }
}