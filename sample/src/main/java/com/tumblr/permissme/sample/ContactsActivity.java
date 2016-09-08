package com.tumblr.permissme.sample;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Activity that shows a list of the user's contacts
 */
public class ContactsActivity extends AppCompatActivity {

	@SuppressWarnings("ConstantConditions")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

		ArrayList<String> contactsList = getListOfContacts();
		final ListView listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactsList));
	}

	private ArrayList<String> getListOfContacts() {
		ArrayList<String> contactsList = new ArrayList<>();
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		if (cur != null && cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				contactsList.add(name);
			}
		}
		if (contactsList.isEmpty()) {
			contactsList.add("No Contacts Found");
		}
		return contactsList;
	}
}
