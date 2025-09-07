import { Capacitor, registerPlugin } from '@capacitor/core';

export interface CalendarPlugin {
  openCalendar(options: { time: number }): Promise<{ success: boolean }>;
}

const Calendar = registerPlugin<CalendarPlugin>('Calendar');
export { Calendar };