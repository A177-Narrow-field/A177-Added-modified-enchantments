package A177_Enchanted.a177_added_modified_enchantments.init;

import A177_Enchanted.a177_added_modified_enchantments.A177_added_modified_enchantments;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, A177_added_modified_enchantments.MODID);
    
    // Cloud block item
    public static final RegistryObject<Item> CLOUD_BLOCK_ITEM = ITEMS.register("cloud_block", 
            () -> new BlockItem(ModBlocks.CLOUD_BLOCK.get(), new Item.Properties()));
}