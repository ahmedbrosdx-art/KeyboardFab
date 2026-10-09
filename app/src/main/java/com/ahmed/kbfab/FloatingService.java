package com.ahmed.kbfab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingService extends Service {

    private WindowManager wm;
    private TextView fab;
    private LinearLayout panel;
    private EditText input;
    private WindowManager.LayoutParams fabLp;
    private boolean panelShown = false;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        startAsForeground();
        buildFab();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private int overlayType() {
        return Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void startAsForeground() {
        String ch = "kbfab";
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder nb;
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel(
                    ch, "الزر العائم", NotificationManager.IMPORTANCE_LOW));
            nb = new Notification.Builder(this, ch);
        } else {
            nb = new Notification.Builder(this);
        }
        nb.setContentTitle("الزر العائم شغّال")
          .setSmallIcon(android.R.drawable.ic_menu_edit);
        startForeground(1, nb.build());
    }

    private void buildFab() {
        fab = new TextView(this);
        fab.setText("⌨");
        fab.setTextSize(26);
        fab.setTextColor(Color.WHITE);
        fab.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#1E88E5"));
        fab.setBackground(bg);
        fab.setElevation(dp(6));

        int size = dp(56);
        fabLp = new WindowManager.LayoutParams(
                size, size, overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        fabLp.gravity = Gravity.TOP | Gravity.START;
        fabLp.x = dp(16);
        fabLp.y = dp(200);

        fab.setOnTouchListener(new android.view.View.OnTouchListener() {
            int startX, startY;
            float touchX, touchY;
            boolean moved;

            @Override
            public boolean onTouch(android.view.View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = fabLp.x; startY = fabLp.y;
                        touchX = e.getRawX(); touchY = e.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - touchX, dy = e.getRawY() - touchY;
                        if (Math.abs(dx) > dp(6) || Math.abs(dy) > dp(6)) moved = true;
                        if (moved) {
                            fabLp.x = startX + (int) dx;
                            fabLp.y = startY + (int) dy;
                            wm.updateViewLayout(fab, fabLp);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) togglePanel();
                        return true;
                }
                return false;
            }
        });

        wm.addView(fab, fabLp);
    }

    private void togglePanel() {
        if (panelShown) hidePanel(); else showPanel();
    }

    private void showPanel() {
        panel = new LinearLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    hidePanel();
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setBackgroundColor(Color.parseColor("#EEF2F5"));
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        panel.setGravity(Gravity.CENTER_VERTICAL);

        input = new EditText(this);
        input.setHint("اكتب هنا...");
        input.setSingleLine(false);
        input.setMaxLines(3);
        panel.addView(input, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button close = new Button(this);
        close.setText("✕");
        close.setOnClickListener(v -> hidePanel());
        panel.addView(close, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;

        wm.addView(panel, lp);
        panelShown = true;

        input.requestFocus();
        input.postDelayed(this::openKeyboard, 150);
        input.postDelayed(this::openKeyboard, 500);
    }

    private void openKeyboard() {
        if (input == null || !panelShown) return;
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hidePanel() {
        if (!panelShown) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        wm.removeView(panel);
        panel = null;
        input = null;
        panelShown = false;
    }

    @Override
    public void onDestroy() {
        hidePanel();
        if (fab != null) wm.removeView(fab);
        super.onDestroy();
    }
}
