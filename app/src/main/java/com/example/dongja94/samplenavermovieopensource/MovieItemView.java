package com.example.dongja94.samplenavermovieopensource;

import android.content.Context;
import android.text.Html;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * Created by dongja94 on 2015-10-19.
 */
public class MovieItemView extends FrameLayout {
    public MovieItemView(Context context) {
        super(context);
        init();
    }

    ImageView iconView;
    TextView titleView, directorView;
    MovieItem mItem;

    DisplayImageOptions options;

    private void init() {
        inflate(getContext(), R.layout.view_movie_item, this);
        iconView = (ImageView)findViewById(R.id.image_icon);
        titleView = (TextView)findViewById(R.id.text_title);
        directorView = (TextView)findViewById(R.id.text_director);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(50))
                .build();

    }

    public void setMovieItem(MovieItem item) {
        titleView.setText(Html.fromHtml(item.title));
        directorView.setText(item.director);

        ImageLoader.getInstance().displayImage(item.image, iconView, options);
    }
}
