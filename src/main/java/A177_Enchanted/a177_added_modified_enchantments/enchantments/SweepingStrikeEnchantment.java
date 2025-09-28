package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SweepingStrikeEnchantment extends Enchantment {
    // 存储每个玩家的连击计数和时间戳
    private static final Map<UUID, Integer> comboCounts = new HashMap<>();
    private static final Map<UUID, Long> lastAttackTimes = new HashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("sweeping_strike");
    }

    public SweepingStrikeEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }// 最多3级

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
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
        // 只能附在剑上
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            UUID playerId = player.getUUID();
            ItemStack weapon = player.getMainHandItem();
            int level = weapon.getEnchantmentLevel(ModEnchantments.SWEEPING_STRIKE.get());
            
            if (level > 0 && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = event.getEntity();
                long currentTime = System.currentTimeMillis();
                
                // 获取上次攻击的时间
                Long lastAttackTime = lastAttackTimes.get(playerId);
                int comboCount = comboCounts.getOrDefault(playerId, 0);
                
                // 计算最大叠加层数：每级+10层上限，基础为10层
                int maxCombo = 10 + (level * 10);
                
                // 如果距离上次攻击超过1秒，则重置连击
                if (lastAttackTime == null || (currentTime - lastAttackTime) > 1000) {
                    comboCount = 0;
                }
                
                // 增加连击计数（不超过最大值）
                if (comboCount < maxCombo) {
                    comboCount++;
                }
                
                // 更新记录
                comboCounts.put(playerId, comboCount);
                lastAttackTimes.put(playerId, currentTime);
                
                // 应用连击伤害加成
                if (comboCount > 0) {
                    float damageIncrease = comboCount * 0.2f; // 每层+20%伤害
                    float newDamage = event.getAmount() * (1.0f + damageIncrease);
                    event.setAmount(newDamage);
                }
            }
        }
    }
}