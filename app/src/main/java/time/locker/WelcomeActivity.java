package time.locker;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.Gravity;
import android.content.pm.CrossProfileApps;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends Activity {

    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
        getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("work", true).apply();						
    } }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);     
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 64, 64, 64);

        TextView description = new TextView(this);
        description.setGravity(Gravity.CENTER);
        description.setTextSize(16f);
        description.setTextColor(Color.WHITE);

        String language = Locale.getDefault().getLanguage();

        if (language.equals("ru")) {
            description.setText("Это приложение для блокировки телефона в ночное время с 10 вечера до 8 утра. Оно не будет давать вам включить экран в это время и обойти или отключить это будет почти невозможно. Включите администратора, а также рабочий профиль для надёжной работы.");
        } else {
            description.setText("This is an application for locking the phone at night from 10 PM to 8 AM. It will not let you turn on screen during this time, and bypass or disable this will be almost impossible. Enable the administrator and work profile for resilient operation.");
        }

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        description.setLayoutParams(textParams);
        LinearLayout buttonBox = new LinearLayout(this);
        buttonBox.setOrientation(LinearLayout.VERTICAL);
        buttonBox.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams buttonBoxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonBoxParams.setMargins(0, 64, 0, 0);
        buttonBox.setLayoutParams(buttonBoxParams);

        Button protectButton = createStyledButton(
                language.equals("ru") ? "Активировать админа" : "Activate admin"
        );

        protectButton.setOnClickListener(v -> {

            if (devicePolicyManager.isAdminActive(adminComponent)) {
                Toast.makeText(WelcomeActivity.this, 
                    language.equals("ru") ? "Админ уже активен" : "Admin already active", 
                Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);                
                startActivity(intent);
            }
            
        });

        Button workProfileButton = createStyledButton(
                language.equals("ru") ? "Создать рабочий профиль" : "Create a work profile"
        );

        workProfileButton.setOnClickListener(v -> {

            LauncherApps launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            
            if (launcherApps.getProfiles().size() > 1 || getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("work", false)) {
                Toast.makeText(
                        WelcomeActivity.this,
                        language.equals("ru")
                                ? "На этом устройстве уже есть рабочий профиль"
                                : "You already have a work profile on this device",
                        Toast.LENGTH_SHORT
                ).show();
            } else {            
            Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, new ComponentName(this, MyDeviceAdminReceiver.class));
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ALLOW_OFFLINE, true);
            startActivityForResult(intent, 100);
            }
                        
        });

        root.addView(description);
        buttonBox.addView(protectButton);
        buttonBox.addView(workProfileButton);
        root.addView(buttonBox);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    private Button createStyledButton(String text) {

        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(16f);
        button.setGravity(Gravity.CENTER);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(Color.parseColor("#d32f2f"));
        shape.setCornerRadius(6f);

        button.setBackground(shape);
        button.setPadding(32, 32, 32, 32);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);

        button.setLayoutParams(params);
        return button;
    }
}
