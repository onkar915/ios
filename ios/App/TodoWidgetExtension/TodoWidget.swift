import WidgetKit
import SwiftUI

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> SimpleEntry {
        SimpleEntry(date: Date(), todos: [])
    }

    func getSnapshot(in context: Context, completion: @escaping (SimpleEntry) -> ()) {
        let entry = SimpleEntry(date: Date(), todos: loadTodoData())
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
        let entries = [SimpleEntry(date: Date(), todos: loadTodoData())]
        let timeline = Timeline(entries: entries, policy: .atEnd)
        completion(timeline)
    }
    
    private func loadTodoData() -> [TodoItem] {
        if let sharedDefaults = UserDefaults(suiteName: "group.io.ionic.starter.widget"),
           let todosData = sharedDefaults.string(forKey: "todos"),
           let data = todosData.data(using: .utf8) {
            
            do {
                let todoItems = try JSONDecoder().decode([TodoItem].self, from: data)
                return todoItems
            } catch {
                print("Error decoding todos: \(error)")
                return []
            }
        }
        return []
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
            
            if entry.todos.isEmpty {
                Text("No medicines scheduled")
                    .font(.caption)
                    .foregroundColor(.gray)
            } else {
                ForEach(entry.todos.prefix(3), id: \.id) { todo in
                    HStack(spacing: 8) {
                        Image(systemName: todo.completed ? "checkmark.circle.fill" : "circle")
                            .foregroundColor(todo.completed ? .green : .gray)
                        
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
                    }
                }
            }
        }
        .padding()
        .containerBackground(for: .widget) {
            Color(.systemBackground)
        }
    }
}

private let dateFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.timeStyle = .short
    formatter.dateStyle = .short
    return formatter
}()

private let isoDateFormatter: ISO8601DateFormatter = {
    let formatter = ISO8601DateFormatter()
    formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
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