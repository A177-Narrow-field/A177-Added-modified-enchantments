package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class ArmorBreakEnchantment extends Enchantment {
    private static final Random RANDOM = new Random();

    public ArmorBreakEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("armor_break");
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 限制只能在附魔台对镐子进行附魔
        return stack.getItem() instanceof PickaxeItem;
    }

    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在镐子上
        return stack.getItem() instanceof PickaxeItem;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = (LivingEntity) event.getEntity();
            // 检查玩家主手装备是否有破铠附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_BREAK.get(), mainHandItem);

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 5%概率击碎敌人身上的盔甲（每次只能击碎一个盔甲）
                if (target instanceof Player targetPlayer) {
                    // 如果击中的是玩家，则必定消耗玩家身上盔甲5%的耐久（每次只能消耗其中一个盔甲）
                    damageRandomArmor(targetPlayer, 0.05f);
                } else {
                    // 如果击中的是生物，有5%概率击碎其身上的盔甲
                    if (RANDOM.nextDouble() < 0.05) {
                        destroyRandomArmor(target);
                    }
                }
            }
        }
    }

    /**
     * 击碎目标身上的随机一个盔甲
     * @param target 目标生物
     */
    private static void destroyRandomArmor(LivingEntity target) {
        List<EquipmentSlot> armorSlots = new ArrayList<>();
        
        // 收集所有装备了盔甲的槽位
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armor = target.getItemBySlot(slot);
                if (!armor.isEmpty()) {
                    armorSlots.add(slot);
                }
            }
        }
        
        // 如果有装备盔甲，则随机选择一个击碎
        if (!armorSlots.isEmpty()) {
            EquipmentSlot selectedSlot = armorSlots.get(RANDOM.nextInt(armorSlots.size()));
            target.setItemSlot(selectedSlot, ItemStack.EMPTY);
        }
    }
    
    /**
     * 消耗目标身上随机一个盔甲5%的耐久
     * @param target 目标玩家
     * @param percentage 耐久消耗百分比
     */
    private static void damageRandomArmor(Player target, float percentage) {
        List<EquipmentSlot> armorSlots = new ArrayList<>();
        
        // 收集所有装备了盔甲的槽位
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armor = target.getItemBySlot(slot);
                if (!armor.isEmpty()) {
                    armorSlots.add(slot);
                }
            }
        }
        
        // 如果有装备盔甲，则随机选择一个消耗耐久
        if (!armorSlots.isEmpty()) {
            EquipmentSlot selectedSlot = armorSlots.get(RANDOM.nextInt(armorSlots.size()));
            ItemStack armor = target.getItemBySlot(selectedSlot);
            
            // 计算耐久消耗
            int maxDamage = armor.getMaxDamage();
            if (maxDamage > 0) {
                int damageToApply = Math.max(5, (int) (maxDamage * percentage)); // 指定百分比耐久，最少5点
                armor.hurtAndBreak(damageToApply, target, (p) -> {
                    // 当盔甲损坏时的回调，这里为空实现
                });
            }
        }
    }
}