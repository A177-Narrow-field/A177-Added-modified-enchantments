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
    // 存储夜视效果的结束时间
    private static final Map<UUID, Long> NIGHT_VISION_END_TIME = new HashMap<>();
    
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
                
                // 检查夜视效果是否应该结束
                if (NIGHT_VISION_END_TIME.containsKey(playerUUID) && currentTime >= NIGHT_VISION_END_TIME.get(playerUUID)) {
                    NIGHT_VISION_END_TIME.remove(playerUUID);
                    // 移除夜视效果
                    player.removeEffect(MobEffects.NIGHT_VISION);
                }
                
                // 检查玩家是否在蹲下
                if (player.isCrouching()) {
                    // 如果玩家之前没有记录蹲下时间，则记录当前时间
                    if (!CROUCH_START_TIME.containsKey(playerUUID)) {
                        CROUCH_START_TIME.put(playerUUID, currentTime);
                    } 
                    // 根据附魔等级确定所需蹲下时间：3秒基础时间，每级减少0.5秒(10 ticks)
                    else if (currentTime - CROUCH_START_TIME.get(playerUUID) >= (60 - (level * 10L)) &&
                             !NIGHT_VISION_END_TIME.containsKey(playerUUID)) {
                        // 触发夜视效果
                        applyNightVisionEffect(player, level);
                        
                        // 设置夜视效果结束时间（30秒后）
                        NIGHT_VISION_END_TIME.put(playerUUID, currentTime + 600);
                        
                        // 清除蹲下时间记录，需要重新蹲下才能再次触发
                        CROUCH_START_TIME.remove(playerUUID);
                    }
                } else {
                    // 如果玩家没有蹲下，清除蹲下时间记录
                    CROUCH_START_TIME.remove(playerUUID);
                }
            } else {
                // 如果玩家没有这个附魔，清除相关记录
                UUID playerUUID = player.getUUID();
                CROUCH_START_TIME.remove(playerUUID);
                NIGHT_VISION_END_TIME.remove(playerUUID);
                // 确保移除可能存在的夜视效果
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    private static void applyNightVisionEffect(Player player, int level) {
        // 给玩家添加夜视效果，持续300秒
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false, true));
    }
}