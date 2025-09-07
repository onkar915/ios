import { registerPlugin } from '@capacitor/core';

export interface TodoWidgetPlugin {
  getTodoData(): Promise<{ todos: string[] }>;
  updateWidget(): Promise<void>;
  saveTodos(options: { todos: string }): Promise<void>;
}

const TodoWidget = registerPlugin<TodoWidgetPlugin>('TodoWidget');

export default TodoWidget;