
import WidgetKit
import AppIntents

struct ConfigurationAppIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource { "Medication Tracker" }
    static var description: IntentDescription { "Track your medication schedule" }
}
