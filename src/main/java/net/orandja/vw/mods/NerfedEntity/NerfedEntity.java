package net.orandja.vw.mods.NerfedEntity;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface NerfedEntity {

    short getTickCooldown();
    void setTickCooldown(short cooldown);

    boolean isMobDead();

    default void nerfAI(CallbackInfo info) {
        setTickCooldown((short) (getTickCooldown() - 1));
        if(getTickCooldown() > 0 && !isMobDead()) {
            info.cancel();
            return;
        }

        setTickCooldown((short) 30);
    }

}
