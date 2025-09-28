package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.List;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class HatredTauntEnchantment extends Enchantment {
    // 缓存玩家当前的附魔等级，避免重复计算
    private static final WeakHashMap<Player, Integer> PLAYER_HATRED_TAUNT_CACHE = new WeakHashMap<>();
    
    // 记录玩家的下次检查时间
    private static final WeakHashMap<Player, Long> PLAYER_NEXT_CHECK_TIME = new WeakHashMap<>();
    
    // 检查间隔 (10 ticks = 0.5秒)
    private static final int CHECK_INTERVAL = 10;
    
    // 基础范围半径
    private static final double BASE_RANGE = 10.0;

    public HatredTauntEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.create("SHIELD", item -> item instanceof ShieldItem), new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
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
        return AllEnchantmentsConfig.HATRED_TAUNT;
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
        // 只能附在盾牌上
        return stack.getItem() instanceof ShieldItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        Player player = event.player;
        long currentTime = player.level().getGameTime();
        
        // 获取玩家的下次检查时间
        Long nextCheckTime = PLAYER_NEXT_CHECK_TIME.get(player);
        if (nextCheckTime != null && currentTime < nextCheckTime) {
            return;
        }
        
        // 设置下次检查时间为0.5秒后（10个tick）
        PLAYER_NEXT_CHECK_TIME.put(player, currentTime + CHECK_INTERVAL);
        
        // 获取缓存的附魔等级
        int cachedLevel = PLAYER_HATRED_TAUNT_CACHE.getOrDefault(player, -1);
        int currentLevel = 0;
        
        // 检查玩家是否正在使用盾牌
        boolean isUsingShield = false;
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        
        if (mainHandItem.getItem() instanceof ShieldItem && player.isUsingItem()) {
            isUsingShield = true;
            if (mainHandItem.isEnchanted() && mainHandItem.getEnchantmentLevel(ModEnchantments.HATRED_TAUNT.get()) > 0) {
                currentLevel = mainHandItem.getEnchantmentLevel(ModEnchantments.HATRED_TAUNT.get());
            }
        } else if (offHandItem.getItem() instanceof ShieldItem && player.isUsingItem()) {
            isUsingShield = true;
            if (offHandItem.isEnchanted() && offHandItem.getEnchantmentLevel(ModEnchantments.HATRED_TAUNT.get()) > 0) {
                currentLevel = offHandItem.getEnchantmentLevel(ModEnchantments.HATRED_TAUNT.get());
            }
        }
        
        // 如果玩家没有使用盾牌，则清零附魔等级
        if (!isUsingShield) {
            currentLevel = 0;
        }
        
        // 如果等级发生变化，更新缓存
        if (cachedLevel != currentLevel) {
            PLAYER_HATRED_TAUNT_CACHE.put(player, currentLevel);
        }
        
        // 如果有附魔等级且正在使用盾牌，处理仇恨吸引逻辑
        if (currentLevel > 0) {
            attractMobHatred(player);
        }
    }
    
    /**
     * 吸引范围内怪物的仇恨
     * @param player 玩家
     */
    private static void attractMobHatred(Player player) {
        // 创建一个包围盒，覆盖玩家周围的区域
        AABB boundingBox = player.getBoundingBox().inflate(BASE_RANGE, BASE_RANGE, BASE_RANGE);
        
        // 获取范围内的所有生物实体
        List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, boundingBox, 
            entity -> entity.isAlive() && entity.getTarget() != player);
        
        // 吸引范围内的怪物仇恨
        for (Mob mob : mobs) {
            // 设置怪物的目标为玩家
            mob.setTarget(player);
        }
    }
}