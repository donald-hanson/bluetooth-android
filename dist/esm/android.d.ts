import { BluetoothDevice } from './definitions';
import 'dh-bluetooth-android';
export declare class BluetoothAndroid {
    list(): Promise<BluetoothDevice[]>;
    connect(id: string): Promise<void>;
    disconnect(id: string): Promise<void>;
    isConnected(id: string): Promise<boolean>;
    write(id: string, data: string): Promise<void>;
    subscribe(id: string, callback: (data: string, err?: any) => void): string;
    unsubscribe(id: string, subscription: string): Promise<void>;
    setDelimiter(id: string, delimiter: string): Promise<void>;
}
