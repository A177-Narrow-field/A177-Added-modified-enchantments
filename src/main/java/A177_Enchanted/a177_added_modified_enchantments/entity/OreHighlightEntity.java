package A177_Enchanted.a177_added_modified_enchantments.entity;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreHighlightEntity extends LivingEntity {
    private static final EntityDataAccessor<BlockPos> ORIGIN_POS = SynchedEntityData.defineId(OreHighlightEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(OreHighlightEntity.class, EntityDataSerializers.INT);
    
    // 添加无参构造函数，供 EntityType 使用
    public OreHighlightEntity(EntityType<? extends OreHighlightEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public OreHighlightEntity(Level level, BlockPos pos, BlockState state) {
        super(ModEntities.ORE_HIGHLIGHT.get(), level);
        this.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        this.entityData.set(ORIGIN_POS, pos);
        // 设置实体生命周期为300 ticks (15秒)
        this.entityData.set(LIFETIME, 300);
        // 添加发光效果
        this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 120, 0, false, false));
        // 禁用物理效果
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ORIGIN_POS, BlockPos.ZERO);
        this.entityData.define(LIFETIME, 0);
    }

    @Override
    public void tick() {
        super.tick();
        
        // 实体不受到重力影响
        this.setNoGravity(true);

        // 每tick检查方块是否还存在
        if (!this.level().isClientSide) {
            BlockPos pos = this.entityData.get(ORIGIN_POS);
            if (this.level().getBlockState(pos).isAir()) {
                this.discard();
                return;
            }
            
            // 更新生命周期并检查是否需要移除
            int lifetime = this.entityData.get(LIFETIME);
            lifetime--;
            this.entityData.set(LIFETIME, lifetime);
            
            // 更新发光效果
            if (this.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)) {
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 120, 0, false, false));
            }
            
            if (lifetime <= 0) {
                this.discard();
            }
        }
    }

    public BlockPos getOriginPos() {
        return this.entityData.get(ORIGIN_POS);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 无敌实体，不受任何伤害
        return false;
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        // 只允许发光效果
        return effect.getEffect() == net.minecraft.world.effect.MobEffects.GLOWING;
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        // 从NBT读取数据
        this.entityData.set(LIFETIME, compound.getInt("Lifetime"));
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        // 将数据写入NBT
        compound.putInt("Lifetime", this.entityData.get(LIFETIME));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPushable() {
        // 不可推动
        return false;
    }

    @Override
    protected void pushEntities() {
        // 不推动其他实体
    }

    @Override
    public boolean isPickable() {
        // 不可被射弹击中
        return false;
    }

    @Override
    protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
        return 0.0F;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public boolean attackable() {
        return false;
    }

    // 实现LivingEntity的抽象方法
    @Override
    public Iterable<ItemStack> getArmorSlots() {
        // 返回空的装甲槽列表
        return java.util.Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        // 返回空物品栈
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        // 不执行任何操作
    }

    @Override
    public net.minecraft.world.entity.HumanoidArm getMainArm() {
        // 返回主手（任意值）
        return net.minecraft.world.entity.HumanoidArm.RIGHT;
    }

    // 添加属性构建器
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FLYING_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }
    
    // 禁用移动
    @Override
    public void travel(net.minecraft.world.phys.Vec3 vec3) {
        // 不执行任何移动逻辑
    }
    
    // 禁用被推动
    @Override
    public void push(double x, double y, double z) {
        // 不执行任何推动逻辑
    }
}