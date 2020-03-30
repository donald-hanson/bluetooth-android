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

export interface BluetoothAndroidPluginRequest {
  id: string;
}

export interface BluetoothAndroidPluginWriteRequest extends BluetoothAndroidPluginRequest {
  data: string;
}

export interface BluetoothAndroidPluginUnsubscribeRequest extends BluetoothAndroidPluginRequest {
  subscription: string;
}

export interface BluetoothAndroidPluginSetDelimiterRequest extends BluetoothAndroidPluginRequest {
  delimiter: string;
}

export type BluetoothAndroidMessageCallback = (data:string, err?: any) => void;

export interface BluetoothAndroidPlugin extends WebPlugin {
  list(): Promise<BluetoothAndroidPluginResult<BluetoothDevice[]>>;
  connect(request: BluetoothAndroidPluginRequest): Promise<BluetoothAndroidPluginResult<boolean>>;
  disconnect(request: BluetoothAndroidPluginRequest): Promise<void>;
  isConnected(request: BluetoothAndroidPluginRequest): Promise<BluetoothAndroidPluginResult<boolean>>;
  write(request: BluetoothAndroidPluginWriteRequest): Promise<void>;
  subscribe(request: BluetoothAndroidPluginRequest, callback: BluetoothAndroidMessageCallback): string;
  unsubscribe(request: BluetoothAndroidPluginUnsubscribeRequest): Promise<void>;
  setDelimiter(request: BluetoothAndroidPluginSetDelimiterRequest): Promise<void>;
}
