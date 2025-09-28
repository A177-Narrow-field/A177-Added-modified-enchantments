package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
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

@Mod.EventBusSubscriber
public class HeartbeatRhythmEnchantment extends Enchantment {

    // 存储玩家下一次可以回复生命值的时间
    private static final Map<Player, Long> PLAYER_NEXT_HEAL_TIME = new HashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("heartbeat_rhythm");
    }
    
    // 回复冷却时间（1秒 = 20 ticks）
    private static final int HEAL_COOLDOWN = 20;

    public HeartbeatRhythmEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 10;
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
        
        // 每秒检查一次（20 ticks）
        if (event.player.tickCount % 20 != 0) {
            return;
        }
        
        Player player = event.player;
        
        // 检查玩家是否装备了胸甲
        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestItem.isEmpty()) {
            return;
        }
        
        // 检查胸甲是否有心动律动附魔
        int level = chestItem.getEnchantmentLevel(ModEnchantments.HEARTBEAT_RHYTHM.get());
        if (level <= 0) {
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
        
        // 如果有正在播放的唱片机，则回复生命值
        if (hasPlayingJukebox) {
            // 检查是否在冷却时间内
            Long nextHealTime = PLAYER_NEXT_HEAL_TIME.get(player);
            long currentTime = player.level().getGameTime();
            
            if (nextHealTime == null || currentTime >= nextHealTime) {
                // 每级回复1%的生命值
                float healAmount = player.getMaxHealth() * (0.01f * level);
                // 确保不会超过最大生命值
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(healAmount);
                    // 设置下一次回复时间
                    PLAYER_NEXT_HEAL_TIME.put(player, currentTime + HEAL_COOLDOWN);
                }
            }
        }
    }
}