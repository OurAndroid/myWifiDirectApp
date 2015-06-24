package com.wifidirect.ui;

import java.util.NoSuchElementException;



import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MyStickyLayout extends LinearLayout{

	private static final String TAG = "MyStickyLayout----";
	private static final boolean DEBUG = true;
	
	public interface OnGiveUpTouchEventListener{
		public boolean giveUpTouchEvent(MotionEvent event);
	}
	
	private View mHeader;
	private View mContent;
	private OnGiveUpTouchEventListener mGiveUpTouchEventListener;
	
	private int mOriginalHeaderHeight;
	private int mHeaderHeight;
	
	private int mStatus = STATUS_EXPANDED;
	public static final int STATUS_EXPANDED = 1;
	public static final int STATUS_COLLAPSED = 2;
	
	private int mTouchSlop;
	
	//记录窗口上次滑动到的坐标
	private int mLastX = 0;
	private int mLastY = 0;
	
	//记录上次手指滑动到的坐标
	private int mLastXIntercept = 0;
	private int mLastYIntercept = 0;
	
	//触发滑动阈值的角度
	private static final int TAN = 2;
	
	private boolean mIsSticky = true;
	private boolean mInitDataSuccess = false;
	
	public MyStickyLayout(Context context) {
		super(context);
	}
	
	public MyStickyLayout(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus){
		super.onWindowFocusChanged(hasWindowFocus);
		if(hasWindowFocus&&(mHeader == null||mContent == null)){
			initData();
		}
		
	}
	
	public void initData(){
		int headerId = getResources().getIdentifier("sticky_header", "id", getContext().getPackageName());
		int contentId = getResources().getIdentifier("sticky_content", "id", getContext().getPackageName());
		if(headerId != 0 && contentId != 0){
			mHeader = findViewById(headerId);
			mContent = findViewById(contentId);
			mOriginalHeaderHeight = mHeader.getMeasuredHeight();
			mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
			mInitDataSuccess = true;
			mHeaderHeight = mOriginalHeaderHeight;
			
			if(DEBUG){
				Log.i(TAG, "mTouchSlop = "+mTouchSlop+"mOriginalHeaderHeight"+mOriginalHeaderHeight);
			}
		}else{
			throw new NoSuchElementException("属性节点不存在");
		}
	}
	
	public void setOnGiveUpTouchEventListener(OnGiveUpTouchEventListener l){
		this.mGiveUpTouchEventListener = l;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		int intercept = 0;
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:{
			Log.i(TAG, "intercept/actiondown"+mLastYIntercept);
			intercept = 0;
			mLastXIntercept = x;
			mLastYIntercept = y;
			mLastX = x;
			mLastY = y;
			break;
		}
		case MotionEvent.ACTION_MOVE:{
			int gapX = x - mLastXIntercept;
			int gapY = y - mLastYIntercept;
			Log.i(TAG, "gapy"+gapY+"mTouchSlop"+mTouchSlop);
			if(y <= getHeaderHeight()){
				intercept = 0;
			}else if(Math.abs(gapY) <= Math.abs(gapX)){
				intercept = 0;
			}else if((mStatus == STATUS_EXPANDED)&&(gapY < -mTouchSlop)){
				intercept = 1;
				
			}else if(mGiveUpTouchEventListener != null ){
				if(mGiveUpTouchEventListener.giveUpTouchEvent(event)&&(gapY >= mTouchSlop)){
					intercept = 1;
				}
			}
			break;
		}
		case MotionEvent.ACTION_UP:{
			mLastXIntercept = mLastYIntercept = 0;
			intercept = 0;
			break;
		}
		default:
			break;

		}
		if(DEBUG){
			Log.i(TAG, "intercept"+intercept);
		}
		
		return intercept != 0;
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		
		int x = (int)event.getX();
		int y = (int)event.getY();
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:{
				Log.i(TAG, "onTouchEvent/actiondown");
				break;
			}
			case MotionEvent.ACTION_MOVE:{
				Log.i(TAG, "onTouchEvent/actionMove");
				int gapX = x - mLastX;
				int gapY = y - mLastY;
				mHeaderHeight += gapY;
				setHeaderHeight(mHeaderHeight);
				break;
			}
			case MotionEvent.ACTION_UP:{
				int destHeight = 0;
				if(mHeaderHeight < mOriginalHeaderHeight*0.5){
					destHeight = 0;
					mStatus = STATUS_COLLAPSED;
				}
					
				if(mHeaderHeight >= mOriginalHeaderHeight*0.5){
					destHeight = mOriginalHeaderHeight;
					mStatus = STATUS_EXPANDED;
				}
				smoothSetHeaderHeight(mHeaderHeight, destHeight, 500);
				break;
				
			}
			default:
				break;
		}
		mLastX = x;
		mLastY = y;
		return true;
	}
	
	public int getHeaderHeight(){
		return mHeaderHeight;
	}
	
	
	
	public void smoothSetHeaderHeight(final int from, final int to, int duration){
		final int frameCount = (int)(duration/1000f*30)+1;
		final float partation = (to - from)/frameCount;
		
		new Thread("Thread#smoothSetHeaderHeight"){
			@Override
			public void run(){
				for(int i = 0; i < frameCount; i++){
					if(i == frameCount - 1)
						mHeaderHeight = to;
					else{
						mHeaderHeight = (int) (from + i*partation);
					}
					post(new Runnable(){
						public void run(){
							setHeaderHeight(mHeaderHeight);
						}
					});
					try{
						sleep(10);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		}.start();
		
	}
	
   public void setHeaderHeight(int height){
	   if(!mInitDataSuccess){
		   initData();
	   }
		if(height < 0){
			height = 0;
		}else if(height > mOriginalHeaderHeight){
			height = mOriginalHeaderHeight;
		}
		
		if(height == 0){
			mStatus = STATUS_COLLAPSED;
		}else{
			mStatus = STATUS_EXPANDED;
		}
		if(mHeader != null && mHeader.getLayoutParams() != null){
			mHeader.getLayoutParams().height = height;
			mHeader.requestLayout();
			mHeaderHeight = height;
		}else{
			if(DEBUG){
				Log.i(TAG, "layoutParams is null");
			}
		}
		
	}
	
//	 private static final String TAG = "StickyLayout------";
//	    private static final boolean DEBUG = true;
//
//	    public interface OnGiveUpTouchEventListener {
//	        public boolean giveUpTouchEvent(MotionEvent event);
//	    }
//
//	    private View mHeader;
//	    private View mContent;
//	    private OnGiveUpTouchEventListener mGiveUpTouchEventListener;
//
//	    // header的高度  单位：px
//	    private int mOriginalHeaderHeight;
//	    private int mHeaderHeight;
//
//	    private int mStatus = STATUS_EXPANDED;
//	    public static final int STATUS_EXPANDED = 1;
//	    public static final int STATUS_COLLAPSED = 2;
//
//	    private int mTouchSlop;
//
//	    // 分别记录上次滑动的坐标
//	    private int mLastX = 0;
//	    private int mLastY = 0;
//
//	    // 分别记录上次滑动的坐标(onInterceptTouchEvent)
//	    private int mLastXIntercept = 0;
//	    private int mLastYIntercept = 0;
//
//	    private ListView listView;
//	    // 用来控制滑动角度，仅当角度a满足如下条件才进行滑动：tan a = deltaX / deltaY > 2
//	    private static final int TAN = 2;
//
//	    private boolean mIsSticky = true;
//	    private boolean mInitDataSucceed = false;
//	    private boolean mDisallowInterceptTouchEventOnHeader = true;
//
//	    public MyStickyLayout(Context context) {
//	        super(context);
//	    }
//
//	    public MyStickyLayout(Context context, AttributeSet attrs) {
//	        super(context, attrs);
//	    }
//
//	    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//	    public MyStickyLayout(Context context, AttributeSet attrs, int defStyle) {
//	        super(context, attrs, defStyle);
//	    }
//
//	    @Override
//	    public void onWindowFocusChanged(boolean hasWindowFocus) {
//	        super.onWindowFocusChanged(hasWindowFocus);
//	        if (hasWindowFocus && (mHeader == null || mContent == null)) {
//	            initData();
//	        }
//	    }
//
//	    private void initData() {
//	        int headerId= getResources().getIdentifier("sticky_header", "id", getContext().getPackageName());
//	        int contentId = getResources().getIdentifier("sticky_content", "id", getContext().getPackageName());
//	        if (headerId != 0 && contentId != 0) {
//	            mHeader = findViewById(headerId);
//	            mContent = findViewById(contentId);
//	            mOriginalHeaderHeight = mHeader.getMeasuredHeight();
//	            mHeaderHeight = mOriginalHeaderHeight;
//	            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
//	            if (mHeaderHeight > 0) {
//	                mInitDataSucceed = true;
//	            }
//	            if (DEBUG) {
//	                Log.d(TAG, "mTouchSlop = " + mTouchSlop + "mHeaderHeight = " + mHeaderHeight);
//	            }
//	        } else {
//	            throw new NoSuchElementException("Did your view with id \"sticky_header\" or \"sticky_content\" exists?");
//	        }
//	    }
//
//	    public void setOnGiveUpTouchEventListener(OnGiveUpTouchEventListener l) {
//	        mGiveUpTouchEventListener = l;
//	    }
//
//	    @Override
//	    public boolean onInterceptTouchEvent(MotionEvent event) {
//	        int intercepted = 0;
//	        int x = (int) event.getX();
//	        int y = (int) event.getY();
//
//	        Log.i(TAG, "进入拦截方法  y---"+y+"headerHeight"+getHeaderHeight());
//	        /**
//	         * intercepter为1时表示拦截，为0表示传递给子View
//	         */
//	        switch (event.getAction()) {
//	        case MotionEvent.ACTION_DOWN: {
//	        	Log.i(TAG, "actiondown事件");
//	            mLastXIntercept = x;
//	            mLastYIntercept = y;
//	            mLastX = x;
//	            mLastY = y;
//	            intercepted = 0;
//	            break;
//	        }
//	        case MotionEvent.ACTION_MOVE: {
//	        	
//	            int deltaX = x - mLastXIntercept;
//	            int deltaY = y - mLastYIntercept;
//	            Log.i(TAG, "actionMove"+"deltaY"+deltaY+"mTouchSlop"+mTouchSlop);
//	            if (mDisallowInterceptTouchEventOnHeader && y <= getHeaderHeight()) {// 在head范围类滑动，不拦截，让头部View自己去处理
//	                intercepted = 0;
//	                Log.i(TAG, "-----没有拦截头部事件");
//	            } else if (Math.abs(deltaY) <= Math.abs(deltaX)) {//滑动的y值小于x值，不拦截
//	                intercepted = 0;
//	            } else if (mStatus == STATUS_EXPANDED && deltaY <= -mTouchSlop) {//展开状态，并且向上滑动距离大于阈值，拦截该事件，由该层layout处理
//	                intercepted = 1;
//	            } else if (mGiveUpTouchEventListener != null) {//子View放弃事件，并且滑动距离大于阈值，拦截
//	                if (mGiveUpTouchEventListener.giveUpTouchEvent(event) && deltaY >= mTouchSlop) {
//	                    intercepted = 1;
//	                }
//	            }
//	            break;
//	        }
//	        case MotionEvent.ACTION_UP: {
//	        	Log.i(TAG, "actionUp");
//	            intercepted = 0;
//	            mLastXIntercept = mLastYIntercept = 0;//一次完整的触摸事件后，将最晚触摸坐标清零
//	            break;
//	        }
//	        default:
//	            break;
//	        }
//
//	        if (DEBUG) {
//	            Log.d(TAG, "intercepted=" + intercepted);
//	        }
//	        return intercepted != 0 && mIsSticky;
//	    }
//
//	    @Override
//	    public boolean onTouchEvent(MotionEvent event) {
//	        if (!mIsSticky) {
//	            return true;
//	        }
//	        Log.i(TAG,"touchEvent");
//	        int x = (int) event.getX();
//	        int y = (int) event.getY();
//	        switch (event.getAction()) {
//	        case MotionEvent.ACTION_DOWN: {
//	            Log.i(TAG,"touchEvent---actiondown");
//	            break;
//	        }
//	        case MotionEvent.ACTION_MOVE: {
//	            int deltaX = x - mLastX;
//	            int deltaY = y - mLastY;
//	            Log.i(TAG,"touchEvent---actionMove");
//
//	            if (DEBUG) {
//	                Log.d(TAG, "mHeaderHeight=" + mHeaderHeight + "  deltaY=" + deltaY + "  mlastY=" + mLastY);
//	            }
//	            mHeaderHeight += deltaY;//更新header的高度
//	            setHeaderHeight(mHeaderHeight);
//	            break;
//	        }
//	        case MotionEvent.ACTION_UP: {
//	            // 这里做了下判断，当松开手的时候，会自动向两边滑动，具体向哪边滑，要看当前所处的位置
//	            int destHeight = 0;
//	            if (mHeaderHeight <= mOriginalHeaderHeight * 0.5) {
//	                destHeight = 0;
//	                mStatus = STATUS_COLLAPSED;
//	            } else {
//	                destHeight = mOriginalHeaderHeight;
//	                mStatus = STATUS_EXPANDED;
//	            }
//	            Log.i(TAG,"touchEvent---actionUp");
//
//	            // 慢慢滑向终点
//	            this.smoothSetHeaderHeight(mHeaderHeight, destHeight, 500);
//	            break;
//	        }
//	        default:
//	            break;
//	        }
//	        mLastX = x;
//	        mLastY = y;
//	        return true;
//	    }
//
//	    public void smoothSetHeaderHeight(final int from, final int to, long duration) {
//	    	Log.i(TAG, "慢慢滑向终点");
//	        smoothSetHeaderHeight(from, to, duration, false);
//	    }
//
//	    public void smoothSetHeaderHeight(final int from, final int to, long duration, final boolean modifyOriginalHeaderHeight) {
//	        final int frameCount = (int) (duration / 1000f * 30) + 1;
//	        final float partation = (to - from) / (float) frameCount;
//	        new Thread("Thread#smoothSetHeaderHeight") {
//
//	            @Override
//	            public void run() {
//	                for (int i = 0; i < frameCount; i++) {
//	                    final int height;
//	                    if (i == frameCount - 1) {
//	                        height = to;
//	                    } else {
//	                        height = (int) (from + partation * i);
//	                    }
//	                    post(new Runnable() {
//	                        public void run() {
//	                            setHeaderHeight(height);
//	                        }
//	                    });
//	                    try {
//	                        sleep(10);
//	                    } catch (InterruptedException e) {
//	                        e.printStackTrace();
//	                    }
//	                }
//
//	                if (modifyOriginalHeaderHeight) {
//	                    setOriginalHeaderHeight(to);
//	                }
//	            };
//
//	        }.start();
//	    }
//
//	    public void setOriginalHeaderHeight(int originalHeaderHeight) {
//	        mOriginalHeaderHeight = originalHeaderHeight;
//	    }
//
//	    public void setHeaderHeight(int height, boolean modifyOriginalHeaderHeight) {
//	        if (modifyOriginalHeaderHeight) {
//	            setOriginalHeaderHeight(height);
//	        }
//	        setHeaderHeight(height);
//	    }
//
//	    public void setHeaderHeight(int height) {
//	        if (!mInitDataSucceed) {
//	            initData();
//	        }
//
//	        if (DEBUG) {
//	            Log.d(TAG, "setHeaderHeight height=" + height);
//	        }
//	        if (height <= 0) {
//	            height = 0;
//	        } else if (height > mOriginalHeaderHeight) {
//	            height = mOriginalHeaderHeight;
//	        }
//
//	        if (height == 0) {
//	            mStatus = STATUS_COLLAPSED;
//	        } else {
//	            mStatus = STATUS_EXPANDED;
//	        }
//
//	        if (mHeader != null && mHeader.getLayoutParams() != null) {
//	            mHeader.getLayoutParams().height = height;
//	            mHeader.requestLayout();
//	            mHeaderHeight = height;
//	        } else {
//	            if (DEBUG) {
//	                Log.e(TAG, "null LayoutParams when setHeaderHeight");
//	            }
//	        }
//	    }
//
//	    public int getHeaderHeight() {
//	        return mHeaderHeight;
//	    }
//
//	    public void setSticky(boolean isSticky) {
//	        mIsSticky = isSticky;
//	    }
//
//	    public void requestDisallowInterceptTouchEventOnHeader(boolean disallowIntercept) {
//	        mDisallowInterceptTouchEventOnHeader = disallowIntercept;
//	    }
	

}
