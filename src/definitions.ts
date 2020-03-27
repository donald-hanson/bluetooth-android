declare module "@capacitor/core" {
  interface PluginRegistry {
    BluetoothAndroidPlugin: BluetoothAndroidPlugin;
  }
}

export interface BluetoothAndroidPlugin {
  echo(options: { value: string }): Promise<{value: string}>;
}
