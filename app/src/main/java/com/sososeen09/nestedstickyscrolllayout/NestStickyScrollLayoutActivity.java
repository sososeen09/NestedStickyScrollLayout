package com.sososeen09.nestedstickyscrolllayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author sososeeno09
 */
public class NestStickyScrollLayoutActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "NestStickyScrollLayoutActivity";
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRvBehavior;
    private ArrayList<Object> mList;
    private ItemTouchHelper mItemTouchHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nest_sticky_scroll_layout);
        mRvBehavior = (RecyclerView) findViewById(R.id.rv_behavior);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            mList.add("Item: " + i);
        }

        mRvBehavior.setLayoutManager(new LinearLayoutManager(this));
        VHAdapter adapter = new VHAdapter();
        mRvBehavior.setAdapter(adapter);

        //关联ItemTouchHelper和RecyclerView
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRvBehavior);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    class VHAdapter extends RecyclerView.Adapter implements onMoveAndSwipedListener {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_recyclerview, parent, false);

            return new VHHolder(view);
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((VHHolder) holder).mTvContent.setText("Item: " + position);
            ((VHHolder) holder).mTvContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CommentPostPopWindow(getApplicationContext(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                }
            });

            Log.d(TAG, "onBindViewHolder: " + holder);

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            //交换mItems数据的位置
            Collections.swap(mList, fromPosition, toPosition);
            //交换RecyclerView列表中item的位置
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            mList.remove(position);
            notifyItemRemoved(position);
        }
    }

    class VHHolder extends RecyclerView.ViewHolder {
        TextView mTvContent;

        public VHHolder(View itemView) {
            super(itemView);
            mTvContent = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }

    public void clickView(View view) {
        Toast.makeText(this, "click view", Toast.LENGTH_SHORT).show();
    }

    class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final onMoveAndSwipedListener mAdapter;

        public SimpleItemTouchHelperCallback(onMoveAndSwipedListener listener) {
            mAdapter = listener;
        }

        /**
         * 这个方法是用来设置我们拖动的方向以及侧滑的方向的
         */
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            //如果是ListView样式的RecyclerView
            //设置拖拽方向为上下左右都可以
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            //设置侧滑方向为从左到右和从右到左都可以
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            //将方向参数设置进去
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        /**
         * 当我们拖动item时会回调此方法
         */
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //如果两个item不是一个类型的，我们让他不可以拖拽
            if (viewHolder.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            //回调adapter中的onItemMove方法
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return super.isLongPressDragEnabled();
//            return false;
        }

        /**
         * 当我们侧滑item时会回调此方法
         */
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }
    public interface onMoveAndSwipedListener {
        boolean onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }
}
