package cz.uhk.cityunavigate.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import cz.uhk.cityunavigate.Database;
import cz.uhk.cityunavigate.DetailActivity;
import cz.uhk.cityunavigate.R;
import cz.uhk.cityunavigate.model.Category;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.model.User;
import cz.uhk.cityunavigate.util.Function;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.Run;

/**
 * Friends list
 */
public class MarkersRecyclerAdapter extends RecyclerView.Adapter<MarkersRecyclerAdapter.CustomViewHolder> {

    private Context context;

    private List<Marker> markerList;

    public MarkersRecyclerAdapter(Context context, List<Marker> markerList) {
        this.context = context;
        this.markerList = markerList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_marker_row, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Marker feedItem = markerList.get(i);
        customViewHolder.bindView(feedItem);
    }

    @Override
    public int getItemCount() {
        if (markerList != null) {
            return markerList.size();
        } else {
            return 0;
        }
    }

    //VIEW HOLDER FOR RECYCLER ADAPTER
    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Marker feedItem;

        private ImageView imgUser;
        private TextView txtTitle, txtText;

        private View viewCategory;

        public CustomViewHolder(View view) {
            super(view);

            imgUser = (ImageView) view.findViewById(R.id.imgImage);

            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            txtText = (TextView) view.findViewById(R.id.txtText);

            viewCategory = view.findViewById(R.id.viewCategory);
            view.setOnClickListener(this);
        }

        public void bindView(final Marker feedItem) {

            this.feedItem = feedItem;

            //Setting text view title
            txtTitle.setText(feedItem.getTitle());
            txtText.setText(feedItem.getText());

            Database.getCategoryById(feedItem.getIdCategory())
                    .success(new Promise.SuccessListener<Category, Object>() {
                        @Override
                        public Object onSuccess(Category result) throws Exception {
                            viewCategory.setBackgroundColor(Color.HSVToColor(150, new float[] { result.getHue(), 0.8f, 1.0f }));
                            return null;
                        }
                    });

            if (feedItem.getImage() != null) {
                Database.downloadImage(feedItem.getImage())
                        .success(new Promise.SuccessListener<Bitmap, Object>() {
                            @Override
                            public Object onSuccess(Bitmap result) throws Exception {
                                imgUser.setVisibility(View.VISIBLE);
                                imgUser.setImageBitmap(result);
                                return null;
                            }
                        })
                        .error(new Promise.ErrorListener<Object>() {
                            @Override
                            public Object onError(Throwable error) {
                                imgUser.setVisibility(View.GONE);
                                return null;
                            }
                        });
            } else {
                imgUser.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {

            Intent detailIntent = new Intent(context, DetailActivity.class);
            detailIntent.putExtra("id", feedItem.getId());
            detailIntent.putExtra("groupid", feedItem.getIdGroup());
            context.startActivity(detailIntent);
        }

    }

}
