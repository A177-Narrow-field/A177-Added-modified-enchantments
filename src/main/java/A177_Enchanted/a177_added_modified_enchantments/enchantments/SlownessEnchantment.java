package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class SlownessEnchantment extends Enchantment {
    // 攻击速度减少的UUID
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("A1B2C3D4-E5F6-7890-ABCD-EF1234567891");

    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_SLOWNESS_CACHE = new WeakHashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("slowness");
    }

    public SlownessEnchantment() {
        // 使用EnchantmentCategory.GEAR来适用于更多类型的物品
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 4;
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
        // 只能附在工具和武器上
        return EnchantmentCategory.DIGGER.canEnchant(stack.getItem()) || EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是工具或武器时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }
    
    @Override
    public boolean isDiscoverable() {
        // // 可在附魔书中发现
        // return true;
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : true;
    }

    @Override
    public boolean isTradeable() {
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTradeable.get() : false;
    }

    @Override
    public boolean isTreasureOnly() {
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : false;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        // 当玩家挖掘方块时，根据附魔等级减少挖掘速度
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();

        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.SLOWNESS.get()) > 0) {
            int level = tool.getEnchantmentLevel(ModEnchantments.SLOWNESS.get());
            // 每级减少20%挖掘速度
            event.setNewSpeed(event.getOriginalSpeed() * (1.0f - level * 0.2f));
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            return;
        }

        // 当主手装备变更时，立即清除缓存并更新属性
        PLAYER_SLOWNESS_CACHE.remove(player);
        updatePlayerAttackSpeedModifier(player, 0); // 先清除修饰符

        ItemStack tool = player.getMainHandItem();
        int currentLevel = 0;
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.SLOWNESS.get()) > 0) {
            currentLevel = tool.getEnchantmentLevel(ModEnchantments.SLOWNESS.get());
        }

        updatePlayerAttackSpeedModifier(player, currentLevel);
        PLAYER_SLOWNESS_CACHE.put(player, currentLevel);
    }

    private static void updatePlayerAttackSpeedModifier(Player player, int level) {
        // 移除旧的修饰符
        AttributeInstance attackSpeedAttribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttribute != null) {
            attackSpeedAttribute.removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        }

        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            // 每级减少20%攻击速度
            if (attackSpeedAttribute != null) {
                attackSpeedAttribute.addTransientModifier(
                        new AttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Slowness attack speed", -level * 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
    }
}