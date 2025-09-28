package A177_Enchanted.a177_added_modified_enchantments.init;

import A177_Enchanted.a177_added_modified_enchantments.A177_added_modified_enchantments;
import A177_Enchanted.a177_added_modified_enchantments.blocks.CloudBlock;
import A177_Enchanted.a177_added_modified_enchantments.blocks.HardenedLavaBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
            DeferredRegister.create(ForgeRegistries.BLOCKS, A177_added_modified_enchantments.MODID);
    
    // Cloud block that disappears after 2 seconds
    public static final RegistryObject<Block> CLOUD_BLOCK = BLOCKS.register("cloud_block", 
            () -> new CloudBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).noOcclusion().instabreak()));
    
    // Hardened lava block that turns into lava after 2 seconds
    public static final RegistryObject<Block> HARDENED_LAVA_BLOCK = BLOCKS.register("hardened_lava_block",
            () -> new HardenedLavaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).lightLevel((state) -> 10).strength(1.5F, 6.0F).pushReaction(PushReaction.NORMAL)));
}