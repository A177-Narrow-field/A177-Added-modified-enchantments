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
import java.util.UUID;

@Mod.EventBusSubscriber
public class NoisySoundEnchantment extends Enchantment {
    // 存储玩家上一次检测到唱片机的时间
    private static final Map<Player, Long> PLAYER_LAST_DETECTION = new HashMap<>();
    
    // 效果持续时间（6秒 = 120 ticks）
    private static final int EFFECT_DURATION = 120;
    
    // 检测间隔（1秒 = 20 ticks）
    private static final int DETECTION_INTERVAL = 20;
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("noise_annoyance");
    }

    public NoisySoundEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 1;
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在头盔上
        return EnchantmentCategory.ARMOR_HEAD.canEnchant(stack.getItem());
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
        return this.category.canEnchant(stack.getItem());
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
        
        // 每1秒检查一次（20 ticks）
        if (player.tickCount % DETECTION_INTERVAL != 0) {
            return;
        }
        
        // 检查玩家是否装备了头盔
        ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);
        if (headItem.isEmpty()) {
            return;
        }
        
        // 检查头盔是否有音躁附魔
        int level = headItem.getEnchantmentLevel(ModEnchantments.NOISY_SOUND.get());
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
        
        // 更新玩家上一次检测到唱片机的时间
        if (hasPlayingJukebox) {
            PLAYER_LAST_DETECTION.put(player, player.level().getGameTime());
        }
        
        // 检查是否仍在效果持续时间内
        Long lastDetectionTime = PLAYER_LAST_DETECTION.get(player);
        boolean shouldApplyEffect = lastDetectionTime != null && 
            (player.level().getGameTime() - lastDetectionTime) < EFFECT_DURATION;
        
        // 应用或移除效果
        if (shouldApplyEffect) {
            // 应用虚弱和挖掘疲劳效果
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, EFFECT_DURATION, 2));
        }
    }
}