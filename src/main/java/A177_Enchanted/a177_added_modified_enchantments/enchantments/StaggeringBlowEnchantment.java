package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class StaggeringBlowEnchantment extends Enchantment {
    // UUID用于属性修饰符，确保唯一性
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("cb3f55d3-645c-4d1a-8b3b-7b4c7b6c7b6d");

    public StaggeringBlowEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在武器和工具上
        return EnchantmentCategory.WEAPON.canEnchant(stack.getItem()) || 
               EnchantmentCategory.DIGGER.canEnchant(stack.getItem()) ||
               EnchantmentCategory.BOW.canEnchant(stack.getItem()) ||
               EnchantmentCategory.CROSSBOW.canEnchant(stack.getItem());
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("staggering_blow");
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
        return this.canEnchant(stack);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.SHARP_EDGE.get();
    }
    
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack itemStack = event.getItemStack();
        
        // 检查物品是否有顿挫附魔
        int level = itemStack.getEnchantmentLevel(ModEnchantments.STAGGERING_BLOW.get());
        
        if (level <= 0) {
            return;
        }
        
        // 只处理装备在主手的物品
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 每级减少1.5点伤害
        double damageReduction = -1.5 * level;
        
        // 添加伤害减少修饰符
        event.addModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 
            new net.minecraft.world.entity.ai.attributes.AttributeModifier(ATTACK_DAMAGE_MODIFIER, 
                "Staggering blow damage reduction", 
                damageReduction, 
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
    }
}