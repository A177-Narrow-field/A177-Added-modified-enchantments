package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class DangerousAuraEnchantment extends Enchantment {
    private static final Map<UUID, Long> PLAYER_NEXT_PROCESS_TIME = new HashMap<>();
    
    // 弱化效果持续时间（ticks）
    private static final int WEAKNESS_DURATION = 120; // 6秒 (6*20=120 ticks)
    
    // 处理间隔（ticks）
    private static final int PROCESS_INTERVAL = 20; // 1秒 (20 ticks)
    
    // 作用范围
    private static final double AURA_RADIUS = 6.0;
    
    public DangerousAuraEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
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
        return AllEnchantmentsConfig.DANGEROUS_AURA;
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
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());
    }
    
    /**
     * 检查玩家是否穿戴了惧慑附魔
     * @param player 玩家
     * @return 是否穿戴了危之震慑附魔
     */
    private static boolean isPlayerWearingDangerousAuraEnchantment(Player player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        return chestplate.getEnchantmentLevel(ModEnchantments.DANGEROUS_AURA.get()) > 0;
    }
    
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        // 如果攻击目标是玩家并且玩家拥有惧慑附魔
        if (event.getNewTarget() instanceof Player player && isPlayerWearingDangerousAuraEnchantment(player)) {
            // 如果攻击者是苦力怕、末影人，则取消攻击
            if (event.getEntity().getType() == EntityType.CREEPER || 
                event.getEntity().getType() == EntityType.ENDERMAN)
            {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        
        // 只在服务端处理
        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否装备了危之震慑附魔的胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = chestplate.getEnchantmentLevel(ModEnchantments.DANGEROUS_AURA.get());

        // 如果没有装备或者附魔等级为0，则不处理
        if (level <= 0) {
            PLAYER_NEXT_PROCESS_TIME.remove(player.getUUID());
            return;
        }

        ServerLevel levelServer = (ServerLevel) player.level();
        long currentTime = levelServer.getGameTime();
        
        // 获取玩家下次处理时间
        long nextProcessTime = PLAYER_NEXT_PROCESS_TIME.getOrDefault(player.getUUID(), 0L);
        
        // 如果还没到处理时间，则跳过
        if (currentTime < nextProcessTime) {
            return;
        }
        
        // 更新下次处理时间
        PLAYER_NEXT_PROCESS_TIME.put(player.getUUID(), currentTime + PROCESS_INTERVAL);
        
        // 给范围内的所有生物添加虚弱效果（除了玩家自己）
        levelServer.getEntitiesOfClass(LivingEntity.class, 
            player.getBoundingBox().inflate(AURA_RADIUS), 
            entity -> entity instanceof LivingEntity && entity != player && entity.isAlive())
            .forEach(livingEntity -> {
                // 给生物添加虚弱效果
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 0));
            });
        
        // 清除玩家身上的负面效果：虚弱、黑暗、挖掘疲劳
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
    }

    // 已移除杀死苦力怕的代码
}