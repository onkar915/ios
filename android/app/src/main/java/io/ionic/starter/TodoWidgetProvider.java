package io.ionic.starter;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TodoWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "WidgetProvider";

    public static void forceWidgetUpdate(Context context, String jsonData) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("TodoPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("todos", jsonData).apply();
            
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, TodoWidgetProvider.class));
            
            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
            for (int id : ids) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.todo_widget);
                manager.updateAppWidget(id, views);
            }
            
            Log.d(TAG, "Widget forcefully updated with new data");
        } catch (Exception e) {
            Log.e(TAG, "Force update error", e);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("action")) {
            String action = intent.getStringExtra("action");
            int position = intent.getIntExtra("position", -1);
            
            SharedPreferences prefs = context.getSharedPreferences("TodoPrefs", Context.MODE_PRIVATE);
            String json = prefs.getString("todos", "[]");
            
            try {
                JSONArray array = new JSONArray(json);
                if (position >= 0 && position < array.length()) {
                    JSONObject todo = array.getJSONObject(position);
                    
                    if ("TOGGLE".equals(action)) {
                        todo.put("completed", !todo.getBoolean("completed"));
                    } else if ("DELETE".equals(action)) {
                        array.remove(position);
                    }
                    
                    prefs.edit().putString("todos", array.toString()).apply();
                    forceWidgetUpdate(context, array.toString());
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error processing action", e);
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.todo_widget);
            
            Intent serviceIntent = new Intent(context, TodoWidgetService.class);
            views.setRemoteAdapter(R.id.widget_list, serviceIntent);
            views.setEmptyView(R.id.widget_list, R.id.empty_view);
            
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}