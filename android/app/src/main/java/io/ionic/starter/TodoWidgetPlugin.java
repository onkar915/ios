package io.ionic.starter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // <-- Add this import
import android.util.Log; // <-- Add this import
import android.widget.RemoteViews;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "TodoWidget")
public class TodoWidgetPlugin extends Plugin {
    private static final String TAG = "WidgetPlugin";

     @PluginMethod
    public void saveTodos(PluginCall call) {
        try {
            String todos = call.getString("todos", "[]");
            Context context = getContext();
            
            // 1. Save to SharedPreferences (blocking write)
            SharedPreferences prefs = context.getSharedPreferences("TodoPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("todos", todos).commit();
            
            // 2. Force immediate widget update
            Intent intent = new Intent(context, TodoWidgetProvider.class);
            intent.setAction("FORCE_UPDATE");
            context.sendBroadcast(intent);
            
            call.resolve();
        } catch (Exception e) {
            Log.e(TAG, "Save error", e);
            call.reject("Error saving todos", e);
        }
    }
    @PluginMethod
    public void updateWidget(PluginCall call) {
        try {
            updateAllWidgets(getContext());
            call.resolve();
        } catch (Exception e) {
            Log.e(TAG, "Update error", e);
            call.reject("Error updating widget", e);
        }
    }

    private void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, TodoWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
        
        // This refreshes the data
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        
        // This forces a UI refresh
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.todo_widget);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        
        Log.d(TAG, "Updated " + appWidgetIds.length + " widgets");
    }
}