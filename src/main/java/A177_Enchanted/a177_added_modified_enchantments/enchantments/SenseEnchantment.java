package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SenseEnchantment extends Enchantment {
    // 存储玩家开始蹲下的时间
    private static final Map<UUID, Long> CROUCH_START_TIME = new HashMap<>();
    // 存储玩家高亮效果的结束时间
    private static final Map<UUID, Long> HIGHLIGHT_END_TIME = new HashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("sense");
    }

    public SenseEnchantment() {
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canEnchant(ItemStack stack) {
        return EnchantmentCategory.ARMOR_HEAD.canEnchant(stack.getItem());
    }//可以附在头盔上

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//确保在附魔台中可以正确应用

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int level = helmet.getEnchantmentLevel(ModEnchantments.SENSE.get());
            
            if (level > 0) {
                UUID playerUUID = player.getUUID();
                long currentTime = player.level().getGameTime();
                
                // 检查高亮效果是否应该结束
                if (HIGHLIGHT_END_TIME.containsKey(playerUUID) && currentTime >= HIGHLIGHT_END_TIME.get(playerUUID)) {
                    HIGHLIGHT_END_TIME.remove(playerUUID);
                }
                
                // 检查玩家是否在蹲下
                if (player.isCrouching()) {
                    // 如果玩家之前没有记录蹲下时间，则记录当前时间
                    if (!CROUCH_START_TIME.containsKey(playerUUID)) {
                        CROUCH_START_TIME.put(playerUUID, currentTime);
                    } 
                    // 根据附魔等级确定所需蹲下时间：1级需要4秒(85　ticks)
                    else if (currentTime - CROUCH_START_TIME.get(playerUUID) >= (80 - (level * 25L)) &&
                             !HIGHLIGHT_END_TIME.containsKey(playerUUID)) {
                        // 触发高亮效果
                        applyHighlightEffect(player, level);
                        
                        // 设置高亮效果结束时间（5秒后）
                        HIGHLIGHT_END_TIME.put(playerUUID, currentTime + 100);
                        
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
                HIGHLIGHT_END_TIME.remove(playerUUID);
            }
        }
    }

    private static void applyHighlightEffect(Player player, int level) {
        // 获取25格范围内的所有怪物
        AABB boundingBox = player.getBoundingBox().inflate(30.0D);
        player.level().getEntitiesOfClass(Mob.class, boundingBox).forEach(entity -> {
            // 给怪物添加发光效果，持续5秒，只有该玩家能看到
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false, true), player);
        });
    }
}