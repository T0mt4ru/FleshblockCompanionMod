package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServiceException extends Exception {
    public final RealmsError realmsError;

    public RealmsServiceException(RealmsError realmsError) {
        this.realmsError = realmsError;
    }

    @Override
    public String getMessage() {
        return this.realmsError.logMessage();
    }
}
