package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class OverworldCurseEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("overworld_curse");
    }

    public OverworldCurseEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 1;
    }// 附魔的初始成本

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 4;
    }// 附魔的等级成本

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//确保在附魔台中可以正确应用

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器和工具上
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof TieredItem;
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手装备是否有主界诅咒附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OVERWORLD_CURSE.get(), mainHandItem);
            
            // 如果有附魔且等级大于0
            if (level > 0) {
                Level world = player.level();
                
                // 判断玩家所在的维度
                if (world.dimension() == Level.OVERWORLD) {
                    // 在主世界伤害减少60%
                    event.setAmount(event.getAmount() * 0.4f);
                } else if (world.dimension() == Level.NETHER) {
                    // 在下界伤害增加30%
                    event.setAmount(event.getAmount() * 1.3f);
                }
            }
        }
    }
}