package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.UUID;

@Mod.EventBusSubscriber
public class StonePelletManEnchantment extends Enchantment {
    // 用于属性修饰符的UUID
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("22222222-3333-4444-5555-666666666666");
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("33333333-4444-5555-6666-777777777777");
    
    public StonePelletManEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
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
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("stone_pellet_man");
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

    // 添加属性修饰符来显示附魔效果
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        // 只有当物品被装备在胸甲槽位时才应用属性修饰符
        if (event.getSlotType() == EquipmentSlot.CHEST) {
            ItemStack stack = event.getItemStack();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STONE_PELLET_MAN.get(), stack);
            
            if (level > 0) {
                // 添加击退抗性修饰符 (10点击退抗性)
                event.addModifier(Attributes.KNOCKBACK_RESISTANCE, 
                    new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "Stone pellet man knockback resistance", 10.0, AttributeModifier.Operation.ADDITION));
                
                // 添加护甲韧性修饰符 (5点韧性)
                event.addModifier(Attributes.ARMOR_TOUGHNESS, 
                    new AttributeModifier(ARMOR_TOUGHNESS_UUID, "Stone pellet man armor toughness", 5.0, AttributeModifier.Operation.ADDITION));
                
                // 添加攻击伤害修饰符 (增加4点伤害)
                event.addModifier(Attributes.ATTACK_DAMAGE, 
                    new AttributeModifier(ATTACK_DAMAGE_UUID, "Stone pellet man attack damage", 4.0, AttributeModifier.Operation.ADDITION));
            }
        }
    }
    
    // 处理伤害事件
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 获取玩家装备的胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STONE_PELLET_MAN.get(), chestplate);
            
            if (level > 0) {
                // 增加20%受到的伤害
                event.setAmount(event.getAmount() * 1.2f);
            }
        }
    }
    
    // 击败生物事件
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            // 获取玩家装备的胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STONE_PELLET_MAN.get(), chestplate);
            
            if (level > 0) {
                // 10%概率获得幸运10持续12秒并回复10%血量
                if (Math.random() < 0.1) {
                    // 给予幸运10效果，持续12秒（240 ticks）
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 240, 9)); // 等级9 = 幸运10
                    
                    // 回复10%血量
                    float maxHealth = player.getMaxHealth();
                    player.heal(maxHealth * 0.1f);
                }
            }
        }
    }
    
    // 玩家每刻更新事件处理
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        // 只在服务端执行，且在结束阶段，每20 ticks（1秒）执行一次
        if (!player.level().isClientSide && event.phase == TickEvent.Phase.END && player.tickCount % 20 == 0) {
            // 获取玩家装备的胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STONE_PELLET_MAN.get(), chestplate);
            
            if (level > 0) {
                // 如果玩家ID名字为"Zi__min"，则持续获得幸运10和村庄英雄
                if ("Zi__min".equals(player.getName().getString())) {
                    // 给予幸运10效果，持续5秒
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 100, 9));
                    // 给予村庄英雄效果，持续5秒
                    player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 100, 0));
                }
            }
        }
    }
}