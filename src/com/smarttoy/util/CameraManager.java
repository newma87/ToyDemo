package com.smarttoy.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.*;

public class CameraManager implements SurfaceHolder.Callback{
	private static final String LOG_TAG = "Camera Manager";
	
    public static interface CameraReadyCallback { 
        public void onCameraReady(); 
    }

    private Camera m_camera = null;
    private SurfaceHolder m_surfaceHolder = null;
    private SurfaceView	  m_surfaceView = null;
    CameraReadyCallback m_cameraReadyCb = null;
 
    private int m_curWidth = 0;
    private int m_curHeight = 0;
    private int m_curCameraId = 0;
    private Camera.Parameters m_params;
    
    public static boolean isCameraSupport(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }
    
    public static int getCameraCount() {
    	return Camera.getNumberOfCameras();
    }
    
    @SuppressWarnings("deprecation")
	public CameraManager(SurfaceView sv){
        m_surfaceView = sv;

        m_surfaceHolder = m_surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        m_surfaceHolder.addCallback(this); 
    }

    private boolean setupCamera(int i) {
    	boolean bRet = false;
    	
    	try {
    		m_camera = Camera.open();
    		m_curCameraId = i;
    		m_params = m_camera.getParameters();
    		
    		List<Camera.Size> lstSize = getSupportedPreviewSize();
    		Camera.Size sz = lstSize.get(lstSize.size() / 2);
    		setCameraSize(sz.width, sz.height);
    		
    		m_camera.setParameters(m_params);
    		m_camera.setPreviewDisplay(m_surfaceHolder);
    		bRet = true;
    	} catch (Exception e) {
    		Log.e(LOG_TAG, e.getMessage());
    	}
    	
    	return bRet;
    }
    
    public boolean openDefaultCamera() {
    	boolean bRet = false;
    	
    	if (getCameraCount() > 0) {
        	bRet = setupCamera(0);
    	}
    	
        return bRet;
    }
    
    public boolean switchCamera() {
    	boolean bRet = false;
    	
    	int count = getCameraCount();
    	if (count > 0) {
    		int index = (m_curCameraId + 1) / count;
    		
    		if (index != m_curCameraId) {
    			bRet = setupCamera(index);
    		} else {
    			bRet = true;
    		}
    	}
    	
    	return bRet;
    }
   
    public void setCameraSize(int width, int height) {
    	if (m_params != null) {
    		m_curWidth = width;
    		m_curHeight = height;
    		m_params.setPreviewSize(width, height);
    		m_camera.setParameters(m_params);
    	}
    }
    
    public List<Camera.Size> getSupportedPreviewSize() {
    	return m_camera.getParameters().getSupportedPreviewSizes();
    }

    public int width() {
        return m_curWidth;
    }

    public int height() {
        return m_curHeight;
    }

    public void setCameraReadyCallback(CameraReadyCallback cb) {
        m_cameraReadyCb = cb;
    }

    public void startPreview(){
        if ( m_camera == null)
            return;
        m_camera.startPreview();
        Log.d(LOG_TAG, "start camera successfully!");
    }
    
    public void stopPreview(){
        if ( m_camera == null)
            return;
        m_camera.stopPreview();
        Log.d(LOG_TAG, "camera stop successfully!");
    }

    public void autoFocus() {
        m_camera.autoFocus(afcb);
    }

    public void release() {
        if ( m_camera != null) {
        	m_camera.setPreviewCallback(null) ;
            m_camera.stopPreview();
            m_camera.release();
            m_camera = null;
        }
    }
    
    public void setPreviewCallBack(PreviewCallback cb) {
    	m_camera.setPreviewCallback(cb);
    }

    private Camera.AutoFocusCallback afcb = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder sh, int format, int w, int h){
    }
    
	@Override
    public void surfaceCreated(SurfaceHolder sh){ 
        if (openDefaultCamera() && m_cameraReadyCb != null) {
            m_cameraReadyCb.onCameraReady();
        }
    }
    
	@Override
    public void surfaceDestroyed(SurfaceHolder sh){
        release();
    }
}

/* Usage:
 * if (CameraManager.isCameraSupport(Context)) {
 * 		CameraManager cm = new CameraManager(SurfaceView sv);
 * 		cm.setCameraReadyCallback(new CameraReadyCallback() {
 * 			public void onCameraReady() {
 * 				cm.setPreviewCallBack(new PreviewCallback() {
 * 					public void onPreviewFrame(byte[] frame, Camera c) {
 * 					}
 * 				});
 * 				cm.startPreview();
 * 			}
 * 		});
 * 
 * 		......
 * 		cm.stopPreview();
 * 		cm.setCameraSize(width, height);
 * 		cm.startPreview();
 * 
 * 		...... 
 * 		cm.stopPreview();
 * 		cm.switchCamera();
 * 		cm.startPreview();
 * }
 */