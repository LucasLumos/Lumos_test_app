package com.example.lumosnew;

import java.util.HashMap;

/*
buttonChar: The on & off state of physical button
capChar: The value of the cap sense.
ledChar: The on & off state of the LED light
alsChar: The value of the ambient light sensor
lightChar: The value of the brightness of LED. Ranging from 255 to 0.
batteryChar: The value recording how long the product has been running.
 */

// This class contains available services and characteristics for the product
public class LumosServices {
    private static HashMap<String, String> attributes = new HashMap();

    // The UUID for the only service currently
    public static String lumosServiceUUID = "56f59bd1-dc9e-4447-9ba5-88d3c27c6281";

    public static String lumosRServiceUUID = "9ab00bd6-1ab9-4912-abc6-f1d44a0dd7a4";

    // Used for setting notification for characteristics
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    // The UUID for a series of characteristics
    public static String buttonCharUUID = "dc4f88dd-605e-4ac8-8b77-1a661d1e3a6e";
    public static String capCharUUID = "257ca050-123d-4a7b-9e9b-f4af13cdd209";
    public static String ledCharUUID = "a7c3dd87-2730-4457-b068-ae2c7517a39c";
    public static String alsCharUUID = "8ca9a6ee-548c-4aeb-9b01-f4c7aa0b3183";
    public static String lightCharUUID = "d2a6673c-abc8-41b1-a6b4-dd1bcb6f185d";
    public static String batteryCharUUID = "d4d5dac2-8e36-497e-877c-82cc32c098b3";

    // The UUID for a series of right pcb characteristics
    public static String RbuttonCharUUID = "d1799b98-e0da-491e-b6e0-b3557a7b0793";
    public static String RcapCharUUID = "a9c4734f-e490-4421-8142-daefdf9f0175";
    public static String RledCharUUID = "162e4caa-1692-402a-8a6c-556c08d6b3a9";
    public static String RalsCharUUID = "0788570d-0116-4cfc-9342-e5d016cfdb59";
    public static String RlightCharUUID = "3f5a21ed-83df-4e71-9507-a043114c95db";
    public static String RbatteryCharUUID = "f38f1739-f2e8-447c-a0fe-72d293cfdbee";
    public static String RLconnectedUUID = "13264462-3995-11eb-adc1-0242ac120002";

    // Add all characteristics to the hash map for later searching purpose
    static {
        attributes.put("dc4f88dd-605e-4ac8-8b77-1a661d1e3a6e", "buttonCharUUID");
        attributes.put("257ca050-123d-4a7b-9e9b-f4af13cdd209", "capCharUUID");
        attributes.put("a7c3dd87-2730-4457-b068-ae2c7517a39c", "ledCharUUID");
        attributes.put("8ca9a6ee-548c-4aeb-9b01-f4c7aa0b3183", "alsCharUUID");
        attributes.put("d2a6673c-abc8-41b1-a6b4-dd1bcb6f185d", "lightCharUUID");
        attributes.put("d4d5dac2-8e36-497e-877c-82cc32c098b3", "batteryCharUUID");
    }

}