package com.bob.digcsdn.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bob.digcsdn.R;
import com.bob.digcsdn.adapter.BlogDetailAdapter;
import com.bob.digcsdn.bean.Blog;
import com.bob.digcsdn.util.Constants;
import com.bob.digcsdn.util.JsoupUtil;
import com.bob.digcsdn.util.VolleyUtil;
import com.bob.digcsdn.view.LoadMoreListView;

import java.util.List;

/**
 * Created by bob on 15-6-8.
 */
public class BlogDetailActivity extends Activity implements View.OnClickListener, LoadMoreListView.OnLoadMoreListener {
    private LoadMoreListView listView;
    private BlogDetailAdapter blogDetailAdapter;

    private ProgressBar progressBar;
    private View reloadView;
    private Button reloadBtn;

    private View backBtn, commentBtn;//评论和回退按钮

    public String url;
    private String fileName;

    public static final int FIRST = 0;
    public static final int NOT_FIRST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_detail);
        init();
        initWidget();
        initEvent();
        executeRefresh(FIRST);
    }

    // 初始化成员变量
    private void init() {
        blogDetailAdapter = new BlogDetailAdapter(this);
        url = getIntent().getExtras().getString("blogLink");
        fileName = url.substring(url.lastIndexOf("/") + 1);

    }

    //初始化控件
    private void initWidget() {
        progressBar = (ProgressBar) findViewById(R.id.pro_common_content);
        reloadBtn = (Button) findViewById(R.id.bt_article_reLoad);
        reloadView = findViewById(R.id.ll_article_reLoad);
        backBtn =  findViewById(R.id.img_article_detail_back);
        commentBtn = findViewById(R.id.img_comment);
        listView = (LoadMoreListView) findViewById(R.id.list_article_view);
        listView.setAdapter(blogDetailAdapter);
    }

    private void initEvent() {
        progressBar.setOnClickListener(this);
        reloadBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        commentBtn.setOnClickListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取点击列表项的状态
                int state = blogDetailAdapter.getList().get(position)
                        .getState();
                switch (state) {
                    case Constants.DEF_BLOG_ITEM_TYPE.IMG: // 点击的是图片
                        String url = blogDetailAdapter.getList().get(position)
                                .getImgLink();

                        Intent intent = new Intent(BlogDetailActivity.this, ImageActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_article_reLoad:
                executeRefresh(FIRST);
                break;
            case R.id.img_article_detail_back:
                finish();
                break;
            case R.id.img_comment:
                Toast.makeText(BlogDetailActivity.this, "comment", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoadMore() {
        if (!JsoupUtil.contentLastPage) {//不是最后一页，才进行加载
            executeRefresh(NOT_FIRST);
        } else listView.setCanLoadMore(false);
    }

    private void executeRefresh(final int refreshType){
        StringRequest htmlRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String html) {
                List<Blog> blogs= JsoupUtil.getContent(html);
                if (blogs.size()== 0){
                    if (refreshType== FIRST){
                        Toast.makeText(getApplicationContext(), "网络信号不佳",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        reloadView.setVisibility(View.VISIBLE);
                    }
                    else listView.setCanLoadMore(false);
                }

                blogDetailAdapter.addList(blogs);
                blogDetailAdapter.notifyDataSetChanged();
                if (refreshType== NOT_FIRST)
                    listView.setCanLoadMore(false);

                progressBar.setVisibility(View.INVISIBLE);
                reloadView.setVisibility(View.INVISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.INVISIBLE);
                reloadView.setVisibility(View.VISIBLE);
            }
        });
        VolleyUtil.getQueue().add(htmlRequest);
    }
}
