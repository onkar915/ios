declare module '*.vue' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module '@capacitor/preferences' {
  interface Preferences {
    [x: string]: any;
    set(options: { key: string; value: string }): Promise<void>;
    get(options: { key: string }): Promise<{ value: string | null }>;
    remove(options: { key: string }): Promise<void>;
  }
  const Preferences: Preferences;
  export { Preferences };
}