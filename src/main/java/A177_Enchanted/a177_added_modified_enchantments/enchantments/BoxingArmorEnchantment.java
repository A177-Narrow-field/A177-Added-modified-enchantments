package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.UUID;

@Mod.EventBusSubscriber
public class BoxingArmorEnchantment extends Enchantment {
    // 攻击伤害增加的UUID
    public static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("B2C3D4E5-F6A7-8901-BCDE-F01234567899");
    
    // 更新间隔（游戏刻）
    private static final int UPDATE_INTERVAL = 10; // 每0.5秒更新一次 (10 ticks = 0.5 second)
    // 记录玩家的下次检查时间
    private static final java.util.Map<Player, Integer> PLAYER_NEXT_CHECK_TIME = new java.util.WeakHashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("boxing_armor");
    }

    public BoxingArmorEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 25;
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
        // 只有当配置允许且物品是胸甲时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        // 检查是否到了更新时间
        int currentTick = player.tickCount;
        Integer nextCheckTick = PLAYER_NEXT_CHECK_TIME.get(player);
        if (nextCheckTick != null && currentTick < nextCheckTick) {
            return;
        }
        
        // 更新下次检查时间
        PLAYER_NEXT_CHECK_TIME.put(player, currentTick + UPDATE_INTERVAL);

        // 检查玩家是否空手
        ItemStack mainHandItem = player.getMainHandItem();
        boolean isFistFighting = mainHandItem.isEmpty();
        
        // 获取胸甲上的拳击甲胄附魔等级
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BOXING_ARMOR.get(), chestplate);
        
        // 只有在玩家空手且胸甲有附魔时才应用效果
        if (isFistFighting && enchantmentLevel > 0) {
            // 更新玩家属性
            updatePlayerAttributes(player, enchantmentLevel);
        } else {
            // 移除玩家属性修饰符
            updatePlayerAttributes(player, 0);
        }
    }

    private static void updatePlayerAttributes(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 每级增加50%伤害
            double damageBonus = level * 0.5;
            
            AttributeModifier damageModifier = new AttributeModifier(
                ATTACK_DAMAGE_MODIFIER_UUID,
                "Boxing armor damage bonus",
                damageBonus,
                AttributeModifier.Operation.MULTIPLY_BASE
            );
            
            // 添加伤害修饰符
            if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                player.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(damageModifier);
            }
        }
    }
}