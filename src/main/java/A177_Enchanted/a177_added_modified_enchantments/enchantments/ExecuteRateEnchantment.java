package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

@Mod.EventBusSubscriber
public class ExecuteRateEnchantment extends Enchantment {
    // 缓存玩家下次可触发时间，避免高频触发
    private static final Map<UUID, Long> PLAYER_EXECUTION_COOLDOWN = new HashMap<>();
    // 检查间隔（tick）
    private static final int EXECUTION_CHECK_INTERVAL = 10;
    
    public ExecuteRateEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.create("HOE", (item) -> item instanceof HoeItem), new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }
    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.EXECUTE_RATE.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.EXECUTE_RATE.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.EXECUTE_RATE.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 只在服务端处理
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 检查是否是玩家攻击生物
        if (event.getSource().getEntity() instanceof Player player) {
            // 检查冷却时间
            long currentTime = player.level().getGameTime();
            if (PLAYER_EXECUTION_COOLDOWN.containsKey(player.getUUID())) {
                long nextAllowedTime = PLAYER_EXECUTION_COOLDOWN.get(player.getUUID());
                if (currentTime < nextAllowedTime) {
                    return;
                }
            }

            // 检查主手是否有附魔锄头
            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.isEmpty() && heldItem.isEnchanted()) {
                int level = heldItem.getEnchantmentLevel(ModEnchantments.EXECUTE_RATE.get());
                
                // 如果有斩杀率附魔
                if (level > 0) {
                    LivingEntity target = event.getEntity();
                    
                    // 检查目标血量是否低于1%
                    float maxHealth = target.getMaxHealth();
                    float currentHealth = target.getHealth();
                    float healthPercentage = (currentHealth / maxHealth) * 100;
                    
                    if (healthPercentage <= 1.0f) {
                        // 计算触发概率：基础5% + 每级5%
                        double triggerChance = 5.0 + (level * 5.0);
                        
                        // 随机判断是否触发
                        if (Math.random() * 100 < triggerChance) {
                            // 立即杀死目标
                            target.setHealth(0.0f);
                            
                            // 设置冷却时间
                            PLAYER_EXECUTION_COOLDOWN.put(player.getUUID(), currentTime + EXECUTION_CHECK_INTERVAL);
                        }
                    }
                }
            }
        }
    }
}