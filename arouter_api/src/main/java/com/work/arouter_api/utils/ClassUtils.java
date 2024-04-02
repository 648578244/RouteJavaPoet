package com.work.arouter_api.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.work.arouter_api.launcher.ARouter;
import com.work.arouter_api.thread.DefaultPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

public class ClassUtils {
    private static final String EXTRACTED_NAME_EXT = ".classes";

    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";


    private static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;
    private static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;

    private static final String KEY_DEX_NUMBER = "dex.number";
    private static final String PREFS_FILE = "multidex.version";


    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    /**
     * 通过指定包名，扫描包下面包含的所有的ClassName
     *
     * @param context     U know
     * @param packageName 包名
     * @return 所有class的集合
     */
    public static Set<String> getFileNameByPackageName(Context context, final String packageName) throws InterruptedException, PackageManager.NameNotFoundException, IOException {
        final Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);
        final CountDownLatch parserCtl = new CountDownLatch(paths.size());
        for (final String path : paths) {
            DefaultPoolExecutor.getInstance().execute(new Runnable() {

                @Override
                public void run() {
                    DexFile dexFile = null;
                    try {
                        if (path.endsWith(EXTRACTED_SUFFIX)) {
                            dexFile = DexFile.loadDex(path, path + ".tmp", 0);
                        } else {
                            dexFile = new DexFile(path);
                        }
                        Enumeration<String> dexEntries = dexFile.entries();
                        while (dexEntries.hasMoreElements()) {
                            String className = dexEntries.nextElement();
                            if (className.startsWith(packageName)) {
                                classNames.add(className);
                            }
                        }
                    } catch (Exception e) {

                    } finally {
                        if (null != dexFile) {
                            try {
                                dexFile.close();
                            } catch (Throwable ignore) {

                            }
                        }
                        parserCtl.countDown();
                    }
                }
            });
        }
        parserCtl.await();
        return classNames;
    }

    private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);

        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir);
        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;
        if (!isVMMultidexCapable()) {
          int totalDexNumber =  getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER,1);
          File dexDir = new File(applicationInfo.dataDir,SECONDARY_FOLDER_NAME);
          for (int secondaryNumber=2;secondaryNumber<=totalDexNumber;secondaryNumber++){
              String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
              File extractedFile = new File(dexDir, fileName);
              if (extractedFile.isFile()) {
                  sourcePaths.add(extractedFile.getAbsolutePath());
                  //we ignore the verify zip part
              } else {
                  throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
              }

          }
        }
        if (ARouter.debuggable()){
            sourcePaths.addAll(tryLoadInstantRunDexFile(applicationInfo));
        }

        return sourcePaths;
    }
    private static List<String> tryLoadInstantRunDexFile(ApplicationInfo applicationInfo) {
        List<String> instantRunSourcePaths = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != applicationInfo.splitSourceDirs) {
            // add the split apk, normally for InstantRun, and newest version.
            instantRunSourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            Log.d(Constants.TAG, "Found InstantRun support");
        } else {
            try {
                // This man is reflection from Google instant run sdk, he will tell me where the dex files go.
                Class pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths");
                Method getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String.class);
                String instantRunDexPath = (String) getDexFileDirectory.invoke(null, applicationInfo.packageName);

                File instantRunFilePath = new File(instantRunDexPath);
                if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                    File[] dexFile = instantRunFilePath.listFiles();
                    for (File file : dexFile) {
                        if (null != file && file.exists() && file.isFile() && file.getName().endsWith(".dex")) {
                            instantRunSourcePaths.add(file.getAbsolutePath());
                        }
                    }
                    Log.d(Constants.TAG, "Found InstantRun support");
                }

            } catch (Exception e) {
                Log.e(Constants.TAG, "InstantRun support error, " + e.getMessage());
            }
        }

        return instantRunSourcePaths;
    }

    private static boolean isVMMultidexCapable() {
        boolean isMultidexCapable = false;
        String vmName = "'Android'";
        String versionString = System.getProperty("java.vm.version");
        if (versionString != null) {
            Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
            if (matcher.matches()) {
                try {
                    int major = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR)
                            || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR)
                            && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR));
                } catch (NumberFormatException ignore) {
                    // let isMultidexCapable be false
                }
            }
        }
        return isMultidexCapable;

    }
}
