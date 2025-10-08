package io.ionic.starter.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;  
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MedicineWidgetService extends IntentService {
    public static final String ACTION_UPDATE_WIDGETS = "io.ionic.starter.widget.action.UPDATE_WIDGETS";
    public MedicineWidgetService() {
        super("MedicineWidgetService");
    }

    public static void startActionUpdateWidgets(Context context) {
        Intent intent = new Intent(context, MedicineWidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGETS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_UPDATE_WIDGETS.equals(intent.getAction())) {
            updateAllWidgets();
        }
    }

   private void updateAllWidgets() {
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
        new ComponentName(this, MedicineWidget.class));
    
    // Get data from SharedPreferences
    SharedPreferences prefs = getSharedPreferences("MedicineTracker", Context.MODE_PRIVATE);
    String medicinesJson = prefs.getString("medicines", "[]");
    
    try {
        JSONArray medicines = new JSONArray(medicinesJson);
        JSONObject[] nextMedicines = getNextTwoMedicines(medicines);
        
        // Update all widgets
        for (int appWidgetId : appWidgetIds) {
            MedicineWidget.updateAppWidget(
                this,
                appWidgetManager,
                appWidgetId,
                nextMedicines[0] != null ? nextMedicines[0].optString("name") : null,
                nextMedicines[1] != null ? nextMedicines[1].optString("name") : null,
                nextMedicines[0] != null ? nextMedicines[0].optString("id") : null,
                nextMedicines[1] != null ? nextMedicines[1].optString("id") : null
            );
        }
    } catch (JSONException e) {
        Log.e("MedicineWidget", "Error parsing medicines", e);  
    }
}
    private JSONObject[] getNextTwoMedicines(JSONArray medicines) throws JSONException {
        JSONObject[] result = new JSONObject[2];
        long now = System.currentTimeMillis();
        
        for (int i = 0; i < medicines.length(); i++) {
            JSONObject med = medicines.getJSONObject(i);
            long scheduleTime = med.getLong("schedule");
            
            if (scheduleTime > now && scheduleTime < now + 12 * 60 * 60 * 1000) {
                if (result[0] == null) {
                    result[0] = med;
                } else if (result[1] == null) {
                    result[1] = med;
                    break;
                }
            }
        }
        return result;
    }
}