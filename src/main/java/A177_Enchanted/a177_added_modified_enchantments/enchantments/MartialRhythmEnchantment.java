package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class MartialRhythmEnchantment extends Enchantment {
    // UUID用于属性修饰符，确保唯一性
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffffffff");
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("ccccdddd-eeee-ffff-aaaa-bbbbccccdddd");
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("martial_rhythm");
    }

    // 存储玩家上一次检测到唱片机的时间
    private static final Map<Player, Long> PLAYER_LAST_DETECTION = new HashMap<>();
    
    // 效果持续时间（0.5秒 = 10 ticks）
    private static final int EFFECT_DURATION = 10;
    
    // 存储玩家当前是否应该应用效果的状态
    private static final Map<Player, Boolean> PLAYER_SHOULD_APPLY_EFFECT = new HashMap<>();

    public MartialRhythmEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        return EnchantmentCategory.ARMOR_CHEST.canEnchant(stack.getItem());
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

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
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
        
        // 只在服务端执行逻辑
        if (event.player.level().isClientSide) {
            return;
        }
        
        Player player = event.player;
        
        // 每2秒检查一次（40 ticks）
        if (player.tickCount % 40 != 0) {
            // 每tick应用当前效果状态，避免视觉闪烁
            applyCurrentEffectState(player);
            return;
        }
        
        // 检查玩家是否装备了胸甲
        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestItem.isEmpty()) {
            // 如果没有装备胸甲，确保移除效果
            PLAYER_SHOULD_APPLY_EFFECT.put(player, false);
            applyCurrentEffectState(player);
            return;
        }
        
        // 检查胸甲是否有武道律动附魔
        int level = chestItem.getEnchantmentLevel(ModEnchantments.MARTIAL_RHYTHM.get());
        if (level <= 0) {
            // 如果没有附魔，确保移除效果
            PLAYER_SHOULD_APPLY_EFFECT.put(player, false);
            applyCurrentEffectState(player);
            return;
        }
        
        // 检查24格范围内是否有正在播放的唱片机
        boolean hasPlayingJukebox = false;
        BlockPos playerPos = player.blockPosition();
        
        // 检查玩家周围24格范围内的方块
        for (int x = -24; x <= 24; x++) {
            for (int y = -24; y <= 24; y++) {
                for (int z = -24; z <= 24; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    // 检查该位置是否为唱片机
                    if (player.level().getBlockState(checkPos).is(Blocks.JUKEBOX)) {
                        BlockEntity blockEntity = player.level().getBlockEntity(checkPos);
                        // 检查是否为唱片机方块实体且正在播放
                        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
                            // 检查唱片机是否有唱片且正在播放
                            if (!jukebox.getItem(0).isEmpty() && jukebox.isRecordPlaying()) {
                                hasPlayingJukebox = true;
                                break;
                            }
                        }
                    }
                }
                if (hasPlayingJukebox) break;
            }
            if (hasPlayingJukebox) break;
        }
        
        // 更新玩家上一次检测到唱片机的时间
        if (hasPlayingJukebox) {
            PLAYER_LAST_DETECTION.put(player, player.level().getGameTime());
        }
        
        // 检查是否仍在效果持续时间内
        Long lastDetectionTime = PLAYER_LAST_DETECTION.get(player);
        boolean shouldApplyEffect = lastDetectionTime != null && 
            (player.level().getGameTime() - lastDetectionTime) < EFFECT_DURATION;
        
        // 更新玩家效果状态
        PLAYER_SHOULD_APPLY_EFFECT.put(player, shouldApplyEffect);
        
        // 应用当前效果状态
        applyCurrentEffectState(player);
    }
    
    /**
     * 应用玩家当前的效果状态
     * @param player 玩家实体
     */
    private static void applyCurrentEffectState(Player player) {
        // 获取玩家的属性实例
        var movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var attackDamageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        
        // 移除所有武道律动相关的修饰符（每次tick都清理，避免残留）
        if (movementSpeedAttribute != null) {
            if (movementSpeedAttribute.getModifier(MOVEMENT_SPEED_UUID) != null) {
                movementSpeedAttribute.removeModifier(MOVEMENT_SPEED_UUID);
            }
        }
        
        if (attackDamageAttribute != null) {
            if (attackDamageAttribute.getModifier(ATTACK_DAMAGE_UUID) != null) {
                attackDamageAttribute.removeModifier(ATTACK_DAMAGE_UUID);
            }
        }
        
        // 检查是否应该应用效果
        Boolean shouldApplyEffect = PLAYER_SHOULD_APPLY_EFFECT.get(player);
        if (shouldApplyEffect != null && shouldApplyEffect) {
            // 检查玩家是否装备了胸甲
            ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!chestItem.isEmpty()) {
                // 检查胸甲是否有武道律动附魔
                int level = chestItem.getEnchantmentLevel(ModEnchantments.MARTIAL_RHYTHM.get());
                if (level > 0) {
                    // 每级增加10%的移动速度和伤害
                    double bonus = 0.1 * level;
                    
                    // 添加移动速度加成
                    if (movementSpeedAttribute != null) {
                        movementSpeedAttribute.addTransientModifier(
                            new AttributeModifier(MOVEMENT_SPEED_UUID, "Martial rhythm speed", bonus, AttributeModifier.Operation.MULTIPLY_BASE));
                    }
                    
                    // 添加攻击伤害加成
                    if (attackDamageAttribute != null) {
                        attackDamageAttribute.addTransientModifier(
                            new AttributeModifier(ATTACK_DAMAGE_UUID, "Martial rhythm damage", bonus, AttributeModifier.Operation.MULTIPLY_BASE));
                    }
                }
            }
        }
    }
}