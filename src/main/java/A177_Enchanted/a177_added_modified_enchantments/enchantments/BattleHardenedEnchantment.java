package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class BattleHardenedEnchantment extends Enchantment {
    
    // UUID用于属性修饰符，确保唯一性
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("cb3f55d3-645c-4d1a-8b3b-7b4c7b6c7b6c");
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("battle_hardened_weapon");
    }

    public BattleHardenedEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem;
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
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
        return super.checkCompatibility(other) && other != ModEnchantments.PROMISING_BLADE.get();
    }
    
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack itemStack = event.getItemStack();
        
        // 检查物品是否有久经沙场附魔
        int level = itemStack.getEnchantmentLevel(ModEnchantments.BATTLE_HARDENED.get());
        if (level <= 0) {
            return;
        }
        
        // 只处理装备在主手的物品
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 只处理有耐久度的物品
        if (!itemStack.isDamageableItem()) {
            return;
        }
        
        // 计算耐久度百分比
        int maxDamage = itemStack.getMaxDamage();
        int currentDamage = itemStack.getDamageValue();
        
        // 计算已损失的耐久度百分比 (0.0 to 1.0)
        double damagePercent = (double) currentDamage / maxDamage;
        
        // 每减少10%耐久度，增加10%攻击伤害
        // 例如：耐久度减少30%，则增加30%攻击伤害
        double damageBonus = damagePercent;
        
        // 添加攻击伤害加成修饰符
        event.addModifier(Attributes.ATTACK_DAMAGE, 
            new AttributeModifier(ATTACK_DAMAGE_MODIFIER, 
                "Battle hardened damage bonus", 
                damageBonus, 
                AttributeModifier.Operation.MULTIPLY_BASE));
    }
}