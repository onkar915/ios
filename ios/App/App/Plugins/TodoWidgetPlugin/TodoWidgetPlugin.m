#import <Capacitor/Capacitor.h>

CAP_PLUGIN(TodoWidgetPlugin, "TodoWidget",
           CAP_PLUGIN_METHOD(saveTodos, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(updateWidget, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getTodoData, CAPPluginReturnPromise);
)