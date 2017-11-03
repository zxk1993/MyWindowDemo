package com.example.zxk.mywindowdemo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZXK on 2017/10/30.
 */

public class WindowController {

    private final int HIDE = 0x001;
    private final int SHOW = 0x002;
    private final int REFRESH = 0x003;
    private final int CLICK = 0x004;

    private static WindowController instance;
    private Context context;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wParamsTop, wParamsBottom;
    //最小滑动距离
    private int mTouchSlop;
    //上下两个布局
    private LinearLayout layoutTop, layoutBottom;
    private TextView top;
    private ViewPager viewPager;
    //viewPager的adapter
    private WindowAdapter windowAdapter;
    //viewPager每一页的Adapter
    private ViewAdapter viewAdapter;
    private RadioGroup radioGroup;
    //存放数据
    private List<String> mDatas;
    //存放每一页的数据
    private List<String> pageData;
    //存放视图
    private List<View> viewList;

    private int lastX, lastY;
    private int downX, downY;
    //down 时的时间戳
    private int downTime;
    //屏幕宽高
    private int screenWidth, screenHeight;
    //top xy坐标, bottom y坐标
    private int topY, bottomY, topX;
    //top 的宽高
    private int topWidth, topHeight;
    //bottom 的高
    private int bottomHeight;
    //状态栏的高度
    private int statusBarHeight;
    //展开 or 收起
    private boolean isShow;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE:
                    hideBottom();
                    break;
                case SHOW:
                    showBottom();
                    break;
                case REFRESH:
                    mWindowManager.updateViewLayout(layoutTop, wParamsTop);
                    if (isShow) {
                        mWindowManager.updateViewLayout(layoutBottom, wParamsBottom);
                    }
                    break;
                case CLICK:
                    click();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    };

    private WindowController(Context context) {
        this.context = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDatas = new ArrayList<>();
        viewList = new ArrayList<>();
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        //需要减去状态栏高度
        screenHeight = mWindowManager.getDefaultDisplay().getHeight() - statusBarHeight;
        topWidth = WindowHelper.dip2px(context, 40);
        topHeight = WindowHelper.dip2px(context, 40);
        bottomHeight = WindowHelper.dip2px(context, 100);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public static WindowController getInstance(Context context) {
        if (instance == null) {
            synchronized (WindowController.class) {
                if (instance == null) {
                    instance = new WindowController(context);
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init() {
        topX = WindowHelper.getCoordinateX(context);
        topY = WindowHelper.getCoordinateY(context);

        bottomY = topY + topHeight;

        initTop();
        initBottom();

        mWindowManager.addView(layoutTop, wParamsTop);
    }

    /**
     * 初始化top视图
     */
    private void initTop() {
        layoutTop = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.window_top, null);
        top = layoutTop.findViewById(R.id.top);

        //监听触摸事件,实现拖动和点击。
        top.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.e("ZXK", "MotionEvent.ACTION_DOWN");
                        downTime = (int) System.currentTimeMillis();

                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        //保留相对距离，后面可以通过绝对坐标算出真实坐标
                        downX = (int) event.getX();
                        downY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:

                        Log.e("ZXK", "MotionEvent.ACTION_MOVE");

                        if (Math.abs(event.getRawX() - lastX) > 0 || Math.abs(event.getRawY() - lastY) > 0) {
                            topX = (int) (event.getRawX() - downX);
                            //需要减去状态栏高度
                            topY = (int) (event.getRawY() - statusBarHeight - downY);

                            //top左右不能越界
                            if (topX < 0) {
                                topX = 0;
                            } else if ((topX + topWidth) > screenWidth) {
                                topX = screenWidth - topWidth;
                            }
                            wParamsTop.x = topX;

                            //top上下不能越界
                            if (topY < 0) {
                                topY = 0;
                            } else if ((topY + topHeight) > screenHeight) {
                                topY = screenHeight - topHeight;
                            }
                            wParamsTop.y = topY;

                            if (screenHeight - topY - topHeight < bottomHeight) {
                                bottomY = topY - bottomHeight;
                            } else {
                                bottomY = topY + topHeight;
                            }
                            wParamsBottom.y = bottomY;

                            lastX = (int) event.getRawX();
                            lastY = (int) event.getRawY();

                            handler.sendEmptyMessage(REFRESH);
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        int currentTime = (int) System.currentTimeMillis();
                        if (currentTime - downTime < 200 && Math.abs(event.getRawX() - lastX) < mTouchSlop && Math.abs(event.getRawY() - lastY) < mTouchSlop) {
                            handler.sendEmptyMessage(CLICK);
                        }

                        //保留坐标
                        WindowHelper.setCoordinateX(context, topX);
                        WindowHelper.setCoordinateY(context, topY);
                        break;
                }
                return true;
            }
        });

        wParamsTop = new WindowManager.LayoutParams();
        wParamsTop.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wParamsTop.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //初始化坐标
        wParamsTop.x = topX;
        wParamsTop.y = topY;
        //弹窗类型
        wParamsTop.type = WindowManager.LayoutParams.TYPE_PHONE;
        //以左上角为基准
        wParamsTop.gravity = Gravity.START | Gravity.TOP;
        wParamsTop.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //如果不加,背景会是一片黑色。
        wParamsTop.format = PixelFormat.RGBA_8888;
    }

    /**
     * 初始化bottom视图
     */
    private void initBottom() {
        layoutBottom = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.window_bottom, null);
        viewPager = layoutBottom.findViewById(R.id.viewPager);
        radioGroup = layoutBottom.findViewById(R.id.radioGroup);
        ((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((RadioButton) radioGroup.getChildAt(position)).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        wParamsBottom = new WindowManager.LayoutParams();
        wParamsBottom.width = WindowManager.LayoutParams.MATCH_PARENT;
        wParamsBottom.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wParamsBottom.x = 0;
        wParamsBottom.y = bottomY;
        wParamsBottom.type = WindowManager.LayoutParams.TYPE_PHONE;
        wParamsBottom.gravity = Gravity.START | Gravity.TOP;
        wParamsBottom.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wParamsBottom.format = PixelFormat.RGBA_8888;
    }


    /**
     * 收起
     */
    private void hideBottom() {
        if (isShow) {
            try {
                mWindowManager.removeView(layoutBottom);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isShow = false;
    }

    /**
     * 展开
     */
    private void showBottom() {
        if (!isShow) {
            //有些点击事件不会触发MotionEvent.ACTION_MOVE,如果top布局在屏幕底部,会出现bug
            if (screenHeight - topY - topHeight < bottomHeight) {
                bottomY = topY - bottomHeight;
            } else {
                bottomY = topY + topHeight;
            }
            wParamsBottom.y = bottomY;

            //加入底部视图
            mWindowManager.addView(layoutBottom, wParamsBottom);

            //初始化底部视图
            initData();
        }
        isShow = true;
    }

    /**
     * 点击
     */
    private void click() {
        if (isShow) {
            handler.sendEmptyMessage(HIDE);
        } else {
            handler.sendEmptyMessage(SHOW);
        }
    }

    /**
     * 初始化数据并更新视图
     */
    private void initData() {
        mDatas.clear();
        for(int i = 0;i<9;i++){
            mDatas.add(i+"");
        }

        int pageSize = (mDatas.size()+2)/3;

        for(int i = 0;i<pageSize;i++){
            LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.window_bottom_item, null);
            RecyclerView recyclerView = layout.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(context,3));
            pageData = new ArrayList<>();
            viewAdapter = new ViewAdapter(context,pageData);
            recyclerView.setAdapter(viewAdapter);

            for(int j = 3*i;j<Math.min(mDatas.size(), (i + 1) * 3);j++){
                pageData.add(mDatas.get(j));
            }
            /*if(pageData.size()>0){
                viewAdapter.notifyDataSetChanged();
            }*/
            viewList.add(layout);
        }

        windowAdapter = new WindowAdapter(viewList);
        viewPager.setAdapter(windowAdapter);

        if(viewList.size()>0){
            //windowAdapter.notifyDataSetChanged();
            handler.sendEmptyMessage(REFRESH);
        }
    }

    public void onDestroy() {
        if (mWindowManager != null) {
            try {
                if (isShow) {
                    hideBottom();
                }
                mWindowManager.removeView(layoutTop);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
