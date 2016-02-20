package com.microsoft.band.sdk.heartrate;

import android.app.Application;
import io.smooch.core.Smooch;

public class heartrate extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Smooch.init(this, "4x5c8tpzib3x75paw8cum9a2g");
    }
}