/*
 * Hewlett-Packard Company
 * All rights reserved.
 *
 * This file, its contents, concepts, methods, behavior, and operation
 * (collectively the "Software") are protected by trade secret, patent,
 * and copyright laws. The use of the Software is governed by a license
 * agreement. Disclosure of the Software to third parties, in any form,
 * in whole or in part, is expressly prohibited except as authorized by
 * the license agreement.
 */

package com.hp.mss.hpprint.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.hp.mss.hpprint.R;

/**
 * The PrintPluginHelper displays a dialog that takes the user to the play store to get a print service plugin.
 */
public class PrintPluginHelper {

    /**
     * This interface allows us to track user interaction with the dialog and create the printJob.
     */
    public interface PluginHelperListener {
        public void printPluginHelperSkippedByPreference();
        public void printPluginHelperSkipped();
        public void printPluginHelperSelected();
        public void printPluginHelperCanceled();
    }
    private PluginHelperListener pluginHelperListener;

    private static final String SHOW_PLUGIN_HELPER_KEY = "com.hp.mss.hpprint.ShowPluginHelper";

    public static void showPluginHelper(final Activity activity,final PluginHelperListener pluginHelperListener) {
        String header = activity.getString(R.string.hp_print_helper_header);
        String message = activity.getString(R.string.install_print_plugin_msg);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        boolean showPluginInstallMessage = preferences.getBoolean(SHOW_PLUGIN_HELPER_KEY, true);
        if (!showPluginInstallMessage) {
            if (pluginHelperListener != null) {
                pluginHelperListener.printPluginHelperSkippedByPreference();
            }
            return;
        }
        View checkBoxView = View.inflate(new ContextThemeWrapper(activity, R.style.printPluginHelperDialogCheckBox), R.layout.checkbox, null);
        final CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setText("Do not show again.");
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.printPluginHelperDialog));
        final AlertDialog pluginDialog = builder.setMessage(message)
                .setTitle(header)
                .setView(checkBoxView)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (pluginHelperListener != null)
                            pluginHelperListener.printPluginHelperCanceled();
                    }
                })
                .setPositiveButton("Get a Print Service", null)
                .setNeutralButton("I have one", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkBox.isChecked()) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                            preferences.edit().putBoolean(SHOW_PLUGIN_HELPER_KEY, false).commit();
                        }
                        if (pluginHelperListener != null)
                            pluginHelperListener.printPluginHelperSkipped();
                    }
                })
                .create();
//        This is to prevent the dialog from closing when user goes to play store
        pluginDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = pluginDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkBox.isChecked()) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                            preferences.edit().putBoolean(SHOW_PLUGIN_HELPER_KEY, false).commit();
                        }
                        openPlayStore(activity);
                        if (pluginHelperListener != null)
                            pluginHelperListener.printPluginHelperSelected();
                    }
                });

            }
        });
        pluginDialog.show();

    }
    public static void openPlayStore(Activity activity) {
        String url = PrintUtil.PLAY_STORE_PRINT_SERVICES_URL;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

}