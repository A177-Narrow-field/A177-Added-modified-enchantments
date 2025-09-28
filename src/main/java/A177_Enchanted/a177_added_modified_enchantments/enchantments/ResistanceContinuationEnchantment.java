package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ResistanceContinuationEnchantment extends Enchantment {

    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("resistance_sustained");
    }

    public ResistanceContinuationEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        return stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getType() == ArmorItem.Type.CHESTPLATE;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为玩家且不在客户端
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            // 检查玩家是否拥有抗性提升效果
            if (player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                // 检查玩家胸甲是否有抗性续效附魔
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESISTANCE_CONTINUATION.get(), chestplate);

                // 如果有附魔且等级大于0
                if (level > 0) {
                    // 给予玩家抗性提升效果，等级根据附魔等级确定，持续3秒（60 ticks）
                    // 注意：药水等级从0开始，所以附魔1级对应药水0级
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, level - 1));
                }
            }
        }
    }
}