package com.ezenit.isoleborromee.fragments;

import gmail.chenyoca.imagemap.ImageMap;
import gmail.chenyoca.imagemap.ImageMap.OnBubbleShowListener;
import gmail.chenyoca.imagemap.TouchImageView.OnEdgeReachListener;
import gmail.chenyoca.imagemap.support.Bubble.RenderDelegate;
import gmail.chenyoca.imagemap.support.CircleShape;
import gmail.chenyoca.imagemap.support.PolyShape;
import gmail.chenyoca.imagemap.support.Shape;
import gmail.chenyoca.imagemap.support.TextShape;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.adapter.AdapterAudioGuide;
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


public class FragmentMap extends Fragment {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final String ARG_IMG_SECTION = "com.ezenit.isoleborromee.fragments.FragmentMap.ARG_IMG_SECTION";
    private static final String ARG_MUSEUM = "com.ezenit.isoleborromee.fragments.FragmentMap.ARG_MUSEUM";
    private static final String ARG_LANGUAGE = "com.ezenit.isoleborromee.fragments.FragmentMap.ARG_LANGUAGE";
    private static final String ARG_IS_FREE = "com.ezenit.isoleborromee.fragments.FragmentMap.ARG_IS_FREE";
    //resd
    private static final int COLOR_MAP_HIGHLIGHT = Color.rgb(207, 51, 60);
    private static final int COLOR_CIRCLE = Color.parseColor("#699099");


    private static final String TAG = "FragmentMap";
    // ===========================================================
    // Fields
    // ===========================================================

    private ImageMap map;
    private OnMapClickListener mapClickListener;
    private ImageLoader imgLoader;

    private View btnLeft;
    private TextView fieldSubTitle;
    private TextView fieldTitle;


    private ArrayList<Long> oldIds;
    private int textSize;
    private int circleRadius;

    //	private boolean     isFocused = true;
    // ===========================================================
    // Constructors
    // ===========================================================
    public static FragmentMap getInstance(int section, Museum museum, String language, boolean isFree) {
        FragmentMap fragmentMap = new FragmentMap();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_IMG_SECTION, section);
        bundle.putString(ARG_MUSEUM, museum.getShortName());
        bundle.putString(ARG_LANGUAGE, language);
        bundle.putBoolean(ARG_IS_FREE, isFree);
        fragmentMap.setArguments(bundle);
        return fragmentMap;
    }

    public static FragmentMap getInstance(int section, AudioGuide guide) {
        // TODO Auto-generated method stub
        return getInstance(section, guide.getMuseum(), guide.getLanguageShort(), guide.isFree());
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public boolean getFocused() {
        // TODO Auto-generated method stub
        return true;
    }
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
            this.mapClickListener = (OnMapClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMapClickListener");
        }
        oldIds = new ArrayList<Long>();
        textSize = (int) getResources().getDimension(R.dimen.font_map);
        circleRadius = (int) getResources().getDimension(R.dimen.circle_radius);

    }

    public boolean isReachedLeftEdge() {
        if (map == null)
            return false;
        return map.isReachedLeftSide();
    }

    public boolean isReachedRightEdge() {
        if (map == null)
            return false;
        return map.isReachedRightSide();
    }

    int widthRoot = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fr_imagemap, null);
        root.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick() called with: v = [" + v + "]");
            }
        });
        map = (ImageMap) root.findViewById(R.id.imagemap);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("maplist", "onClick: " + "maplist");
            }
        });
        map.setOnEdgeListener(new OnEdgeReachListener() {

            @Override
            public void onRightReached() {
                // TODO Auto-generated method stub
                if (mapClickListener != null)
                    mapClickListener.onMapEdgeReached();
            }

            @Override
            public void onOutOfEdged() {
                // TODO Auto-generated method stub
                if (mapClickListener != null)
                    mapClickListener.onMapOutOfEdge();
            }

            @Override
            public void onLeftReached() {
                // TODO Auto-generated method stub
                if (mapClickListener != null)
                    mapClickListener.onMapEdgeReached();
            }
        });

        imgLoader = ImageLoader.getInstance();
        View bubbleView = inflater.inflate(R.layout.map_bubble, container, false);

        int section = getArguments().getInt(ARG_IMG_SECTION);
        Museum museum = Museum.getMuseum(getArguments().getString(ARG_MUSEUM));
        String language = getArguments().getString(ARG_LANGUAGE);
        boolean isFree = getArguments().getBoolean(ARG_IS_FREE);

        setUpMap(root, section, museum, language, isFree, bubbleView, container);

//		setCustomActionBar(inflater,(ViewGroup) root, museum);
        return root;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
//		resetActionBar();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void setUpMap(final View root, final int section, final Museum museum
            , final String language, final boolean isFree, final View bubbleView, final ViewGroup container) {
        PopupWindow popupWindow = new PopupWindow();
        popupWindow.setContentView(bubbleView);
//		AppIsole.getMapImage(getActivity(),museum,section);
        final File imageFile = AppIsole.getMapImage(getActivity(), museum, section);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                int width = root.getWidth();
                int height = (int) (width / MiscUtils.getBitmapAspect(imageFile));
                System.out.println("Image File : " + imageFile);
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
                        // TODO Auto-generated method stub
                        map.setMapBitmap(loadedImage);
                        PointF imgSize = MiscUtils.getBitmapSize(imageFile);
//						Log.d("Tag", "Size "+imgSize.x+","+imgSize+","+map.getHeight()+",."+loadedImage.getHeight());
                        if (loadedImage != null)
                            root.setBackgroundColor(loadedImage.getPixel(0, 0));


                        ArrayList<Room> rooms = TableMap.getAllRooms(museum, section);
                        float scale = loadedImage.getHeight() / imgSize.y;
                        for (Room room : rooms) {
                                Log.d(TAG, "out room: "+room.getRoomNo());
                                PolyShape polygon = new PolyShape(room.getId(), COLOR_MAP_HIGHLIGHT);
                                polygon.setValues(room.getCoords(), scale);
                                map.addShapeAndRefToBubble(polygon);
                                long roomId = room.getId();
                                String coords = room.getCircleCoords();
                                String roomNumber = mTrimRoomNumber(room.getRoomNo());


                                TextShape textShape = new TextShape(String.valueOf(roomNumber), roomId + "_circle", Color.WHITE);
                                textShape.setValues(coords, scale);
                                textShape.setTextSize(textSize);
                                textShape.setTextAlign(Align.CENTER);
                                textShape.visible = true;
                                textShape.zIndex = 2;
                                CircleShape circle = new CircleShape(roomId + "_no", COLOR_CIRCLE);
                                ///radius old 20 new 26 and textsize old 16 new 14

                                circle.setRadius(circleRadius);
                                circle.setAlaph(255);
                                circle.setValues(coords, scale);
                                circle.visible = true;
                                circle.zIndex = 1;
                                map.addOverShape(circle);
                                map.addOverShape(textShape);

                        }

                            map.sortOverShapes();


                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        // TODO Auto-generated method stub

                    }
                });

                widthRoot = width;

                Rect parentBounds = new Rect(0, 0, root.getWidth(), root.getHeight());
                map.setBubbleView(bubbleView, new RenderDelegate() {

                    @Override
                    public void onDisplay(Shape shape, View bubbleView) {
                        // TODO Auto-generated method stub

                    }
                }, parentBounds);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });


        final ArrayList<BaseAudioGuide> audioGuides = new ArrayList<TableAudioGuide.BaseAudioGuide>();

        final ListView bubbleList = (ListView) bubbleView.findViewById(R.id.list);
       /* LinearLayout.LayoutParams layoutParams
                = new LinearLayout.LayoutParams((int) (widthRoot * 0.35f), LayoutParams.WRAP_CONTENT);
        bubbleList.setLayoutParams(layoutParams);*/
        final AdapterAudioGuide adapter = new AdapterAudioGuide(getActivity(), museum, language, audioGuides);
        bubbleList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adpView, View view, int position,
                                    long id) {
                if (mapClickListener != null)
                    mapClickListener.onMapAudioClicked(adapter.getItem(position));
            }
        });


        bubbleList.setAdapter(adapter);

        final float listMaxHeight = getResources().getDimension(R.dimen.popup_list_max_height);

        map.setOnBubbleShowListener(new OnBubbleShowListener() {

            @Override
            public void onBubbleShowed(Shape shape) {
                bubbleList.setVisibility(View.VISIBLE);
                //int 	section = getArguments().getInt(ARG_IMG_SECTION);
                Room room = TableMap.getRoom((Long) shape.tag);
                TableAudioGuide.populateAudioGuides(room, language, isFree, audioGuides);
                ///
               /* Intent intent = new Intent(getActivity(), AndroidDatabaseManager.class);
                startActivity(intent);*/
                adapter.changeSource(audioGuides);
                MiscUtils.setListViewHeightBasedOnChildren(bubbleList, listMaxHeight);


                ArrayList<Room> rooms = TableMap.getRooms(room);

                if (!oldIds.isEmpty()) {
                    for (Long id : oldIds) {
                        Shape oldCircle = map.getOverShape(id + "_no");
                        oldCircle.visible = true;
                    }
                    oldIds.clear();
                }


                for (Room currRoom : rooms) {
                    Shape currentCircle = map.getOverShape(currRoom.getId() + "_no");
                    currentCircle.visible = false;
                    oldIds.add(currRoom.getId());
                    Shape highlight = map.getShape(currRoom.getId());
                    highlight.visible = true;
                }


            }

            @Override
            public void closeBubbleList() {
                Log.d(TAG, "closeBubbleList() called");
                if (!oldIds.isEmpty()) {
                    for (Long id : oldIds) {
                        Shape oldCircle = map.getOverShape(id + "_no");
                        oldCircle.visible = true;
                    }
                    oldIds.clear();
                }
                bubbleList.setVisibility(View.INVISIBLE);

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

//	private void setCustomActionBar(LayoutInflater inflater,ViewGroup root
//			,Museum museum) {
//		ActionBar actionBar = getActivity().getActionBar();
//		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//		View actionBarView = inflater.inflate(R.layout.acbar_title_subtitle, root,false);
//		actionBar.setCustomView(actionBarView);



//		btnLeft = actionBarView.findViewById(R.id.btnLeft);
//		
//		btnLeft.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				getFragmentManager().popBackStack();
//			}
//		});
//		
//		fieldTitle 		= (TextView) actionBarView.findViewById(R.id.fieldTitle);
//		fieldSubTitle 	= (TextView) actionBarView.findViewById(R.id.fieldSubTitle);
//		
//		fieldTitle.setText(R.string.map_del_palazzo);
//		fieldSubTitle.setText(museum.getNameId());
//	
//		
//		fieldTitle.setVisibility(View.VISIBLE);
//		btnLeft.setVisibility(View.VISIBLE);
//		fieldSubTitle.setVisibility(View.VISIBLE);
//	}
//	
//	private void resetActionBar(){
//		fieldTitle.setVisibility(View.GONE);
//		btnLeft.setVisibility(View.GONE);
//		fieldSubTitle.setVisibility(View.GONE);
//	}

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================


    public interface OnMapClickListener {
        public void onMapAudioClicked(BaseAudioGuide guide);

        public void onMapEdgeReached();

        public void onMapOutOfEdge();
    }


}
