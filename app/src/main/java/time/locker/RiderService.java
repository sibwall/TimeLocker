package time.locker;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import java.util.*;
import android.app.admin.DevicePolicyManager;
import android.net.Uri;
import android.app.slice.SliceManager;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.os.*;
import android.provider.*;
import android.os.storage.*;

public class RiderService extends JobService {  

	private void startFGS() {      	
		try {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel("no", "no", 1));

        Notification notification = new Notification.Builder(this, "no")
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build();

		if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, notification);
		}} catch (Throwable t) {}
	}

	private static Thread thread;
	
	private final void ServiceMainVoid() {		   
		if (thread == null) {       
			thread = new Thread(() -> {
				Context context = getApplicationContext();
				DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);     
				KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				while (true) {
					android.os.SystemClock.sleep(1000);
					int hour = java.time.LocalTime.now().getHour();
					if (hour >= 22 || hour < 8) {
						try {
							if (!km.isKeyguardLocked()) dpm.lockNow();
							if (notificationManager.isNotificationPolicyAccessGranted()) notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
						} catch (Throwable e) {
							dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
						}                        
					} else {
						if (notificationManager.isNotificationPolicyAccessGranted()) notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);						
					}
				}				
			});
			thread.start();			                                       
		}        
    }

	private static final int PERIODIC_JOB_ID = 1001;
    private static final int DELAYED_JOB_ID = 1002;

    @Override
    public boolean onStartJob(JobParameters params) {        
        scheduleJobs(getApplicationContext());
		return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {                    
		scheduleJobs(getApplicationContext());
		return true;
    }

    public static void scheduleJobs(Context context) {
		try {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) return;

        ComponentName componentName = new ComponentName(context, RiderService.class);

        boolean isPeriodicScheduled = false;
        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == PERIODIC_JOB_ID) {
                isPeriodicScheduled = true;
                break;
            }
        }

        if (!isPeriodicScheduled) {
            JobInfo.Builder periodicBuilder = new JobInfo.Builder(PERIODIC_JOB_ID, componentName)
                    .setPeriodic(JobInfo.getMinPeriodMillis())
                    .setPersisted(true)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false);
            
            periodicBuilder.setRequiresBatteryNotLow(false);
            periodicBuilder.setRequiresStorageNotLow(false);            

            jobScheduler.schedule(periodicBuilder.build());
        }

        JobInfo.Builder delayedBuilder = new JobInfo.Builder(DELAYED_JOB_ID, componentName)
                .setMinimumLatency(30 * 1000L)                
                .setPersisted(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false);

        delayedBuilder.setRequiresBatteryNotLow(false);
        delayedBuilder.setRequiresStorageNotLow(false);
        
        jobScheduler.schedule(delayedBuilder.build());
		} catch (Throwable t) {}	
    }
				
		@Override
    public final void onCreate() {
        super.onCreate();
		startFGS();
		scheduleJobs(this);	
		ServiceMainVoid();
	}		
			    
    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {    	    
		startFGS();
		return START_STICKY;
    }

    @Override
    public final void onDestroy() {		
        scheduleJobs(this);	        
		super.onDestroy();
    }
}
