var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { Plugins } from '@capacitor/core';
import 'dh-bluetooth-android';
const { BluetoothAndroidPlugin } = Plugins;
export class BluetoothAndroid {
    list() {
        return __awaiter(this, void 0, void 0, function* () {
            var list = yield BluetoothAndroidPlugin.list();
            return list.result;
        });
    }
    connect(id) {
        return __awaiter(this, void 0, void 0, function* () {
            yield BluetoothAndroidPlugin.connect({ id });
        });
    }
    disconnect(id) {
        return __awaiter(this, void 0, void 0, function* () {
            yield BluetoothAndroidPlugin.disconnect({ id });
        });
    }
    isConnected(id) {
        return __awaiter(this, void 0, void 0, function* () {
            var isConnected = yield BluetoothAndroidPlugin.isConnected({ id });
            return isConnected.result;
        });
    }
    write(id, data) {
        return __awaiter(this, void 0, void 0, function* () {
            yield BluetoothAndroidPlugin.write({ id, data });
        });
    }
    subscribe(id, callback) {
        return BluetoothAndroidPlugin.subscribe({ id }, callback);
    }
    unsubscribe(id, subscription) {
        return __awaiter(this, void 0, void 0, function* () {
            yield BluetoothAndroidPlugin.unsubscribe({ id, subscription });
        });
    }
}
//# sourceMappingURL=android.js.map