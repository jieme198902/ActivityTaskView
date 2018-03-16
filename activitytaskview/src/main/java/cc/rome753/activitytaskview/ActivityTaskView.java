package cc.rome753.activitytaskview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Observable;
import java.util.TreeMap;

/**
 * Created by rome753@163.com on 2017/3/31.
 */

public class ActivityTaskView extends LinearLayout {

    public static final String TAG = ActivityTaskView.class.getSimpleName();
    private LinearLayout mLinearLayout;
    private View mTinyView;
    private TreeMap<Integer, LinearLayout> mLayoutMap;
    private HashMap<Integer, ObserverTextView> mObserverTextViewMap;

    private ActivityLifecycleObservable mObservable;

    private int mStatusHeight;

    public ActivityTaskView(Context context) {
        this(context, null);
    }

    public ActivityTaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityTaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_activity_task, this);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll);
        mTinyView = findViewById(R.id.tiny_view);
        mObservable = new ActivityLifecycleObservable();
        mLayoutMap = new TreeMap<>();
        mObserverTextViewMap = new HashMap<>();

        mStatusHeight = getStatusBarHeight();

        showTinyOrNot();
    }

    public void add(ActivityTask.TaskInfo taskInfo) {
        int activityId = taskInfo.getActivityId();
        int taskId = taskInfo.getTaskId();
        ObserverTextView textView = createObserverTextView(activityId, taskInfo.getActivityName());
        mObserverTextViewMap.put(activityId, textView);
        LinearLayout layout = mLayoutMap.get(taskId);
        if (layout == null) {
            layout = createLinearLayout();
            mLayoutMap.put(taskId, layout);
            mLinearLayout.addView(layout);
            LinearLayout.LayoutParams params = (LayoutParams) layout.getLayoutParams();
            params.leftMargin = 2;
            layout.setLayoutParams(params);

            Log.i(TAG, "addLayout " + taskId);
        }
        layout.addView(textView, 0);
        LinearLayout.LayoutParams params = (LayoutParams) textView.getLayoutParams();
        textView.setLayoutParams(params);
        mObservable.addObserver(textView);
        Log.i(TAG, "addObserverTextView " + taskId);
    }

    public void remove(ActivityTask.TaskInfo taskInfo) {
        int taskId = taskInfo.getTaskId();
        LinearLayout layout = mLayoutMap.get(taskId);
        if (layout == null) {
            Log.e(TAG, "LinearLayout not found");
            return;
        }
        ObserverTextView textView = mObserverTextViewMap.remove(taskInfo.getActivityId());
        if (textView == null) {
            Log.e(TAG, "ObserverTextView not found");
            return;
        }
        layout.removeView(textView);
        Log.i(TAG, "removeObserverTextView " + taskId);
        if (layout.getChildCount() == 0) {
            mLayoutMap.remove(taskId);
            mLinearLayout.removeView(layout);
            Log.i(TAG, "removeLinearLayout " + taskId);
        }
        mObservable.deleteObserver(textView);
    }

    public void lifecycleChange(ActivityTask.TaskInfo taskInfo){
        switch (taskInfo.getLifecycle()){
            case 0:
                add(taskInfo);
                break;
            case 5:
                remove(taskInfo);
                break;
            default:
                mObservable.lifecycleChange(taskInfo);
                break;
        }
    }

    private LinearLayout createLinearLayout() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(VERTICAL);
        layout.setBackgroundResource(R.drawable.bg_rect_inner);
        layout.setPadding(10,0,10,0);
        return layout;
    }

    private ObserverTextView createObserverTextView(int activityId, String text) {
        ObserverTextView textView = new ObserverTextView(getContext());
        textView.setText(text);
        textView.setTag(activityId);
        return textView;
    }

    float mInnerX;
    float mInnerY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInnerX = event.getX();
                mInnerY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX();
                float y = event.getRawY();
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
                params.x = (int) (x - mInnerX);
                params.y = (int) (y - mInnerY - mStatusHeight);
                WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                if(windowManager != null) {
                    windowManager.updateViewLayout(this, params);
                }
                break;
            case MotionEvent.ACTION_UP:
                showTinyOrNot();
                break;

        }
        return true;
    }

    private void showTinyOrNot() {
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) getLayoutParams();
        if(p == null || p.x < 10){
            mTinyView.setVisibility(VISIBLE);
            mLinearLayout.setVisibility(GONE);
        }else{
            mTinyView.setVisibility(GONE);
            mLinearLayout.setVisibility(VISIBLE);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    static class ActivityLifecycleObservable extends Observable {

        void lifecycleChange(ActivityTask.TaskInfo info) {
            setChanged();
            notifyObservers(info);
        }
    }
}
