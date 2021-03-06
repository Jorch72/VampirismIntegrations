package de.teamlapen.vampirism_integrations.mca;

import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.world.IVampirismVillage;
import mca.entity.EntityVillagerMCA;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Vampirism's basic extension of MCA's villager
 */
abstract class EntityVillagerVampirismMCA extends EntityVillagerMCA {
    private static @Nullable
    IVampirismVillage getNearestVillage(World w, BlockPos pos, int r) {
        Village v = w.villageCollection.getNearestVillage(pos, r);
        if (v != null) {
            return VampirismAPI.getVampirismVillage(v);
        }
        return null;
    }
    protected boolean peaceful = false;
    protected
    @Nullable
    IVampirismVillage vampirismVillage;
    /**
     * A timer which reaches 0 every 70 to 120 ticks
     */
    private int randomTickDivider;


    public EntityVillagerVampirismMCA(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        float f = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int i = 0;

        if (entity instanceof EntityLivingBase) {
            f += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase) entity).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag) {
            if (i > 0) {
                entity.addVelocity((double) (-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F), 0.1D, (double) (MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0) {
                entity.setFire(j * 4);
            }

            this.applyEnchantments(this, entity);

        }


        return flag;
    }

    @Override
    public boolean attackEntityFrom(DamageSource src, float amount) {
        if (this.isEntityInvulnerable(src)) {
            return false;
        } else if (super.attackEntityFrom(src, amount)) {
            Entity entity = src.getTrueSource();
            if (entity instanceof EntityLivingBase) {
                this.setAttackTarget((EntityLivingBase) entity);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean getCanSpawnHere() {
        return (peaceful || this.world.getDifficulty() != EnumDifficulty.PEACEFUL) && super.getCanSpawnHere();
    }

    @Nullable
    public IVampirismVillage getVampirismVillage() {
        return vampirismVillage;
    }

    @Override
    public void onLivingUpdate() {
        this.updateArmSwingProgress();
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!this.world.isRemote && !peaceful && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            this.setDead();
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        if (this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);

        }
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        if (--this.randomTickDivider <= 0) {
            this.randomTickDivider = 70 + rand.nextInt(50);
            this.vampirismVillage = getNearestVillage(world, getPosition(), 32);
        }

    }
}
