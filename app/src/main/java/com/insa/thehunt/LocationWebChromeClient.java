package com.insa.thehunt;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


class LocationWebChromeClient extends WebChromeClient {
    private Activity mContext;
    //请求开启定位
    public static int REQUEST_CODE_ENABLE_LOCATION = 100;
    public static int REQUEST_CODE_ACCESS_LOCATION_PERMISSION = 101;

    private LocationWebChromeClientListener mLocationWebChromeClientListener;

    private GeolocationPermissions.Callback mGeolocationPermissionsCallback;
    private String mOrigin;

    private boolean mShowRequestPermissionRationale = false;

    public LocationWebChromeClient(final Activity activity) {
        this.mContext = activity;
        mLocationWebChromeClientListener = new LocationWebChromeClientListener() {
            @Override
            public boolean onReturnFromLocationSetting(int requestCode) {
                if (requestCode == REQUEST_CODE_ENABLE_LOCATION) {
                    if (mGeolocationPermissionsCallback != null) {
                        if (isEnabledLocationFunction()) {
                            mGeolocationPermissionsCallback.invoke(mOrigin, true, true);
                        } else {
                            //显然，从设置界面回来还没有开启定位服务，肯定是要拒绝定位了
                            Toast.makeText(mContext, "您拒绝了定位请求", Toast.LENGTH_SHORT).show();
                            mGeolocationPermissionsCallback.invoke(mOrigin, false, false);
                        }
                    }
                    return true;
                }
                return false;
            }

            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                boolean pass = true;
                for (Integer result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        pass = false;
                        break;
                    }
                }
                if (pass) {
                    onAccessLocationPermissionGranted();
                } else {
                    onAccessLocationPermissionRejected();
                }
            }

            public void onAccessLocationPermissionGranted() {
                doJudgeLocationServiceEnabled();
            }

            public void onAccessLocationPermissionRejected() {
                if (mShowRequestPermissionRationale) {
                    Toast.makeText(mContext, "您拒绝了定位请求", Toast.LENGTH_SHORT).show();
                    mGeolocationPermissionsCallback.invoke(mOrigin, false, false);
                } else {
                    doRequestAppSetting();
                }
            }
        };
    }

    private void doRequestAppSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("温馨提示");
        builder.setMessage(String.format("您禁止了应用获取当前位置的权限，是否前往开启？", mOrigin));
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent mIntent = new Intent();
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    mIntent.setData(Uri.fromParts("package", mContext.getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    mIntent.setAction(Intent.ACTION_VIEW);
                    mIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                    mIntent.putExtra("com.android.settings.ApplicationPkgName", mContext.getPackageName());
                }
                mContext.startActivity(mIntent);
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mGeolocationPermissionsCallback.invoke(mOrigin, false, false);
            }
        });
        builder.create().show();
    }

    public LocationWebChromeClientListener getLocationWebChromeClientListener() {
        return mLocationWebChromeClientListener;
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt();
    }


    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback geolocationPermissionsCallback) {
        this.mOrigin = origin;
        this.mGeolocationPermissionsCallback = geolocationPermissionsCallback;
        //是否拥有定位权限
        if (hasAccessLocationPermission()) {
            doJudgeLocationServiceEnabled();
        } else {
            //请求定位
            requestAccessLocationPermission();
        }
    }

    private void doJudgeLocationServiceEnabled() {
        //requesting permission to use your current location
        if (isEnabledLocationFunction()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Tips");
            builder.setMessage(String.format("Website%s，requesting permission to use your current location", mOrigin));
            builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mGeolocationPermissionsCallback.invoke(mOrigin, true, true);
                }
            });
            builder.setNegativeButton("Not allowed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mGeolocationPermissionsCallback.invoke(mOrigin, false, false);
                }
            });
            builder.create().show();
        } else {
            //请求开启定位功能
            requestEnableLocationFunction(mOrigin, mGeolocationPermissionsCallback);
        }
    }

    /**
     * Request to enable location services
     */
    private void requestEnableLocationFunction(final String origin, final GeolocationPermissions.Callback geolocationPermissionsCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Tips");
        builder.setMessage(String.format("Website%s，requesting permission to use your current location.", origin));
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivityForResult(intent, REQUEST_CODE_ENABLE_LOCATION);
            }
        });
        builder.setNegativeButton("Not allowed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                geolocationPermissionsCallback.invoke(origin, false, false);
            }
        });
        builder.create().show();
    }

    private boolean isEnabledLocationFunction() {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private boolean hasAccessLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestAccessLocationPermission() {
        // 是否要显示问什么要获取权限的解释界面
        /**
         * 什么情况下 shouldShowRequestPermissionRationale会返回true？
         * - 首次请求权限，但是用户禁止了，但是没有勾选“禁止后不再询问”，这样，之后的请求都会返回true
         * 什么情况下，shouldShowRequestPermissionRationale会返回false？
         * - 首次请求权限或者请求权限时，用户勾选了“禁止后不再询问”，之后的请求都会返回false
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                //请求过定位权限，但是被用户拒绝了（但是没有勾选“禁止后不再询问”）
                // 显示解释权限用途的界面，然后再继续请求权限
                mShowRequestPermissionRationale = true;
            } else {
                mShowRequestPermissionRationale = false;
            }
        } else {
            mShowRequestPermissionRationale = false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Tips");
        builder.setMessage(String.format("Website%s，requesting permission to use your current location.", mOrigin));
        builder.setPositiveButton(" Allow ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mContext.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_LOCATION_PERMISSION);
                } else {
                    doRequestAppSetting();
                }
            }
        });
        builder.setNegativeButton(" Not allowed ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mGeolocationPermissionsCallback.invoke(mOrigin, false, false);
            }
        });
        builder.create().show();
    }


    interface LocationWebChromeClientListener {

        /**
         * 用户从开启定位页面回来了
         */
        boolean onReturnFromLocationSetting(int requestCode);

        /**
         * 请求权限结果
         */
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }


}
