package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class SickleSlashEnchantment extends Enchantment {
    
    // UUID用于属性修饰符，确保唯一性
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("cb3f55d3-645c-4d1a-8b3b-7b4c7b6c7b70");

    public SickleSlashEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
    
    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.SICKLE_SLASH.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.SICKLE_SLASH.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.SICKLE_SLASH.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack) && isDiscoverable();
    }//可以正确的出现在附魔台

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在锄上
        return stack.getItem() instanceof HoeItem;
    }
    
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack itemStack = event.getItemStack();
        
        // 检查物品是否有镰斩附魔
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SICKLE_SLASH.get(), itemStack);
        if (level <= 0) {
            return;
        }
        
        // 只处理装备在主手的物品
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 每级增加80%伤害（显示在物品描述中）
        double damageBonus = level * 0.8;
        
        // 添加伤害加成修饰符
        event.addModifier(Attributes.ATTACK_DAMAGE, 
            new AttributeModifier(ATTACK_DAMAGE_MODIFIER, 
                "Sickle slash damage bonus", 
                damageBonus, 
                AttributeModifier.Operation.MULTIPLY_BASE));
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手装备是否有镰斩附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SICKLE_SLASH.get(), mainHandItem);
            
            // 如果有附魔且等级大于0
            if (level > 0) {
                // 增加伤害（每级增加80%伤害）
                float additionalDamage = event.getAmount() * (level * 0.8f);
                event.setAmount(event.getAmount() + additionalDamage);
            }
        }
    }
}