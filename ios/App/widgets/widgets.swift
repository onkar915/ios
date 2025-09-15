import WidgetKit
import SwiftUI

struct Provider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> SimpleEntry {
        SimpleEntry(date: Date(), configuration: ConfigurationAppIntent())
    }

    func snapshot(for configuration: ConfigurationAppIntent, in context: Context) async -> SimpleEntry {
        SimpleEntry(date: Date(), configuration: configuration)
    }
    
    func timeline(for configuration: ConfigurationAppIntent, in context: Context) async -> Timeline<SimpleEntry> {
        let entry = SimpleEntry(date: Date(), configuration: configuration)
        return Timeline(entries: [entry], policy: .never)
    }
}

struct SimpleEntry: TimelineEntry {
    let date: Date
    let configuration: ConfigurationAppIntent
}

struct Medication: Identifiable {
    let id = UUID()
    let name: String
    let dosage: String
    let isTaken: Bool
    let time: Date
}

struct widgetsEntryView: View {
    var entry: Provider.Entry
    
    // Hardcoded medication data
    private var medications: [Medication] {
        [
            Medication(name: "Pilate", dosage: "10mg", isTaken: true, time: Date()),
            Medication(name: "Lysteda", dosage: "10mg", isTaken: false, time: Date())
        ]
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Medication Tracker")
                .font(.headline)
                .foregroundColor(.primary)
            
            ForEach(medications) { medication in
                HStack(spacing: 12) {
                    // Status indicator (green tick or red cross)
                    ZStack {
                        Circle()
                            .fill(medication.isTaken ? Color.green : Color.red)
                            .frame(width: 24, height: 24)
                        
                        Image(systemName: medication.isTaken ? "checkmark" : "xmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text(medication.name)
                            .font(.subheadline)
                            .foregroundColor(.primary)
                        
                        Text(medication.dosage)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    VStack(alignment: .trailing, spacing: 2) {
                        Text(medication.time, style: .time)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        
                        Text(medication.time, style: .date)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.vertical, 4)
            }
            
            Spacer()
            
            Text("Last updated: \(Date(), style: .time)")
                .font(.caption2)
                .foregroundColor(.gray)
        }
        .padding()
        .containerBackground(.fill.tertiary, for: .widget)
    }
}

struct widgets: Widget {
    let kind: String = "widgets"

    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: kind, intent: ConfigurationAppIntent.self, provider: Provider()) { entry in
            widgetsEntryView(entry: entry)
        }
        .configurationDisplayName("Medication Tracker")
        .description("Track your medication schedule")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

#Preview(as: .systemSmall) {
    widgets()
} timeline: {
    SimpleEntry(date: .now, configuration: ConfigurationAppIntent())
}

#Preview(as: .systemMedium) {
    widgets()
} timeline: {
    SimpleEntry(date: .now, configuration: ConfigurationAppIntent())
}
