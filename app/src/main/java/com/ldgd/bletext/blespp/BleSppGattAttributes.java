/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ldgd.bletext.blespp;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class BleSppGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    //B-0002/B-0004/TRL-01
//    Service UUID：fee0
//    Notify：fee1
//    Write:fee1
    public static String BLE_SPP_Service = "0000fee0-0000-1000-8000-00805f9b34fb";
    public static String BLE_SPP_Notify_Characteristic = "0000fee1-0000-1000-8000-00805f9b34fb";
    public static String  BLE_SPP_Write_Characteristic = "0000fee1-0000-1000-8000-00805f9b34fb";
    public static String  BLE_SPP_AT_Characteristic = "0000fee2-0000-1000-8000-00805f9b34fb";
    static {
        //B-0002/B-0004/TRL-01 SPP Service
        attributes.put(BLE_SPP_Service, "BLE SPP Service");
        attributes.put(BLE_SPP_Notify_Characteristic, "BLE SPP Notify Characteristic");
        attributes.put(BLE_SPP_Write_Characteristic, "BLE SPP Write Characteristic");
        attributes.put(BLE_SPP_AT_Characteristic, "BLE SPP AT Characteristic");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
