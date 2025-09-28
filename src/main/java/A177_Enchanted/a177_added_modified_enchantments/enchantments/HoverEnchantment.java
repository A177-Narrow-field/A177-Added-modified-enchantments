package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HoverEnchantment extends Enchantment {
    // 存储玩家状态
    private static final Map<UUID, Boolean> WAS_SNEAKING_MAP = new HashMap<>();
    private static final Map<UUID, Integer> HOVER_TICKS_MAP = new HashMap<>();

    // 配置常量 - 便于调整
    private static final int MAX_HOVER_TICKS = 200; // 最大悬停时间（200 ticks = 10秒）

    public HoverEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
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
        return this.getMinCost(level) + 15;
    }

    public AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.HOVER;
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在鞋子上
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    // 添加与急坠急停附魔的冲突规则
    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment)
                && enchantment != ModEnchantments.FAST_FALL.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;
        UUID playerId = player.getUUID();

        // 检查靴子是否附魔
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        boolean hasEnchant = boots.getEnchantmentLevel(ModEnchantments.HOVER.get()) > 0;

        // 如果没有附魔，清除状态并返回
        if (!hasEnchant) {
            WAS_SNEAKING_MAP.remove(playerId);
            HOVER_TICKS_MAP.remove(playerId);
            return;
        }

        boolean isSneaking = player.isCrouching();
        boolean wasSneaking = WAS_SNEAKING_MAP.getOrDefault(playerId, false);
        int hoverTicks = HOVER_TICKS_MAP.getOrDefault(playerId, 0);

        // 只有空中才生效
        if (!player.onGround() && !player.isInWater() && !player.isInLava()) {
            if (isSneaking) {
                // 按住蹲下时应用悬停效果
                // 设置垂直速度为0实现悬停
                player.setDeltaMovement(
                        player.getDeltaMovement().x,
                        0, // 垂直速度设为0
                        player.getDeltaMovement().z
                );
                
                // 增加悬停计数
                HOVER_TICKS_MAP.put(playerId, hoverTicks + 1);
            } else {
                // 没有按住蹲下键，清除悬停状态
                HOVER_TICKS_MAP.remove(playerId);
            }
        } else {
            // 玩家在地面或液体中，移除悬停状态
            HOVER_TICKS_MAP.remove(playerId);
        }

        // 更新状态
        WAS_SNEAKING_MAP.put(playerId, isSneaking);
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.FEET) {
            return;
        }
        
        // 当鞋子装备变更时，立即清除状态
        UUID playerId = player.getUUID();
        WAS_SNEAKING_MAP.remove(playerId);
        HOVER_TICKS_MAP.remove(playerId);
    }
}