import Capacitor
import Foundation
import WidgetKit

@objc(TodoWidgetPlugin)
public class TodoWidgetPlugin: CAPPlugin {
    private let appGroupId = "group.io.ionic.starter.widget"
    
    @objc func saveTodos(_ call: CAPPluginCall) {
        guard let todosString = call.getString("todos") else {
            call.reject("No todos provided")
            return
        }
        
        if let sharedDefaults = UserDefaults(suiteName: appGroupId) {
            sharedDefaults.set(todosString, forKey: "todos")
            sharedDefaults.synchronize()
            
            // Reload widget timeline
            WidgetCenter.shared.reloadAllTimelines()
            
            call.resolve(["success": true])
        } else {
            call.reject("Failed to access app group")
        }
    }
    
    @objc func updateWidget(_ call: CAPPluginCall) {
        WidgetCenter.shared.reloadAllTimelines()
        call.resolve(["success": true])
    }
    
    @objc func getTodoData(_ call: CAPPluginCall) {
        if let sharedDefaults = UserDefaults(suiteName: appGroupId),
           let todosString = sharedDefaults.string(forKey: "todos") {
            call.resolve(["todos": todosString])
        } else {
            call.resolve(["todos": "[]"])
        }
    }
}