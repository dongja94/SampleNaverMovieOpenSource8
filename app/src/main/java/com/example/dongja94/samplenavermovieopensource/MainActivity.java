package com.example.dongja94.samplenavermovieopensource;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainActivity extends AppCompatActivity {

    EditText keywordView;
    ListView listView;
    PullToRefreshListView refreshView;
    SwipeRefreshLayout refreshLayout;
    MovieAdapter mAdapter;
    ArrayAdapter<Song> mListAdapter;
    private static final boolean isNaverMovie = true;
    private static final boolean isRefreshLibrary = false;

    boolean isUpdate = false;

    boolean isLastItem = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        keywordView = (EditText) findViewById(R.id.edit_keyword);
        if (isRefreshLibrary) {
//            refreshView = (PullToRefreshListView) findViewById(R.id.listView);
            refreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
                @Override
                public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                    String keyword = mAdapter.getKeyword();
                    searchMovie(keyword);
                }
            });

            refreshView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
                @Override
                public void onLastItemVisible() {
                    getMoreItem();
                }
            });

            listView = refreshView.getRefreshableView();
        } else {
            refreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh);
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    String keyword = mAdapter.getKeyword();
                    searchMovie(keyword);
                }
            });
            listView = (ListView)findViewById(R.id.listView);

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (isLastItem && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        getMoreItem();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (totalItemCount > 0 && (firstVisibleItem + visibleItemCount >= totalItemCount - 1)) {
                        isLastItem = true;
                    } else {
                        isLastItem = false;
                    }
                }
            });
        }
        if (isNaverMovie) {
            mAdapter = new MovieAdapter();
            listView.setAdapter(mAdapter);
            keywordView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchMovie(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else {
            mListAdapter = new ArrayAdapter<Song>(this, android.R.layout.simple_list_item_1);
            listView.setAdapter(mListAdapter);

            NetworkManager.getInstance().getMelonChart(this, 1, 10, new NetworkManager.OnResultListener<Melon>() {
                @Override
                public void onSuccess(Melon result) {
                    for (Song s : result.songs.songList) {
                        mListAdapter.add(s);
                    }
                }

                @Override
                public void onFail(int code) {

                }
            });
        }

    }

    private void getMoreItem() {
        if (!isUpdate) {
            String keyword = mAdapter.getKeyword();
            int startIndex = mAdapter.getStartIndex();
            if (!TextUtils.isEmpty(keyword) && startIndex != -1) {
                isUpdate = true;
                NetworkManager.getInstance().getNetworkMelon(MainActivity.this, keyword, startIndex, 10, new NetworkManager.OnResultListener<NaverMovies>() {
                    @Override
                    public void onSuccess(NaverMovies result) {
                        for (MovieItem item : result.items) {
                            mAdapter.add(item);
                        }
                        isUpdate = false;
                    }

                    @Override
                    public void onFail(int code) {
                        isUpdate = false;
                    }
                });
            }
        }
    }
    private void searchMovie(final String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            NetworkManager.getInstance().getNetworkMelon(this, keyword, 1, 10, new NetworkManager.OnResultListener<NaverMovies>() {
                @Override
                public void onSuccess(NaverMovies result) {
                    mAdapter.setKeyword(keyword);
                    mAdapter.setTotalCount(result.total);
                    mAdapter.clear();
                    for (MovieItem item : result.items) {
                        mAdapter.add(item);
                    }
                    if (isRefreshLibrary) {
                        refreshView.onRefreshComplete();
                    } else {
                        refreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                refreshLayout.setRefreshing(false);
                            }
                        }, 2000);
                    }
                }

                @Override
                public void onFail(int code) {
                    Toast.makeText(MainActivity.this, "error : " + code, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mAdapter.clear();
            mAdapter.setKeyword(keyword);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkManager.getInstance().cancelAll(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
