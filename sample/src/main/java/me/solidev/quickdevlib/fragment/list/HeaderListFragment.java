package me.solidev.quickdevlib.fragment.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.solidev.library.ui.adapter.Item;
import me.solidev.library.ui.adapter.MultiTypeAdapter;
import me.solidev.library.ui.fragment.AbsListFragment;
import me.solidev.library.rx.TransformUtils;
import me.solidev.library.ui.recyclerview.LinearDecoration;
import me.solidev.library.ui.widget.banner.BannerController;
import me.solidev.library.ui.widget.banner.BannerItem;
import me.solidev.library.utils.FileUtil;
import me.solidev.library.utils.json.JsonConvert;
import me.solidev.quickdevlib.entity.AppBannerItem;
import me.solidev.quickdevlib.entity.news_type.DefaultNewsItem;
import me.solidev.quickdevlib.entity.NewsItem;
import me.solidev.quickdevlib.entity.news_type.SubjectNewsItem;
import me.solidev.quickdevlib.entity.news_type.TextNewsItem;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by _SOLID
 * Date:2016/9/29
 * Time:9:30
 * Desc:带有header的例子
 */

public class HeaderListFragment extends AbsListFragment<NewsItem> {
    private BannerController mBannerController;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBannerController = new BannerController(getContext());
    }

    @Override
    protected void customConfig() {
        addItemDecoration(new LinearDecoration(getContext(), RecyclerView.VERTICAL));
    }

    @Override
    public void loadData(final int pageIndex) {

        if (pageIndex > 5) {//模拟没有数据
            onDataSuccessReceived(pageIndex, new ArrayList<NewsItem>());
            return;
        }
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        subscriber.onNext(FileUtil.getString(getContext(), "raw://news_list"));
                    }
                })
                .compose(TransformUtils.<String>defaultSchedulers())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showError(new Exception(e));
                    }

                    @Override
                    public void onNext(String newsString) {
                        Random r = new Random();
                        if (pageIndex == r.nextInt(4))
                            newsString = "1";//模拟数据出错
                        List<NewsItem> items;
                        try {
                            JSONObject jsonObject = new JSONObject(newsString);
                            if (pageIndex == getInitPageIndex()) {//添加header
                                JsonConvert<List<AppBannerItem>> convertAppBannerItem = new JsonConvert<List<AppBannerItem>>() {
                                };
                                mBannerController.setBannerList(convertAppBannerItem.parseData(jsonObject.getString("banners")));
                                addHeaderView(mBannerController.getView());
                            }

                            JsonConvert<List<NewsItem>> convertNewsItem = new JsonConvert<List<NewsItem>>() {
                            };
                            items = convertNewsItem.parseData(jsonObject.getString("datas"));
                            onDataSuccessReceived(pageIndex, items);

                        } catch (JSONException e) {
                            onError(e);
                        }


                    }
                });

    }


    @Override
    protected MultiTypeAdapter getAdapter() {//当有多个itemType的时候则需重写此方法
        return new MultiTypeAdapter(getItems()) {
            @NonNull
            @Override
            public Class onFlattenClass(@NonNull Item item) {
                NewsItem newsItem = (NewsItem) item;
                switch (newsItem.getDocType()) {
                    case 1:
                        return DefaultNewsItem.class;
                    case 2:
                        return SubjectNewsItem.class;
                    case 3:
                        return TextNewsItem.class;
                    default:
                        return DefaultNewsItem.class;
                }
            }
        };
    }
}
