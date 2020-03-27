import { WebPlugin } from '@capacitor/core';
import { BluetoothAndroidPlugin } from './definitions';

export class BluetoothAndroidPluginWeb extends WebPlugin implements BluetoothAndroidPlugin {
  constructor() {
    super({
      name: 'BluetoothAndroidPlugin',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const BluetoothAndroidPlugin = new BluetoothAndroidPluginWeb();

export { BluetoothAndroidPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(BluetoothAndroidPlugin);
