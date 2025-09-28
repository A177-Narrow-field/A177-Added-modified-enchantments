package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class RainNourishmentEnchantment extends Enchantment {
    // 存储玩家的治疗计时器
    private static final Map<UUID, Integer> healTimers = new HashMap<>();
    
    // 治疗间隔 (60 ticks = 3秒)
    private static final int HEAL_INTERVAL = 60;
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("rain_nourishment");
    }

    public RainNourishmentEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 5;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        UUID playerId = player.getUUID();
        
        // 检查玩家是否装备了雨润附魔的头盔
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RAIN_NOURISHMENT.get(), helmet);
        
        // 只在玩家拥有附魔时处理效果
        if (level > 0) {
            // 检查玩家是否在主世界且正在下雨
            if (player.level().dimension() == net.minecraft.world.level.Level.OVERWORLD && 
                player.level().isRaining()) {
                
                // 更新计时器
                Integer timer = healTimers.get(playerId);
                if (timer == null) {
                    timer = 0;
                }
                timer++;
                
                // 如果计时器达到间隔，则回复生命值
                if (timer >= HEAL_INTERVAL) {
                    // 回复生命值（每级回复1点生命值）
                    if (player.getHealth() < player.getMaxHealth()) {
                        player.heal((float) level);
                    }
                    
                    // 重置计时器
                    timer = 0;
                }
                
                // 更新记录
                healTimers.put(playerId, timer);
            } else {
                // 如果不在下雨或不在主世界，重置计时器
                healTimers.remove(playerId);
            }
        } else {
            // 如果玩家没有该附魔，移除其记录
            healTimers.remove(playerId);
        }
    }
}