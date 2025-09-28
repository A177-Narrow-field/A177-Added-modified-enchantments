package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
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
public class RangerEnchantment extends Enchantment {
    // 用于跟踪玩家是否正在使用弓箭
    private static final Map<UUID, Boolean> PLAYER_USING_BOW = new HashMap<>();
    
    // 用于跟踪玩家的移动速度修饰符
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("12a065be-91a4-4fb2-b38d-765c80f0a6ff");

    public RangerEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("ranger");
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
        // 参考StaggeringBlowEnchantment的实现方式，确保弩也能在附魔台附魔
        return EnchantmentCategory.BOW.canEnchant(stack.getItem()) || 
               EnchantmentCategory.CROSSBOW.canEnchant(stack.getItem());
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在弓和弩上
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    @SubscribeEvent
    public static void onPlayerStartUsingItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack stack = event.getItem();
            
            // 检查物品是否为弓或弩且有游侠附魔
            if ((stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem)) {
                int level = stack.getEnchantmentLevel(ModEnchantments.RANGER.get());
                if (level > 0) {
                    PLAYER_USING_BOW.put(player.getUUID(), true);
                    updatePlayerMovementSpeed(player, level);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerStopUsingItem(LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity() instanceof Player player) {
            // 移除速度加成
            PLAYER_USING_BOW.remove(player.getUUID());
            updatePlayerMovementSpeed(player, 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        PLAYER_USING_BOW.remove(player.getUUID());
        updatePlayerMovementSpeed(player, 0);
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

        // 检查玩家主手物品
        ItemStack mainHandItem = player.getMainHandItem();
        // 检查物品是否为弓或弩且有游侠附魔
        if ((mainHandItem.getItem() instanceof BowItem || mainHandItem.getItem() instanceof CrossbowItem) &&
            mainHandItem.getEnchantmentLevel(ModEnchantments.RANGER.get()) > 0) {
            // 如果是弩，不管是否蓄力都给予速度加成
            if (mainHandItem.getItem() instanceof CrossbowItem) {
                int level = mainHandItem.getEnchantmentLevel(ModEnchantments.RANGER.get());
                // 确保速度加成处于激活状态
                if (!PLAYER_USING_BOW.getOrDefault(player.getUUID(), false)) {
                    PLAYER_USING_BOW.put(player.getUUID(), true);
                    updatePlayerMovementSpeed(player, level);
                }
            }
            // 如果是弓，则保持原来的行为
            else if (player.isUsingItem()) {
                ItemStack usingItem = player.getUseItem();
                // 检查当前使用的物品是否为弓且有游侠附魔
                if (usingItem.getItem() instanceof BowItem &&
                    usingItem.getEnchantmentLevel(ModEnchantments.RANGER.get()) > 0) {
                    // 确保速度加成处于激活状态
                    if (!PLAYER_USING_BOW.getOrDefault(player.getUUID(), false)) {
                        PLAYER_USING_BOW.put(player.getUUID(), true);
                        updatePlayerMovementSpeed(player, usingItem.getEnchantmentLevel(ModEnchantments.RANGER.get()));
                    }
                } else {
                    // 如果当前使用的物品不是带游侠附魔的弓，则移除速度加成
                    if (PLAYER_USING_BOW.getOrDefault(player.getUUID(), false)) {
                        PLAYER_USING_BOW.remove(player.getUUID());
                        updatePlayerMovementSpeed(player, 0);
                    }
                }
            } else {
                // 如果玩家没有在使用物品，则移除速度加成
                if (PLAYER_USING_BOW.getOrDefault(player.getUUID(), false)) {
                    PLAYER_USING_BOW.remove(player.getUUID());
                    updatePlayerMovementSpeed(player, 0);
                }
            }
        } else {
            // 如果玩家没有持有带游侠附魔的弓或弩，则移除速度加成
            if (PLAYER_USING_BOW.getOrDefault(player.getUUID(), false)) {
                PLAYER_USING_BOW.remove(player.getUUID());
                updatePlayerMovementSpeed(player, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否为箭的事件
        if (event.getEntity() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓或弩）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有游侠附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.RANGER.get());
                if (level > 0) {
                    // 每级增加40%箭矢速度
                    double velocityMultiplier = 1.0 + (level * 0.4);
                    arrow.setDeltaMovement(arrow.getDeltaMovement().scale(velocityMultiplier));
                }
            }
        }
    }

    /**
     * 更新玩家移动速度
     * @param player 玩家
     * @param level 附魔等级，0表示移除效果
     */
    private static void updatePlayerMovementSpeed(Player player, int level) {
        if (player.level().isClientSide()) {
            return;
        }

        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) {
            return;
        }

        // 移除旧的修饰符
        if (speedAttribute.getModifier(SPEED_MODIFIER_UUID) != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的速度修饰符
        if (level > 0) {
            // 每级增加40%移动速度
            var speedModifier = new AttributeModifier(
                    SPEED_MODIFIER_UUID,
                    "Ranger movement speed boost",
                    level * 0.4,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            speedAttribute.addTransientModifier(speedModifier);
        }
    }
}