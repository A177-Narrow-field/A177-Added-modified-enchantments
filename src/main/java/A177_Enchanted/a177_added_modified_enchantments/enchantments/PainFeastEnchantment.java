package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class PainFeastEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("pain_feast");
    }

    public PainFeastEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是胸甲时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }//确保在附魔台中可以正确应用

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 不是宝藏附魔

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 可以通过交易获得


    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        // 与大胃袋附魔冲突
        return super.checkCompatibility(enchantment) && enchantment != ModEnchantments.GLUTTONOUS_POUCH.get();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.isEnchanted() && chestplate.getEnchantmentLevel(ModEnchantments.PAIN_FEAST.get()) > 0) {
                // 检查是否为饥饿伤害
                if (event.getSource().is(DamageTypes.STARVE)) {
                    // 饥饿伤害不回复饥饿度，并且受到8伤害
                    event.setAmount(event.getAmount() * 8);
                    return;
                }
                // 检查是否为摔落伤害或窒息伤害
                if (event.getSource().is(DamageTypes.FALL) || event.getSource().is(DamageTypes.DROWN)) {
                    // 摔落伤害和窒息伤害不回复饥饿度
                    return;
                }
                
                int level = chestplate.getEnchantmentLevel(ModEnchantments.PAIN_FEAST.get());
                // 每级回复2点饥饿度，最多5级
                int foodToRestore = level * 2;
                
                // 回复饥饿度
                player.getFoodData().setFoodLevel(Math.min(20, player.getFoodData().getFoodLevel() + foodToRestore));
            }
        }
    }
}