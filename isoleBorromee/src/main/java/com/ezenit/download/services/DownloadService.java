
package com.ezenit.download.services;

import com.ezenit.download.utils.DownloaderIntents;
import com.ezenit.download.services.IDownloadService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

public class DownloadService extends Service {
	
	// ===========================================================
	// Constants
	// ===========================================================


	private static final String ACTION_DOWNLOAD = "com.ezenit.download.services.IDownloadService";
	
	// ===========================================================
	// Fields
	// ===========================================================

    private DownloadManager mDownloadManager;
    
	// ===========================================================
	// Constructors
	// ===========================================================

    public static void addDownload(Context context,String url,String folderPath){
    	Intent service = new Intent(context,DownloadService.class); 
    	service.setAction(ACTION_DOWNLOAD);
    	service.putExtra(DownloaderIntents.TYPE, DownloaderIntents.Types.ADD);
    	service.putExtra(DownloaderIntents.URL, url);
    	service.putExtra(DownloaderIntents.FOLDER, folderPath);
    	context.startService(service);
    }
    
    public static void resumeDownload(Context context,String url,String folderPath){
    	Intent service = new Intent(context,DownloadService.class);    	
    	service.setAction(ACTION_DOWNLOAD);
    	service.putExtra(DownloaderIntents.TYPE, DownloaderIntents.Types.CONTINUE);
    	service.putExtra(DownloaderIntents.URL, url);
    	service.putExtra(DownloaderIntents.FOLDER, folderPath);
    	context.startService(service);
    }
    
    public static void pauseDownload(Context context,String url){
    	Intent service = new Intent(context,DownloadService.class); 
    	service.setAction(ACTION_DOWNLOAD);
    	service.putExtra(DownloaderIntents.TYPE, DownloaderIntents.Types.PAUSE);
    	service.putExtra(DownloaderIntents.URL, url);
    	context.startService(service);
    }
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
    
    @Override
    public IBinder onBind(Intent intent) {

        return new DownloadServiceImpl();
    }

    @Override
    public void onCreate() {

        super.onCreate();
        mDownloadManager = new DownloadManager(this);
    }
    

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if(intent==null||intent.getAction()==null){
        	return;
        }

        if (intent.getAction().equals(ACTION_DOWNLOAD)) {
            int type = intent.getIntExtra(DownloaderIntents.TYPE, -1);
            String folder = intent.getStringExtra(DownloaderIntents.FOLDER);
            String url;

            switch (type) {
                case DownloaderIntents.Types.START:
                    if (!mDownloadManager.isRunning()) {
                        mDownloadManager.startManage();
                    } else {
                        mDownloadManager.reBroadcastAddAllTask();
                    }
                    break;
                case DownloaderIntents.Types.ADD:
                    url = intent.getStringExtra(DownloaderIntents.URL);
                    if (!TextUtils.isEmpty(url) && !mDownloadManager.hasTask(url)) {
                        mDownloadManager.addTask(url,folder);
                    }
                    break;
                case DownloaderIntents.Types.CONTINUE:
                    url = intent.getStringExtra(DownloaderIntents.URL);
                    if (!TextUtils.isEmpty(url)) {
                        mDownloadManager.continueTask(url);
                    }
                    break;
                case DownloaderIntents.Types.DELETE:
                    url = intent.getStringExtra(DownloaderIntents.URL);
                    if (!TextUtils.isEmpty(url)) {
                        mDownloadManager.deleteTask(url);
                    }
                    break;
                case DownloaderIntents.Types.PAUSE:
                    url = intent.getStringExtra(DownloaderIntents.URL);
                    if (!TextUtils.isEmpty(url)) {
                        mDownloadManager.pauseTask(url);
                    }
                    break;
                case DownloaderIntents.Types.STOP:
                    mDownloadManager.close();
                    // mDownloadManager = null;
                    break;

                default:
                    break;
            }
        }

    }
    
   
    
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

    private class DownloadServiceImpl extends IDownloadService.Stub {

        @Override
        public void startManage() throws RemoteException {
        	mDownloadManager.startManage();
        
        }

        @Override
        public void addTask(String url,String destFolder) throws RemoteException {
            mDownloadManager.addTask(url,destFolder);
        }

        @Override
        public void pauseTask(String url) throws RemoteException {

        }

        @Override
        public void deleteTask(String url) throws RemoteException {

        }

        @Override
        public void continueTask(String url) throws RemoteException {

        }

    }

}
