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

import java.util.Random;

@Mod.EventBusSubscriber
public class ResistanceSurgeEnchantment extends Enchantment {
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("resistance_burst");
    }

    public ResistanceSurgeEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
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
        return 10;
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
            // 检查玩家胸甲是否有抗性突效附魔
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RESISTANCE_SURGE.get(), chestplate);

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 计算触发概率（每级10%）
                double chance = level * 0.1;
                Random random = new Random();

                // 判断是否触发效果
                if (random.nextDouble() < chance) {
                    // 给予玩家抗性提升效果，持续3秒（60 ticks）
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0));
                }
            }
        }
    }
}