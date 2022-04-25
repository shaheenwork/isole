
package com.ezenit.mediaplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;

/**
 * Class containing utility functions to create Bitmaps display song info and
 * album art.
 */
public final class CoverBitmap {
	/**
	 * Draw cover in background and a box with song info on top.
	 */
	public static final int STYLE_OVERLAPPING_BOX = 0;
	/**
	 * Draw cover on top or left with song info on bottom or right (depending
	 * on orientation).
	 */
	public static final int STYLE_INFO_BELOW = 1;
	/**
	 * Draw no song info, only the cover.
	 */
	public static final int STYLE_NO_INFO = 2;

	private static int TEXT_SIZE = -1;
	private static int TEXT_SIZE_BIG;
	private static int PADDING;
	private static int TEXT_SPACE;
	private static Bitmap SONG_ICON;
	private static Bitmap ALBUM_ICON;
	private static Bitmap ARTIST_ICON;

	/**
	 * Initialize the regular text size members.
	 *
	 * @param context A context to use.
	 */
	private static void loadTextSizes(Context context)
	{
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		TEXT_SIZE = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, metrics);
		TEXT_SIZE_BIG = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, metrics);
		PADDING = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
		TEXT_SPACE = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, metrics);
	}

//	/**
//	 * Initialize the icon bitmaps.
//	 *
//	 * @param context A context to use.
//	 */
//	private static void loadIcons(Context context)
//	{
//		Resources res = context.getResources();
//		SONG_ICON = BitmapFactory.decodeResource(res, R.drawable.ic_tab_songs_selected);
//		ALBUM_ICON = BitmapFactory.decodeResource(res, R.drawable.ic_tab_albums_selected);
//		ARTIST_ICON = BitmapFactory.decodeResource(res, R.drawable.ic_tab_artists_selected);
//	}

	/**
	 * Helper function to draw text within a set width
	 *
	 * @param canvas The canvas to draw to
	 * @param text The text to draw
	 * @param left The x coordinate of the left edge of the text
	 * @param top The y coordinate of the top edge of the text
	 * @param width The measured width of the text
	 * @param maxWidth The maximum width of the text. Text outside of this width
	 * will be truncated.
	 * @param paint The paint style to use
	 */
	private static void drawText(Canvas canvas, String text, int left, int top, int width, int maxWidth, Paint paint)
	{
		canvas.save();
		int offset = Math.max(0, maxWidth - width) / 2;
		canvas.clipRect(left, top, left + maxWidth, top + paint.getTextSize() * 2);
		canvas.drawText(text, left + offset, top - paint.ascent(), paint);
		canvas.restore();
	}

	/**
	 * Create an image representing the given song. Includes cover art and
	 * possibly song title/artist/ablum, depending on the given style.
	 *
	 * @param context A context to use.
	 * @param style One of CoverBitmap.STYLE_*
	 * @param coverArt The cover art for the song.
	 * @param song Title and other data are taken from here for info modes.
	 * @param width Maximum width of image
	 * @param height Maximum height of image
	 * @return The image, or null if the song was null, or width or height
	 * were less than 1
	 */
	public static Bitmap createBitmap(Context context, int style, Bitmap coverArt, AudioGuide song, int width, int height)
	{
		switch (style) {
		case STYLE_OVERLAPPING_BOX:
			return createOverlappingBitmap(context, coverArt, song, width, height);
		case STYLE_INFO_BELOW:
			return createSeparatedBitmap(context, coverArt, song, width, height);
		case STYLE_NO_INFO:
			return createScaledBitmap(coverArt, width, height);
		default:
			throw new IllegalArgumentException("Invalid bitmap type given: " + style);
		}
	}

	private static Bitmap createOverlappingBitmap(Context context, Bitmap cover, AudioGuide guide, int width, int height)
	{
		if (TEXT_SIZE == -1)
			loadTextSizes(context);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		String title = guide.getTitle() == null ? "" : guide.getTitle();
		String museum = guide.getMuseum().getShortName() == null ? "" : guide.getMuseum().getShortName();
		

		int titleSize = TEXT_SIZE_BIG;
		int subSize = TEXT_SIZE;
		int padding = PADDING;

		paint.setTextSize(titleSize);
		int titleWidth = (int)paint.measureText(title);
		paint.setTextSize(subSize);
		int museumWidth = (int)paint.measureText(museum);

		int boxWidth = Math.min(width, Math.max(titleWidth,  museumWidth) + padding * 2);
		int boxHeight = Math.min(height, titleSize + subSize * 2 + padding * 4);

		int coverWidth;
		int coverHeight;

		if (cover == null) {
			coverWidth = 0;
			coverHeight = 0;
		} else {
			coverWidth = cover.getWidth();
			coverHeight = cover.getHeight();

			float scale = Math.min((float)width / coverWidth, (float)height / coverHeight);

			coverWidth *= scale;
			coverHeight *= scale;
		}

		int bitmapWidth = Math.max(coverWidth, boxWidth);
		int bitmapHeight = Math.max(coverHeight, boxHeight);

		Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);

		if (cover != null) {
			int x = (bitmapWidth - coverWidth) / 2;
			int y = (bitmapHeight - coverHeight) / 2;
			Rect rect = new Rect(x, y, x + coverWidth, y + coverHeight);
			canvas.drawBitmap(cover, null, rect, paint);
		}

		int left = (bitmapWidth - boxWidth) / 2;
		int top = (bitmapHeight - boxHeight) / 2;
		int right = (bitmapWidth + boxWidth) / 2;
		int bottom = (bitmapHeight + boxHeight) / 2;

		paint.setARGB(150, 0, 0, 0);
		canvas.drawRect(left, top, right, bottom, paint);

		int maxWidth = boxWidth - padding * 2;
		paint.setARGB(255, 255, 255, 255);
		top += padding;
		left += padding;

		paint.setTextSize(titleSize);
		drawText(canvas, title, left, top, titleWidth, maxWidth, paint);
		top += titleSize + padding;

		paint.setTextSize(subSize);
		drawText(canvas, museum, left, top, museumWidth, maxWidth, paint);
		top += subSize + padding;


		return bitmap;
	}

	private static Bitmap createSeparatedBitmap(Context context, Bitmap cover, AudioGuide guide, int width, int height)
	{
		if (TEXT_SIZE == -1)
			loadTextSizes(context);
//		if (SONG_ICON == null)
//			loadIcons(context);

		boolean horizontal = width > height;

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		String title = guide.getTitle() == null ? "" : guide.getTitle();
		String museum = guide.getMuseum().getShortName() == null ? "" : guide.getMuseum().getShortName();

		int textSize = TEXT_SIZE;
		int padding = PADDING;

		int coverWidth;
		int coverHeight;

		if (cover == null) {
			coverWidth = 0;
			coverHeight = 0;
		} else {
			coverWidth = cover.getWidth();
			coverHeight = cover.getHeight();

			int maxWidth = horizontal ? width - TEXT_SPACE : width;
			int maxHeight = horizontal ? height : height - textSize * 3 - padding * 4;
			float scale = Math.min((float)maxWidth / coverWidth, (float)maxHeight / coverHeight);

			coverWidth *= scale;
			coverHeight *= scale;
		}

		paint.setTextSize(textSize);
		int titleWidth = (int)paint.measureText(title);
		int museumWidth = (int)paint.measureText(museum);

		int maxBoxWidth = horizontal ? width - coverWidth : width;
		int maxBoxHeight = horizontal ? height : height - coverHeight;
		int boxWidth = Math.min(maxBoxWidth, textSize + Math.max(titleWidth,  museumWidth) + padding * 3);
		int boxHeight = Math.min(maxBoxHeight, textSize * 3 + padding * 4);

		int bitmapWidth = horizontal ? coverWidth + boxWidth : Math.max(coverWidth, boxWidth);
		int bitmapHeight = horizontal ? Math.max(coverHeight, boxHeight) : coverHeight + boxHeight;

		Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);

		if (cover != null) {
			int x = horizontal ? 0 : (bitmapWidth - coverWidth) / 2;
			int y = horizontal ? (bitmapHeight - coverHeight) / 2 : 0;
			Rect rect = new Rect(x, y, x + coverWidth, y + coverHeight);
			canvas.drawBitmap(cover, null, rect, paint);
		}

		int top;
		int left;

		if (horizontal) {
			top = (bitmapHeight - boxHeight) / 2;
			left = padding + coverWidth;
		} else {
			top = padding + coverHeight;
			left = padding;
		}

		int maxWidth = boxWidth - padding * 3 - textSize;
		paint.setARGB(255, 255, 255, 255);

		canvas.drawBitmap(SONG_ICON, left, top, paint);
		drawText(canvas, title, left + padding + textSize, top, maxWidth, maxWidth, paint);
		top += textSize + padding;

		canvas.drawBitmap(ALBUM_ICON, left, top, paint);
		drawText(canvas, museum, left + padding + textSize, top, maxWidth, maxWidth, paint);
		top += textSize + padding;

		return bitmap;
	}

	/**
	 * Scales a bitmap to fit in a rectangle of the given size. Aspect ratio is
	 * preserved. At least one dimension of the result will match the provided
	 * dimension exactly.
	 *
	 * @param source The bitmap to be scaled
	 * @param width Maximum width of image
	 * @param height Maximum height of image
	 * @return The scaled bitmap.
	 */
	private static Bitmap createScaledBitmap(Bitmap source, int width, int height)
	{
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		float scale = Math.min((float)width / sourceWidth, (float)height / sourceHeight);
		sourceWidth *= scale;
		sourceHeight *= scale;
		return Bitmap.createScaledBitmap(source, sourceWidth, sourceHeight, false);
	}

	/**
	 * Generate the default cover (a rendition of a CD). Returns a square iamge.
	 * Both dimensions are the lesser of width and height.
	 *
	 * @param width The max width
	 * @param height The max height
	 * @return The default cover.
	 */
	public static Bitmap generateDefaultCover(int width, int height)
	{
		int size = Math.min(width, height);
		int halfSize = size / 2;
		int eightSize = size / 8;

		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
		LinearGradient gradient = new LinearGradient(size, 0, 0, size, 0xff646464, 0xff464646, Shader.TileMode.CLAMP);
		RectF oval = new RectF(eightSize, 0, size - eightSize, size);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		Canvas canvas = new Canvas(bitmap);
		canvas.rotate(-45, halfSize, halfSize);

		paint.setShader(gradient);
		canvas.translate(size / 20, size / 20);
		canvas.scale(0.9f, 0.9f);
		canvas.drawOval(oval, paint);

		paint.setShader(null);
		paint.setColor(0xff000000);
		canvas.translate(size / 3, size / 3);
		canvas.scale(0.333f, 0.333f);
		canvas.drawOval(oval, paint);

		paint.setShader(gradient);
		canvas.translate(size / 3, size / 3);
		canvas.scale(0.333f, 0.333f);
		canvas.drawOval(oval, paint);

		return bitmap;
	}
}
