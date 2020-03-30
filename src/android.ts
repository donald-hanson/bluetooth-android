import { BluetoothDevice } from './definitions';
import { Plugins } from '@capacitor/core';
import 'dh-bluetooth-android';
const {BluetoothAndroidPlugin} = Plugins;

export class BluetoothAndroid {
    public async list(): Promise<BluetoothDevice[]> {
        var list = await BluetoothAndroidPlugin.list();
        return list.result;
    }

    public async connect(id: string): Promise<void> {
        await BluetoothAndroidPlugin.connect({id});
    }

    public async disconnect(id: string): Promise<void> {
        await BluetoothAndroidPlugin.disconnect({id});
    }

    public async isConnected(id: string): Promise<boolean> {
        var isConnected = await BluetoothAndroidPlugin.isConnected({id});
        return isConnected.result;
    }

    public async write(id: string, data:string): Promise<void> {
        await BluetoothAndroidPlugin.write({id, data});
    }

    public subscribe(id: string, callback: (data:string, err?:any) => void) : string {
        return BluetoothAndroidPlugin.subscribe({id}, (data:{result:string},err?:any) => {
            callback(data?.result, err);
        });
    }

    public async unsubscribe(id: string, subscription: string): Promise<void> {
        await BluetoothAndroidPlugin.unsubscribe({id,subscription});
    }
}