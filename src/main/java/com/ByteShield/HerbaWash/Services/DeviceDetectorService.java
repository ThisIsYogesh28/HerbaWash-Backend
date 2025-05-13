package com.ByteShield.HerbaWash.Services;

public class DeviceDetectorService {
    public static String detectDeviceType(String userAgent){
        if (userAgent==null)
            return "Unknown";
        if (userAgent.contains("mobile"))
            return "Mobile";
        else if (userAgent.contains("tablet"))
            return "Tablet";
        else return "Desktop";

    }
}
