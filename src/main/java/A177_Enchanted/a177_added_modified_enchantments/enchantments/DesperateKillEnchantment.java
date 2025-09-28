package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class DesperateKillEnchantment extends Enchantment {
    // 伤害增加的UUID
    public static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("A1B2C3D4-E5F6-A7B8-C9D0-1234567890AB");

    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_DESPERATE_KILL_CACHE = new WeakHashMap<>();
    
    // 更新间隔（游戏刻）
    private static final int UPDATE_INTERVAL = 20; // 每秒更新一次 (20 ticks = 1 second)
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Integer> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();

    public DesperateKillEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack) && isDiscoverable();
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("desperate_kill");
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide) {
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

        // 检查玩家是否手持附魔武器
        ItemStack weapon = player.getItemBySlot(EquipmentSlot.MAINHAND);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESPERATE_KILL.get(), weapon);

        // 更新玩家属性
        updatePlayerAttributes(player, enchantmentLevel);
        PLAYER_DESPERATE_KILL_CACHE.put(player, enchantmentLevel);
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.MAINHAND) {
            return;
        }

        // 当装备变更时，立即清除缓存并更新属性
        PLAYER_DESPERATE_KILL_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
        updatePlayerAttributes(player, 0); // 先清除修饰符
    }

    private static void updatePlayerAttributes(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null) {
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 检查玩家血量是否低于20%
            float healthPercentage = player.getHealth() / player.getMaxHealth();
            
            // 如果血量低于20%，增加伤害
            if (healthPercentage < 0.2) {
                // 每级增加100%伤害
                double damageBonus = level * 1.0;

                // 添加伤害修饰符
                if (player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null) {
                    player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).addTransientModifier(
                            new AttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Desperate kill damage", damageBonus, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    );
                }
            }
        }
    }
}