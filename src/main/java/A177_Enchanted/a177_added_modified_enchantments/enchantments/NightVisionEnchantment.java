package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class NightVisionEnchantment extends Enchantment {
    // 存储玩家开始蹲下的时间
    private static final Map<UUID, Long> CROUCH_START_TIME = new HashMap<>();
    // 存储玩家是否已经触发了夜视效果
    private static final Map<UUID, Boolean> ACTIVE_NIGHT_VISION = new HashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("night_vision");
    }

    public NightVisionEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
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
    public boolean canEnchant(ItemStack stack) {
        return EnchantmentCategory.ARMOR_HEAD.canEnchant(stack.getItem());
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
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
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int level = helmet.getEnchantmentLevel(ModEnchantments.NIGHT_VISION.get());
            
            if (level > 0) {
                UUID playerUUID = player.getUUID();
                long currentTime = player.level().getGameTime();
                
                // 检查玩家是否在蹲下
                if (player.isCrouching()) {
                    // 如果玩家之前没有记录蹲下时间，则记录当前时间
                    if (!CROUCH_START_TIME.containsKey(playerUUID)) {
                        CROUCH_START_TIME.put(playerUUID, currentTime);
                    } 
                    // 根据附魔等级确定所需蹲下时间：3.5秒基础时间，每级减少1秒(20 ticks)
                    // 只有当效果未激活时才能再次触发
                    else if (currentTime - CROUCH_START_TIME.get(playerUUID) >= (70 - (level * 20L)) &&
                             !ACTIVE_NIGHT_VISION.getOrDefault(playerUUID, false)) {
                        // 检查玩家是否有足够的经验值
                        if (player.experienceLevel > 0 || player.experienceProgress > 0) {
                            // 触发夜视效果
                            applyNightVisionEffect(player, level);
                            // 播放末地传送门框架放置末影之眼的音效
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                                SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                            
                            // 消耗10点经验值（不是等级）
                            player.giveExperiencePoints(-10);
                            
                            // 标记效果已激活
                            ACTIVE_NIGHT_VISION.put(playerUUID, true);
                            
                            // 清除蹲下时间记录
                            CROUCH_START_TIME.remove(playerUUID);
                        }
                    }
                } else {
                    // 如果玩家没有蹲下，清除蹲下时间记录和激活状态
                    CROUCH_START_TIME.remove(playerUUID);
                    ACTIVE_NIGHT_VISION.remove(playerUUID);
                }
            } else {
                // 如果玩家没有这个附魔，清除相关记录
                UUID playerUUID = player.getUUID();
                CROUCH_START_TIME.remove(playerUUID);
                ACTIVE_NIGHT_VISION.remove(playerUUID);
            }
        }
    }

    private static void applyNightVisionEffect(Player player, int level) {
        // 给玩家添加夜视效果，持续30秒
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false, true));
    }
}