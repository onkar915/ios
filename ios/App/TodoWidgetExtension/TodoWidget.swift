import WidgetKit
import SwiftUI

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> SimpleEntry {
        SimpleEntry(date: Date(), todos: [
            TodoItem(text: "Aspirin", completed: false, dateTime: "2024-01-15T10:00:00Z", id: 1),
            TodoItem(text: "Vitamin C", completed: true, dateTime: "2024-01-15T12:00:00Z", id: 2)
        ])
    }

    func getSnapshot(in context: Context, completion: @escaping (SimpleEntry) -> ()) {
        let entry = SimpleEntry(date: Date(), todos: [
            TodoItem(text: "Aspirin", completed: false, dateTime: "2024-01-15T10:00:00Z", id: 1),
            TodoItem(text: "Vitamin C", completed: true, dateTime: "2024-01-15T12:00:00Z", id: 2),
            TodoItem(text: "Pain Relief", completed: false, dateTime: "2024-01-15T15:00:00Z", id: 3)
        ])
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
        let entries = [SimpleEntry(date: Date(), todos: loadTodoData())]
        let timeline = Timeline(entries: entries, policy: .atEnd)
        completion(timeline)
    }
    
    private func loadTodoData() -> [TodoItem] {
        // For simulator testing, return hardcoded data
        return [
            TodoItem(text: "Aspirin", completed: false, dateTime: "2024-01-15T10:00:00Z", id: 1),
            TodoItem(text: "Vitamin C", completed: true, dateTime: "2024-01-15T12:00:00Z", id: 2),
            TodoItem(text: "Pain Relief", completed: false, dateTime: "2024-01-15T15:00:00Z", id: 3)
        ]
    }
}

struct SimpleEntry: TimelineEntry {
    let date: Date
    let todos: [TodoItem]
}

struct TodoItem: Decodable {
    let text: String
    let completed: Bool
    let dateTime: String
    let id: Int
}

struct TodoWidgetEntryView: View {
    var entry: Provider.Entry

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Medicines")
                .font(.headline)
                .bold()
                .foregroundColor(.blue)
            
            if entry.todos.isEmpty {
                Text("No medicines scheduled")
                    .font(.caption)
                    .foregroundColor(.gray)
            } else {
                ForEach(entry.todos.prefix(3), id: \.id) { todo in
                    HStack(spacing: 8) {
                        Image(systemName: todo.completed ? "checkmark.circle.fill" : "circle")
                            .foregroundColor(todo.completed ? .green : .gray)
                            .font(.system(size: 14))
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text(todo.text)
                                .font(.caption)
                                .lineLimit(1)
                                .strikethrough(todo.completed)
                            
                            if let date = isoDateFormatter.date(from: todo.dateTime) {
                                Text(dateFormatter.string(from: date))
                                    .font(.caption2)
                                    .foregroundColor(.gray)
                            }
                        }
                        
                        Spacer()
                    }
                }
            }
            
            Spacer()
            
            Text("Last updated: \(timeFormatter.string(from: Date()))")
                .font(.system(size: 8))
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color(.systemBackground))
    }
}

private let dateFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.timeStyle = .short
    formatter.dateStyle = .short
    return formatter
}()

private let timeFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.timeStyle = .medium
    return formatter
}()

private let isoDateFormatter: ISO8601DateFormatter = {
    let formatter = ISO8601DateFormatter()
    formatter.formatOptions = [.withInternetDateTime]
    return formatter
}()

struct TodoWidget: Widget {
    let kind: String = "TodoWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            TodoWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Medicine Widget")
        .description("Displays your medicine reminders.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

#if DEBUG
struct TodoWidget_Previews: PreviewProvider {
    static var previews: some View {
        TodoWidgetEntryView(entry: SimpleEntry(
            date: Date(),
            todos: [
                TodoItem(text: "Aspirin", completed: false, dateTime: "2024-01-15T10:00:00Z", id: 1),
                TodoItem(text: "Vitamin C", completed: true, dateTime: "2024-01-15T12:00:00Z", id: 2),
                TodoItem(text: "Pain Relief", completed: false, dateTime: "2024-01-15T15:00:00Z", id: 3)
            ]
        ))
        .previewContext(WidgetPreviewContext(family: .systemSmall))
        
        TodoWidgetEntryView(entry: SimpleEntry(
            date: Date(),
            todos: [
                TodoItem(text: "Aspirin", completed: false, dateTime: "2024-01-15T10:00:00Z", id: 1),
                TodoItem(text: "Vitamin C", completed: true, dateTime: "2024-01-15T12:00:00Z", id: 2)
            ]
        ))
        .previewContext(WidgetPreviewContext(family: .systemMedium))
    }
}
#endif