package com.sososeen09.nestedstickyscrolllayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * @author sososeen09
 */
public class CommentPostPopWindow extends PopupWindow {
    private View mMenuView;
    private LinearLayout mLlComment;
    private EditText mEtComment;
    private TextView mTvSend;
    private TextView mTvTextCount;
    private TextView mTvLimitRemind;
    private CheckBox mCbAnony;

    private final InputMethodManager mInputMethodManager;

    @SuppressLint("InflateParams")
    public CommentPostPopWindow(Context context, View.OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.view_comment_post, null);
        mLlComment = (LinearLayout) mMenuView.findViewById(R.id.ll_comment_dialog);
        mEtComment = (EditText) mMenuView.findViewById(R.id.et_comment_dialog);
        mTvSend = (TextView) mMenuView.findViewById(R.id.tv_send_post);
        mTvTextCount = (TextView) mMenuView.findViewById(R.id.tv_text_count);
        mTvLimitRemind = (TextView) mMenuView.findViewById(R.id.tv_limit_remind);
        mCbAnony = (CheckBox) mMenuView.findViewById(R.id.cb_anonymity);

        // 设置按钮监听
        initListener(itemsOnClick);
        mTvSend.setEnabled(false);

        this.setContentView(mMenuView);

        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        this.setFocusable(true);
        // 设置CommentPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.PopupAnimation);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x80000000);

        this.setBackgroundDrawable(dw);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.ll_comment_dialog).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                        if (mInputMethodManager != null) {
                            mEtComment.clearFocus();
                        }
                    }
                }

                return true;
            }
        });

        mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mEtComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mInputMethodManager.showSoftInput(mEtComment, InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    mInputMethodManager.hideSoftInputFromInputMethod(mEtComment.getWindowToken(), 0);
                }
            }
        });

        mEtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mTvTextCount.setText(String.valueOf(s.length()));
                if (s.length() == 0) {
                    mTvSend.setEnabled(false);
                }else if (s.length() > 150) {
                    mTvTextCount.setTextColor(Color.parseColor("#F13930"));
                    mTvLimitRemind.setVisibility(View.VISIBLE);
                    mTvSend.setEnabled(false);
                } else {
                    mTvTextCount.setTextColor(Color.parseColor("#666666"));
                    mTvLimitRemind.setVisibility(View.GONE);
                    mTvSend.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        this.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mEtComment.setFocusable(true);
        mEtComment.setFocusableInTouchMode(true);
        mEtComment.requestFocus();

    }

    public String getComment() {
        return mEtComment.getText().toString();
    }

    public void clearComment() {
        mEtComment.setText("");
        dismiss();
        mEtComment.clearFocus();
    }

    /**
     * 是否匿名
     * @return
     */
    public boolean isAnnoy() {
        return mCbAnony.isChecked();
    }

    private void initListener(View.OnClickListener itemsOnClick) {
        mTvSend.setOnClickListener(itemsOnClick);
    }
}

