package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class HorseBootsEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("horse_boots");
    }
    
    // 属性修饰符UUID（确保唯一性）
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    public HorseBootsEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在靴子上
        return EnchantmentCategory.ARMOR_FEET.canEnchant(stack.getItem());
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 检查实体是否被玩家骑乘
        if (!entity.level().isClientSide && entity.getControllingPassenger() instanceof Player) {
            Player player = (Player) entity.getControllingPassenger();
            
            // 检查玩家是否拥有策马靴附魔
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            int enchantmentLevel = boots.getEnchantmentLevel(ModEnchantments.HORSE_BOOTS.get());
            
            if (enchantmentLevel > 0) {
                // 每级增加60%速度
                double speedMultiplier = 0.60 * enchantmentLevel;
                
                // 移除旧的速度修饰符（如果存在）
                entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                    .removeModifier(SPEED_MODIFIER_UUID);
                
                // 应用新的速度加成
                entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                    .addPermanentModifier(
                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            SPEED_MODIFIER_UUID, 
                            "Horse boots speed boost", 
                            speedMultiplier, 
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
                        )
                    );
            } else {
                // 移除速度加成
                entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                    .removeModifier(SPEED_MODIFIER_UUID);
            }
        } else if (!entity.level().isClientSide) {
            // 如果没有被玩家骑乘，移除速度加成
            entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                .removeModifier(SPEED_MODIFIER_UUID);
        }
    }
    
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 检查实体是否被玩家骑乘
        Entity passenger = entity.getControllingPassenger();
        if (passenger instanceof Player) {
            Player player = (Player) passenger;
            
            // 检查玩家是否拥有策马靴附魔
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            int enchantmentLevel = boots.getEnchantmentLevel(ModEnchantments.HORSE_BOOTS.get());
            
            // 如果有附魔，则完全免疫摔落伤害
            if (enchantmentLevel > 0) {
                event.setCanceled(true);
            }
        }
    }
}