package tt.oo.mm;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Cover implements IXposedHookLoadPackage {
    String provider = LocationManager.GPS_PROVIDER;

    String fileDirectory = Environment.getExternalStoragePublicDirectory("Xposed_Modules").getAbsolutePath();
    String fileLatitudePath = fileDirectory + "/latitude.txt";
    String fileLongitudePath = fileDirectory + "/longitude.txt";


    // 北京
    double latitude = -1;
    double longitude = -1;

    // 可以不用
    int lac = 4566;
    int cid = 17952525;
    int mcc = 460;

    private double readLatitudeFromFile(Context context) {
        initDirectory();
        return readValueFromFile(context, fileLatitudePath);
    }

    private void saveLatitudeToFile(Context context, double value) {
        initDirectory();
        saveValueToFile(context, value, fileLatitudePath);
    }

    private double readLongitudeFromFile(Context context) {
        initDirectory();
        return readValueFromFile(context, fileLongitudePath);
    }

    private void saveLongitudeToFile(Context context, double value) {
        initDirectory();
        saveValueToFile(context, value, fileLongitudePath);
    }

    private void initDirectory() {
        File dir = new File(fileDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
            XposedBridge.log("FFFF create dir success: " + fileDirectory);
        }
    }

    private void saveValueToFile(Context context, double value, String path) {

        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
                XposedBridge.log("FFFF check file exist: " + path);
            } catch (IOException e) {
                e.printStackTrace();
                XposedBridge.log("FFFF check file exist fail: " + path);
                return;
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(value + "");
            bw.close();
            XposedBridge.log("FFFF write file value: " + value);
        } catch (Exception e) {
            e.printStackTrace();
            XposedBridge.log("FFFF write file value: " + value + " fail");
        }
    }

    private double readValueFromFile(Context context, String path) {
        File file = new File(path);
        if (!file.exists()) {
            XposedBridge.log("FFFF read file value: fail(no such file)");
            return -1;
        }

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            XposedBridge.log("FFFF read file value: fail");
            return -1;
        }

        try {
            XposedBridge.log("FFFF read file value: " + text.toString());
            return Double.parseDouble(text.toString());
        } catch (Exception e) {
            e.printStackTrace();
            XposedBridge.log("FFFF read file value: " + "(can not parse to double)");
            return -1;
        }
    }

    private Location getLocation(Context context) {

        // 更新经纬度的值
        latitude = readLatitudeFromFile(context);
        longitude = readLongitudeFromFile(context);

        Location location = new Location(provider);
        location.setAccuracy(100);
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }

        location.setTime(System.currentTimeMillis());

        return location;
    }

    private boolean noNeedToChangeLocation() {
        if (readLatitudeFromFile(AndroidAppHelper.currentApplication()) == -1 && readLongitudeFromFile(AndroidAppHelper.currentApplication()) == -1) {
            return true;
        }
        return false;
    }

    private CellLocation getCellLocation() {
        GsmCellLocation cellLocation = new GsmCellLocation();
        cellLocation.setLacAndCid(lac, cid);
        return cellLocation;
    }

    private ArrayList getCell(int mcc, int mnc, int lac, int cid, int sid, int networkType) {
        ArrayList arrayList = new ArrayList();
        CellInfoGsm cellInfoGsm = (CellInfoGsm) XposedHelpers.newInstance(CellInfoGsm.class);
        XposedHelpers.callMethod(cellInfoGsm, "setCellIdentity", XposedHelpers.newInstance(CellIdentityGsm.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(
                lac), Integer.valueOf(cid)}));
        CellInfoCdma cellInfoCdma = (CellInfoCdma) XposedHelpers.newInstance(CellInfoCdma.class);
        XposedHelpers.callMethod(cellInfoCdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityCdma.class, new Object[]{Integer.valueOf(lac), Integer.valueOf(sid), Integer.valueOf(cid), Integer.valueOf(0), Integer.valueOf(0)}));
        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) XposedHelpers.newInstance(CellInfoWcdma.class);
        XposedHelpers.callMethod(cellInfoWcdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityWcdma.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(lac), Integer.valueOf(cid), Integer.valueOf(300)}));
        CellInfoLte cellInfoLte = (CellInfoLte) XposedHelpers.newInstance(CellInfoLte.class);
        XposedHelpers.callMethod(cellInfoLte, "setCellIdentity", XposedHelpers.newInstance(CellIdentityLte.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(cid), Integer.valueOf(300), Integer.valueOf(lac)}));
        if (networkType == 1 || networkType == 2) {
            arrayList.add(cellInfoGsm);
        } else if (networkType == 13) {
            arrayList.add(cellInfoLte);
        } else if (networkType == 4 || networkType == 5 || networkType == 6 || networkType == 7 || networkType == 12 || networkType == 14) {
            arrayList.add(cellInfoCdma);
        } else if (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15) {
            arrayList.add(cellInfoWcdma);
        }
        return arrayList;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam mLpp) throws Throwable {
        XposedBridge.log("TOM Loaded Package Name:" + mLpp.packageName);

        /**
         * tt.oo.mm.LocationActivity
         */
        if (mLpp.packageName.equals("tt.oo.mm")) {
            XposedHelpers.findAndHookMethod("tt.oo.mm.LocationActivity", mLpp.classLoader, "setCurrentPosition", double.class, double.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    latitude = (double) param.args[0];
                    longitude = (double) param.args[1];

                    XposedBridge.log("TOM hook position before:" + latitude + " " + longitude);

                    // 保存经纬度的值
                    saveLatitudeToFile(AndroidAppHelper.currentApplication(), latitude);
                    saveLongitudeToFile(AndroidAppHelper.currentApplication(), longitude);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("TOM hook position after:" + latitude + " " + longitude);
                }
            });
        }

        /**
         * WifiManager
         */
        // hook getScanResults
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", mLpp.classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(new ArrayList<ScanResult>());
                XposedBridge.log("TOM hook WifiManager getScanResults method after");
            }
        });

        // hook isWifiEnabled
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", mLpp.classLoader, "isWifiEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
                XposedBridge.log("TOM hook WifiManager isWifiEnabled method after");
            }
        });

        // hook getWifiState
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", mLpp.classLoader, "getWifiState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(WifiManager.WIFI_STATE_DISABLED);
                XposedBridge.log("TOM hook WifiManager getWifiState method after");
            }
        });

        /**
         * android.telephony.TelephonyManager
         */
        // hook getCellLocation
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", mLpp.classLoader, "getCellLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
                XposedBridge.log("TOM hook TelephonyManager getCellLocation method after");
            }
        });

        // hook getAllCellInfo
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", mLpp.classLoader, "getAllCellInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
                XposedBridge.log("TOM hook TelephonyManager getAllCellInfo method after");
            }
        });


        // hook getPhoneCount
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", mLpp.classLoader, "getPhoneCount", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(1);
                XposedBridge.log("TOM hook TelephonyManager getPhoneCount method after");
            }
        });

        /**
         * PhoneStateListener
         */
        // hook TelephonyManager onCellInfoChanged
        XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", mLpp.classLoader, "onCellInfoChanged", List.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = null;//getCell(mcc, 0, lac, cid, 0, TelephonyManager.PHONE_TYPE_GSM);
                XposedBridge.log("TOM hook PhoneStateListener onCellInfoChanged method before");
            }
        });

        // hook onCellLocationChanged
        XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", mLpp.classLoader, "onCellLocationChanged", CellLocation.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = null;//getCellLocation();
                XposedBridge.log("TOM hook PhoneStateListener onCellLocationChanged method before");
            }
        });

        /**
         * android.location.LocationManager
         */

        // hook addGpsStatusListener
        XposedHelpers.findAndHookMethod(LocationManager.class, "addGpsStatusListener", GpsStatus.Listener.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("TOM hook LocationManager addGpsStatusListener method before");
                if (param.args[0] != null) {
                    XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 1);
                    XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 3);
                }
            }
        });

        // hook addNmeaListener
        XposedHelpers.findAndHookMethod(LocationManager.class, "addNmeaListener", GpsStatus.NmeaListener.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("TOM hook LocationManager addNmeaListener method before");
                param.setResult(false);
            }
        });

        // hook getBestProvider
        XposedHelpers.findAndHookMethod("android.location.LocationManager", mLpp.classLoader, "getBestProvider", Criteria.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(provider);
                XposedBridge.log("TOM hook LocationManager getBestProvider method after");
            }
        });

        // hook getProviders
        XposedHelpers.findAndHookMethod("android.location.LocationManager", mLpp.classLoader, "getProviders", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<String> list = new ArrayList<>();
                list.add(provider);
                param.setResult(list);
                XposedBridge.log("TOM hook LocationManager getProviders method after");
            }
        });

        // hook getProviders
        XposedHelpers.findAndHookMethod("android.location.LocationManager", mLpp.classLoader, "getProviders", Criteria.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<String> list = new ArrayList<>();
                list.add(provider);
                param.setResult(list);
                XposedBridge.log("TOM hook LocationManager getProviders method after");
            }
        });


        // hook getLastKnownLocation
        XposedHelpers.findAndHookMethod("android.location.LocationManager", mLpp.classLoader, "getLastKnownLocation", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (noNeedToChangeLocation()) {
                    return;
                }
                param.setResult(getLocation(AndroidAppHelper.currentApplication()));
                XposedBridge.log("TOM hook LocationManager getLastKnownLocation method after");
            }
        });

        // hook wrapListener
        XposedHelpers.findAndHookMethod("android.location.LocationManager", mLpp.classLoader, "wrapListener", LocationListener.class, Looper.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("TOM hook LocationManager wrapListener method before");
                final LocationListener originalLocationListener = (LocationListener) param.args[0];
                param.args[0] = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (noNeedToChangeLocation()) {
                            originalLocationListener.onLocationChanged(location);
                        } else {
                            originalLocationListener.onLocationChanged(getLocation(AndroidAppHelper.currentApplication()));
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };

                if (!noNeedToChangeLocation()) {
                    originalLocationListener.onLocationChanged(getLocation(AndroidAppHelper.currentApplication()));
                }
            }
        });
    }
}
