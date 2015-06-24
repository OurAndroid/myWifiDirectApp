package com.wifidirect.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;

public class MyExpandableListView extends ExpandableListView implements OnScrollListener{
	
	private static final String TAG = "MyExpandableListView";
	
	public interface OnHeaderUpdateListener{
		
		public View getHeader();
		
		public void updateHeader(View headerView, int firstVisibleGroupPos);
	}
	
	private View mHeaderView;
	private int mHeaderWidth;
	private int mHeaderHeight;
	
	private OnHeaderUpdateListener mHeaderUpdateListener;
	
	protected boolean mIsHeaderGroupClickable = false;
	
	public MyExpandableListView(Context context){
		super(context);
		initView();
	}
	
	public MyExpandableListView(Context context, AttributeSet attrs){
		super(context, attrs);
		initView();
	}
	
	public MyExpandableListView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		initView();
	}
	
	
	private void initView(){
		setFadingEdgeLength(0);//消除listView中的上下阴影?
		setOnScrollListener(this);
	}
	
	public void setOnHeaderUpdateListener(OnHeaderUpdateListener listener){
		mHeaderUpdateListener = listener;
		if(listener == null){
			mHeaderView = null;
			mHeaderWidth = mHeaderHeight = 0;
			return;
		}
		mHeaderView = listener.getHeader();
		int firstVisiblePos = getFirstVisiblePosition(); //获取第一个可见的子元素
		int firstVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(firstVisiblePos));//获取该子元素所在的组
		listener.updateHeader(mHeaderView, firstVisibleGroupPos);
	 	requestLayout();//刷新页面布局  实现原理？为什么和下面的一起用？
   		postInvalidate();//在UI线程外异步刷新界面
	}
	
	/**
	 * 测量界面布局
	 */

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(mHeaderView == null){
			return;
		}
		measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
		mHeaderWidth = mHeaderView.getMeasuredWidth();
		mHeaderHeight = mHeaderView.getMeasuredHeight();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(mHeaderView == null){
			return;
		}
		int headPosition = mHeaderView.getTop();
		//设置headView的位置
		mHeaderView.layout(0, headPosition, mHeaderWidth, headPosition+mHeaderHeight);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas){
		super.dispatchDraw(canvas);
		if(mHeaderView != null){
			drawChild(canvas, mHeaderView, getDrawingTime());//绘制headView
		}
	}
	
	/**
	 * TODO:group点击事件,重写dispatchTouchEvent，
	 * 实现思路：判断触摸的位置是否在header范围内
	 */
	
	
	

	public void requestRefreshHeader(){
		refreshHeader();
		invalidate(new Rect(0, 0, mHeaderWidth, mHeaderHeight));
	}
	
	protected void refreshHeader(){
		if(mHeaderView == null){
			return;
		}
		int firstVisiblePos = getFirstVisiblePosition();
		int second = firstVisiblePos + 1;
		int firstVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(firstVisiblePos));
		int group = getPackedPositionGroup(getExpandableListPosition(second));
		
		if(group == firstVisibleGroupPos + 1){			//如果前两个可见子元素所在group不同
             View view = getChildAt(1);  //获取第二个子元素，这个子元素是父标题还是子元素？？
             if(view == null){
            	 return;
             }
             if(view.getTop() <= mHeaderHeight){
            	 int position = mHeaderHeight - view.getTop();
            	 mHeaderView.layout(0, -position, mHeaderWidth, mHeaderHeight-position);//将原先的group挤出界面
             }else{
            	 mHeaderView.layout(0, 0, mHeaderWidth, mHeaderHeight);
             }
		}else{//前两个子元素属于同一个group
			 mHeaderView.layout(0, 0, mHeaderWidth, mHeaderHeight);
		}
		
		if(mHeaderUpdateListener != null){
			mHeaderUpdateListener.updateHeader(mHeaderView, firstVisibleGroupPos);
		}
	}

	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

//在滑动完成后调用
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		   if(totalItemCount > 0){
			   refreshHeader();
		   }
		
	}

}
