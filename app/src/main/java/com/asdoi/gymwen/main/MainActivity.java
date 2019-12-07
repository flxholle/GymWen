package com.asdoi.gymwen.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.asdoi.gymwen.ActivityFeatures;
import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.main.Fragments.VertretungFragment;
import com.asdoi.gymwen.vertretungsplanInternal.VertretungsPlanFeatures;
import com.commit451.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import com.commit451.modalbottomsheetdialogfragment.Option;
import com.github.javiersantos.appupdater.enums.Display;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends ActivityFeatures implements NavigationView.OnNavigationItemSelectedListener, ModalBottomSheetDialogFragment.Listener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    VertretungFragment lastLoadedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
//        supportActionBar?drawer.setDisplayHomeAsUpEnabled(true);
//        supportActionBar?.setHomeButtonEnabled(true);
        drawer.addDrawerListener(toggle);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_today, R.id.nav_tomorrow, R.id.nav_settings,
                R.id.nav_all_classes_today, R.id.nav_all_classes_tomorrow)
                .setDrawerLayout(drawer)
                .build();
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController)

        navigationView.setNavigationItemSelectedListener(this);

//        VertretungsPlanFeatures.reloadDocs();
//        VertretungsPlanFeatures.refresh();

        if (!VertretungsPlanFeatures.isUninit())
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        toggle.syncState();

        if (!ApplicationFeatures.initSettings(false, true)) {
            finish();
        }
        checkUpdates(Display.DIALOG, false);
        showChangelogCK(true);
//        showChanglogTonny();
    }


    @Override
    public void onPostCreate(Bundle b) {
        super.onPostCreate(b);
        toggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        /*ModalBottomSheetDialogFragment.Builder mb = new ModalBottomSheetDialogFragment.Builder();
        mb.add(R.menu.menu_main).show(getSupportFragmentManager(), "options");
        mb.show(getSupportFragmentManager(),"s");*/
        return true;
    }

    @Override
    public void onModalOptionSelected(String s, Option o) {
//        onOptionsItemSelected();
        onOptionsItemSelected(new MenuItem() {
            @Override
            public int getItemId() {
                return o.getId();
            }

            @Override
            public int getGroupId() {
                return 0;
            }

            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public MenuItem setTitle(CharSequence charSequence) {
                return null;
            }

            @Override
            public MenuItem setTitle(int i) {
                return null;
            }

            @Override
            public CharSequence getTitle() {
                return null;
            }

            @Override
            public MenuItem setTitleCondensed(CharSequence charSequence) {
                return null;
            }

            @Override
            public CharSequence getTitleCondensed() {
                return null;
            }

            @Override
            public MenuItem setIcon(Drawable drawable) {
                return null;
            }

            @Override
            public MenuItem setIcon(int i) {
                return null;
            }

            @Override
            public Drawable getIcon() {
                return null;
            }

            @Override
            public MenuItem setIntent(Intent intent) {
                return null;
            }

            @Override
            public Intent getIntent() {
                return null;
            }

            @Override
            public MenuItem setShortcut(char c, char c1) {
                return null;
            }

            @Override
            public MenuItem setNumericShortcut(char c) {
                return null;
            }

            @Override
            public char getNumericShortcut() {
                return 0;
            }

            @Override
            public MenuItem setAlphabeticShortcut(char c) {
                return null;
            }

            @Override
            public char getAlphabeticShortcut() {
                return 0;
            }

            @Override
            public MenuItem setCheckable(boolean b) {
                return null;
            }

            @Override
            public boolean isCheckable() {
                return false;
            }

            @Override
            public MenuItem setChecked(boolean b) {
                return null;
            }

            @Override
            public boolean isChecked() {
                return false;
            }

            @Override
            public MenuItem setVisible(boolean b) {
                return null;
            }

            @Override
            public boolean isVisible() {
                return false;
            }

            @Override
            public MenuItem setEnabled(boolean b) {
                return null;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public boolean hasSubMenu() {
                return false;
            }

            @Override
            public SubMenu getSubMenu() {
                return null;
            }

            @Override
            public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
                return null;
            }

            @Override
            public ContextMenu.ContextMenuInfo getMenuInfo() {
                return null;
            }

            @Override
            public void setShowAsAction(int i) {

            }

            @Override
            public MenuItem setShowAsActionFlags(int i) {
                return null;
            }

            @Override
            public MenuItem setActionView(View view) {
                return null;
            }

            @Override
            public MenuItem setActionView(int i) {
                return null;
            }

            @Override
            public View getActionView() {
                return null;
            }

            @Override
            public MenuItem setActionProvider(ActionProvider actionProvider) {
                return null;
            }

            @Override
            public ActionProvider getActionProvider() {
                return null;
            }

            @Override
            public boolean expandActionView() {
                return false;
            }

            @Override
            public boolean collapseActionView() {
                return false;
            }

            @Override
            public boolean isActionViewExpanded() {
                return false;
            }

            @Override
            public MenuItem setOnActionExpandListener(OnActionExpandListener onActionExpandListener) {
                return null;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        VertretungFragment fragment = null;
        Intent intent = null;
        String itemTitle = "" + item.getTitle();

        switch (id) {
            case R.id.nav_both:
                fragment = new VertretungFragment(true);
                break;
            case R.id.nav_today:
                fragment = new VertretungFragment(true, false);
                break;
            case R.id.nav_all_classes_today:
                fragment = new VertretungFragment(true, true);
                break;
            case R.id.nav_tomorrow:
                fragment = new VertretungFragment(false, false);
                break;
            case R.id.nav_all_classes_tomorrow:
                fragment = new VertretungFragment(false, true);
                break;
            case R.id.action_settings: //Fallthrough
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                drawer.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_website:
                intent = new Intent(this, WebsiteActivity.class);
//                intent.putExtra("url","gym-wen.de/information/unsere-schule/");
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_mebis:
                tabIntent("https://lernplattform.mebis.bayern.de/my/");
                break;
            case R.id.nav_mensa:
                String packageName = "de.eezzy.admin.apnr40";
                intent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent == null) {
                    tabIntent("https://www.kitafino.de/sys_k2/index.php?action=bestellen");
                } else {
                    startActivity(intent);
                }
                break;
            case R.id.nav_shop:
                tabIntent("http://shop.apromote-werbemittel.de/");
                break;
            case R.id.action_impressum: // Fallthrough
                intent = new Intent(this, ImpressumActivity.class);
                startActivity(intent);
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.action_refresh2:
            case R.id.action_refresh:
                item.setTitle(getSupportActionBar().getTitle());
                VertretungsPlanFeatures.setDocs(null, null);
                if (lastLoadedFragment == null)
                    fragment = new VertretungFragment(true);
                else {
                    if (VertretungFragment.both)
                        fragment = new VertretungFragment(true);
                    else
                        fragment = new VertretungFragment(VertretungFragment.today, VertretungFragment.all);
                }
                break;
            /*case R.id.nav_gradesManagement:
//                createFile("text/plain","test.txt");
//                downloadFile("https://gitlab.com/asdoi/colorrush/raw/master/VersionFile.txt");
                Snackbar snackbar = Snackbar.make(findViewById(R.id.nav_host_fragment), "Dieses Feature ist noch nicht benutzbar", Snackbar.LENGTH_SHORT);
                snackbar.show();
                break;*/
            case R.id.action_update:
                checkUpdates(Display.DIALOG, true);
                break;
            case R.id.action_changelog:
                showChangelogCK(false);
                break;
            default:
//                fragment = new All_Classes_today();
                break;

        }

        lastLoadedFragment = fragment;

        if (fragment != null) {
            if (!item.getTitle().toString().trim().isEmpty())
                getSupportActionBar().setTitle(item.getTitle());
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
        }

        item.setTitle(itemTitle);

        ApplicationFeatures.proofeNotification();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onNavigationItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    private static boolean pressedBack = false;

    @Override
    public void onBackPressed() {
        if (pressedBack) {
            finish();
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());

        } else {
            Toast.makeText(getApplicationContext(), R.string.back_button, Toast.LENGTH_LONG).show();
            pressedBack = true;
        }
        VertretungsPlanFeatures.saveDocs();
    }
}
