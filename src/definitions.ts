import { WebPlugin } from '@capacitor/core';

declare module "@capacitor/core" {
  interface PluginRegistry {
    BluetoothAndroidPlugin: BluetoothAndroidPlugin;
  }
}

export interface BluetoothDevice {
  class: number;
  id: string;
  address: string;
  name: string;
}

export interface BluetoothAndroidPluginResult<T> {
  result: T;
}

export interface BluetoothAndroidPlugin extends WebPlugin {
  list(): Promise<BluetoothAndroidPluginResult<BluetoothDevice[]>>;
  connect(id:string): Promise<BluetoothAndroidPluginResult<boolean>>;
  disconnect(id:string): Promise<void>;
  isConnected(id:string): Promise<BluetoothAndroidPluginResult<boolean>>;
  write(id:string, data:string): Promise<void>;
  // subscribe(id:string): Promise<BluetoothAndroidPluginResult<string>>;
  // unsubscribe(id:string): Promise<void>;
}
