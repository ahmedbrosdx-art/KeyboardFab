package com.ahmed.kbfab;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        root.setPadding(p, p, p, p);

        TextView info = new TextView(this);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        info.setText("1) اضغط \"السماح بالظهور فوق التطبيقات\" وفعّل الخيار\n"
                + "2) ارجع هنا واضغط \"تشغيل الزر العائم\"\n"
                + "3) افتح أي تطبيق واضغط الزر ⌨ يطلع الكيبورد");
        root.addView(info);

        Button perm = new Button(this);
        perm.setText("السماح بالظهور فوق التطبيقات");
        perm.setOnClickListener(v -> startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()))));
        root.addView(perm);

        Button start = new Button(this);
        start.setText("تشغيل الزر العائم");
        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                perm.performClick();
                return;
            }
            Intent i = new Intent(this, FloatingService.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
            else startService(i);
        });
        root.addView(start);

        Button stop = new Button(this);
        stop.setText("إيقاف الزر العائم");
        stop.setOnClickListener(v -> stopService(new Intent(this, FloatingService.class)));
        root.addView(stop);

        setContentView(root);
    }
}
