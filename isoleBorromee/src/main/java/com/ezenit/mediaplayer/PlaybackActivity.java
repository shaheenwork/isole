
package com.ezenit.mediaplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;
import com.ezenit.isoleborromee.R;
import com.mapsaurus.paneslayout.PanesActivity;

/**
 * Base activity for activities that contain playback controls. Handles
 * communication with the PlaybackService and response to state and guide
 * changes.
 */
public abstract class PlaybackActivity extends PanesActivity
	implements Handler.Callback,
	           View.OnClickListener,
	           CoverView.Callback
{
	private Action mUpAction;
	private Action mDownAction;

	/**
	 * A Handler running on the UI thread, in contrast with mHandler which runs
	 * on a worker thread.
	 */
	protected final Handler mUiHandler = new Handler(this);
	/**
	 * A Handler running on a worker thread.
	 */
	protected Handler mHandler;
	/**
	 * The looper for the worker thread.
	 */
	protected Looper mLooper;

	protected CoverView mCoverView;
	protected ImageButton mPlayPauseButton;
	protected ImageButton mShuffleButton;
	protected ImageButton mEndButton;

	protected int mState;
	private long mLastStateEvent;
	private long mLastSongEvent;

	@Override
	public void onCreate(Bundle state)
	{
		super.onCreate(state);

		PlaybackService.addActivity(this);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		HandlerThread thread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_LOWEST);
		thread.start();

		mLooper = thread.getLooper();
		mHandler = new Handler(mLooper, this);
	}

	@Override
	public void onDestroy()
	{
		PlaybackService.removeActivity(this);
		mLooper.quit();
		super.onDestroy();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		if (PlaybackService.hasInstance())
			onServiceReady();
		else
			startService(new Intent(this, PlaybackService.class));

		SharedPreferences prefs = PlaybackService.getSettings(this);
		mUpAction = Action.getAction(prefs, PrefKeys.SWIPE_UP_ACTION, Action.Nothing);
		mDownAction = Action.getAction(prefs, PrefKeys.SWIPE_DOWN_ACTION, Action.Nothing);

		Window window = getWindow();
		if (prefs.getBoolean(PrefKeys.DISABLE_LOCKSCREEN, false))
			window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		else
			window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		MediaButtonReceiver.registerMediaButton(this);
		MediaButtonReceiver.setInCall(false);
		if (PlaybackService.hasInstance()) {
			PlaybackService service = PlaybackService.get(this);
			service.userActionTriggered();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
		case KeyEvent.KEYCODE_MEDIA_NEXT:
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			return MediaButtonReceiver.processKey(this, event);
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		switch (keyCode) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
		case KeyEvent.KEYCODE_MEDIA_NEXT:
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			return MediaButtonReceiver.processKey(this, event);
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void shiftCurrentAudioGuide(int delta)
	{
		setSong(PlaybackService.get(this).shiftCurrentSong(delta));
	}

	public void playPause()
	{
		PlaybackService service = PlaybackService.get(this);
		int state = service.playPause();
		if ((state & PlaybackService.FLAG_ERROR) != 0)
			Toast.makeText(this, service.getErrorMessage(), Toast.LENGTH_LONG).show();
		setState(state);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId()) {
		case R.id.next:
			shiftCurrentAudioGuide(AudioGuideTimeline.SHIFT_NEXT_AUDIO_GUIDE);
			break;
		case R.id.play_pause:
			playPause();
			break;
//		case R.id.previous:
//			shiftCurrentSong(AudioGuideTimeline.SHIFT_PREVIOUS_SONG);
//			break;
//		case R.id.end_action:
//			cycleFinishAction();
//			break;
//		case R.id.shuffle:
//			cycleShuffle();
//			break;
		}
	}
	
	private static final String TAG = PlaybackActivity.class.getName();

	/**
	 * Called when the PlaybackService state has changed.
	 *
	 * @param state PlaybackService state
	 * @param toggled The flags that have changed from the previous state
	 */
	protected void onStateChange(int state, int toggled)
	{
		if ((toggled & PlaybackService.FLAG_PLAYING) != 0 && mPlayPauseButton != null) {
//			Log.d(TAG, "state, toggleid "+toggled);
			mPlayPauseButton.setImageResource((state & PlaybackService.FLAG_PLAYING) == 0 ? R.drawable.ic_play : R.drawable.ic_pause);
		}
		if ((toggled & PlaybackService.MASK_FINISH) != 0 && mEndButton != null) {
//			mEndButton.setImageResource(AudioGuideTimeline.FINISH_ICONS[PlaybackService.finishAction(state)]);
		}
		if ((toggled & PlaybackService.MASK_SHUFFLE) != 0 && mShuffleButton != null) {
//			mShuffleButton.setImageResource(AudioGuideTimeline.SHUFFLE_ICONS[PlaybackService.shuffleMode(state)]);
		}
	}

	protected void setState(final int state)
	{
		mLastStateEvent = SystemClock.uptimeMillis();

		if (mState != state) {
			final int toggled = mState ^ state;
			mState = state;
			runOnUiThread(new Runnable() {
				@Override
				public void run()
				{
					onStateChange(state, toggled);
				}
			});
		}
	}

	/**
	 * Called by PlaybackService to update the state.
	 */
	public void setState(long uptime, int state)
	{
		if (uptime > mLastStateEvent)
			setState(state);
	}

	/**
	 * Sets up components when the PlaybackService is initialized and available to
	 * interact with. Override to implement further post-initialization behavior.
	 */
	protected void onServiceReady()
	{
//		Log.d("Tag", "Service in ready");
		PlaybackService service = PlaybackService.get(this);
		setSong(service.getAudioGuide(0));
		setState(service.getState());
	}
	
	

	/**
	 * Called when the current guide changes.
	 *
	 * @param guide The new guide
	 */
	protected void onSongChange(AudioGuide guide)
	{
		if (mCoverView != null)
			mCoverView.queryAudioGuides(PlaybackService.get(this));
	}

	protected void setSong(final AudioGuide guide)
	{
		mLastSongEvent = SystemClock.uptimeMillis();
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				onSongChange(guide);
			}
		});
	}

	/**
	 * Called by PlaybackService to update the current guide.
	 */
	public void setSong(long uptime, AudioGuide guide)
	{
		if (uptime > mLastSongEvent)
			setSong(guide);
	}

	/**
	 * Called by PlaybackService to update an active guide (next, previous, or
	 * current).
	 */
	public void replaceSong(int delta, AudioGuide guide)
	{
		if (mCoverView != null)
			mCoverView.setAudioGuide(delta + 1, guide);
	}

	/**
	 * Called when the guide timeline position/size has changed.
	 */
	public void onPositionInfoChanged()
	{
	}

	/**
	 * Called when the content of the media store has changed.
	 */
	public void onMediaChange()
	{
	}

	static final int MENU_SORT = 1;
	static final int MENU_PREFS = 2;
	static final int MENU_LIBRARY = 3;
	static final int MENU_PLAYBACK = 5;
	static final int MENU_SEARCH = 7;
	static final int MENU_ENQUEUE_ALBUM = 8;
	static final int MENU_ENQUEUE_ARTIST = 9;
	static final int MENU_ENQUEUE_GENRE = 10;
	static final int MENU_CLEAR_QUEUE = 11;
	static final int MENU_TOGGLE_CONTROLS = 12;

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
//		menu.add(0, MENU_PREFS, 0, R.string.settings).setIcon(R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
//		case MENU_PREFS:
//			startActivity(new Intent(this, PreferencesActivity.class));
//			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		return false;
	}

	/**
	 * Cycle shuffle mode.
	 */
	public void cycleShuffle()
	{
		setState(PlaybackService.get(this).cycleShuffle());
	}

	/**
	 * Cycle the finish action.
	 */
	public void cycleFinishAction()
	{
		setState(PlaybackService.get(this).cycleFinishAction());
	}

	

	@Override
	public void upSwipe()
	{
		PlaybackService.get(this).performAction(mUpAction, this);
	}

	@Override
	public void downSwipe()
	{
		PlaybackService.get(this).performAction(mDownAction, this);
	}

	private static final int GROUP_SHUFFLE = 100;
	private static final int GROUP_FINISH = 101;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
	{
		if (view == mShuffleButton) {
//			menu.add(GROUP_SHUFFLE, AudioGuideTimeline.SHUFFLE_NONE, 0, R.string.no_shuffle);
//			menu.add(GROUP_SHUFFLE, AudioGuideTimeline.SHUFFLE_SONGS, 0, R.string.shuffle_songs);
//			menu.add(GROUP_SHUFFLE, AudioGuideTimeline.SHUFFLE_ALBUMS, 0, R.string.shuffle_albums);
		} else if (view == mEndButton) {
//		    menu.add(GROUP_FINISH, AudioGuideTimeline.FINISH_STOP, 0, R.string.no_repeat);
//			menu.add(GROUP_FINISH, AudioGuideTimeline.FINISH_REPEAT, 0, R.string.repeat);
//			menu.add(GROUP_FINISH, AudioGuideTimeline.FINISH_REPEAT_CURRENT, 0, R.string.repeat_current_song);
//			menu.add(GROUP_FINISH, AudioGuideTimeline.FINISH_STOP_CURRENT, 0, R.string.stop_current_song);
//			menu.add(GROUP_FINISH, AudioGuideTimeline.FINISH_RANDOM, 0, R.string.random);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		int group = item.getGroupId();
		int id = item.getItemId();
		if (group == GROUP_SHUFFLE)
			setState(PlaybackService.get(this).setShuffleMode(id));
		else if (group == GROUP_FINISH)
			setState(PlaybackService.get(this).setFinishAction(id));
		return true;
	}
}
