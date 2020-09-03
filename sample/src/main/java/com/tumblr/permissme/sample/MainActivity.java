package com.tumblr.permissme.sample;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.tumblr.permissme.PermissMe;

public class MainActivity extends AppCompatActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_app_settings:
			launchAppSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Button contactsButton = (Button) findViewById(R.id.contacts_button);
		final Button sharedElementButton = (Button) findViewById(R.id.shared_element_button);

		contactsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final ImageView imageView = (ImageView) findViewById(R.id.contacts_thumb);
				final ActivityOptionsCompat options = ActivityOptionsCompat.
						makeSceneTransitionAnimation(
								MainActivity.this,
								imageView,
								getString(R.string.activity_image_trans)
						);
				PermissMe.with(MainActivity.this)
						.setRequiredPermissions(Manifest.permission.READ_CONTACTS)
						.launchActivityWithPermissions(ContactsActivity.class, null, options.toBundle());
			}
		});

		sharedElementButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final ImageView imageView = (ImageView) findViewById(R.id.gallery_thumb);
				final ActivityOptionsCompat options = ActivityOptionsCompat.
						makeSceneTransitionAnimation(
								MainActivity.this,
								imageView,
								getString(R.string.activity_image_trans)
						);

				PermissMe.with(MainActivity.this)
						.setOptionalPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
						.launchActivityWithPermissions(
								new Intent(MainActivity.this, SharedElementActivity.class),
								options.toBundle()
						);
			}
		});
	}

	private void launchAppSettings() {
		final Intent intent = new Intent();
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(Uri.parse("package:" + getPackageName()));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}

}
