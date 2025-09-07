package io.ionic.starter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class TodoRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "WidgetFactory";
    private final Context context;
    private List<TodoItem> items = new ArrayList<>();

    public TodoRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        loadData();
    }

    private void loadData() {
        items.clear();
        SharedPreferences prefs = context.getSharedPreferences("TodoPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("todos", "[]");
        
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject todo = array.getJSONObject(i);
                items.add(new TodoItem(
                    todo.getString("text"),
                    todo.optBoolean("completed", false)
                ));
            }
            Log.d(TAG, "Loaded " + items.size() + " todos");
        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
            items.add(new TodoItem("Error loading data", false));
        }
    }

   @Override
public RemoteViews getViewAt(int position) {
    if (position >= items.size()) {
        return null;
    }
    
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_todo_item);
    TodoItem item = items.get(position);
    
    // Set todo text
    views.setTextViewText(R.id.todo_text, item.getText());
    
    // Set completion state - change background color
    views.setInt(R.id.complete_checkbox, "setBackgroundResource", 
        item.isCompleted() ? R.drawable.circle_bg_green : R.drawable.circle_bg_gray);
    
    // Set click intents
    Intent toggleIntent = new Intent();
    toggleIntent.putExtra("action", "TOGGLE");
    toggleIntent.putExtra("position", position);
    views.setOnClickFillInIntent(R.id.complete_checkbox, toggleIntent);
    
    Intent deleteIntent = new Intent();
    deleteIntent.putExtra("action", "DELETE");
    deleteIntent.putExtra("position", position);
    views.setOnClickFillInIntent(R.id.delete_button, deleteIntent);
    
    return views;
}

    @Override 
    public int getCount() { 
        return items.size(); 
    }
    
    @Override
    public void onDataSetChanged() { 
        loadData();
    }
    
    @Override 
    public int getViewTypeCount() { 
        return 1; 
    }
    
    @Override 
    public RemoteViews getLoadingView() { 
        return null; 
    }
    
    @Override 
    public long getItemId(int position) { 
        return position; 
    }
    
    @Override 
    public boolean hasStableIds() { 
        return true; 
    }
    
    @Override 
    public void onDestroy() { 
        items.clear(); 
    }
}