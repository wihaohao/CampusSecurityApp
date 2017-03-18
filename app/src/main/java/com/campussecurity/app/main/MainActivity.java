package com.campussecurity.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.campussecurity.app.App;
import com.campussecurity.app.Constant;
import com.campussecurity.app.R;
import com.campussecurity.app.databinding.ActivityMainBinding;
import com.campussecurity.app.login.LoginActivity;
import com.campussecurity.app.main.model.IconModel;
import com.campussecurity.app.main.model.UpLoadImageEvent;
import com.campussecurity.app.securitycheck.SecurityCheckListActivity;
import com.hao.common.adapter.OnRVItemClickListener;
import com.hao.common.base.BaseDataBindingActivity;
import com.hao.common.base.TopBarType;
import com.hao.common.image.ImageLoader;
import com.hao.common.manager.AppManager;
import com.hao.common.nucleus.factory.RequiresPresenter;
import com.hao.common.rx.RxBus;
import com.hao.common.utils.SPUtil;

import java.util.List;

import rx.functions.Action1;

@RequiresPresenter(MainPresenter.class)
public class MainActivity extends BaseDataBindingActivity<MainPresenter, ActivityMainBinding> implements AppBarLayout.OnOffsetChangedListener, OnRVItemClickListener, View.OnClickListener {
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    IconModelAdapter iconModelAdapter;
    private MaterialDialog materialDialog;

    @Override
    protected int getRootLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
       /* mBinding.titleBar.setTitleText(getString(R.string.title_home_activity));
        mBinding.titleBar.setRightText(R.string.act_destroy);*/
        mBinding.mainAppbar.addOnOffsetChangedListener(this);

        startAlphaAnimation(mBinding.mainTextviewTitle, 0, View.INVISIBLE);
        iconModelAdapter = new IconModelAdapter(mBinding.gridView, R.layout.item_icon_model);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mBinding.gridView.setLayoutManager(gridLayoutManager);
        mBinding.gridView.setAdapter(iconModelAdapter);

    }


    @Override
    protected TopBarType getTopBarType() {
        return TopBarType.TitleBar;
    }

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    protected void setListener() {
        iconModelAdapter.setOnRVItemClickListener(this);
        mBinding.imAvatar.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mBinding.setUser(((App) AppManager.getApp()).cacheUser);
    }

    @Override
    protected void onStart() {
        super.onStart();
        RxBus.toObservableAndBindToLifecycle(UpLoadImageEvent.class,this).subscribe(upLoadImageEvent -> {
            ImageLoader.getInstance().displayCricleImage(this,upLoadImageEvent.url,mBinding.imAvatar);
        });
    }

    @Override
    public void onBackPressed() {
        AppManager.getInstance().exitWithDoubleClick();
    }

    public void showModuleToUI(List<IconModel> iconModels) {
        iconModelAdapter.setData(iconModels);

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(mBinding.mainTextviewTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mBinding.mainTextviewTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mBinding.mainLlTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mBinding.mainLlTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    @Override
    public void onRVItemClick(ViewGroup parent, View itemView, int position) {
        IconModel iconModel = iconModelAdapter.getItem(position);
        mSwipeBackHelper.forward(new Intent(this, iconModel.toActivity));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.im_avatar:
                if(materialDialog==null){
                    materialDialog=new MaterialDialog.Builder(this)
                            .items(getString(R.string.set_avatar),getString(R.string.change_password),
                                    getString(R.string.lagou)).itemsCallback((dialog, itemView, position, text) -> {
                                switch (position){
                                    case 0:
                                        mSwipeBackHelper.forward(SetHeadPhotoActivity.class);
                                        break;
                                    case 1:
                                        mSwipeBackHelper.forward(PasswordEditActivity.class);
                                        break;
                                    case 2:
                                        SPUtil.putString(Constant.APP_USER_PASSWORD,"");
                                        mSwipeBackHelper.forwardAndFinish(LoginActivity.class);
                                        break;
                                }
                            }).show();
                }else{
                    materialDialog.show();
                }

                break;
        }
    }
}
