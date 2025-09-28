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

@Mod.EventBusSubscriber
public class ArmorPiercingEnchantment extends Enchantment {
    public ArmorPiercingEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("armor_piercing");
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
        return 10;
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
            // 检查玩家主手装备是否有锥心附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMOR_PIERCING.get(), mainHandItem);

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 获取目标的护甲值
                float armorValue = target.getArmorValue();
                
                // 每1点护甲值增加50%伤害
                float additionalDamage = armorValue * 0.5f * level;
                
                // 增加伤害
                event.setAmount(event.getAmount() + additionalDamage);
            }
        }
    }
}