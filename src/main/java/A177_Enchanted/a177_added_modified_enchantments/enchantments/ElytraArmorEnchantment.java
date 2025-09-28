package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.UUID;

@Mod.EventBusSubscriber
public class ElytraArmorEnchantment extends Enchantment {
    // 为每个属性定义唯一的UUID，防止与其他修饰符冲突
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_ID = UUID.fromString("12345678-1234-1234-1234-123456789013");
    
    public ElytraArmorEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在鞘翅上
        return stack.getItem() instanceof ElytraItem;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("elytra_armor");
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
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    /**
     * 添加属性修饰符来显示附魔效果
     */
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        // 只有当物品被装备在胸甲槽位时才应用属性修饰符
        if (event.getSlotType() == EquipmentSlot.CHEST) {
            ItemStack stack = event.getItemStack();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ELYTRA_ARMOR.get(), stack);
            
            if (level > 0) {
                // 添加护甲值修饰符
                event.addModifier(Attributes.ARMOR, 
                    new AttributeModifier(ARMOR_MODIFIER_ID, "Elytra armor bonus", 8.0, AttributeModifier.Operation.ADDITION));
                
                // 添加韧性值修饰符
                event.addModifier(Attributes.ARMOR_TOUGHNESS, 
                    new AttributeModifier(ARMOR_TOUGHNESS_MODIFIER_ID, "Elytra armor toughness bonus", 5.0, AttributeModifier.Operation.ADDITION));
            }
        }
    }
}