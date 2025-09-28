package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class DropReaperEnchantment extends Enchantment {

    public DropReaperEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.create("HOE", item -> item instanceof HoeItem), new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
        // 只能附在锄上
        return stack.getItem() instanceof HoeItem;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.DROP_REAPER.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.DROP_REAPER.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.DROP_REAPER.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        // 检查死亡实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手装备是否有掉落收割附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DROP_REAPER.get(), mainHandItem);

            // 如果有附魔且等级大于0
            if (level > 0 && !event.getDrops().isEmpty()) {
                // 每级增加一倍掉落物（即乘以2的等级次方）
                int multiplier = (int) Math.pow(2.0, level) - 1; // 减1是因为原本就会掉落一次

                // 增加额外的掉落物
                List<ItemEntity> extraDrops = new ArrayList<>();
                for (int i = 0; i < multiplier; i++) {
                    for (ItemEntity drop : event.getDrops()) {
                        if (!drop.getItem().isEmpty()) {
                            // 创建新的物品实体，避免修改原始实体
                            ItemEntity extraDrop = new ItemEntity(
                                drop.level(), 
                                drop.getX(), 
                                drop.getY(), 
                                drop.getZ(), 
                                drop.getItem().copy()
                            );
                            extraDrops.add(extraDrop);
                        }
                    }
                }

                // 将额外掉落物添加到事件中
                event.getDrops().addAll(extraDrops);
            }
        }
    }
}