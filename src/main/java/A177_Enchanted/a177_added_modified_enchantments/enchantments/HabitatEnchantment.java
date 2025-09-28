package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class HabitatEnchantment extends Enchantment {
    
    // 存储玩家下一次可以回复生命值的时间
    private static final Map<Player, Long> PLAYER_NEXT_HEAL_TIME = new HashMap<>();
    
    // 回复冷却时间（2秒 = 40 ticks）
    private static final int HEAL_COOLDOWN = 40;

    public HabitatEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
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

    public AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.HABITAT;
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        return EnchantmentCategory.ARMOR_CHEST.canEnchant(stack.getItem());
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
        return super.checkCompatibility(other) && other != ModEnchantments.CAMPFIRE_HEAL.get();
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
        
        // 只在服务端执行逻辑
        if (player.level().isClientSide) {
            return;
        }
        
        // 检查玩家是否装备了胸甲
        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestItem.isEmpty()) {
            return;
        }
        
        // 检查胸甲是否有栖所附魔
        int level = chestItem.getEnchantmentLevel(ModEnchantments.HABITAT.get());
        if (level <= 0) {
            return;
        }
        
        // 检查玩家位置是否有床
        boolean hasBedNearby = false;
        BlockPos playerPos = player.blockPosition();
        
        // 检查玩家脚下及周围是否有床
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = playerPos.offset(x, 0, z);
                BlockState blockState = player.level().getBlockState(checkPos);
                if (blockState.getBlock() instanceof BedBlock) {
                    hasBedNearby = true;
                    break;
                }
            }
            if (hasBedNearby) break;
        }
        
        // 如果玩家位置没有床，则不触发效果
        if (!hasBedNearby) {
            return;
        }
        
        // 检查10格范围内是否有怪物
        boolean hasMonsterNearby = false;
        AABB boundingBox = new AABB(playerPos).inflate(10);
        List<Entity> entities = player.level().getEntities(player, boundingBox);
        
        for (Entity entity : entities) {
            if (entity instanceof Monster) { // 移除存活检查
                hasMonsterNearby = true;
                break;
            }
        }
        
        // 如果范围内有怪物，则不触发效果
        if (hasMonsterNearby) {
            return;
        }
        
        // 检查是否在冷却时间内
        Long nextHealTime = PLAYER_NEXT_HEAL_TIME.get(player);
        long currentTime = player.level().getGameTime();
        
        if (nextHealTime == null || currentTime >= nextHealTime) {
            // 恢复2格血量
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(2.0f);
                // 设置下一次回复时间
                PLAYER_NEXT_HEAL_TIME.put(player, currentTime + HEAL_COOLDOWN);
            }
        }
    }
}