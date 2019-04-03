package tt.oo.mm;

import android.app.AndroidAppHelper;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Cover implements IXposedHookLoadPackage {

    double latitude = 30.660793;
    double longitude = 104.081008;
    String provider = LocationManager.NETWORK_PROVIDER;
    int lac = 39687;
    int cid = 163294727;
    int mcc = 460;

    private Location getLocation() {
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
                param.setResult(getCellLocation());
                XposedBridge.log("TOM hook TelephonyManager getCellLocation method after");
            }
        });

        // hook getAllCellInfo
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", mLpp.classLoader, "getAllCellInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(getCell(mcc, 0, lac, cid, 0, TelephonyManager.PHONE_TYPE_GSM));
                XposedBridge.log("TOM hook TelephonyManager getAllCellInfo method after");
            }
        });

        // hook TelephonyManager getAllCellInfo
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", mLpp.classLoader, "getAllCellInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(getCell(mcc, 0, lac, cid, 0, TelephonyManager.PHONE_TYPE_GSM));
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
                param.args[0] = getCell(mcc, 0, lac, cid, 0, TelephonyManager.PHONE_TYPE_GSM);
                XposedBridge.log("TOM hook PhoneStateListener onCellInfoChanged method before");
            }
        });

        // hook onCellLocationChanged
        XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", mLpp.classLoader, "onCellLocationChanged", CellLocation.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = getCellLocation();
                XposedBridge.log("TOM hook PhoneStateListener onCellLocationChanged method before");
            }
        });

        /**
         * android.location.LocationManager
         */

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
                param.setResult(getLocation());
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
                        originalLocationListener.onLocationChanged(getLocation());
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
            }
        });
    }
}
