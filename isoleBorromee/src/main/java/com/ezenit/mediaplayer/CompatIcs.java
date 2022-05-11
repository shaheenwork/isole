
package com.ezenit.mediaplayer;

import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.util.Log;

/**
 * Framework methods only in ICS or above go here.
 */
@TargetApi(14)
public class CompatIcs {
	/**
	 * Used with updateRemote method.
	 */
	private static RemoteControlClient sRemote;

	/**
	 * Perform initialization required for RemoteControlClient.
	 *
	 * @param context A context to use.
	 * @param am The AudioManager service.
	 */
	public static void registerRemote(Context context, AudioManager am)
	{
		if (!MediaButtonReceiver.useHeadsetControls(context)) {
			// RemoteControlClient requires MEDIA_BUTTON intent
			return;
		}

		MediaButtonReceiver.registerMediaButton(context);

		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName()));
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE);
		RemoteControlClient remote = new RemoteControlClient(mediaPendingIntent);
		int flags = RemoteControlClient.FLAG_KEY_MEDIA_NEXT
			| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
			| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
			| RemoteControlClient.FLAG_KEY_MEDIA_PLAY
			| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE;
		remote.setTransportControlFlags(flags);
		am.registerRemoteControlClient(remote);
		sRemote = remote;
	}

	/**
	 * Update the remote with new metadata.
	 * {@link #registerRemote(Context, AudioManager)} must have been called
	 * first.
	 *
	 * @param context A context to use.
	 * @param guide The guide containing the new metadata.
	 * @param state PlaybackService state, used to determine playback state.
	 */
	public static void updateRemote(Context context, AudioGuide guide, int state)
	{
		RemoteControlClient remote = sRemote;
		if (remote == null)
			return;
//		Log.d("Log", "Working"+state);
		remote.setPlaybackState((state & PlaybackService.FLAG_PLAYING) != 0 ? RemoteControlClient.PLAYSTATE_PLAYING : RemoteControlClient.PLAYSTATE_PAUSED);
		RemoteControlClient.MetadataEditor editor = remote.editMetadata(true);
		if (guide != null) {
			editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, context.getResources().getString(guide.getMuseum().getNameId()));
			editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, guide.getTitle());
			Bitmap bitmap = guide.getCover(context,512,512);
			if (bitmap != null) {
				// Create a copy of the cover art, since RemoteControlClient likes
				// to recycle what we give it.
				bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);
			}
			editor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap);
		}
		editor.apply();
	}
}
