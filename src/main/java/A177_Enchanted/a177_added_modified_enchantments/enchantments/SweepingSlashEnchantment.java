package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
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

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SweepingSlashEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("sweeping_slash");
    }
    
    // 存储已受到真实伤害的目标，防止重复伤害
    private static final Set<UUID> PROCESSED_TARGETS = new HashSet<>();

    public SweepingSlashEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在剑上
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只能附在剑上
        return canEnchant(stack) && isDiscoverable();
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

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家，受伤实体是否为生物实体
        if (event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            ItemStack mainHandItem = player.getMainHandItem();
            
            // 检查主手物品是否有弧斩附魔
            if (mainHandItem.isEnchanted()) {
                int level = mainHandItem.getEnchantmentLevel(ModEnchantments.SWEEPING_SLASH.get());
                if (level > 0) {
                    // 检查是否为横扫攻击伤害
                    // 在Minecraft中，横扫攻击的伤害类型是PLAYER_ATTACK，但不是直接攻击
                    // 我们通过检查伤害值是否为1.0f（默认横扫伤害）来判断
                    if (event.getAmount() == 1.0f) {
                        // 每级增加100%横扫伤害
                        float additionalDamage = event.getAmount() * level * 1.0f;
                        event.setAmount(event.getAmount() + additionalDamage);
                        
                        // 添加新功能：对被横扫攻击命中的目标造成5%最大生命值的真实伤害
                        // 检查目标是否已经被处理过，防止重复伤害
                        if (!PROCESSED_TARGETS.contains(target.getUUID())) {
                            float trueDamage = target.getMaxHealth() * 0.05f;
                            target.hurt(player.damageSources().magic(), trueDamage);
                            
                            // 标记目标已被处理
                            PROCESSED_TARGETS.add(target.getUUID());
                            
                            // 消耗5%武器耐久度
                            if (mainHandItem.isDamageableItem()) {
                                int damage = Math.max(1, (int) (mainHandItem.getMaxDamage() * 0.05));
                                mainHandItem.hurtAndBreak(damage, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                            }
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        // 当玩家攻击实体时，清空已处理目标列表，为下一次横扫攻击做准备
        PROCESSED_TARGETS.clear();
    }
}