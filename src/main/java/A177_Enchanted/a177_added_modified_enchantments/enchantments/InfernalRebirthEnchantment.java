package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class InfernalRebirthEnchantment extends Enchantment {
    // 存储玩家上次回血时间
    private static final Map<UUID, Long> lastHealTimeMap = new HashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("infernal_rebirth");
    }
    
    public InfernalRebirthEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 是否可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//确保在附魔台中可以正确应用


    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        // 与焚斥之心冲突
        return super.checkCompatibility(enchantment) && 
               enchantment != ModEnchantments.INFERNAL_ARMOR.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFERNAL_REBIRTH.get(), chestplate);

        if (level > 0) {
            // 如果玩家着火，每秒回复1%血量（每20 ticks回复一次）
            if (player.getRemainingFireTicks() > 0) {
                // 检查回血冷却时间 (1秒 = 20 ticks)
                UUID playerId = player.getUUID();
                long currentTime = player.level().getGameTime();
                long lastHealTime = lastHealTimeMap.getOrDefault(playerId, 0L);
                
                if (currentTime - lastHealTime >= 20) {
                    // 回复血量 (每级1%)
                    float healAmount = player.getMaxHealth() * (0.01f * level);
                    player.heal(healAmount);
                    
                    // 更新上次回血时间
                    lastHealTimeMap.put(playerId, currentTime);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查是否是玩家受到攻击
        if (!(event.getEntity() instanceof Player player)) return;
        
        // 检查玩家是否装备了炙浴附魔的胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFERNAL_REBIRTH.get(), chestplate);
        
        // 如果没有装备或者附魔等级为0，则不处理
        if (level <= 0) return;

        // 获取伤害源
        DamageSource source = event.getSource();

        // 使用 Minecraft 推荐的标签方式判断是否属于火焰伤害
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            // 减少50%火焰伤害
            event.setAmount(event.getAmount() * 0.5f);
        }
    }
}