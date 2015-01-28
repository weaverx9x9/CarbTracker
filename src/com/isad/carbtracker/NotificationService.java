package com.isad.carbtracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NotificationService extends Service {
	// For Debugging.
	//private static final String TAG = "CarbTrackerNotificationService";
	//private static final boolean D = true;
	
	private NotificationManager mNM;
	//private AbstractDbAdapter mFoodsDatabase;
	
	private int NOTIFICATION = R.string.local_service_started;
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class NotificationBinder extends Binder {
		NotificationService getService() {
			return NotificationService.this;
		}
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification(0);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//if(D) Log.i(TAG, "Recieved start id " + startId + ": " + intent);
		// Want this to run until explicitly stopped so return sticky.
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		// cancel the persistent notification.
		mNM.cancel(NOTIFICATION);
		
		// Tell the user we stopped.
		Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
	}

	private final IBinder mBinder = new NotificationBinder();
	
	public void removeNotification() {
		mNM.cancel(NOTIFICATION);
	}
	
	public void upDateNotification(double carbs) {
		showNotification(carbs);
	}
	
	private void showNotification(double carbs) {
		// Use the same text for the ticker and the expanded notification.
		String text = (String) getText(R.string.enter_weight);
		String date = Tools.getDate();
		date = Tools.formatDate(date, "/");
		text += " " + date;
		
		// Set the icon, scrolling text and time stamp.
		int roundedCarbs = (int)Math.round(carbs);
		Notification notification = new Notification(R.drawable.icon, Integer.toString(roundedCarbs), System.currentTimeMillis());
		
		// The PendingIntent to launch our activity if the user selects this notification.
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new 
									  Intent(this, WeightEntryActivity.class), 0);
		
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);
		
		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
	}
}
