package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class SharpEdgeEnchantment extends Enchantment {
    // 攻击速度修饰符UUID
    public static final UUID ATTACK_SPEED_UUID = UUID.fromString("f1e2d3c4-b5a6-47a8-b9c0-d1e2f3a4b5c6");

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("sharp_edge_weapon");
    }
    
    public SharpEdgeEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }//是宝藏附魔

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 可交易

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//确保在附魔台中可以正确应用

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean checkCompatibility(Enchantment p_44615_) {
        return super.checkCompatibility(p_44615_) && p_44615_ != ModEnchantments.BLUNT_CRUSHING.get() && p_44615_ != ModEnchantments.STAGGERING_BLOW.get();//确保不与其他锋刃附魔冲突
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手装备是否有锋刃附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHARP_EDGE.get(), mainHandItem);

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 增加伤害（每级增加20%伤害）
                float additionalDamage = event.getAmount() * (0.2f * level);
                event.setAmount(event.getAmount() + additionalDamage);
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack weapon = player.getMainHandItem();
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHARP_EDGE.get(), weapon);
        
        // 如果武器有锋刃附魔，则增加耐久度消耗
        if (level > 0) {
            // 每级增加3点耐久度消耗
            weapon.hurtAndBreak(level * 3, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
    }
    
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHARP_EDGE.get(), tool);
        
        // 如果工具有锋刃附魔，则在挖掘方块时增加耐久度消耗
        if (level > 0) {
            // 每级增加3点耐久度消耗
            tool.hurtAndBreak(level * 3, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.MAINHAND) {
            return;
        }

        // 移除旧的攻击速度修饰符
        removeAttackSpeedModifier(player);

        // 如果新装备有锋刃附魔，则应用攻击速度增益
        ItemStack newItem = event.getTo();
        int level = newItem.getEnchantmentLevel(ModEnchantments.SHARP_EDGE.get());
        if (level > 0) {
            applyAttackSpeedModifier(player, level);
        }
    }

    /**
     * 应用攻击速度增益修饰符
     * 每级增加40%攻击速度
     *
     * @param player 玩家实体
     * @param level  附魔等级
     */
    private static void applyAttackSpeedModifier(Player player, int level) {
        // 获取玩家攻击速度属性实例
        var attackSpeedAttribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttribute == null) return;

        // 移除旧的修饰符
        if (attackSpeedAttribute.getModifier(ATTACK_SPEED_UUID) != null) {
            attackSpeedAttribute.removeModifier(ATTACK_SPEED_UUID);
        }

        // 添加新的修饰符（每级增加40%攻击速度）
        double speedIncrease = level * 0.4;
        AttributeModifier modifier = new AttributeModifier(ATTACK_SPEED_UUID, "Sharp edge speed increase", speedIncrease, AttributeModifier.Operation.MULTIPLY_TOTAL);
        attackSpeedAttribute.addTransientModifier(modifier);
    }

    /**
     * 移除攻击速度修饰符
     *
     * @param player 玩家实体
     */
    private static void removeAttackSpeedModifier(Player player) {
        var attackSpeedAttribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttribute == null) return;

        // 移除修饰符（如果存在）
        if (attackSpeedAttribute.getModifier(ATTACK_SPEED_UUID) != null) {
            attackSpeedAttribute.removeModifier(ATTACK_SPEED_UUID);
        }
    }
}