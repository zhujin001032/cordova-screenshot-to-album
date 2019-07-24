package com.jasonhe.cordova.screenshot;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Screenshot extends CordovaPlugin {
    private CallbackContext mCallbackContext;
    private String mAction;
    private JSONArray mArgs;


    private String mFormat;
    private String mFileName;
    private Integer mQuality;

    protected final static String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int PERMISSION_DENIED_ERROR = 20;
    public static final int SAVE_SCREENSHOT_SEC = 0;
    public static final int SAVE_SCREENSHOT_ALBUM_SEC = 1;

    @Override
    public Object onMessage(String id, Object data) {
        if (id.equals("onGotXWalkBitmap")) {
            Bitmap bitmap = (Bitmap) data;
            if (bitmap != null) {
                if (mAction.equals("saveScreenshotToAlbum")) {
                    saveScreenshotToAlbum(bitmap);
                } else if (mAction.equals("saveScreenshot")) {
                    saveScreenshot(bitmap, mFormat, mFileName, mQuality);
                } else if (mAction.equals("getScreenshotAsURI")) {
                    getScreenshotAsURI(bitmap, mQuality);
                }
            }
        }
        return null;
    }

    private Bitmap getBitmap() {
        Bitmap bitmap = null;

        boolean isCrosswalk = false;
        try {
            Class.forName("org.crosswalk.engine.XWalkWebViewEngine");
            isCrosswalk = true;
        } catch (Exception e) {
        }

        if (isCrosswalk) {
            webView.getPluginManager().postMessage("captureXWalkBitmap", this);
        } else {
            View view = webView.getView();//.getRootView();
            view.setDrawingCacheEnabled(true);
            bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
        }

        return bitmap;
    }

    private void scanPhoto(String imageFileName) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imageFileName);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.cordova.getActivity().sendBroadcast(mediaScanIntent);
    }
      /**
     * 保存图片到图库
     * @param context
     * @param bmp
     */
    private void saveScreenshotToAlbum(Bitmap bitmap) {
     
        Context context = this.cordova.getActivity();
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(),
                "desheng");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            MyToastUtils.showShortToast(context, "保存失败");
            e.printStackTrace();
        } catch (IOException e) {
            MyToastUtils.showShortToast(context, "保存失败");
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
            MyToastUtils.showShortToast(context, "保存成功");

             JSONObject jsonRes = new JSONObject();
            jsonRes.put("filePath", file.getAbsolutePath());
            PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
            mCallbackContext.sendPluginResult(result);

        } catch (JSONException e) {
            mCallbackContext.error(e.getMessage());

        } catch (IOException e) {
            mCallbackContext.error(e.getMessage());

        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(file.getPath()))));
    }


    private void saveScreenshot(Bitmap bitmap, String format, String fileName, Integer quality) {
        try {
            File folder = new File(Environment.getExternalStorageDirectory(), "Pictures");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File f = new File(folder, fileName + "." + format);

            FileOutputStream fos = new FileOutputStream(f);
            if (format.equals("png")) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } else if (format.equals("jpg")) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality == null ? 100 : quality, fos);
            }
            JSONObject jsonRes = new JSONObject();
            jsonRes.put("filePath", f.getAbsolutePath());
            PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
            mCallbackContext.sendPluginResult(result);

            scanPhoto(f.getAbsolutePath());
            fos.close();
        } catch (JSONException e) {
            mCallbackContext.error(e.getMessage());

        } catch (IOException e) {
            mCallbackContext.error(e.getMessage());

        }
    }

    private void getScreenshotAsURI(Bitmap bitmap, int quality) {
        try {
            ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();

            if (bitmap.compress(CompressFormat.JPEG, quality, jpeg_data)) {
                byte[] code = jpeg_data.toByteArray();
                byte[] output = Base64.encode(code, Base64.NO_WRAP);
                String js_out = new String(output);
                js_out = "data:image/jpeg;base64," + js_out;
                JSONObject jsonRes = new JSONObject();
                jsonRes.put("URI", js_out);
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
                mCallbackContext.sendPluginResult(result);

                js_out = null;
                output = null;
                code = null;
            }

            jpeg_data = null;

        } catch (JSONException e) {
            mCallbackContext.error(e.getMessage());

        } catch (Exception e) {
            mCallbackContext.error(e.getMessage());

        }
    }


    public void saveScreenshot() throws JSONException{
        mFormat = (String) mArgs.get(0);
        mQuality = (Integer) mArgs.get(1);
        mFileName = (String) mArgs.get(2);

        super.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFormat.equals("png") || mFormat.equals("jpg")) {
                    Bitmap bitmap = getBitmap();
                    if (bitmap != null) {
                        saveScreenshot(bitmap, mFormat, mFileName, mQuality);
                    }
                } else {
                    mCallbackContext.error("format " + mFormat + " not found");

                }
            }
        });
    }

    public void saveScreenshotToAlbum() throws JSONException{
            
            super.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    
                        Bitmap bitmap = getBitmap();
                        if (bitmap != null) {
                            saveScreenshotToAlbum(bitmap);
                        }
                }
            });
    }

    public void getScreenshotAsURI() throws JSONException{
        mQuality = (Integer) mArgs.get(0);

        super.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getBitmap();
                if (bitmap != null) {
                    getScreenshotAsURI(bitmap, mQuality);
                }
            }
        });
    }

     public void getScreenshotAsURISync() throws JSONException{
        mQuality = (Integer) mArgs.get(0);
        
        Runnable r = new Runnable(){
            @Override
            public void run() {
                Bitmap bitmap = getBitmap();
                if (bitmap != null) {
                    getScreenshotAsURI(bitmap, mQuality);
                }
                synchronized (this) { this.notify(); }
            }
        };

        synchronized (r) {
            super.cordova.getActivity().runOnUiThread(r);
            try{
                r.wait();
            } catch (InterruptedException e){
                mCallbackContext.error(e.getMessage());
            }
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        // starting on ICS, some WebView methods
        // can only be called on UI threads
        mCallbackContext = callbackContext;
        mAction = action;
        mArgs = args;
        if (action.equals("saveScreenshotToAlbum")) {
            if(PermissionHelper.hasPermission(this, PERMISSIONS[0])) {
                saveScreenshotToAlbum();
            } else {
                PermissionHelper.requestPermissions(this, SAVE_SCREENSHOT_ALBUM_SEC, PERMISSIONS);
            }
            return true;
        }else if (action.equals("saveScreenshot")) {
            if(PermissionHelper.hasPermission(this, PERMISSIONS[0])) {
                saveScreenshot();
            } else {
                PermissionHelper.requestPermissions(this, SAVE_SCREENSHOT_SEC, PERMISSIONS);
            }
            return true;
        } else if (action.equals("getScreenshotAsURI")) {
            getScreenshotAsURI();
            return true;
        } else if (action.equals("getScreenshotAsURISync")){
            getScreenshotAsURISync();
            return true;
        }
        callbackContext.error("action not found");
        return false;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                mCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch(requestCode)
        {
            case SAVE_SCREENSHOT_SEC:
                saveScreenshot();
                break;
            case SAVE_SCREENSHOT_ALBUM_SEC:
                saveScreenshotToAlbum();
                break;
        }
    }


}