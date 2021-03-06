
package com.ezenit.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

/**
 * Receives media button events and calls to PlaybackService to respond
 * appropriately.
 */
public class MediaButtonReceiver extends BroadcastReceiver {
		/**
	 * If another button event is received before this time in milliseconds
	 * expires, the event with be considered a double click.
	 */
	private static final int DOUBLE_CLICK_DELAY = 400;

	/**
	 * Whether the headset controls should be used. 1 for yes, 0 for no, -1 for
	 * uninitialized.
	 */
	private static int sUseControls = -1;
	/**
	 * Whether the phone is currently in a call. 1 for yes, 0 for no, -1 for
	 * uninitialized.
	 */
	private static int sInCall = -1;
	/**
	 * Time of the last play/pause click. Used to detect double-clicks.
	 */
	private static long sLastClickTime = 0;
	/**
	 * Whether a beep should be played in response to double clicks be used.
	 * 1 for yes, 0 for no, -1 for uninitialized.
	 */
	private static int sBeep = -1;
	/**
	 * Lazy-loaded AsyncPlayer for beep sounds.
	 */
	private static AsyncPlayer sBeepPlayer;
	/**
	 * Lazy-loaded URI of the beep resource.
	 */
	private static Uri sBeepSound;

	/**
	 * Play a beep sound.
	 */
	private static void beep(Context context)
	{
		if (sBeep == -1) {
			SharedPreferences settings = PlaybackService.getSettings(context);
			sBeep = settings.getBoolean(PrefKeys.MEDIA_BUTTON_BEEP, true) ? 1 : 0;
		}

		if (sBeep == 1) {
			if (sBeepPlayer == null) {
				sBeepPlayer = new AsyncPlayer("BeepPlayer");
				sBeepSound = Uri.parse("android.resource://com.ezenit.isoleborromee/raw/beep");
			}
			sBeepPlayer.play(context, sBeepSound, false, AudioManager.STREAM_MUSIC);
		}
	}

	/**
	 * Reload the preferences and enable/disable buttons as appropriate.
	 *
	 * @param context A context to use.
	 */
	public static void reloadPreference(Context context)
	{
		sUseControls = -1;
		sBeep = -1;
		if (useHeadsetControls(context)) {
			registerMediaButton(context);
		} else {
			unregisterMediaButton(context);
		}
	}

	/**
	 * Return whether headset controls should be used, loading the preference
	 * if necessary.
	 *
	 * @param context A context to use.
	 */
	public static boolean useHeadsetControls(Context context)
	{
		if (sUseControls == -1) {
			SharedPreferences settings = PlaybackService.getSettings(context);
			sUseControls = settings.getBoolean(PrefKeys.MEDIA_BUTTON, true) ? 1 : 0;
		}

		return sUseControls == 1;
	}

	/**
	 * Return whether the phone is currently in a call.
	 *
	 * @param context A context to use.
	 */
	private static boolean isInCall(Context context)
	{
		if (sInCall == -1) {
			TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			sInCall = (byte)(manager.getCallState() == TelephonyManager.CALL_STATE_IDLE ? 0 : 1);
		}
		return sInCall == 1;
	}

	/**
	 * Set the cached value for whether the phone is in a call.
	 *
	 * @param value True if in a call, false otherwise.
	 */
	public static void setInCall(boolean value)
	{
		sInCall = value ? 1 : 0;
	}

	/**
	 * Process a media button key press.
	 *
	 * @param context A context to use.
	 * @param event The key press event.
	 * @return True if the event was handled and the broadcast should be
	 * aborted.
	 */
	public static boolean processKey(Context context, KeyEvent event)
	{
		if (event == null || isInCall(context) || !useHeadsetControls(context))
			return false;

		int action = event.getAction();
		String act = null;

		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			// single click: pause/resume.
			// double click: next track

			if (action == KeyEvent.ACTION_DOWN) {
				long time = SystemClock.uptimeMillis();
				if (time - sLastClickTime < DOUBLE_CLICK_DELAY) {
					beep(context);
					act = PlaybackService.ACTION_NEXT_SONG_AUTOPLAY;
				} else {
					act = PlaybackService.ACTION_TOGGLE_PLAYBACK;
				}
				sLastClickTime = time;
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			if (action == KeyEvent.ACTION_DOWN)
				act = PlaybackService.ACTION_NEXT_SONG_AUTOPLAY;
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			if (action == KeyEvent.ACTION_DOWN)
				act = PlaybackService.ACTION_PREVIOUS_SONG_AUTOPLAY;
			break;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
			if (action == KeyEvent.ACTION_DOWN)
				act = PlaybackService.ACTION_PLAY;
			break;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			if (action == KeyEvent.ACTION_DOWN)
				act = PlaybackService.ACTION_PAUSE;
			break;
		default:
			return false;
		}

		if (act != null) {
			Intent intent = new Intent(context, PlaybackService.class);
			intent.setAction(act);
			context.startService(intent);
		}

		return true;
	}

	/**
	 * Request focus on the media buttons from AudioManager if media buttons
	 * are enabled.
	 *
	 * @param context A context to use.
	 */
	public static void registerMediaButton(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO || !useHeadsetControls(context))
			return;

		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		ComponentName receiver = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
		CompatFroyo.registerMediaButtonEventReceiver(audioManager, receiver);
	}

	/**
	 * Unregister the media buttons from AudioManager.
	 *
	 * @param context A context to use.
	 */
	public static void unregisterMediaButton(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO)
			return;

		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		ComponentName receiver = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
		CompatFroyo.unregisterMediaButtonEventReceiver(audioManager, receiver);
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			boolean handled = processKey(context, event);
			if (handled && isOrderedBroadcast())
				abortBroadcast();
		}
	}
}
