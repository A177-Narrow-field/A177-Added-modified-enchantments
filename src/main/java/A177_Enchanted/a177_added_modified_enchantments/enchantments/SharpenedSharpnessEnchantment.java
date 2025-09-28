package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.UUID;

@Mod.EventBusSubscriber
public class SharpenedSharpnessEnchantment extends Enchantment {
    
    // 创建一个自定义的附魔类别，支持武器和工具
    public static final EnchantmentCategory WEAPON_AND_DIGGER = EnchantmentCategory.create("WEAPON_AND_DIGGER", 
        item -> EnchantmentCategory.WEAPON.canEnchant(item) || EnchantmentCategory.DIGGER.canEnchant(item));
    
    // UUID用于属性修饰符，确保唯一性
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("cb3f55d3-645c-4d1a-8b3b-7b4c7b6c7b6e");
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("sharpened_sharpness");
    }

    public SharpenedSharpnessEnchantment() {
        super(Rarity.VERY_RARE, WEAPON_AND_DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在工具和武器上
        return EnchantmentCategory.DIGGER.canEnchant(stack.getItem()) || EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.SHARPNESS;
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack itemStack = event.getItemStack();
        
        // 检查物品是否有强化锋利附魔
        int level = itemStack.getEnchantmentLevel(ModEnchantments.SHARPENED_SHARPNESS.get());
        
        if (level <= 0) {
            return;
        }
        
        // 只处理装备在主手的物品
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 每级增加2点伤害
        double damageBonus = 2.0 * level;
        
        // 添加伤害加成修饰符
        event.addModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 
            new net.minecraft.world.entity.ai.attributes.AttributeModifier(ATTACK_DAMAGE_MODIFIER, 
                "Sharpened sharpness damage bonus", 
                damageBonus, 
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害源是否来自玩家
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            ItemStack weapon = player.getMainHandItem();
            
            // 检查武器是否带有强化锋利附魔
            int level = weapon.getEnchantmentLevel(ModEnchantments.SHARPENED_SHARPNESS.get());
            if (level > 0) {
                // 每级增加2点伤害
                event.setAmount(event.getAmount() + (level * 2.0f));
            }
        }
    }
}