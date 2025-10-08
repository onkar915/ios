package io.ionic.starter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Handle potential early crashes
        try {
            // Handle initial intent
            handleIntent(getIntent());
        } catch (Exception e) {
            Log.e("MainActivity", "Startup crash prevented", e);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        try {
            if (intent != null && intent.getAction() != null) {
                final String medicineId = intent.getStringExtra("medicineId");
                final String action = getActionFromIntent(intent);
                
                if (!action.isEmpty() && medicineId != null) {
                    // Wait for bridge to be ready
                    getBridge().getWebView().post(() -> {
                        String js = String.format(
                            "window.dispatchEvent(new CustomEvent('widgetAction', { detail: { action: '%s', medicineId: '%s' } }));",
                            action, medicineId
                        );
                        getBridge().getWebView().evaluateJavascript(js, null);
                    });
                }
            }
        } catch (Exception e) {
            Log.e("IntentHandler", "Intent handling failed", e);
        }
    }

    private String getActionFromIntent(Intent intent) {
        if ("ACTION_MEDICINE_TAKEN".equals(intent.getAction())) {
            return "taken";
        } else if ("ACTION_MEDICINE_MISSED".equals(intent.getAction())) {
            return "missed";
        }
        return "";
    }
}