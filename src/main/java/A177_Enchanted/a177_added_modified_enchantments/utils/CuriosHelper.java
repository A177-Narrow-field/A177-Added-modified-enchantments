package A177_Enchanted.a177_added_modified_enchantments.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;
import java.util.function.Predicate;

public class CuriosHelper {
    public static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    /**
     * 检查玩家是否在Curios槽位中装备了满足条件的物品
     *
     * @param player    玩家实体
     * @param predicate 条件判断函数
     * @return 如果找到满足条件的物品则返回true，否则返回false
     */
    public static boolean hasCurioItem(Player player, Predicate<ItemStack> predicate) {
        if (!CURIOS_LOADED) {
            return false;
        }

        boolean[] found = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getCurios().forEach((id, slotInventory) -> {
                    IDynamicStackHandler stacks = slotInventory.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!stack.isEmpty() && predicate.test(stack)) {
                            found[0] = true;
                        }
                    }
                })
        );
        return found[0];
    }
}