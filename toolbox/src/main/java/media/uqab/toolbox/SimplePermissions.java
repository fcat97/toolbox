/*
 MIT LICENSE

 Copyright [2023] [Shahriar Zaman]

 Permission is hereby granted, free of charge, to any person obtaining a
 copy of this software and associated documentation files (the “Software”),
 to deal in the Software without restriction, including without
 limitation the rights to use, copy, modify, merge, publish, distribute,
 sublicense, and/or sell copies of the Software, and to permit persons to
 whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


package com.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Map;

/**
 * A simple permission handler for android.
 * Get instance of this class with [register] method.
 *
 * @author github/fCat97
 */
public class SimplePermissions {
    private final ActivityResultLauncher<String[]> launcher;
    private Func onGranted;
    private Func onRejected;

    private SimplePermissions(ComponentActivity activity) {
        launcher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handleResult
        );
    }

    private SimplePermissions(Fragment fragment) {
        launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handleResult
        );
    }

    private void handleResult(Map<String, Boolean> permissions) {
        ArrayList<String> granted = new ArrayList<>();
        ArrayList<String> rejected = new ArrayList<>();

        for (String key: permissions.keySet()) {
            if (Boolean.TRUE.equals(permissions.get(key))) {
                granted.add(key);
            } else {
                rejected.add(key);
            }
        }

        if (onRejected != null) onRejected.apply(rejected.toArray(new String[]{}));
        if (onGranted != null) onGranted.apply(granted.toArray(new String[]{}));
    }

    /**
     * Request a set of permissions.
     *
     * @param permissions [Manifest.permission]s to request
     * @param onGranted callback when any permission is granted
     * @param onRejected callback when any permission is rejected
     */
    public void request(
        String[] permissions,
        Func onRejected,
        Func onGranted
    ) {
        this.onGranted = onGranted;
        this.onRejected = onRejected;
        launcher.launch(permissions);
    }

    /**
     * Check if a permission is granted or not
     */
    public static boolean isGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if a set of permissions are granted or not
     */
    public static boolean areGranted(Context context, String[] permissions) {
        for (String p: permissions) {
            if (!isGranted(context, p)) return false;
        }
        return true;
    }

    public static SimplePermissions register(ComponentActivity activity) {
        return new SimplePermissions(activity);
    }

    public static SimplePermissions register(Fragment fragment) {
        return new SimplePermissions(fragment);
    }

    /**
     * Check if the permission is available on running android version
     *
     * Complete list of permissions is here:
     * <a href="https://developer.android.com/reference/android/Manifest.permission">Manifest.permission</a>
     */
    @SuppressWarnings("ALL")
    public static boolean isAvailable(String permission) {
        int sdk = Build.VERSION.SDK_INT;
        boolean r;

        try {
            switch (permission) {
                case Manifest.permission.ACCESS_NETWORK_STATE: {
                    r = sdk >= 1;
                    break;
                }
                case Manifest.permission.CAMERA: {
                    r =  sdk >= 1;
                    break;
                }
                case Manifest.permission.MODIFY_AUDIO_SETTINGS: {
                    r =  sdk >= 1;
                    break;
                }
                case Manifest.permission.RECORD_AUDIO: {
                    r =  sdk >= 1;
                    break;
                }

                // added from 23 --------------------------------------
                case Manifest.permission.WRITE_EXTERNAL_STORAGE: {
                    r = sdk >= 23 && sdk < 33;
                    break;
                }

                // added from 28----------------------------------------
                case Manifest.permission.FOREGROUND_SERVICE: {
                    r =  sdk >= 28;
                    break;
                }

                // added from 33----------------------------------------
                case Manifest.permission.POST_NOTIFICATIONS: {
                    r =  sdk >= 33;
                    break;
                }

                // add more here if needed
                default: {
                    r =  false;
                }
            }
        } catch (Exception e) {
            // if the permission is not available on this android version
            r = false;
        }

        return r;
    }

    public interface Func {
        void apply(String[] strings);
    }
}