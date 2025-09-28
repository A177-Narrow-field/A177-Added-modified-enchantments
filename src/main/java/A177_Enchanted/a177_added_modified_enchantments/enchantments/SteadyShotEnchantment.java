package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SteadyShotEnchantment extends Enchantment {
    // 用于跟踪玩家是否正在使用弓箭
    private static final Map<UUID, Boolean> PLAYER_USING_BOW = new HashMap<>();
    
    // 用于跟踪玩家的移动速度修饰符
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("22a065be-91a4-4fb2-b38d-765c80f0a700");
    
    // 用于跟踪玩家的击退抗性修饰符
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("32a065be-91a4-4fb2-b38d-765c80f0a701");

    public SteadyShotEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("steady_shot");
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());}//确保在附魔台中可以正确应用

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在弓上
        return stack.getItem() instanceof BowItem;
    }

    @SubscribeEvent
    public static void onPlayerStartUsingItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack stack = event.getItem();
            
            // 检查物品是否为弓且有磐射附魔
            if (stack.getItem() instanceof BowItem) {
                int level = stack.getEnchantmentLevel(ModEnchantments.STEADY_SHOT.get());
                if (level > 0) {
                    PLAYER_USING_BOW.put(player.getUUID(), true);
                    updatePlayerAttributes(player, level);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerStopUsingItem(LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity() instanceof Player player) {
            // 移除属性加成
            PLAYER_USING_BOW.remove(player.getUUID());
            updatePlayerAttributes(player, 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        PLAYER_USING_BOW.remove(player.getUUID());
        updatePlayerAttributes(player, 0);
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否正在使用物品
        if (player.isUsingItem()) {
            ItemStack usingItem = player.getUseItem();
            // 检查当前使用的物品是否为弓且有磐射附魔
            if ((usingItem.getItem() instanceof BowItem) &&
                usingItem.getEnchantmentLevel(ModEnchantments.STEADY_SHOT.get()) > 0) {
                // 确保属性加成处于激活状态
                if (!PLAYER_USING_BOW.getOrDefault(player.getUUID(), false)) {
                    PLAYER_USING_BOW.put(player.getUUID(), true);
                    updatePlayerAttributes(player, usingItem.getEnchantmentLevel(ModEnchantments.STEADY_SHOT.get()));
                }
            } else {
                // 如果当前使用的物品不是带磐射附魔的弓，则移除属性加成
                if (PLAYER_USING_BOW.getOrDefault(player.getUUID(), false)) {
                    PLAYER_USING_BOW.remove(player.getUUID());
                    updatePlayerAttributes(player, 0);
                }
            }
        } else {
            // 如果玩家没有在使用物品，则移除属性加成
            if (PLAYER_USING_BOW.getOrDefault(player.getUUID(), false)) {
                PLAYER_USING_BOW.remove(player.getUUID());
                updatePlayerAttributes(player, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否为箭的事件
        if (event.getEntity() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有磐射附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.STEADY_SHOT.get());
                if (level > 0) {
                    // 每级增加1.5伤害
                    arrow.setBaseDamage(arrow.getBaseDamage() + (level * 1.5));
                }
            }
        }
    }

    /**
     * 更新玩家属性（移动速度和击退抗性）
     * @param player 玩家
     * @param level 附魔等级，0表示移除效果
     */
    private static void updatePlayerAttributes(Player player, int level) {
        if (player.level().isClientSide()) {
            return;
        }

        // 更新移动速度
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            // 移除旧的修饰符
            if (speedAttribute.getModifier(SPEED_MODIFIER_UUID) != null) {
                speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
            }

            // 如果等级大于0，添加新的速度修饰符
            if (level > 0) {
                // 每级减少30%移动速度
                var speedModifier = new AttributeModifier(
                        SPEED_MODIFIER_UUID,
                        "SteadyShot movement speed reduction",
                        level * -0.3,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                speedAttribute.addTransientModifier(speedModifier);
            }
        }

        // 更新击退抗性
        var knockbackResistanceAttribute = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackResistanceAttribute != null) {
            // 移除旧的修饰符
            if (knockbackResistanceAttribute.getModifier(KNOCKBACK_RESISTANCE_UUID) != null) {
                knockbackResistanceAttribute.removeModifier(KNOCKBACK_RESISTANCE_UUID);
            }

            // 如果等级大于0，添加新的击退抗性修饰符
            if (level > 0) {
                // 免疫击退（添加1.0的击退抗性）
                var knockbackResistanceModifier = new AttributeModifier(
                        KNOCKBACK_RESISTANCE_UUID,
                        "SteadyShot knockback resistance",
                        1.0,
                        AttributeModifier.Operation.ADDITION
                );
                knockbackResistanceAttribute.addTransientModifier(knockbackResistanceModifier);
            }
        }
    }
}