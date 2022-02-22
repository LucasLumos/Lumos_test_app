package com.example.lumosnew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class mBroadcastReceiver extends BroadcastReceiver {
    public static MainActivity mMainActivity = new MainActivity();
    @Override
    // Defining behaviors when receiving broadcast
    public void onReceive(Context context, Intent intent) {
        // Log.i(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~ Broadcast Received");
        StringBuilder sb = new StringBuilder();
        sb.append(intent.getAction());
        String str = sb.toString();
        Log.d("Tag", "onReceive received broadcast: ");
        // Read all characteristics after connected to the device
        if (str.equals("main_activity_connected")){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //mMainActivity.readAllChar();
                }
            }).start();
        }
    }
}


