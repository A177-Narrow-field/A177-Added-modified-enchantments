package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class DualWieldEnchantment extends Enchantment {
    // UUID用于属性修饰符，确保唯一性
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("ffff0000-1111-2222-3333-444444444444");
    private static final UUID BONUS_SPEED_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("dual_wield");
    }

    public DualWieldEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
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
        return this.canEnchant(stack) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        // 每10个tick检查一次，减少性能消耗
        if (event.player.tickCount % 10 != 0) {
            return;
        }
        
        Player player = event.player;
        
        // 获取玩家双手的物品
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        
        // 获取玩家的属性实例
        var attackSpeedAttribute = player.getAttribute(Attributes.ATTACK_SPEED);
        var attackDamageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        
        // 移除所有双持相关的修饰符（每次tick都清理，避免残留）
        if (attackSpeedAttribute != null) {
            if (attackSpeedAttribute.getModifier(ATTACK_SPEED_UUID) != null) {
                attackSpeedAttribute.removeModifier(ATTACK_SPEED_UUID);
            }
            if (attackSpeedAttribute.getModifier(BONUS_SPEED_UUID) != null) {
                attackSpeedAttribute.removeModifier(BONUS_SPEED_UUID);
            }
        }
        
        if (attackDamageAttribute != null) {
            if (attackDamageAttribute.getModifier(ATTACK_DAMAGE_UUID) != null) {
                attackDamageAttribute.removeModifier(ATTACK_DAMAGE_UUID);
            }
        }
        
        // 检查是否双手持有相同类型的武器且主手有无双附魔
        if (!mainHandItem.isEmpty() && 
            !offHandItem.isEmpty() && 
            mainHandItem.getItem() == offHandItem.getItem() &&
            mainHandItem.getEnchantmentLevel(ModEnchantments.DUAL_WIELD.get()) > 0) {
            
            // 添加基础攻击速度加成（100%）
            if (attackSpeedAttribute != null) {
                attackSpeedAttribute.addTransientModifier(
                    new AttributeModifier(ATTACK_SPEED_UUID, "Dual wield speed", 1.0, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            
            // 添加攻击伤害加成（20%）
            if (attackDamageAttribute != null) {
                attackDamageAttribute.addTransientModifier(
                    new AttributeModifier(ATTACK_DAMAGE_UUID, "Dual wield damage", 0.2, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            
            // 如果双手武器都有无双附魔，额外增加攻击速度（100%）
            if (mainHandItem.getEnchantmentLevel(ModEnchantments.DUAL_WIELD.get()) > 0 && 
                offHandItem.getEnchantmentLevel(ModEnchantments.DUAL_WIELD.get()) > 0) {
                if (attackSpeedAttribute != null) {
                    attackSpeedAttribute.addTransientModifier(
                        new AttributeModifier(BONUS_SPEED_UUID, "Dual wield bonus speed", 1.0, AttributeModifier.Operation.MULTIPLY_BASE));
                }
            }
        }
    }
}