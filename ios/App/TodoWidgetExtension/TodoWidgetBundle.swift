import WidgetKit
import SwiftUI

@main
struct TodoWidgetBundle: WidgetBundle {
    @WidgetBundleBuilder
    var body: some Widget {
        TodoWidget()
    }
}