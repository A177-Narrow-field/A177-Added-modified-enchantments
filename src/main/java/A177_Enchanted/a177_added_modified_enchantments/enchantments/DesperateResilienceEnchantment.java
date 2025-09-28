package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
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
public class DesperateResilienceEnchantment extends Enchantment {
    // 免伤增加的UUID
    public static final UUID DAMAGE_REDUCTION_MODIFIER_UUID = UUID.fromString("B1C2D3E4-F5A6-B7C8-D9E0-1234567890CD");
    // 击退抗性增加的UUID
    public static final UUID KNOCKBACK_RESISTANCE_MODIFIER_UUID = UUID.fromString("C1D2E3F4-A5B6-C7D8-E9F0-1234567890DC");
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("desperate_resilience");
    }

    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_DESPERATE_RESILIENCE_CACHE = new WeakHashMap<>();
    
    // 更新间隔（游戏刻）
    private static final int UPDATE_INTERVAL = 20; // 每秒更新一次 (20 ticks = 1 second)
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Integer> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();

    public DesperateResilienceEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 5;
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
        return stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
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
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }
    
    // 与炎壳和胃袋附魔冲突
    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) 
                && other != ModEnchantments.FLAME_SHELL.get()
                && other != ModEnchantments.STOMACH_POUCH.get();
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

        // 检查玩家是否穿着附魔胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESPERATE_RESILIENCE.get(), chestplate);

        // 更新玩家属性
        updatePlayerAttributes(player, enchantmentLevel);
        PLAYER_DESPERATE_RESILIENCE_CACHE.put(player, enchantmentLevel);
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.CHEST) {
            return;
        }

        // 当装备变更时，立即清除缓存并更新属性
        PLAYER_DESPERATE_RESILIENCE_CACHE.remove(player);
        PLAYER_NEXT_CHECK_TIME.remove(player);
        updatePlayerAttributes(player, 0); // 先清除修饰符
    }

    private static void updatePlayerAttributes(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE) != null) {
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE_MODIFIER_UUID);
        }
        
        if (player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) != null) {
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).removeModifier(DAMAGE_REDUCTION_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 检查玩家血量是否低于20%
            float healthPercentage = player.getHealth() / player.getMaxHealth();
            
            // 如果血量低于20%，增加免伤和击退抗性
            if (healthPercentage < 0.2) {
                // 每级增加15%免伤（使用ARMOR属性模拟免伤）
                double damageReductionBonus = level * 0.15;
                
                // 增加10点击退抗性
                double knockbackResistanceBonus = 10.0;

                // 添加免伤修饰符
                if (player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) != null) {
                    player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).addTransientModifier(
                            new AttributeModifier(DAMAGE_REDUCTION_MODIFIER_UUID, "Desperate resilience damage reduction", damageReductionBonus, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    );
                }
                
                // 添加击退抗性修饰符
                if (player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE) != null) {
                    player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(
                            new AttributeModifier(KNOCKBACK_RESISTANCE_MODIFIER_UUID, "Desperate resilience knockback resistance", knockbackResistanceBonus, AttributeModifier.Operation.ADDITION)
                    );
                }
            }
        }
    }
}