package com.example.devin.listview_slide_pull;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * 感谢原作者Created by cui on 2018/3/20
 * 亲测可用-wellZheng
 * 先添加headerView footerView，
 * 在onTouchEvent中 down move up 中判断状态
 * 下拉刷新过程有3种状态： 下拉刷新  +  释放刷新 +刷新中
 * 上拉加载过程有2种状态： 上拉加载  + 加载中
 * 下拉刷新中：
 * move中判断是 下拉刷新 还是 释放刷新
 * up中进行刷新操作:  当前是下拉刷新，则隐藏header;当前是释放刷新，则进行刷新动作。
 * 下拉刷新：headerView开始慢慢显示到完全显示出来
 * 释放刷新：headerView完全显示出来，但是你还在下拉，move没有停止
 * 刷新你中：headerView完全显示出来，手指离开屏幕 ACTION_UP
 */

public class CustomListView extends ListView implements AbsListView.OnScrollListener {

    //header
    private View headerView;
    private FrameLayout flHeader;
    private ImageView ivArrowHeader;
    private TextView tvStateHeader;
    private int headerViewHeight;
    private ProgressBar progressBarHeader;

    //设置3种下拉刷新状态
    public final int DOWN_REFRESH = 1;
    public final int RELEASE_REFRESH = 2;
    public final int REFRESHING = 3;
    private int currentState = DOWN_REFRESH;//当前状态是下拉刷新


    //footer
    private View footerView;
    private int footerViewHeight;

    //当前是不是上拉加载中
    public final  int LOAD_MORE=4;
    public final  int LOAD_MORE_REFRESHING = 5;
    private  int currentState2 = LOAD_MORE;


    private int downY;
    private int moveY;


    public CustomListView(Context context) {
        this(context, null);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHeaderView();
        initFooterView();
        setOnScrollListener(this);
    }


    private void initHeaderView() {
        headerView = View.inflate(getContext(), R.layout.custom_listview_header, null);
        flHeader = headerView.findViewById(R.id.fl_header);
        progressBarHeader = headerView.findViewById(R.id.pb_header);
        ivArrowHeader = headerView.findViewById(R.id.iv_arrow_header);
        tvStateHeader = headerView.findViewById(R.id.tv_state_header);

        headerView.measure(0, 0);
        headerViewHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        addHeaderView(headerView);
    }

    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.custom_listview_foot, null);
        footerView.measure(0, 0);
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerViewHeight, 0, 0);

        addFooterView(footerView);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveY = (int) ev.getY();
                int instance = moveY - downY;

                if (instance > 0 && getFirstVisiblePosition() == 0 && currentState != REFRESHING) {
                    //当手指滑动item,取消item的点击事件，不然我们滑动Item也伴随着item点击事件的发生
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    onTouchEvent(cancelEvent);

                    int paddingTop = -headerViewHeight + instance;
                    if (paddingTop < 0 && currentState != DOWN_REFRESH) {
                        currentState = DOWN_REFRESH;
                        //下拉刷新
                        tvStateHeader.setText("下拉刷新");
                        ivArrowHeader.startAnimation(downAnim());
                    } else if (paddingTop > 0 && currentState != RELEASE_REFRESH) {
                        currentState = RELEASE_REFRESH;
                        //释放刷新
                        tvStateHeader.setText("松开刷新");
                        ivArrowHeader.startAnimation(UpAnim());
                    }
                    int maxTop = getMeasuredHeight() / 2;
                    if (paddingTop > maxTop) {
                        headerView.setPadding(0, maxTop, 0, 0);
                    } else {
                        headerView.setPadding(0, paddingTop, 0, 0);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentState == DOWN_REFRESH) {
                    headerView.setPadding(0, -headerViewHeight, 0, 0);
                } else if (currentState == RELEASE_REFRESH) {
                    currentState = REFRESHING;
                    headerView.setPadding(0, 0, 0, 0);
                    //执行刷新操作
                    tvStateHeader.setText("刷新中...");
                    ivArrowHeader.setVisibility(View.GONE);
                    ivArrowHeader.clearAnimation();
                    progressBarHeader.setVisibility(View.VISIBLE);
                    if (onRefreshListener != null) {
                        onRefreshListener.onRefresh();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 结束刷新
     */
    public void finishRefresh() {
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        currentState = DOWN_REFRESH;
        ivArrowHeader.setVisibility(View.VISIBLE);
        ivArrowHeader.clearAnimation();
        progressBarHeader.setVisibility(View.GONE);
    }


    /**
     * 下拉刷新回调
     *
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    private OnRefreshListener onRefreshListener;


    //  setOnScrollListener
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_IDLE) {
            if (getLastVisiblePosition() == getCount() - 1  &&  currentState2==LOAD_MORE) {
                currentState2= LOAD_MORE_REFRESHING;
                footerView.setPadding(0, 0, 0, 0);
                setSelection(getCount());
                if (onLoadMoreListener != null) {
                    onLoadMoreListener.onLoadMore();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    //加载更多回调
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private OnLoadMoreListener onLoadMoreListener;

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }


    //结束加载更多
    public void finishLoadMore() {
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        currentState2= LOAD_MORE;
    }

    public RotateAnimation UpAnim(){
        RotateAnimation animation = new RotateAnimation(0, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(500);
        return animation;
    }
    public  RotateAnimation downAnim(){
        RotateAnimation animation = new RotateAnimation(-180f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(500);
        return animation;
    }
}
