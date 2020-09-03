package com.tumblr.permissme.sample;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tumblr.permissme.PermissMe;
import com.tumblr.permissme.utils.PermissMeUtils;

import java.util.ArrayList;

/**
 * An activity that shows a grid of user's photos if we have storage permissions
 */
public class SharedElementActivity extends AppCompatActivity {

	private View mEmptyView;
	private RecyclerView mRecyclerView;

	@SuppressWarnings("ConstantConditions")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shared_element);

		mEmptyView = findViewById(R.id.negative_layout);

		final Button loadPhotosButton = findViewById(R.id.load_photos_button);
		loadPhotosButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PermissMe.with(SharedElementActivity.this)
						.setRequiredPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
						.listener(new PermissMe.PermissionListener() {
							@Override
							public void onSuccess() {
								shouldShowEmptyView(false);
								loadPhotos();
							}

							@Override
							public void onRequiredPermissionDenied(final String[] deniedPermissions,
							                                       final boolean[] isAutoDenied) {
								/** No-op **/
							}

							@Override
							public void onOptionalPermissionDenied(final String[] deniedPermissions,
							                                       final boolean[] isAutoDenied) {
								/** NA **/
							}
						}).verifyPermissions();
			}
		});

		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
		mRecyclerView.setAdapter(new ImageAdapter());

		if (PermissMeUtils.needToRequestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			// we don't have access to photos!
			shouldShowEmptyView(true);
		} else {
			shouldShowEmptyView(false);
			loadPhotos();
		}
	}

	private void loadPhotos() {
		((ImageAdapter)(mRecyclerView.getAdapter())).reloadWithImages(getAllShownImagesPath(this));
	}

	private void shouldShowEmptyView(final boolean shouldShowEmptyView) {
		if (shouldShowEmptyView) {
			mEmptyView.setVisibility(View.VISIBLE);
			mRecyclerView.setVisibility(View.GONE);
		} else {
			mEmptyView.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Getting All Images Path.
	 *
	 * @param activity
	 *            the activity
	 * @return ArrayList with images Path
	 */
	private ArrayList<String> getAllShownImagesPath(AppCompatActivity activity) {

		final Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		final String[] projection = { MediaStore.MediaColumns.DATA };
		final Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
		int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

		final ArrayList<String> listOfAllImages = new ArrayList<>();
		while (cursor.moveToNext()) {
			listOfAllImages.add(cursor.getString(column_index_data));
		}
		return listOfAllImages;
	}

	private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

		private ArrayList<String> mImagePaths;

		public void reloadWithImages(final ArrayList<String> imagePaths) {
			mImagePaths = imagePaths;
			notifyDataSetChanged();
		}

		@Override
		public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			final View view =
					LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_image_item, parent, false);
			return new ImageViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ImageViewHolder holder, int position) {
			Glide.with(holder.imageView.getContext())
					.load(mImagePaths.get(position))
					.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
					.into(holder.imageView);
		}

		@Override
		public int getItemCount() {
			return mImagePaths.size();
		}

		public class ImageViewHolder extends RecyclerView.ViewHolder {
			ImageView imageView;

			public ImageViewHolder(View itemView) {
				super(itemView);
				imageView = (ImageView) itemView.findViewById(R.id.image);
			}
		}
	}
}
