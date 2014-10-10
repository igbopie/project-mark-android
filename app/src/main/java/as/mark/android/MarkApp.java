package as.mark.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by igbopie on 10/9/14.
 */
public class MarkApp  extends Application {

    private static Context context;

    public static final String SHARED_PREF = "myprefe";
    public static final String SHARED_PREF_USERNAME = "username";
    public static final String SHARED_PREF_PASSWORD = "password";
    public static final String SHARED_PREF_TOKEN = "token";
    public static final String SHARED_PREF_AUTHENTICATED = "authenticated";
    public static final String SHARED_PREF_GCM_TOKEN = "gcmToken";
    public static final String SHARED_PREF_LAST_APP_VERSION = "lastAppVersion";


    public void onCreate(){
        super.onCreate();
        MarkApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MarkApp.context;
    }

    public static boolean isLoggedIn(){
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return prefs.getBoolean(SHARED_PREF_AUTHENTICATED,false);
    }
    public static String getToken(){
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return prefs.getString(SHARED_PREF_TOKEN,null);
    }
    public static String getUsername(){
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return prefs.getString(SHARED_PREF_USERNAME,"");
    }

    public static String getPassword(){
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return prefs.getString(SHARED_PREF_PASSWORD,"");
    }

    public static void login(String username,String password,String token){
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREF_USERNAME,username);
        editor.putString(SHARED_PREF_PASSWORD,password);
        editor.putString(SHARED_PREF_TOKEN,token);
        editor.putBoolean(SHARED_PREF_AUTHENTICATED,true);
        editor.commit();
    }
    public static void logout(){
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREF_USERNAME,null);
        editor.putString(SHARED_PREF_PASSWORD,null);
        editor.putString(SHARED_PREF_TOKEN,null);
        editor.putBoolean(SHARED_PREF_AUTHENTICATED,false);
        editor.commit();
    }

    public static String getGcmToken(){
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return prefs.getString(SHARED_PREF_GCM_TOKEN,"");
    }

    public static int getLastAppVersion(){
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return prefs.getInt(SHARED_PREF_LAST_APP_VERSION, Integer.MIN_VALUE);
    }

    public static void storeGcmToken(String gcmToken) {
        SharedPreferences prefs = MarkApp.getAppContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int appVersion = getAppVersion();
        //Utils.debug(MarkApp.class, "Saving regId on app version " + appVersion);
        editor.putString(SHARED_PREF_GCM_TOKEN, gcmToken);
        editor.putInt(SHARED_PREF_LAST_APP_VERSION, appVersion);
        editor.commit();
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
