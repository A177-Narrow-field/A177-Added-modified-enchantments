package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class KingEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("king");
    }
    
    public KingEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//可以在附魔台

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在头盔上
        return EnchantmentCategory.ARMOR_HEAD.canEnchant(stack.getItem());
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return !(enchantment instanceof GeneralEnchantment) && !(enchantment instanceof CommanderEnchantment) && super.checkCompatibility(enchantment);// 与将军和统领附魔冲突
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }

        // 检查玩家是否拥有国王附魔
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getEnchantmentLevel(ModEnchantments.KING.get()) > 0) {
            applyHealingEffect(player.level(), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            // 检查玩家是否拥有国王附魔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getEnchantmentLevel(ModEnchantments.KING.get()) > 0) {
                applyWeaknessEffect(player.level(), player, event.getSource().getEntity());
            }
        }
    }

    /**
     * 每11秒为10格内友方生物回复1点血量，每级减少2秒冷却时间
     * @param level 世界
     * @param player 玩家
     */
    public static void applyHealingEffect(Level level, Player player) {
        if (level.isClientSide) return;

        // 获取头盔上的国王附魔等级
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        int enchantmentLevel = helmet.getEnchantmentLevel(ModEnchantments.KING.get());
        
        if (enchantmentLevel <= 0) return;

        // 计算冷却时间：11秒 - (等级 * 2秒)
        int cooldown = 220 - (enchantmentLevel * 40); // 220 ticks = 11秒, 40 ticks = 2秒
        if (level.getGameTime() % cooldown != 0) return;

        // 获取10格内的友方生物
        AABB boundingBox = new AABB(player.blockPosition()).inflate(10);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, boundingBox);

        // 为友方生物回复血量
        for (LivingEntity entity : entities) {
            // 判断是否为友方（玩家总是友方，其他生物需要是同一玩家的宠物等）
            if (entity instanceof Player) {
                Player targetPlayer = (Player) entity;
                if (targetPlayer.getHealth() < targetPlayer.getMaxHealth()) {
                    targetPlayer.heal(1.0F);
                }
            } else if (entity.isAlliedTo(player)) {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    entity.heal(1.0F);
                }
            }
        }
    }

    /**
     * 当穿戴者受到伤害时，给10格内友方生物施加虚弱效果
     * @param level 世界
     * @param player 玩家
     * @param attacker 攻击者
     */
    public static void applyWeaknessEffect(Level level, Player player, Entity attacker) {
        if (level.isClientSide) return;

        // 获取头盔上的国王附魔等级
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        int enchantmentLevel = helmet.getEnchantmentLevel(ModEnchantments.KING.get());

        if (enchantmentLevel <= 0) return;

        // 获取10格内的友方生物
        AABB boundingBox = new AABB(player.blockPosition()).inflate(10);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, boundingBox);

        // 为友方生物施加虚弱效果
        for (LivingEntity entity : entities) {
            if (entity instanceof Player) {
                Player targetPlayer = (Player) entity;
                if (targetPlayer != player) { // 不包括穿戴者自己
                    targetPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0)); // 3秒虚弱
                }
            } else if (entity.isAlliedTo(player)) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0)); // 3秒虚弱
            }
        }
    }
}