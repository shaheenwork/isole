
package com.ezenit.mediaplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import android.content.Context;

import com.ezenit.isoleborromee.db.table.TableAudioGuide;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;

/**
 * Contains the list of currently playing songs, implements repeat and shuffle
 * support, and contains methods to fetch more songs from the MediaStore.
 */
public final class AudioGuideTimeline {
	/**
	 * Stop playback.
	 *
	 * @see AudioGuideTimeline#setFinishAction(int)
	 */
	public static final int FINISH_STOP = 0;
	/**
	 * Repeat from the beginning.
	 *
	 * @see AudioGuideTimeline#setFinishAction(int)
	 */
	public static final int FINISH_REPEAT = 1;
	/**
	 * Repeat the current song. This behavior is implemented entirely in
	 * {@link PlaybackService#onCompletion(android.media.MediaPlayer)};
	 * pressing the next or previous buttons will advance the song as normal;
	 * only allowing the song to play until the end will repeat it.
	 *
	 * @see AudioGuideTimeline#setFinishAction(int)
	 */
	public static final int FINISH_REPEAT_CURRENT = 2;
	/**
	 * Stop playback after current song. This behavior is implemented entirely
	 * in {@link PlaybackService#onCompletion(android.media.MediaPlayer)};
	 * pressing the next or previous buttons will advance the song as normal;
	 * only allowing the song to play until the end.
	 *
	 * @see AudioGuideTimeline#setFinishAction(int)
	 */
	public static final int FINISH_STOP_CURRENT = 3;
	/**
	 * Add random songs to the playlist.
	 *
	 * @see AudioGuideTimeline#setFinishAction(int)
	 */
	public static final int FINISH_RANDOM = 4;

//	/**
//	 * Icons corresponding to each of the finish actions.
//	 */
//	public static final int[] FINISH_ICONS =
//		{ R.drawable.repeat_inactive, R.drawable.repeat_active, R.drawable.repeat_current_active, R.drawable.stop_current_active, R.drawable.random_active };

	/**
	 * Clear the timeline and use only the provided songs.
	 *
	 * @see AudioGuideTimeline#addSongs(Context, QueryTask)
	 */
	public static final int MODE_PLAY = 0;
	/**
	 * Clear the queue and add the songs after the current song.
	 *
	 * @see AudioGuideTimeline#addSongs(Context, QueryTask)
	 */
	public static final int MODE_PLAY_NEXT = 1;
	/**
	 * Add the songs at the end of the timeline, clearing random songs.
	 *
	 * @see AudioGuideTimeline#addSongs(Context, QueryTask)
	 */
	public static final int MODE_ENQUEUE = 2;
	/**
	 * Like play mode, but make the song at the given position play first by
	 * removing the songs before the given position in the query and appending
	 * them to the end of the queue.
	 *
	 * Pass the position in QueryTask.data.
	 *
	 * @see AudioGuideTimeline#addSongs(Context, QueryTask)
	 */
	public static final int MODE_PLAY_POS_FIRST = 3;
	/**
	 * Like play mode, but make the song with the given id play first by
	 * removing the songs before the song in the query and appending
	 * them to the end of the queue. If there are multiple songs with
	 * the given id, picks the first song with that id.
	 *
	 * Pass the id in QueryTask.data.
	 *
	 * @see AudioGuideTimeline#addSongs(Context, QueryTask)
	 */
	public static final int MODE_PLAY_ID_FIRST = 4;
	/**
	 * Like enqueue mode, but make the song with the given id play first by
	 * removing the songs before the song in the query and appending
	 * them to the end of the queue. If there are multiple songs with
	 * the given id, picks the first song with that id.
	 *
	 * Pass the id in QueryTask.data.
	 *
	 * @see AudioGuideTimeline#addSongs(Context, QueryTask)
	 */
	public static final int MODE_ENQUEUE_ID_FIRST = 5;
	/**
	 * Like enqueue mode, but make the song at the given position play first by
	 * removing the songs before the given position in the query and appending
	 * them to the end of the queue.
	 *
	 * Pass the position in QueryTask.data.
	 *
	 * @see AudioGuideTimeline#addSongs(Context, QueryTask)
	 */
	public static final int MODE_ENQUEUE_POS_FIRST = 6;

	/**
	 * Disable shuffle.
	 *
	 * @see AudioGuideTimeline#setShuffleMode(int)
	 */
	public static final int SHUFFLE_NONE = 0;
	/**
	 * Randomize order of songs.
	 *
	 * @see AudioGuideTimeline#setShuffleMode(int)
	 */
	public static final int SHUFFLE_SONGS = 1;
	/**
	 * Randomize order of albums, preserving the order of tracks inside the
	 * albums.
	 *
	 * @see AudioGuideTimeline#setShuffleMode(int)
	 */
	public static final int SHUFFLE_ALBUMS = 2;

//	/**
//	 * Icons corresponding to each of the shuffle actions.
//	 */
//	public static final int[] SHUFFLE_ICONS =
//		{ R.drawable.shuffle_inactive, R.drawable.shuffle_active, R.drawable.shuffle_album_active };

	/**
	 * Move current position to the previous album.
	 *
	 * @see AudioGuideTimeline#shiftCurrentSong(int)
	 */
	public static final int SHIFT_PREVIOUS_ALBUM = -2;
	/**
	 * Move current position to the previous song.
	 *
	 * @see AudioGuideTimeline#shiftCurrentSong(int)
	 */
	public static final int SHIFT_PREVIOUS_AUDIO_GUIDE = -1;
	/**
	 * Move current position to the next song.
	 *
	 * @see AudioGuideTimeline#shiftCurrentSong(int)
	 */
	public static final int SHIFT_NEXT_AUDIO_GUIDE = 1;
	/**
	 * Move current position to the next album.
	 *
	 * @see AudioGuideTimeline#shiftCurrentSong(int)
	 */
	public static final int SHIFT_NEXT_ALBUM = 2;

	/**
	 * All the songs currently contained in the timeline. Each Song object
	 * should be unique, even if it refers to the same media.
	 */
	private ArrayList<AudioGuide> mSongs = new ArrayList<AudioGuide>(12);
	/**
	 * The position of the current song (i.e. the playing song).
	 */
	private int mCurrentPos;
	/**
	 * How to shuffle/whether to shuffle. One of SongTimeline.SHUFFLE_*.
	 */
	private int mShuffleMode;
	/**
	 * What to do when the end of the playlist is reached.
	 * Must be one of SongTimeline.FINISH_*.
	 */
	private int mFinishAction;

	// for shuffleAll()
	private ArrayList<AudioGuide> mShuffledSongs;

	// for saveActiveSongs()
	private AudioGuide mSavedPrevious;
	private AudioGuide mSavedCurrent;
	private AudioGuide mSavedNext;
	private int mSavedPos;
	private int mSavedSize;

	/**
	 * Interface to respond to timeline changes.
	 */
	public interface Callback {
		/**
		 * Called when an active song in the timeline is replaced by a method
		 * other than shiftCurrentSong()
		 *
		 * @param delta The distance from the current song. Will always be -1,
		 * 0, or 1.
		 * @param song The new song at the position
		 */
		public void activeSongReplaced(int delta, AudioGuide song);

		/**
		 * Called when the timeline state has changed and should be saved to
		 * storage.
		 */
		public void timelineChanged();

		/**
		 * Called when the length of the timeline has changed.
		 */
		public void positionInfoChanged();
	}
	/**
	 * The current Callback, if any.
	 */
	private Callback mCallback;

	public AudioGuideTimeline()
	{
		
	}

	/**
	 * Compares the ids of songs.
	 */
	public static class IdComparator implements Comparator<AudioGuide> {
		@Override
		public int compare(AudioGuide a, AudioGuide b)
		{
			return a.getId() < b.getId() ? -1 : (a.getId() == b.getId() ? 0 : 1);
		}
	}

	/**
	 * Compares the flags of songs.
	 */
	public static class FlagComparator implements Comparator<AudioGuide> {
		@Override
		public int compare(AudioGuide a, AudioGuide b)
		{
			return a.getCodeNo().compareTo(b.getCodeNo());
		}
	}

	/**
	 * Initializes the timeline with data read from the stream. Data should have
	 * been saved by a call to {@link AudioGuideTimeline#writeState(DataOutputStream)}.
	 *
	 * @param in The stream to read from.
	 */
	public void readState(DataInputStream in) throws IOException
	{
		synchronized (this) {
			int n = in.readInt();
			if (n > 0) {
				ArrayList<AudioGuide> songs = new ArrayList<AudioGuide>(n);

				// Fill the selection with the ids of all the saved songs
				// and initialize the timeline with unpopulated songs.
				StringBuilder selection = new StringBuilder();
				for (int i = 0; i != n; ++i) {
					long id = in.readLong();
					if (id == -1)
						continue;
					if (i != 0)
						selection.append(',');
					selection.append(id);
				}
				TableAudioGuide.populateAudioGuides(songs, selection.toString());

				// Sort songs by id---this is the order the query will
				// return its results in.
				Collections.sort(songs, new IdComparator());

				// Revert to the order the songs were saved in.
				Collections.sort(songs, new FlagComparator());
			
					mSongs = songs;
			}
		

			mCurrentPos = Math.min(mSongs == null ? -1 : mSongs.size(), in.readInt());
			mFinishAction = in.readInt();
			mShuffleMode = in.readInt();
		}
	}

	/**
	 * Writes the current songs and state to the given stream.
	 *
	 * @param out The stream to write to.
	 */
	public void writeState(DataOutputStream out) throws IOException
	{
		// Must update PlaybackService.STATE_VERSION when changing behavior
		// here.
		synchronized (this) {
			ArrayList<AudioGuide> songs = mSongs;

			int size = songs.size();
			out.writeInt(size);

			for (int i = 0; i != size; ++i) {
				AudioGuide song = songs.get(i);
				if (song == null) {
					out.writeLong(-1);
				} else {
					out.writeLong(song.getId());
//					out.writeShort(song.getCodeNo());
				}
			}

			out.writeInt(mCurrentPos);
			out.writeInt(mFinishAction);
			out.writeInt(mShuffleMode);
		}
	}

	/**
	 * Sets the current callback to <code>callback</code>.
	 */
	public void setCallback(Callback callback)
	{
		mCallback = callback;
	}

	/**
	 * Return the current shuffle mode.
	 *
	 * @return The shuffle mode. One of SongTimeline.SHUFFLE_*.
	 */
	public int getShuffleMode()
	{
		return mShuffleMode;
	}

	/**
	 * Return the finish action.
	 *
	 * @see AudioGuideTimeline#setFinishAction(int)
	 */
	public int getFinishAction()
	{
		return mFinishAction;
	}

	/**
	 * Set how to shuffle. Will shuffle the current set of songs when enabling
	 * shuffling if random mode is not enabled.
	 *
	 * @param mode One of SongTimeline.MODE_*
	 */
	public void setShuffleMode(int mode)
	{
		if (mode == mShuffleMode)
			return;

		synchronized (this) {
			saveActiveSongs();
			mShuffledSongs = null;
			mShuffleMode = mode;
			if (mode != SHUFFLE_NONE && mFinishAction != FINISH_RANDOM && !mSongs.isEmpty()) {
				shuffleAll();
				ArrayList<AudioGuide> songs = mShuffledSongs;
				mShuffledSongs = null;
				mCurrentPos = songs.indexOf(mSavedCurrent);
				mSongs = songs;
			}
			broadcastChangedSongs();
		}

		changed();
	}

	/**
	 * Set what to do when the end of the playlist is reached. Must be one of
	 * SongTimeline.FINISH_* (stop, repeat, or add random song).
	 */
	public void setFinishAction(int action)
	{
		saveActiveSongs();
		mFinishAction = action;
		broadcastChangedSongs();
		changed();
	}

	/**
	 * Shuffle all the songs in the timeline, storing the result in
	 * mShuffledSongs.
	 *
	 * @return The first song from the shuffled songs.
	 */
	private AudioGuide shuffleAll()
	{
		if (mShuffledSongs != null)
			return mShuffledSongs.get(0);

		ArrayList<AudioGuide> songs = new ArrayList<AudioGuide>(mSongs);
//		MediaUtils.shuffle(songs, mShuffleMode == SHUFFLE_ALBUMS);
		Collections.shuffle(songs);
		mShuffledSongs = songs;
		return songs.get(0);
	}

	/**
	 * Returns the song <code>delta</code> places away from the current
	 * position. Returns null if there is a problem retrieving the song.
	 *
	 * @param delta The offset from the current position. Must be -1, 0, or 1.
	 */
	public AudioGuide getSong(int delta)
	{
		//shn change Assert.assertTrue(delta >= -1 && delta <= 1);

		ArrayList<AudioGuide> timeline = mSongs;
		AudioGuide song;

		synchronized (this) {
			int pos = mCurrentPos + delta;
			int size = timeline.size();

			if (pos < 0) {
				if (size == 0 || mFinishAction == FINISH_RANDOM)
					return null;
				song = timeline.get(Math.max(0, size - 1));
			} else if (pos > size) {
				return null;
			} else if (pos == size) {
				if (mFinishAction == FINISH_RANDOM) {
					
					song = TableAudioGuide.getRandomGuide();
					if (song == null)
						return null;
					timeline.add(song);
				} else {
					if (size == 0)
						// empty queue
						return null;
					else if (mShuffleMode != SHUFFLE_NONE)
						song = shuffleAll();
					else
						song = timeline.get(0);
				}
			} else {
				song = timeline.get(pos);
			}
		}

		if (song == null)
			// we have no songs in the library
			return null;

		return song;
	}

	/**
	 * Internal implementation for shiftCurrentSong. Does all the work except
	 * broadcasting the timeline change: updates mCurrentPos and handles
	 * shuffling, repeating, and random mode.
	 *
	 * @param delta -1 to move to the previous song or 1 for the next.
	 */
	private void shiftCurrentSongInternal(int delta)
	{
		int pos = mCurrentPos + delta;

		if (mFinishAction != FINISH_RANDOM && pos == mSongs.size()) {
			if (mShuffleMode != SHUFFLE_NONE && !mSongs.isEmpty()) {
				if (mShuffledSongs == null)
					shuffleAll();
				mSongs = mShuffledSongs;
			}

			pos = 0;
		} else if (pos < 0) {
			if (mFinishAction == FINISH_RANDOM)
				pos = 0;
			else
				pos = Math.max(0, mSongs.size() - 1);
		}

		mCurrentPos = pos;
		mShuffledSongs = null;
	}

	/**
	 * Move to the next or previous song or album.
	 *
	 * @param delta One of SongTimeline.SHIFT_*.
	 * @return The Song at the new position
	 */
	public AudioGuide shiftCurrentSong(int delta)
	{
		synchronized (this) {
			if (delta == SHIFT_PREVIOUS_AUDIO_GUIDE|| delta == SHIFT_NEXT_AUDIO_GUIDE) {
				shiftCurrentSongInternal(delta);
			} else {
				AudioGuide song = getSong(0);
				Museum currentAlbum = song.getMuseum();
				long currentSong = song.getId();
				delta = delta > 0 ? 1 : -1;
				do {
					shiftCurrentSongInternal(delta);
					song = getSong(0);
				} while (currentAlbum.getShortName().equals(song.getMuseum().getShortName())
						&& currentSong != song.getId());
			}
		}
		changed();
		return getSong(0);
	}

	/**
	 * Run the given query and add the results to the song timeline.
	 *
	 * @param context A context to use.
	 * @param query The query to be run. The mode variable must be initialized
	 * to one of SongTimeline.MODE_*. The type and data variables may also need
	 * to be initialized depending on the given mode.
	 * @return The number of songs that were added.
	 */
	public int addSongs(Context context, QueryTask query)
	{
//		Cursor cursor = query.runQuery(context.getContentResolver());
//		if (cursor == null) {
//			return 0;
//		}
//
		int count = 0;
//		if (count == 0) {
//			return 0;
//		}

		int mode 		= query.mode;
		long data 		= query.data;
		String language = query.language;
		Museum musueum 	= query.museum;
		boolean isFree  = query.isFree;

		ArrayList<AudioGuide> timeline = mSongs;
		synchronized (this) {
			saveActiveSongs();

			switch (mode) {
			case MODE_ENQUEUE:
			case MODE_ENQUEUE_POS_FIRST:
			case MODE_ENQUEUE_ID_FIRST:
				if (mFinishAction == FINISH_RANDOM) {
					int j = timeline.size();
					while (--j > mCurrentPos) {
						if (timeline.get(j).isRandom())
							timeline.remove(j);
					}
				}
				break;
			case MODE_PLAY_NEXT:
				timeline.subList(mCurrentPos + 1, timeline.size()).clear();
				break;
			case MODE_PLAY:
			case MODE_PLAY_POS_FIRST:
			case MODE_PLAY_ID_FIRST:
				timeline.clear();
				mCurrentPos = 0;
				break;
			default:
				throw new IllegalArgumentException("Invalid mode: " + mode);
			}

			int start = 0;
			TableAudioGuide.populateAudioGuides(musueum,language,isFree, timeline);
			
			count = timeline.size();
			AudioGuide jumpSong = null;
		
			for (int j = 0; j != count; ++j) {
				AudioGuide song = timeline.get(j);
				if (jumpSong == null) {
					if ((mode == MODE_PLAY_POS_FIRST || mode == MODE_ENQUEUE_POS_FIRST) && j == data) {
						jumpSong = song;
					} else if (mode == MODE_PLAY_ID_FIRST || mode == MODE_ENQUEUE_ID_FIRST) {
						long id = song.getId();
						if (id == data)
							jumpSong = song;
					}
				}
			}

			if (jumpSong != null) {
				int jumpPos = timeline.indexOf(jumpSong);
				if (jumpPos != start) {
					// Get the sublist twice to avoid a ConcurrentModificationException.
					timeline.addAll(timeline.subList(start, jumpPos));
					timeline.subList(start, jumpPos).clear();
				}
			}

			broadcastChangedSongs();
		}

		changed();

		return count;
	}

	/**
	 * Removes any songs greater than 10 songs before the current song when in
	 * random mode.
	 */
	public void purge()
	{
		synchronized (this) {
			if (mFinishAction == FINISH_RANDOM) {
				while (mCurrentPos > 10) {
					mSongs.remove(0);
					--mCurrentPos;
				}
			}
		}
	}

	/**
	 * Clear the song queue.
	 */
	public void clearQueue()
	{
		synchronized (this) {
			if (mCurrentPos + 1 < mSongs.size())
				mSongs.subList(mCurrentPos + 1, mSongs.size()).clear();
		}

		mCallback.activeSongReplaced(+1, getSong(+1));
		mCallback.positionInfoChanged();

		changed();
	}
	
	public void clearAll()
	{
		synchronized (this) {
			mSongs.clear();
		}

//		mCallback.activeSongReplaced(+1, getSong(+1));
//		mCallback.positionInfoChanged();

		changed();
	}

	/**
	 * Save the active songs for use with broadcastChangedSongs().
	 *
	 * @see AudioGuideTimeline#broadcastChangedSongs()
	 */
	private void saveActiveSongs()
	{
		mSavedPrevious = getSong(-1);
		mSavedCurrent = getSong(0);
		mSavedNext = getSong(+1);
		mSavedPos = mCurrentPos;
		mSavedSize = mSongs.size();
	}

	/**
	 * Broadcast the active songs that have changed since the last call to
	 * saveActiveSongs()
	 *
	 * @see AudioGuideTimeline#saveActiveSongs()
	 */
	private void broadcastChangedSongs()
	{
		AudioGuide previous = getSong(-1);
		AudioGuide current = getSong(0);
		AudioGuide next = getSong(+1);

		if (AudioGuide.getId(mSavedPrevious) != AudioGuide.getId(previous))
			mCallback.activeSongReplaced(-1, previous);
		if (AudioGuide.getId(mSavedNext) != AudioGuide.getId(next))
			mCallback.activeSongReplaced(1, next);
		if (AudioGuide.getId(mSavedCurrent) != AudioGuide.getId(current))
			mCallback.activeSongReplaced(0, current);

		if (mCurrentPos != mSavedPos || mSongs.size() != mSavedSize)
			mCallback.positionInfoChanged();
	}

	/**
	 * Remove the song with the given id from the timeline.
	 *
	 * @param id The MediaStore id of the song to remove.
	 */
	public void removeSong(long id)
	{
		synchronized (this) {
			saveActiveSongs();

			ArrayList<AudioGuide> songs = mSongs;
			ListIterator<AudioGuide> it = songs.listIterator();
			while (it.hasNext()) {
				int i = it.nextIndex();
				if (AudioGuide.getId(it.next()) == id) {
					if (i < mCurrentPos)
						--mCurrentPos;
					it.remove();
				}
			}

			broadcastChangedSongs();
		}

		changed();
	}

	/**
	 * Broadcasts that the timeline state has changed.
	 */
	private void changed()
	{
		if (mCallback != null)
			mCallback.timelineChanged();
	}

	/**
	 * Return true if the finish action is to stop at the end of the queue and
	 * the current song is the last in the queue.
	 */
	public boolean isEndOfQueue()
	{
		synchronized (this) {
			return mFinishAction == FINISH_STOP && mCurrentPos == mSongs.size() - 1;
		}
	}

	/**
	 * Returns the position of the current song in the timeline.
	 */
	public int getPosition()
	{
		return mCurrentPos;
	}

	/**
	 * Returns the current number of songs in the timeline.
	 */
	public int getLength()
	{
		return mSongs.size();
	}
}
