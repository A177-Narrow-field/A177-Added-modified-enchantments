package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class BurningFuryEnchantment extends Enchantment {
    // 属性修饰符UUID（确保唯一性）
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-4567-890123456789");
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("burning_fury");
    }
    
    public BurningFuryEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 根据配置决定是否可在附魔台获得
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Player player = event.player;
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BURNING_FURY.get(), chestplate);
        
        if (level > 0) {
            // 检查玩家是否正在燃烧
            if (player.getRemainingFireTicks() > 0) {
                // 增加攻击力，每级增加20%基础攻击力
                double attackBonus = 0.2 * level;
                addAttributeModifier(player, attackBonus);
            } else {
                // 移除攻击力加成
                removeAttributeModifier(player);
            }
        } else {
            // 移除攻击力加成
            removeAttributeModifier(player);
        }
    }

    private static void addAttributeModifier(Player player, double attackBonus) {
        // 添加攻击力修饰符
        if (player.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_UUID) == null) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(
                new AttributeModifier(ATTACK_DAMAGE_UUID, "Burning Fury", attackBonus, AttributeModifier.Operation.MULTIPLY_BASE)
            );
        }
    }

    private static void removeAttributeModifier(Player player) {
        if (player.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_UUID) != null) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_UUID);
        }
    }
}