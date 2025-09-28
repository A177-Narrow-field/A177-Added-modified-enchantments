package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.utils.CuriosHelper;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class SwiftStrikeEnchantment extends Enchantment {
    // 攻击速度增加的UUID
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("F1A2B3C4-D5E6-7890-ABCD-EF1234567890");

    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_SWIFT_STRIKE_CACHE = new WeakHashMap<>();
    
    // 定义可以应用于武器和工具的附魔类别
    private static final EnchantmentCategory WEAPON_AND_TOOL = EnchantmentCategory.create("WEAPON_AND_TOOL", 
        (item) -> EnchantmentCategory.WEAPON.canEnchant(item) || EnchantmentCategory.DIGGER.canEnchant(item));
        
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("swift_strike");
    }

    public SwiftStrikeEnchantment() {
        super(Rarity.VERY_RARE, WEAPON_AND_TOOL, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 10;
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
        // 可以附在武器和工具上
        return WEAPON_AND_TOOL.canEnchant(stack.getItem());
    }

    @Override
    public boolean isTradeable() {
        // // 不可通过交易获得
        // return false;
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTradeable.get() : false;
    }

    @Override
    public boolean isDiscoverable() {
        // return true;// 可在附魔台发现
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : true;
    }// 可在附魔台发现

    @Override
    public boolean isTreasureOnly() {
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是武器或工具时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        // 当玩家挖掘方块时，根据附魔等级增加挖掘速度
        Player player = event.getEntity();
        int totalLevel = calculateTotalEnchantmentLevel(player);
        
        if (totalLevel > 0) {
            // 每级增加50%挖掘速度
            event.setNewSpeed(event.getOriginalSpeed() * (1.0f + totalLevel * 0.5f));
        }
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // 当玩家破坏方块后，消耗工具耐久度
        Player player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        
        // 检查工具是否附有SwiftStrike附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.SWIFT_STRIKE.get()) > 0) {
            int level = tool.getEnchantmentLevel(ModEnchantments.SWIFT_STRIKE.get());
            // 消耗耐久度（每次使用消耗1%耐久度，最少1点）
            consumeToolDurability(tool, player, level);
        }
    }
    
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        // 当玩家攻击实体后，消耗工具耐久度
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        
        // 检查工具是否附有SwiftStrike附魔
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.SWIFT_STRIKE.get()) > 0) {
            int level = tool.getEnchantmentLevel(ModEnchantments.SWIFT_STRIKE.get());
            // 消耗耐久度（每次使用消耗1%耐久度，最少1点）
            consumeToolDurability(tool, player, level);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // 当任何装备变更时，立即清除缓存并更新属性
        PLAYER_SWIFT_STRIKE_CACHE.remove(player);
        int currentLevel = calculateTotalEnchantmentLevel(player);
        updatePlayerAttackSpeedModifier(player, currentLevel);
        PLAYER_SWIFT_STRIKE_CACHE.put(player, currentLevel);
    }
    
    /**
     * 消耗工具耐久度
     * @param tool 工具物品
     * @param player 玩家实体
     * @param level SwiftStrike附魔等级
     */
    private static void consumeToolDurability(ItemStack tool, Player player, int level) {
        // 计算需要消耗的耐久度（1%耐久度，最少1点）
        int maxDamage = tool.getMaxDamage();
        if (maxDamage > 0) {
            // 计算消耗值：1%的耐久度，最少为1点
            int consumeAmount = Math.max(1, maxDamage / 100);
            
            // 创造模式玩家不消耗耐久度
            if (!player.getAbilities().instabuild) {
                tool.hurtAndBreak(consumeAmount, player, (entity) -> {
                    entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                });
            }
        }
    }
    
    /**
     * 计算玩家主手工具和Curios饰品上的迅捷打击附魔等级总和
     * @param player 玩家实体
     * @return 附魔等级总和
     */
    private static int calculateTotalEnchantmentLevel(Player player) {
        int totalLevel = 0;
        
        // 检查主手工具
        ItemStack tool = player.getMainHandItem();
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.SWIFT_STRIKE.get()) > 0) {
            totalLevel += tool.getEnchantmentLevel(ModEnchantments.SWIFT_STRIKE.get());
        }
        
        // 检查Curios饰品
        if (CuriosHelper.CURIOS_LOADED) {
            // 使用数组包装totalLevel以便在lambda中修改
            final int[] levelWrapper = {totalLevel};
            CuriosHelper.hasCurioItem(player, stack -> {
                if (!stack.isEmpty() && stack.isEnchanted() && stack.getEnchantmentLevel(ModEnchantments.SWIFT_STRIKE.get()) > 0) {
                    levelWrapper[0] += stack.getEnchantmentLevel(ModEnchantments.SWIFT_STRIKE.get());
                    return true; // 继续检查其他物品
                }
                return false;
            });
            totalLevel = levelWrapper[0];
        }
        
        return totalLevel;
    }
    
    private static void updatePlayerAttackSpeedModifier(Player player, int level) {
        // 移除旧的修饰符
        if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        }
        
        // 如果等级大于0，添加新的修饰符
        if (level > 0) {
            double speedBonus;
            if (level >= 10) {
                // 10级时提供1200%攻击速度加成
                speedBonus = 12.0;
            } else {
                // 每级增加60%攻击速度（1-9级）
                speedBonus = level * 0.8;
            }
            
            if (player.getAttribute(Attributes.ATTACK_SPEED) != null) {
                player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                    new AttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Swift strike attack speed", speedBonus, AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
    }
}