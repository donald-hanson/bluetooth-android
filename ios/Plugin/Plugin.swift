import Foundation
import Capacitor
import ExternalAccessory
import UIKit

typealias JSObject = [String:Any]

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(BluetoothAndroidPlugin)
public class BluetoothAndroidPlugin: CAPPlugin {
    
    @objc func list(_ call: CAPPluginCall) {
        var devices = [JSObject]()
                
        let connectedAccessories = EAAccessoryManager.shared().connectedAccessories
        
        for connectedAccessory in connectedAccessories {
            var device = JSObject()
            
            device["name"] = connectedAccessory.name
            device["id"] = connectedAccessory.connectionID
            device["address"] = connectedAccessory.serialNumber
            
            devices.append(device)
        }
        
        call.success([
            "result": devices
        ])
    }
}
