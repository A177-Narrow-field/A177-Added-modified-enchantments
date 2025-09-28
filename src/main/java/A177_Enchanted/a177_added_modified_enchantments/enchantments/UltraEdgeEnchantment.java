package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.UUID;

@Mod.EventBusSubscriber
public class UltraEdgeEnchantment extends Enchantment {
    
    // UUID用于属性修饰符，确保唯一性
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("cb3f55d3-645c-4d1a-8b3b-7b4c7b6c7b6f");
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("ultra_edge");
    }

    public UltraEdgeEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 10;
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
        // 可以附在工具和武器上
        return EnchantmentCategory.WEAPON.canEnchant(stack.getItem()) || 
               EnchantmentCategory.DIGGER.canEnchant(stack.getItem());
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
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean isAllowedOnBooks() {
        // 允许在附魔书上显示，这样创造模式物品栏会显示附魔书
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other);
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack itemStack = event.getItemStack();
        
        // 检查物品是否有极锋附魔
        int level = itemStack.getEnchantmentLevel(ModEnchantments.ULTRA_EDGE.get());
        if (level <= 0) {
            return;
        }
        
        // 只处理装备在主手的物品
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 每级增加40%伤害（显示在物品描述中）
        double damageBonus = level * 0.4;
        
        // 添加伤害加成修饰符
        event.addModifier(Attributes.ATTACK_DAMAGE, 
            new AttributeModifier(ATTACK_DAMAGE_MODIFIER, 
                "Ultra edge damage bonus", 
                damageBonus, 
                AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            
            // 检查主手物品是否有极锋附魔
            if (mainHandItem.isEnchanted()) {
                int level = mainHandItem.getEnchantmentLevel(ModEnchantments.ULTRA_EDGE.get());
                if (level > 0) {
                    // 每级增加40%伤害
                    float additionalDamage = event.getAmount() * (0.4f * level);
                    event.setAmount(event.getAmount() + additionalDamage);
                    
                    // 消耗耐久度：每次使用消耗2%耐久度，最少1点
                    int currentDamage = mainHandItem.getDamageValue();
                    int maxDamage = mainHandItem.getMaxDamage();
                    if (maxDamage > 0) { // 确保物品有耐久度
                        int damageToApply = Math.max(1, maxDamage / 50); // 计算2%耐久度，最少1点
                        // 防止耐久度超过最大值
                        int newDamage = Math.min(currentDamage + damageToApply, maxDamage);
                        mainHandItem.setDamageValue(newDamage);
                    }
                }
            }
        }
    }
}