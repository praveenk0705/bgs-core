package com.red_folder.phonegap.plugin.backgroundservice;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import android.util.Log;

import org.apache.cordova.CordovaPlugin;

import com.red_folder.phonegap.plugin.backgroundservice.BackgroundServicePluginLogic.ExecuteResult;
import com.red_folder.phonegap.plugin.backgroundservice.BackgroundServicePluginLogic.ExecuteStatus;

public class BackgroundServicePlugin extends CordovaPlugin implements BackgroundServicePluginLogic.IUpdateListener {

	/*
	 ************************************************************************************************
	 * Static values 
	 ************************************************************************************************
	 */
	private static final String TAG = BackgroundServicePlugin.class.getSimpleName();

	/*
	 ************************************************************************************************
	 * Fields 
	 ************************************************************************************************
	 */
	// Part fix for https://github.com/Red-Folder/Cordova-Plugin-BackgroundService/issues/19
	//private final BackgroundServicePluginLogic mLogic = new BackgroundServicePluginLogic();
	private BackgroundServicePluginLogic mLogic = null;

	/*
	 ************************************************************************************************
	 * Overriden Methods 
	 ************************************************************************************************
	 */
	// Part fix for https://github.com/Red-Folder/Cordova-Plugin-BackgroundService/issues/19
	//public boolean execute(String action, JSONArray data, CallbackContext callback) {
	@Override
	public boolean execute(final String action, final JSONArray data, final CallbackContext callback) {
	
	
		boolean result = false;
		
		
		if (this.mLogic == null)
			this.mLogic = new BackgroundServicePluginLogic(this.cordova.getActivity());
		
		// Part fix for https://github.com/Red-Folder/Cordova-Plugin-BackgroundService/issues/19
		//if (this.mLogic.isInitialized())
		//	this.mLogic.initialize(this.cordova.getActivity());
		
		try {
			
			if (this.mLogic.isActionValid(action)) {
				
				//Part fix for https://github.com/Red-Folder/Cordova-Plugin-BackgroundService/issues/19
				//final String finalAction = action;
				//final JSONArray finalData = data;
				//final CallbackContext finalCallback = callback;

				final BackgroundServicePluginLogic.IUpdateListener listener = this;
				final Object[] listenerExtras = new Object[] { callback };
			
				cordova.getThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						ExecuteResult logicResult = mLogic.execute(action, data, listener, listenerExtras);

						Log.i(TAG, "logicResult = " +  logicResult.toString());
						
						PluginResult pluginResult = transformResult(logicResult);
												
						Log.i(TAG, "pluginResult = " +  pluginResult.toString());
						Log.i(TAG, "pluginResult.getMessage() = " +  pluginResult.getMessage());
						if (pluginResult.getKeepCallback())
							Log.i(TAG, "Keep Callback");
						else
							Log.i(TAG, "Dont keep Callback");
						
						callback.sendPluginResult(pluginResult);
					}
				});

				result = true;
			} else {
				result = false;
			}
			
		} catch (Exception ex) {
			Log.i(TAG, "Exception - " + ex.getMessage());
		}

		return result;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (this.mLogic != null) {
			this.mLogic.onDestroy();
			this.mLogic = null;
		}
	}

	/*
	 ************************************************************************************************
	 * Public Methods 
	 ************************************************************************************************
	 */
	public void handleUpdate(ExecuteResult logicResult, Object[] listenerExtras) {
		Log.i(TAG, "Starting handleUpdate");
		sendUpdateToListener(logicResult, listenerExtras);
		Log.i(TAG, "Finished handleUpdate");
	}
	
	public void closeListener(ExecuteResult logicResult, Object[] listenerExtras) {
		Log.i(TAG, "Starting closeListener");
		sendUpdateToListener(logicResult, listenerExtras);
		Log.i(TAG, "Finished closeListener");
	}

	/*
	 ************************************************************************************************
	 * Private Methods 
	 ************************************************************************************************
	 */
	private void sendUpdateToListener(ExecuteResult logicResult, Object[] listenerExtras) {
		try {
			if (listenerExtras != null && listenerExtras.length > 0) {
				Log.i(TAG, "Sending update");
				CallbackContext callback = (CallbackContext)listenerExtras[0];
		
				callback.sendPluginResult(transformResult(logicResult));
				Log.i(TAG, "Sent update");
			}
		} catch (Exception ex) {
			Log.i(TAG, "Sending update failed", ex);
		}
	}
	
	private PluginResult transformResult(ExecuteResult logicResult) {
		PluginResult pluginResult = null;
		
		Log.i(TAG, "Start of transformResult");
		if (logicResult.getStatus() == ExecuteStatus.OK) {
			Log.i(TAG, "Status is OK");
			
			if (logicResult.getData() == null) {
				Log.i(TAG, "We dont have data");
				pluginResult = new PluginResult(PluginResult.Status.OK);
			} else {
				Log.i(TAG, "We have data");
				pluginResult = new PluginResult(PluginResult.Status.OK, logicResult.getData());
			}
		}

		if (logicResult.getStatus() == ExecuteStatus.ERROR) {
			Log.i(TAG, "Status is ERROR");
			
			if (logicResult.getData() == null) {
				Log.i(TAG, "We dont have data");
				pluginResult = new PluginResult(PluginResult.Status.ERROR, "Unknown error");
			} else {
				Log.i(TAG, "We have data");
				pluginResult = new PluginResult(PluginResult.Status.ERROR, logicResult.getData());
			}
		}
		
		if (logicResult.getStatus() == ExecuteStatus.INVALID_ACTION) {
			Log.i(TAG, "Status is INVALID_ACTION");
			
			if (logicResult.getData() == null) {
				Log.i(TAG, "We have data");
				pluginResult = new PluginResult(PluginResult.Status.INVALID_ACTION, "Unknown error");
			} else {
				Log.i(TAG, "We dont have data");
				pluginResult = new PluginResult(PluginResult.Status.INVALID_ACTION, logicResult.getData());
			}
		}
		
		if (!logicResult.isFinished()) {
			Log.i(TAG, "Keep Callback set to true");
			pluginResult.setKeepCallback(true);
		}
		
		Log.i(TAG, "End of transformResult");
		return pluginResult;
	}
}
