import { BluetoothDevice } from './definitions';
import 'dh-bluetooth-android';
export declare class BluetoothAndroid {
    list(): Promise<BluetoothDevice[]>;
    connect(id: string): Promise<void>;
    disconnect(id: string): Promise<void>;
    isConnected(id: string): Promise<boolean>;
    write(id: string, data: string): Promise<void>;
}
