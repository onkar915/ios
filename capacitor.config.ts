import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'io.ionic.starter',
  appName: 'medtodo',
  webDir: 'dist',
    ios: {
    "scheme": "widget"
  }
};

export default config;
