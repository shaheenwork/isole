package com.ezenit.isoleborromee.fragments;

import gmail.chenyoca.imagemap.HighlightImageView;
import gmail.chenyoca.imagemap.support.CircleShape;
import gmail.chenyoca.imagemap.support.PolyShape;
import gmail.chenyoca.imagemap.support.TextShape;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezenit.customview.AspectRatioImageView;
import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.db.table.TableAudioGuide;
import com.ezenit.isoleborromee.db.table.TableMap;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.BaseAudioGuide;
import com.ezenit.isoleborromee.db.table.TableMap.Room;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.utils.MiscUtils;
import com.ezenit.isoleborromee.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class FragmentAudioDetail extends Fragment {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final String ARG_CODE_NO = "com.ezenit.isoleborromee.fragment.FragmentAudioDetail.ARG_ID";
    private static final String ARG_MUSEUM = "com.ezenit.isoleborromee.fragment.FragmentAudioDetail.ARG_MUSUEM";
    private static final String ARG_LANGUAGE = "com.ezenit.isoleborromee.fragment.FragmentAudioDetail.ARG_LANGUAGE";
    private static final String ARG_IS_FREE = "com.ezenit.isoleborromee.fragment.FragmentAudioDetail.ARG_IS_FREE";
    private static final String ARG_IS_PLAYING = "com.ezenit.isoleborromee.fragment.FragmentAudioDetail.ARG_IS_PLAYING";

    private static final String TAG_PLAY = "play";
    private static final String TAG_PAUSE = "pause";

    private static final String TAG = FragmentAudioDetail.class.getName();


    private static final int COLOR_MAP_HIGHLIGHT = Color.rgb(207, 51, 60);
    private static final int COLOR_CIRCLE = Color.parseColor("#699099");
    private static final int COLOR_SELECTED_CIRCLE = Color.parseColor("#BC0000");

    // ===========================================================
    // Fields
    // ===========================================================

    private OnAudioDetailClickListener audioClickListener;
    private TextView lblPlay;
    private AudioGuide audioGuide;
//	private Museum	 museum;

    private boolean showedIntro = false;

    private View btnLeft;
    private TextView fieldSubTitle;
    private TextView fieldTitle;

    private int textSize;

    // ===========================================================
    // Constructors
    // ===========================================================
    public static FragmentAudioDetail getInstance(BaseAudioGuide audioGuide, boolean isPlaying) {
        FragmentAudioDetail fragment = new FragmentAudioDetail();
        Bundle arg = new Bundle();
        populateArgument(arg, audioGuide, isPlaying);
        fragment.setArguments(arg);

        return fragment;
    }

    public void updateInstance(BaseAudioGuide baseAudioGuide, boolean isPlaying) {

        audioGuide = TableAudioGuide.getAudioGuide(baseAudioGuide);
        Bundle arg = getArguments();
        populateArgument(arg, baseAudioGuide, isPlaying);
        populateView(audioGuide, true);

        updatePlayPauseButton(isPlaying);

        fieldTitle.setText(audioGuide.getTitle());
        fieldSubTitle.setText(audioGuide.getMuseum().getNameId());
    }


    // ===========================================================
    // Getter & Setter
    // ===========================================================

//	public void setOnAudioClickListener(
//			OnAudioDetailClickListener audioClickListener) {
//		this.audioClickListener = audioClickListener;
//	}


    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            audioClickListener = (OnAudioDetailClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
        textSize = (int) getResources().getDimension(R.dimen.font_map);
    }

    @Override
    public View onCreateView(LayoutInflater inflater
            , ViewGroup container
            , Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fr_audio_detail, container, false);


        String code = getArguments().getString(ARG_CODE_NO);
        String language = getArguments().getString(ARG_LANGUAGE);
        Museum museum = Museum.getMuseum(getArguments().getString(ARG_MUSEUM));
        boolean isFree = getArguments().getBoolean(ARG_IS_FREE);

        audioGuide = TableAudioGuide.getAudioGuide(code, language, museum, isFree);

        boolean isPlaying = getArguments().getBoolean(ARG_IS_PLAYING);

        populateView(audioGuide, root);
        setListeners(root);

//		setHasOptionsMenu(true);
        updatePlayPauseButton(isPlaying);

        setCustomActionBar(inflater, (ViewGroup) root, audioGuide);
        root.setOnTouchListener(rootTouchListener);
        return root;
    }


    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        resetActionBar();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mn_audio_guide_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLanguage(AppIsole.getAppLocaleAsStr());
        View root = getView();
        if (root != null && root.findViewById(R.id.fieldSearch) != null) {
            ((EditText) root.findViewById(R.id.fieldSearch))
                    .setHint(R.string.search_by_code);
        }
    }


    // ===========================================================
    // Methods
    // ===========================================================

    private void updateLanguage(String language) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String codeNo = bundle.getString(ARG_CODE_NO);
            Museum musueum = Museum.getMuseum(bundle.getString(ARG_MUSEUM));
            boolean isFree = bundle.getBoolean(ARG_IS_FREE);
            bundle.putString(ARG_LANGUAGE, language);

            audioGuide = TableAudioGuide.getAudioGuide(codeNo, language, musueum, isFree);
            if (audioGuide != null) {
                populateView(audioGuide, true);
            }
        }
    }

    private static void populateArgument(Bundle bundle, BaseAudioGuide audioGuide, boolean isPlaying) {
        bundle.putString(ARG_CODE_NO, audioGuide.getCodeNo());
        bundle.putString(ARG_MUSEUM, audioGuide.getMuseum().getShortName());
        bundle.putString(ARG_LANGUAGE, audioGuide.getLanguageShort());
        bundle.putBoolean(ARG_IS_FREE, audioGuide.isFree());
        bundle.putBoolean(ARG_IS_PLAYING, isPlaying);
    }

    private void populateView(AudioGuide audioGuide, boolean update) {
        View root = getView();
        if (root != null) {
            populateView(audioGuide, root, update);
        }
    }

    private void populateView(AudioGuide audioGuide, View root) {
        populateView(audioGuide, root, false);
    }

    private void populateView(AudioGuide audioGuide, final View root, boolean update) {


        TextView fieldTitle = (TextView) root.findViewById(R.id.fieldTitle);
        fieldTitle.setText(audioGuide.getTitle());

        TextView fieldCode = (TextView) root.findViewById(R.id.fieldCode);
        fieldCode.setText(audioGuide.getCodeNo());

        TextView fieldDescription = (TextView) root.findViewById(R.id.fieldDescription);
        fieldDescription.setText(audioGuide.getDescription());

        ImageView imgGallery = (ImageView) root.findViewById(R.id.imgAudio);
        ImageLoader imgLoader = ImageLoader.getInstance();

        final View galleryIndicator = root.findViewById(R.id.btnGallery);


        imgLoader.displayImage("file://" + AppIsole.getAudioCoverFolder(getActivity()
                , audioGuide) + File.separator + audioGuide.getCodeNo() + ".jpg", imgGallery,
                new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                                                FailReason failReason) {
                        ((AspectRatioImageView) view).setImageResource(R.drawable.bg_no_image_detail);
                        galleryIndicator.setVisibility(View.GONE);

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        ((AspectRatioImageView) view).setImageBitmap(loadedImage);

                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
//						((AspectRatioImageView)view).setImageResource(R.drawable.bg_no_image_detail);
//						galleryIndicator.setVisibility(View.GONE);

                    }
                });


        TextView fieldPosition = (TextView) root.findViewById(R.id.fieldPosition);
        TextView lblPosition = (TextView) root.findViewById(R.id.lblPosition);
        TextView lblhyphen = (TextView) root.findViewById(R.id.lblHyphen);

        if (TableAudioGuide.hasChildren(audioGuide)) {
            if (lblhyphen != null)
                lblhyphen.setVisibility(View.VISIBLE);
            lblPosition.setVisibility(View.VISIBLE);
            fieldPosition.setVisibility(View.VISIBLE);

            BaseAudioGuide parentGuide = TableAudioGuide.getBaseAudioGuide(audioGuide);
            Log.d(TAG, "populateView: " + parentGuide);
            Log.d(TAG, "populateView: " + parentGuide.getTitle());
            fieldPosition.setText(parentGuide.getTitle());
        } else {
            if (lblhyphen != null)
                lblhyphen.setVisibility(View.GONE);
            lblPosition.setVisibility(View.GONE);
            fieldPosition.setVisibility(View.GONE);


        }

        final ArrayList<Room> rooms = TableMap.getRooms(audioGuide.getRoom(), audioGuide.getMuseum());

        final HighlightImageView imageView = (HighlightImageView) root.findViewById(R.id.map);

        Log.d(TAG, "populateView: " + rooms);
        if (!rooms.isEmpty()) {

            final Room baseRoom = rooms.get(0);
            imageView.setOnTouchListener(null);
            imageView.setFocusable(false);
            imageView.setFocusableInTouchMode(false);
            imageView.clearShapes();
            final File imageFile = AppIsole.getMapImage(getActivity(), audioGuide.getMuseum(), baseRoom.getSection());


            root.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                @SuppressWarnings("deprecation")
                @SuppressLint("NewApi")
                @Override
                public void onGlobalLayout() {
                    // TODO Auto-generated method stub
                    int width = root.getWidth();
                    int height = (int) (width / MiscUtils.getBitmapAspect(imageFile));
                    ImageLoader imgLoader = ImageLoader.getInstance();
                    imgLoader.loadImage(Uri.fromFile(imageFile).toString(), new ImageSize(width, height), new ImageLoadingListener() {

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                                    FailReason failReason) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                            PointF imgSize = MiscUtils.getBitmapSize(imageFile);
                            float scale = loadedImage.getHeight() / imgSize.y;
                            for (Room room : rooms) {
                                imageView.setImageBitmap(loadedImage);
                                PolyShape shape = new PolyShape("Shape_" + room.getId(), COLOR_MAP_HIGHLIGHT);
                                shape.setValues(room.getCoords(), scale);
                                shape.visible = true;
                                shape.zIndex = 0;
                                imageView.addShape(shape);

                            }

                            ArrayList<Room> allrooms = TableMap.getAllRooms(baseRoom.getMusuem(), baseRoom.getSection());

                            Log.d(TAG, "onLoadingComplete: " + baseRoom.getMusuem());
                            for (Room room : allrooms) {
                                String roomNo = room.getRoomNo();
                                String coords = room.getCircleCoords();
                                String roomNumber = mTrimRoomNumber(roomNo);
                                TextShape textShape = new TextShape(String.valueOf(roomNumber), roomNo + "_circle", Color.WHITE);
                                textShape.setValues(coords, scale);
                                textShape.setTextSize(textSize);
                                textShape.setTextAlign(Align.CENTER);
                                textShape.visible = true;
                                textShape.zIndex = 2;
                                Log.d(TAG, "onLoadingComplete: " + baseRoom.getRoomNo());
                                if (roomNo != baseRoom.getRoomNo()) {
                                    CircleShape circle;
                                    if (Integer.parseInt(audioGuide.getRoom())==Integer.parseInt(roomNo)) {
                                         circle = new CircleShape(roomNo + "_no", COLOR_SELECTED_CIRCLE);
                                    }
                                    else {
                                        circle = new CircleShape(roomNo + "_no", COLOR_CIRCLE);
                                    }
                                    circle.setRadius(20);
                                    circle.setAlaph(255);
                                    circle.setValues(coords, scale);
                                    circle.visible = true;
                                    circle.zIndex = 1;
                                    imageView.addOverShape(circle);
                                }

                                imageView.addOverShape(textShape);

                            }
                            imageView.sortOverShapes();

                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            // TODO Auto-generated method stub

                        }
                    });

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                        root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    else
                        root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        } else {
            showMapImage(imageView, root);
        }

    }

    /**
     * in here 001P and 002P not in map.xml so we didn't have room number in the database so there for it don't the map
     * in here this code to show all the map contents and the map .
     *
     * @param imageView
     * @param root
     */
    private void showMapImage(final HighlightImageView imageView, final View root) {

        imageView.setOnTouchListener(null);
        imageView.setFocusable(false);
        imageView.setFocusableInTouchMode(false);
        imageView.clearShapes();
        final File imageFile = AppIsole.getMapImage(getActivity(), audioGuide.getMuseum(), 1);


        root.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                int width = root.getWidth();
                int height = (int) (width / MiscUtils.getBitmapAspect(imageFile));
                ImageLoader imgLoader = ImageLoader.getInstance();
                imgLoader.loadImage(Uri.fromFile(imageFile).toString(), new ImageSize(width, height), new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                                                FailReason failReason) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        Museum museum = Museum.getMuseum(getArguments().getString(ARG_MUSEUM));
                        PointF imgSize = MiscUtils.getBitmapSize(imageFile);
                        float scale = loadedImage.getHeight() / imgSize.y;
                        imageView.setImageBitmap(loadedImage);

                        ArrayList<Room> allrooms = TableMap.getAllRooms(museum, 1);
                        for (Room room : allrooms) {
                            String roomNo = room.getRoomNo();
                            String coords = room.getCircleCoords();
                            String roomNumber = mTrimRoomNumber(roomNo);
                            TextShape textShape = new TextShape(String.valueOf(roomNumber), roomNo + "_circle", Color.WHITE);
                            textShape.setValues(coords, scale);
                            textShape.setTextSize(textSize);
                            textShape.setTextAlign(Align.CENTER);
                            textShape.visible = true;
                            textShape.zIndex = 2;
                            CircleShape circle = new CircleShape(roomNo + "_no", COLOR_CIRCLE);
                            circle.setRadius(20);
                            circle.setAlaph(255);
                            circle.setValues(coords, scale);
                            circle.visible = true;
                            circle.zIndex = 1;
                            imageView.addOverShape(circle);
                            imageView.addOverShape(textShape);


                        }
                        imageView.sortOverShapes();

                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        // TODO Auto-generated method stub

                    }
                });

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            lblPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);
            lblPlay.setText(R.string.pause_audio);
            lblPlay.setTag(TAG_PAUSE);

        } else {
            lblPlay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0);
            lblPlay.setText(R.string.play_audio);
            lblPlay.setTag(TAG_PLAY);
        }
        getArguments().putBoolean(ARG_IS_PLAYING, isPlaying);
    }

    private void setListeners(View root) {
        View btnPlay = root.findViewById(R.id.btnPlay);
        lblPlay = (TextView) root.findViewById(R.id.lblPlayAudioDetail);
        btnPlay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (audioClickListener != null) {
                    audioClickListener.onAudioClicked(audioGuide, lblPlay.getTag().equals(TAG_PLAY));

                }
            }
        });

//		View btnGallery = root.findViewById(R.id.btnGallery);
//		btnGallery.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if(audioClickListener!=null)
//					audioClickListener.onAudioGalleryClicked(0, audioGuide);
//				
//			}
//		});

        ImageView image = (ImageView) root.findViewById(R.id.imgAudio);
        image.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (audioClickListener != null)
                    audioClickListener.onAudioGalleryClicked(0, audioGuide);
            }
        });
    }

    public static final String mTrimRoomNumber(String roomNumber) {
        int codeLength = roomNumber.length();
        int roomCode = 0;
        StringBuilder dataRoomCode = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            char item = roomNumber.charAt(i);
            if (Character.isDigit(item)) {
                roomCode = Integer.parseInt(String.valueOf(item));
                dataRoomCode.append(roomCode);
            }
        }
        return dataRoomCode.toString();
    }

    private void setCustomActionBar(LayoutInflater inflater, ViewGroup root
            , AudioGuide guide) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View actionBarView = inflater.inflate(R.layout.acbar_title_subtitle, root, false);
        actionBar.setCustomView(actionBarView);

        Toolbar toolbar=(Toolbar)actionBarView.getParent();
        toolbar.setContentInsetsAbsolute(0,0);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.getContentInsetEnd();
        toolbar.setPadding(0, 0, 0, 0);


        btnLeft = actionBarView.findViewById(R.id.btnLeft);

        btnLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        fieldTitle = (TextView) actionBarView.findViewById(R.id.fieldTitle);
        fieldSubTitle = (TextView) actionBarView.findViewById(R.id.fieldSubTitle);

        fieldTitle.setText(guide.getTitle());
        fieldSubTitle.setText(guide.getMuseum().getNameId());


        fieldTitle.setVisibility(View.VISIBLE);
        btnLeft.setVisibility(View.VISIBLE);
        fieldSubTitle.setVisibility(View.VISIBLE);
    }

    private void resetActionBar() {


        fieldTitle.setVisibility(View.GONE);
        btnLeft.setVisibility(View.GONE);
        fieldSubTitle.setVisibility(View.GONE);
    }


    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private float mLastTouchX;
    private float mLastTouchY;

    private int mActivePointerId;

    private boolean movingUpDown;
    private boolean movingSideWays;

    private OnTouchListener rootTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent ev) {


            final int action = ev.getAction();

            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = ev.getX();
                    final float y = ev.getY();

                    mLastTouchX = x;
                    mLastTouchY = y;

                    // Save the ID of this pointer
                    mActivePointerId = ev.getPointerId(0);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {

//			        final int pointerIndex = ev.getPointerId(0);
                    if (ev.getPointerCount() > 0) {
                        final float x = ev.getX(0);
                        final float y = ev.getY(0);

                        final float dx = x - mLastTouchX;
                        final float dy = y - mLastTouchY;

                        //			        mPosX += dx;
                        //			        mPosY += dy;
                        if ((dy > 10 || dy < -10)) {
                            movingUpDown = true;
                        } else if (!movingUpDown && (dx > 10 || dx < -10)) {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            movingSideWays = true;
                            return false;
                        }

                    }


                    break;
                }

                case MotionEvent.ACTION_UP: {
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    movingUpDown = false;
                    movingSideWays = false;
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    movingUpDown = false;
                    movingSideWays = false;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    // Extract the index of the pointer that left the touch sensor
                    final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    final int pointerId = ev.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchX = ev.getX(newPointerIndex);
                        mLastTouchY = ev.getY(newPointerIndex);
                        mActivePointerId = ev.getPointerId(newPointerIndex);
                    }
                    break;
                }
            }
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        }
    };

    public interface OnAudioDetailClickListener {
        public void onAudioClicked(AudioGuide audio, boolean pause);

        public void onMapClicked(AudioGuide museum);

        public void onAudioGalleryClicked(int index, AudioGuide guide);
    }

    public void onStateChange(BaseAudioGuide guide, boolean isPlaying) {
//		Log.d(TAG, "Playing state "+state+","+toggled);
//		if ((toggled & PlaybackService.FLAG_PLAYING) != 0 && lblPlay!= null) {
//			Log.e(TAG, "Inside Playing state "+state+","+toggled);
        if (guide != null && this.audioGuide != null && audioGuide.getId() == guide.getId()) {
            updatePlayPauseButton(isPlaying);
        } else {
            updatePlayPauseButton(false);
        }
//		}
    }

    public void onSongChange(AudioGuide audioGuide, boolean isPlaying) {
        if (audioGuide != null && this.audioGuide != null && audioGuide.getId() == this.audioGuide.getId()) {
            updatePlayPauseButton(isPlaying);
        } else {
            updatePlayPauseButton(false);
        }

    }
}
