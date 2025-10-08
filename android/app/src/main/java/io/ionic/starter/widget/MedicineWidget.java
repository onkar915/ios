package io.ionic.starter.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import io.ionic.starter.MainActivity;
import io.ionic.starter.R;

public class MedicineWidget extends AppWidgetProvider {
    public static final String ACTION_TAKEN = "ACTION_MEDICINE_TAKEN";
    public static final String ACTION_MISSED = "ACTION_MEDICINE_MISSED";
    public static final String EXTRA_MEDICINE_ID = "medicineId";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId, String medicine1Name, String medicine2Name,
                              String medicine1Id, String medicine2Id) {
        
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_medicine);
        
        // Setup medicine views
        setupMedicineView(context, views, R.id.medicine1_container, 
                        R.id.medicine1_name, R.id.medicine1_taken, 
                        R.id.medicine1_missed, medicine1Name, medicine1Id);
        
        setupMedicineView(context, views, R.id.medicine2_container, 
                        R.id.medicine2_name, R.id.medicine2_taken, 
                        R.id.medicine2_missed, medicine2Name, medicine2Id);
        
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setupMedicineView(Context context, RemoteViews views, 
                                        int containerId, int nameId, int takenId, 
                                        int missedId, String name, String id) {
        if (name != null && id != null) {
            views.setTextViewText(nameId, name);
            views.setViewVisibility(containerId, android.view.View.VISIBLE);
            
            // Taken button
            Intent takenIntent = new Intent(context, MedicineWidget.class);
            takenIntent.setAction(ACTION_TAKEN);
            takenIntent.putExtra(EXTRA_MEDICINE_ID, id);
            PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context, 0, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(takenId, takenPendingIntent);
            
            // Missed button
            Intent missedIntent = new Intent(context, MedicineWidget.class);
            missedIntent.setAction(ACTION_MISSED);
            missedIntent.putExtra(EXTRA_MEDICINE_ID, id);
            PendingIntent missedPendingIntent = PendingIntent.getBroadcast(
                context, 1, missedIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(missedId, missedPendingIntent);
        } else {
            views.setViewVisibility(containerId, android.view.View.GONE);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the service to update widgets
        Intent serviceIntent = new Intent(context, MedicineWidgetService.class);
        serviceIntent.setAction(MedicineWidgetService.ACTION_UPDATE_WIDGETS);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Start service when first widget is created
        onUpdate(context, AppWidgetManager.getInstance(context), 
                getWidgetIds(context));
    }

    private int[] getWidgetIds(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, MedicineWidget.class);
        return manager.getAppWidgetIds(component);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        
        if (ACTION_TAKEN.equals(intent.getAction()) || ACTION_MISSED.equals(intent.getAction())) {
            String medicineId = intent.getStringExtra(EXTRA_MEDICINE_ID);
            if (medicineId != null) {
                Intent appIntent = new Intent(context, MainActivity.class);
                appIntent.setAction(intent.getAction());
                appIntent.putExtra(EXTRA_MEDICINE_ID, medicineId);
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(appIntent);
            }
        }
        
        // Update widgets using the now-public constant
        Intent serviceIntent = new Intent(context, MedicineWidgetService.class);
        serviceIntent.setAction(MedicineWidgetService.ACTION_UPDATE_WIDGETS);
        context.startService(serviceIntent);
    }

    private void openAppWithAction(Context context, String medicineId, String action) {
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setAction("ACTION_MEDICINE_" + action.toUpperCase());
        appIntent.putExtra(EXTRA_MEDICINE_ID, medicineId);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(appIntent);
        
        MedicineWidgetService.startActionUpdateWidgets(context);
    }
}