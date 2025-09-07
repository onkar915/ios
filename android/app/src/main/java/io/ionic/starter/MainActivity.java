package io.ionic.starter;

import android.webkit.JavascriptInterface;
import com.getcapacitor.BridgeActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class MainActivity extends BridgeActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        this.bridge.getWebView().addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void updateWidgetData(String jsonData) {
                TodoWidgetProvider.forceWidgetUpdate(MainActivity.this, jsonData);
            }
            
            @JavascriptInterface
            public void openCalendarForEvent(long startTime, String title, String description) {
                try {
                    // Open calendar with pre-filled event details
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra("beginTime", startTime);
                    intent.putExtra("endTime", startTime + 3600000); // 1 hour later
                    intent.putExtra("title", title);
                    intent.putExtra("description", description);
                    intent.putExtra("allDay", false);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("Calendar", "Error opening calendar for event", e);
                    
                    // Fallback: open calendar without event details
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("content://com.android.calendar/time/" + startTime));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e2) {
                        Log.e("Calendar", "Error with fallback calendar opening", e2);
                    }
                }
            }
            
            @JavascriptInterface
            public void openCalendar() {
                try {
                    // Open calendar app to current time
                    long currentTime = System.currentTimeMillis();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("content://com.android.calendar/time/" + currentTime));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("Calendar", "Error opening calendar", e);
                }
            }
        }, "AndroidBridge");
    }
}