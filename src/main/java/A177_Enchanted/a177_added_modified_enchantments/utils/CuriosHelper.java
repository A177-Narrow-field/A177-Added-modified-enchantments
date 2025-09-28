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

    /**
     * 处理玩家死亡时的Curios物品
     *
     * @param player         玩家实体
     * @param soulboundItems 灵魂绑定物品列表
     */
    public static void handleCuriosItemsOnDeath(Player player, java.util.List<ItemStack> soulboundItems) {
        if (!CURIOS_LOADED) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getCurios().forEach((id, slotInventory) -> {
                    IDynamicStackHandler stacks = slotInventory.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            soulboundItems.add(stack.copy());
                            stacks.setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                })
        );
    }

    /**
     * 尝试将物品放在合适的Curios槽位中
     *
     * @param player 玩家实体
     * @param stack  物品堆
     * @return 是否成功放置
     */
    public static boolean tryPlaceInCurios(Player player, ItemStack stack) {
        if (!CURIOS_LOADED || stack.isEmpty()) {
            return false;
        }

        boolean[] placed = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.getCurios().forEach((id, slotInventory) -> {
                    if (placed[0]) return; // 已经放置了就不再处理

                    IDynamicStackHandler stacks = slotInventory.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack slotStack = stacks.getStackInSlot(i);
                        if (slotStack.isEmpty()) {
                            stacks.setStackInSlot(i, stack.copy());
                            placed[0] = true;
                            return;
                        }
                    }
                })
        );
        return placed[0];
    }
}