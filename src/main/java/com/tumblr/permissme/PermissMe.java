package com.tumblr.permissme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.tumblr.permissme.utils.PermissMeUtils;

/**
 * The class that will handle requesting permissions if the app does not have a queried permission.
 * This class is a {@link Fragment} that will be attached to the caller activity passed through the
 * constructor:
 * {@link Builder#with(AppCompatActivity)} or the activity the caller fragment is attached to,
 * passed through {@link Builder#with(Fragment)} .
 * <p>
 * Exceptions:
 * {@link IllegalArgumentException} thrown if caller activity/caller fragment's activity is not of
 * type {@link AppCompatActivity}
 * <p>
 * Usage: For usage examples see the demo app or refer to User Guide at:
 * See <a href="https://tumblr.github.io/PermissMe/">PermissMe User Guide</a>
 *
 * <p>
 * Notes:
 * {@link Builder#mOptionalPermissions} set through {@link Builder#setOptionalPermissions(String...)}
 * will be queried iff the user grants the {@link Builder#mRequiredPermissions} set through
 * {@link Builder#setRequiredPermissions(String...)}.
 * <p>
 * If the caller does not specify {@link Builder#setRequiredPermissions(String...)}, then the
 * {@link Builder#mOptionalPermissions} will be queried as usual.
 * <p>
 * There are two {@link Builder} methods that will start the permissions flow:
 * {@link Builder#verifyPermissions()} and {@link Builder#launchActivityWithPermissions(Class, Bundle, Bundle)}
 * 1. {@link Builder#launchActivityWithPermissions(Class, Bundle, Bundle)}
 * This should be used to launch a specific activity after the user successfully grants the {@link
 * Builder#mRequiredPermissions}.
 * The target activity will still be launched if the user denied {@link Builder#mOptionalPermissions}. To receive
 * callbacks on
 * which permissions were denied, use set a {@link PermissionListener} through {@link
 * Builder#setListener(PermissionListener)}
 * 2. {@link Builder#verifyPermissions()}
 * This should be used to start the permissions flow from wherever in the flow it was executed;
 * a callback can be provided by settings a {@link PermissionListener} through {@link
 * Builder#setListener(PermissionListener)}
 * <p>
 * More information about usage and details can be found at:
 * See <a href="https://tumblr.github.io/PermissMe/java-docs.html">PermissMe JavaDocs</a>
 * <p>
 */
public class PermissMe extends Fragment {
	/**
	 * TAG to get a reference to the fragment + logging + analytics and all that fun stuff
	 */
	private static final String TAG = PermissMe.class.getSimpleName();
	/**
	 * The destination class extra key
	 */
	@VisibleForTesting
	/*package*/ static final String DESTINATION_ACTIVITY_CLASS_EXTRA = "DESTINATION_ACTIVITY_CLASS_EXTRA";
	/**
	 * The destination activity's extras if required
	 */
	private static final String DESTINATION_ACTIVITY_BUNDLE_EXTRA = "DESTINATION_ACTIVITY_BUNDLE_EXTRA";
	/**
	 * The launch options bundle to be passed in when starting the activity intent:
	 * {@link AppCompatActivity#startActivity(Intent, Bundle)}
	 */
	private static final String DESTINATION_ACTIVITY_LAUNCH_OPTIONS_EXTRA = "DESTINATION_ACTIVITY_LAUNCH_OPTIONS_EXTRA";
	/**
	 * The required permissions extras key
	 */
	private static final String REQUIRED_PERMISSIONS_REQUESTED_EXTRA = "REQUIRED_PERMISSIONS_REQUESTED_EXTRA";
	/**
	 * The optional permissions extra key
	 */
	private static final String OPTIONAL_PERMISSIONS_REQUESTED_EXTRA = "OPTIONAL_PERMISSIONS_REQUESTED_EXTRA";
	/**
	 * The activity enter transition type extra key
	 */
	private static final String DESTINATION_ACTIVITY_ENTER_TRANSITION_EXTRA = "DESTINATION_ACTIVITY_ENTER_TRANSITION_EXTRA";
	/**
	 * The activity exit transition type extra key
	 */
	private static final String DESTINATION_ACTIVITY_EXIT_TRANSITION_EXTRA = "DESTINATION_ACTIVITY_EXIT_TRANSITION_EXTRA";
	/**
	 * The flag which indicates whether we should show the failure snackbar on auto-fail of permission
	 */
	@VisibleForTesting
	/*package*/ static final String SHOULD_SHOW_UI_UPON_FAILURE_EXTRA = "SHOULD_SHOW_UI_UPON_FAILURE_EXTRA";
	/**
	 * The custom auto-denied permission message extra key
	 */
	private static final String CUSTOM_AUTO_DENIED_MESSAGE_EXTRA = "CUSTOM_AUTO_DENIED_MESSAGE_EXTRA";
	/**
	 * The request code that the destination activity is started with extra key
	 */
	private static final String DESTINATION_START_ACTIVITY_WITH_REQUEST_CODE =
			"DESTINATION_START_ACTIVITY_WITH_REQUEST_CODE";
	/**
	 * Flag that indicates to finish activity upon result
	 */
	@VisibleForTesting
	/*package*/ static final String DESTINATION_FINISH_ACTIVITY_UPON_RESULT_EXTRA =
			"DESTINATION_FINISH_ACTIVITY_UPON_RESULT_EXTRA";
	/**
	 * The required-permissions request code
	 */
	@VisibleForTesting
	/*package*/ static final int REQUIRED_PERMISSION_REQUEST_CODE = 1;
	/**
	 * The optional-permissions request code
	 */
	@VisibleForTesting
	/*package*/ static final int OPTIONAL_PERMISSION_REQUEST_CODE = 2;

	/**
	 * A listener to provide callbacks to in regards to the status of permissions granted/ungranted by user; see
	 * {@link PermissionListener}
	 */
	@VisibleForTesting
	/*package*/ PermissionListener mListener;

	/**
	 * Contains all the data sent by the caller
	 */
	@NonNull
	@VisibleForTesting
	/*package*/ Bundle mPermissionsInfoBundle = new Bundle();

	/**
	 * An intent the user passed in directly through {@link Builder#launchActivityWithPermissions(Intent, Bundle)}
	 */
	@Nullable
	@VisibleForTesting
	/*package*/ Intent mLaunchIntent;

	/**
	 * A constructor to start the flow of launching a destination activity class with permissions.
	 *
	 * @param callerActivity
	 * 		the caller activity
	 * @return A builder to add more parameters if necessary or call
	 * {@link Builder#launchActivityWithPermissions(Class, Bundle, Bundle)} or {@link Builder#verifyPermissions()}
	 * to start the flow
	 */
	public static Builder with(final AppCompatActivity callerActivity) {
		return new Builder(callerActivity);
	}

	/**
	 * A constructor to start the flow of verifying permissions; set a {@link PermissionListener} to receive
	 * callbacks about the grant/deny status of the permissions when the user takes an action.
	 *
	 * @param callerFragment
	 * 		the caller fragment used to get the {@link AppCompatActivity} it is attached to
	 * @return A builder to add more parameters if necessary or call
	 * {@link Builder#launchActivityWithPermissions(Class, Bundle, Bundle)} or {@link Builder#verifyPermissions()}
	 * to start the flow
	 * @throws IllegalArgumentException
	 * 		if the callerFragment is attached to an activity that is not of type {@link AppCompatActivity}
	 */
	public static Builder with(final Fragment callerFragment) {
		return new Builder(callerFragment);
	}

	/**
	 * The Builder to make a permissions request. A request can be executed with:
	 * 1) {@link Builder#launchActivityWithPermissions(Class, Bundle, Bundle)}. The Bundle is for the activity
	 * that will be launched once the required permissions are granted by the user
	 * 2) {@link Builder#verifyPermissions()}. It will not do anything special; just queries for the
	 * permissions specified; set a listener to receive callbacks on user's interactions.
	 */
	public static class Builder {
		@NonNull
		private final AppCompatActivity mCallerActivity;

		@Nullable
		private PermissionListener mListener;

		@AnimRes
		private int mEnterAnim;

		@AnimRes
		private int mExitAnim;

		@Nullable
		private Fragment mTargetFragment;

		@Nullable
		private String mCustomAutoDeniedMessage;
		private boolean mNoUIForFailure;
		private boolean mShouldStartActivityForResult;
		private boolean mShouldFinishActivityAfterResult;
		private int mRequestCode = -1;
		private String[] mRequiredPermissions = {};
		private String[] mOptionalPermissions = {};

		/**
		 * Constructor
		 *
		 * @param callerActivity
		 * 		caller activity
		 */
		public Builder(@NonNull final AppCompatActivity callerActivity) {
			mCallerActivity = callerActivity;
		}

		/**
		 * Constructor
		 *
		 * @param callerFragment
		 * 		caller fragment
		 */
		public Builder(@NonNull final Fragment callerFragment) {
			if (!(callerFragment.getActivity() instanceof AppCompatActivity)) {
				throw new IllegalArgumentException("PermissMe requires fragment to be added to an Activity of type AppCompatActivity");
			}
			mCallerActivity = (AppCompatActivity) callerFragment.getActivity();
		}

		/**
		 * The {@link PermissionListener} that will provide callbacks when permissions are granted/denied.
		 *
		 * @param listener
		 * 		the listener
		 * @return this, for chaining
		 */
		public Builder listener(final PermissionListener listener) {
			mListener = listener;
			return this;
		}

		/**
		 * Indicates that we don't want to see the permission-denied UI when we get a auto-denied permissions.
		 * Auto-denied happens when the user has tapped "Do not ask again" for permission request previously or
		 * currently.
		 *
		 * @return this, for chaining
		 */
		public Builder showNoUIOnFailure() {
			mNoUIForFailure = true;
			return this;
		}

		/**
		 * Sets a custom message that will be shown upon auto-denied permission
		 * {@link com.google.android.material.snackbar.Snackbar}. If no custom message was
		 * set, the default message will be used.
		 * To not show a {@link com.google.android.material.snackbar.Snackbar} at all, use
		 * {@link #showNoUIOnFailure()}
		 *
		 * @param failureMsg
		 * 		The custom message that should show up for the auto-denied permission snackbar
		 * @return this, for chaining
		 */
		public Builder customAutoFailureMessage(final String failureMsg) {
			mCustomAutoDeniedMessage = failureMsg;
			return this;
		}

		/**
		 * Finishes the caller activity upon the result of the permissions request
		 *
		 * @return this, for chaining
		 */
		public Builder finishActivityUponResult() {
			mShouldFinishActivityAfterResult = true;
			return this;
		}

		/**
		 * A target fragment that can be specified if {@link #requestCode(int)} is specified to
		 * {@link Fragment#startActivityForResult(Intent, int)}. The target fragment will be the one that
		 * receives the result of the activity being started for result. Otherwise, it will send the result
		 * to the caller activity by default.
		 *
		 * @param targetFragment
		 * 		the target fragment to return the
		 * 		{@link android.app.Activity#onActivityResult(int, int, Intent)} call
		 * @return this, for chaining
		 */
		public Builder targetFragment(final Fragment targetFragment) {
			mTargetFragment = targetFragment;
			return this;
		}

		/**
		 * A request code if the destination activity specified in
		 * {@link #launchActivityWithPermissions(Class, Bundle, Bundle)} needs to be launched to listen for result
		 *
		 * @param requestCode
		 * 		The request code to launch the destination activity with
		 * @return this for chaining
		 */
		public Builder requestCode(final int requestCode) {
			mShouldStartActivityForResult = true;
			mRequestCode = requestCode;
			return this;
		}

		/**
		 * Animation for the activity that will be started; not required
		 *
		 * @param enterAnim
		 * 		animation resource for the enter animation of the destination activity,
		 * 		0 if no enter animation required
		 * @param exitAnim
		 * 		animation resource for the exit animation of the destination activity,
		 * 		0 if no exit animation required
		 * @return this, for chaining
		 */
		public Builder destinationActivityAnim(@AnimRes final int enterAnim,
		                                       @AnimRes final int exitAnim) {
			mEnterAnim = enterAnim;
			mExitAnim = exitAnim;
			return this;
		}

		/**
		 * Set the required permissions to query; {@link PermissionListener#onSuccess()} will be called iff
		 * the required permissions are granted.
		 * {@link PermissionListener#onRequiredPermissionDenied(String[], boolean[])}
		 * will be called if a required permission is denied
		 *
		 * @param requiredPermissions
		 * 		the required permissions
		 * @return this, for chaining
		 */
		public Builder setRequiredPermissions(final String... requiredPermissions) {
			mRequiredPermissions = requiredPermissions.clone();
			return this;
		}

		/**
		 * Set the optional permissions to query; {@link PermissionListener#onSuccess()} will called even if optional
		 * permissions are NOT granted. {@link PermissionListener#onOptionalPermissionDenied(String[], boolean[])}
		 * will also be called if an optional permission is denied
		 *
		 * @param optionalPermissions
		 * 		the optional permissions
		 * @return this, for chaining
		 */
		public Builder setOptionalPermissions(final String... optionalPermissions) {
			mOptionalPermissions = optionalPermissions.clone();
			return this;
		}

		/**
		 * Launches the destination activity intent.The required/optional permissions queried are specified
		 * using {@link #setRequiredPermissions(String...)} and {@link #setOptionalPermissions(String...)}
		 * If the package already has the permissions granted, this will launch the destination intent using
		 * <p>
		 * {@link AppCompatActivity#startActivity(Intent)} method, with other parameters such as
		 * {@link #destinationActivityAnim(int, int)}, {@link #setTargetFragment(Fragment, int)}, etc.
		 *
		 * @param activityIntent
		 * 		The activity intent to be started once permissions are checked+granted
		 * @param optionsBundle
		 * 		Additional options for how the activity should be started
		 * 		See bundle field in {@link AppCompatActivity#startActivity(Intent, Bundle)} or
		 * 		{@link AppCompatActivity#startActivityForResult(Intent, int, Bundle)}
		 * @throws RuntimeException
		 * 		thrown if no permissions are specified when this method is called
		 */
		public void launchActivityWithPermissions(@NonNull final Intent activityIntent,
		                                          @Nullable final Bundle optionsBundle) {
			final Bundle bundle = getPermissionsDataBundle(mCallerActivity,
					mRequiredPermissions, mOptionalPermissions);
			if (bundle != null) {
				bundle.putBundle(DESTINATION_ACTIVITY_LAUNCH_OPTIONS_EXTRA, optionsBundle);
				final PermissMe fragment = launchPermissMe(bundle);
				fragment.setLaunchIntent(activityIntent);
			} else {
				launchIntentWithParameters(
						activityIntent,
						mCallerActivity,
						optionsBundle,
						mTargetFragment,
						mEnterAnim,
						mExitAnim,
						mRequestCode,
						mShouldFinishActivityAfterResult
				);
			}
		}

		/**
		 * Launches the destination activity.The required/optional permissions queried are specified
		 * using {@link #setRequiredPermissions(String...)} and {@link #setOptionalPermissions(String...)}
		 * If the package already has the permissions granted, this will launch the destination activity class,
		 * and set the bundle as the bundle of the destinationAcitivity.
		 *
		 * @param destinationActivity
		 * 		The activity to launch if the required permissions are granted by the user
		 * @param destinationBundle
		 * 		The bundle with extra information the destination class may need
		 * @param optionsBundle Additional options for how the activity should be started
		 * 		See bundle field in {@link AppCompatActivity#startActivity(Intent, Bundle)} or
		 * 		{@link AppCompatActivity#startActivityForResult(Intent, int, Bundle)}
		 * @throws RuntimeException
		 * 		thrown if no permissions are specified when this method is called
		 */
		public void launchActivityWithPermissions(@NonNull final Class destinationActivity,
		                                          @Nullable final Bundle destinationBundle,
		                                          @Nullable final Bundle optionsBundle) {

			final Bundle bundle = getPermissionsDataBundle(mCallerActivity,
					mRequiredPermissions, mOptionalPermissions);
			if (bundle != null) {
				bundle.putSerializable(DESTINATION_ACTIVITY_CLASS_EXTRA, destinationActivity);
				bundle.putBundle(DESTINATION_ACTIVITY_BUNDLE_EXTRA, destinationBundle);
				bundle.putBundle(DESTINATION_ACTIVITY_LAUNCH_OPTIONS_EXTRA, optionsBundle);
				launchPermissMe(bundle);
			} else {
				final Intent intent = new Intent(mCallerActivity, destinationActivity);
				if (destinationBundle != null) {
					intent.putExtras(destinationBundle);
				}
				launchIntentWithParameters(
						intent,
						mCallerActivity,
						optionsBundle,
						mTargetFragment,
						mEnterAnim, mExitAnim,
						mRequestCode,
						mShouldFinishActivityAfterResult
				);
			}
		}

		/**
		 * This method will start the permission check flow and query for the permissions set through
		 * {@link #setRequiredPermissions(String...)} and {@link #setOptionalPermissions(String...)}
		 *
		 * @throws RuntimeException thrown if no permissions are specified when this method is called
		 */
		public void verifyPermissions() {
			final Bundle permissionsDataBundle = getPermissionsDataBundle(mCallerActivity,
					mRequiredPermissions, mOptionalPermissions);
			if (permissionsDataBundle != null) {
				addDefaultDataToPermissionsBundle(permissionsDataBundle);
				startPermissionsFragment(mCallerActivity, permissionsDataBundle, mListener);
			} else {
				if (mListener != null) {
					mListener.onSuccess();
				}
			}
		}

		/**
		 * Data that will always be sent in to the permissions bundle regardless of the execution flow
		 *
		 * @param bundle
		 * 		the permission bundle to add the data into
		 */
		private void addDefaultDataToPermissionsBundle(final Bundle bundle) {
			bundle.putBoolean(SHOULD_SHOW_UI_UPON_FAILURE_EXTRA, !mNoUIForFailure);
			bundle.putString(CUSTOM_AUTO_DENIED_MESSAGE_EXTRA, mCustomAutoDeniedMessage);
		}

		private PermissMe launchPermissMe(final Bundle bundle) {
			bundle.putBoolean(DESTINATION_FINISH_ACTIVITY_UPON_RESULT_EXTRA, mShouldFinishActivityAfterResult);
			bundle.putInt(DESTINATION_ACTIVITY_ENTER_TRANSITION_EXTRA, mEnterAnim);
			bundle.putInt(DESTINATION_ACTIVITY_EXIT_TRANSITION_EXTRA, mExitAnim);
			addDefaultDataToPermissionsBundle(bundle);

			if (mShouldStartActivityForResult) {
				bundle.putInt(DESTINATION_START_ACTIVITY_WITH_REQUEST_CODE, mRequestCode);
			}

			final PermissMe fragment = startPermissionsFragment(mCallerActivity, bundle, mListener);
			if (mTargetFragment != null) {
				fragment.setTargetFragment(mTargetFragment, mRequestCode);
			}
			return fragment;
		}
	}

	private void setLaunchIntent(final Intent activityIntent) {
		mLaunchIntent = activityIntent;
	}

	private static Bundle getPermissionsDataBundle(final Context context,
												   final String[] requiredPermissions,
												   final String[] optionalPermissions) {

		if (requiredPermissions.length <= 0 && optionalPermissions.length < 0) {
			throw new RuntimeException("No permissions specified to ask user to grant. "
					+ "Specify permissions using setRequiredPermissions() and "
					+ "setOptionalPermissions()");
		}

		final boolean shouldRequestRequiredPermissions =
				PermissMeUtils.needToRequestPermission(context, requiredPermissions);
		final boolean shouldRequestOptionalPermissions =
				PermissMeUtils.needToRequestPermission(context, optionalPermissions);

		Bundle bundle = null;
		if (shouldRequestRequiredPermissions || shouldRequestOptionalPermissions) {
			bundle = new Bundle();
			if (shouldRequestRequiredPermissions) {
				bundle.putStringArray(REQUIRED_PERMISSIONS_REQUESTED_EXTRA, requiredPermissions);
			}

			if (shouldRequestOptionalPermissions) {
				bundle.putStringArray(OPTIONAL_PERMISSIONS_REQUESTED_EXTRA, optionalPermissions);
			}
		}
		return bundle;
	}

	/**
	 * Launches the intent passed in and deals with any of the parameters specified
	 *
	 * @param activityIntent
	 * 		the activity intent to launch
	 * @param callerActivity
	 * 		the caller activity to launch the intent with
	 * @param launchOptionsBundle
	 * 		Additional options for how the Activity should be started, passed in through
	 * 		{@link AppCompatActivity#startActivity(Intent, Bundle)} or
	 * 		{@link AppCompatActivity#startActivityForResult(Intent, int, Bundle)}
	 * @param targetFragment
	 * 		a target fragment if specified to receive the callback
	 * 		{@link AppCompatActivity#onActivityResult(int, int, Intent)} with the specified request code parameter
	 * @param enterAnim
	 * 		the res id of the activity-enter animation, 0 if none specified
	 * @param exitAnim
	 * 		the res id of the activity-exit animation, 0 if none specified
	 * @param requestCode
	 * 		the request code to start the destination activity with
	 * 		{@link AppCompatActivity#startActivityForResult(Intent, int)}
	 * @param shouldFinishActivityAfterResult
	 * 		boolean whether should finish the caller activity after launching the
	 */
	@SuppressWarnings("checkstyle:parameternumber")
	@VisibleForTesting
	/*package*/ static void launchIntentWithParameters(@NonNull final Intent activityIntent,
	                                                   @NonNull final AppCompatActivity callerActivity,
	                                                   @Nullable final Bundle launchOptionsBundle,
	                                                   @Nullable final Fragment targetFragment,
	                                                   @AnimRes final int enterAnim,
	                                                   @AnimRes final int exitAnim,
	                                                   final int requestCode,
	                                                   final boolean shouldFinishActivityAfterResult) {
		// Launch the activity on the main thread incase there are some animations the caller wants executed
		PermissMeUtils.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (requestCode != 0) {
					if (targetFragment != null) {
						targetFragment.startActivityForResult(activityIntent, requestCode, launchOptionsBundle);
					} else {
						callerActivity.startActivityForResult(activityIntent, requestCode, launchOptionsBundle);
					}
				} else {
					callerActivity.startActivity(activityIntent, launchOptionsBundle);
				}

				if (enterAnim != 0 || exitAnim != 0) {
					PermissMe.overrideDefaultTransition(callerActivity, enterAnim, exitAnim);
				}

				if (shouldFinishActivityAfterResult) {
					callerActivity.finish();
				}
			}
		});
	}

	@NonNull
	private static PermissMe startPermissionsFragment(final AppCompatActivity activity,
	                                                  @NonNull final Bundle permissionsData,
	                                                  @Nullable final PermissionListener listener) {
		PermissMe permissionFragment = (PermissMe) activity
						.getSupportFragmentManager()
						.findFragmentByTag(PermissMe.TAG);
		if (permissionFragment == null) {
			permissionFragment = new PermissMe();
			activity.getSupportFragmentManager()
					.beginTransaction()
					.add(permissionFragment, PermissMe.TAG)
					.commit();
		}

		if (listener != null) {
			permissionFragment.setListener(listener);
		}
		permissionFragment.setDataBundle(permissionsData);
		permissionFragment.startPermissionsFlow();
		return permissionFragment;
	}

	private void setDataBundle(final Bundle bundle) {
		mPermissionsInfoBundle = bundle;
	}

	private void startPermissionsFlow() {
		// Request the permissions once this fragment is attached to its activity (queue to main thread)
		// Note - if there are required permissions, we will check for optional permissions in the callback if user allows
		// required permissions.
		PermissMeUtils.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				final String[] requiredPermissions = getUngrantedRequiredPermissions();
				// We want to query for optional permissions once the user accepts existing required permissions
				final String[] optionalPermissions =
						requiredPermissions.length == 0 ? getUngrantedOptionalPermissions() : null;

				if (requiredPermissions.length != 0) {
					requestPermissions(requiredPermissions, REQUIRED_PERMISSION_REQUEST_CODE);
				} else if (optionalPermissions.length != 0) {
					requestPermissions(optionalPermissions, OPTIONAL_PERMISSION_REQUEST_CODE);
				}
			}
		});
	}

	/**
	 * Returns an array of permissions denied/ungranted by the user.
	 *
	 * @param permissionsExtraKey Bundle Key in mPermissionsInfoBundle that has the list of permissions you want to check
	 */
	@NonNull
	private String[] getUngrantedPermissions(final String permissionsExtraKey) {
		final String[] permissions = mPermissionsInfoBundle.getStringArray(permissionsExtraKey);
		final Context ctx = getContext();
		return ctx != null ? PermissMeUtils.getDeniedPermissions(ctx, permissions) : new String[0];
	}

	@NonNull
	private String[] getUngrantedRequiredPermissions() {
		return getUngrantedPermissions(REQUIRED_PERMISSIONS_REQUESTED_EXTRA);
	}

	@NonNull
	private String[] getUngrantedOptionalPermissions() {
		return getUngrantedPermissions(OPTIONAL_PERMISSIONS_REQUESTED_EXTRA);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Retain the data being held in this fragment; there should be no memory leaks because we do not keep a
		// reference to the caller activity and call cleanUp() when the permissions flow is complete.
		setRetainInstance(true);
	}

	// TODO this method should have a test
	@Override
	public void onRequestPermissionsResult(final int requestCode,
										   @NonNull final String[] permissions,
										   @NonNull final int[] grantResults) {

		// can occur if permissions check is cancelled in between
		if (permissions.length == 0) {
			return;
		}

		if (requestCode == REQUIRED_PERMISSION_REQUEST_CODE) {
			// Check if required permissions were granted
			if (PermissMeUtils.verifyPermissions(grantResults)) {
				final String[] optionalPermissions = getUngrantedOptionalPermissions();

				if (optionalPermissions.length == 0) {
					// No optional permissions, we're done
					onPermissionsFinalResults(requestCode, permissions, true);
				} else {
					// Query for grant of optional permissions
					requestPermissions(optionalPermissions, OPTIONAL_PERMISSION_REQUEST_CODE);
				}
			} else {
				onPermissionsFinalResults(requestCode, permissions, false);
			}
		} else if (requestCode == OPTIONAL_PERMISSION_REQUEST_CODE) {
			onPermissionsFinalResults(requestCode, permissions, PermissMeUtils.verifyPermissions(grantResults));
		} else {
			// Nothing special to do
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	// TODO this method should have a test
	private void onPermissionsFinalResults(final int requestCode,
	                                       final String[] permissions,
	                                       final boolean permissionGranted) {
		if (requestCode == REQUIRED_PERMISSION_REQUEST_CODE) {
			if (permissionGranted) {
				// There were only required permissions
				onSuccess();
			} else {
				// Required permission was denied
				onPermissionDenied(requestCode, permissions);
			}
		} else if (requestCode == OPTIONAL_PERMISSION_REQUEST_CODE) {
			// Regardless of whether user denied/granted the permission, report onSuccess because the permissions
			// were optional anyway
			onSuccess();
			if (!permissionGranted) {
				// Provide the permission denied callback for the optional permission
				onPermissionDenied(requestCode, permissions);
			}
		}
		cleanUp();
	}

	/**
	 * This helper fragment should not be handling any callbacks, there should be a target fragment set through
	 * the builder using {@link Builder#targetFragment(Fragment)}. The target fragment's
	 * {@link Fragment#onActivityResult(int, int, Intent)} will be called from here.
	 */
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (getTargetFragment() != null) {
			getTargetFragment().onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		cleanUp();
	}

	/**
	 * Listener that can be used to receive a callback for the permission request made.
	 */
	public interface PermissionListener {
		/**
		 * The callback when required permissions were granted by the user (does not guarantee optional permissions
		 * were granted. This callback occurs even when optional permissions were denied)
		 */
		void onSuccess();

		/**
		 * The callback when required permissions were ungranted
		 *
		 * @param deniedPermissions
		 * 		the denied required permissions that were queried for
		 * @param isAutoDenied
		 * 		an array of booleans indicating whether the deniedPermissions were auto-denied,
		 * 		ergo, the user previously or currently checked "Never ask again" on the permission dialog.
		 * 		Each boolean is associated with a permission in the deniedPermissions list
		 * 		A {@link com.google.android.material.snackbar.Snackbar} is shown by default telling the user to turn the
		 * 		permission on with an action button that will take them to the app's settings. Caller can specify
		 * 		turning the snackbar off using {@link Builder#showNoUIOnFailure()} to handle this by themselves.
		 */
		void onRequiredPermissionDenied(final String[] deniedPermissions, boolean[] isAutoDenied);

		/**
		 * The callback when optional permissions were ungranted. {@link PermissionListener#onSuccess()} will still
		 * be called because the permissions were optional. This method is provided if you want to do logging or
		 * perform any special operations when optional permissions were denied. Denying optional permissions should
		 * still allow the operation the user was trying to execute to continue.
		 *
		 * @param deniedPermissions
		 * 		the denied optional permissions that were queried for
		 * @param isAutoDenied
		 * 		an array of booleans indicating whether the deniedPermissions were auto-denied,
		 * 		ergo, the user previously or currently checked "Never ask again" on the permission dialog.
		 * 		Each boolean is associated with a permission in the deniedPermissions list
		 */
		void onOptionalPermissionDenied(final String[] deniedPermissions, boolean[] isAutoDenied);
	}

	/**
	 * Get the denied permissions, check if each one was auto-denied or not, pass this info to the listeners and
	 * show the UI for an auto-denied required permission
	 *
	 * @param requestCode indicates whether these are required or optional permissions
	 * @param permissions the permissions in question
	 */
	@VisibleForTesting
	/*package*/ void onPermissionDenied(final int requestCode, final String[] permissions) {
		final String[] deniedPermissions = PermissMeUtils.getDeniedPermissions(getActivity(), permissions);

		// There will definitely be denied permissions at this point, can suppress this
		@SuppressWarnings("ConstantConditions")
		final boolean[] isAutoDeniedPermissions = new boolean[deniedPermissions.length];
		boolean hasAtleastOneAutoDeniedPermission = false;
		for (int i = 0; i < deniedPermissions.length; i++) {
			final boolean isAutoDenied = PermissMeUtils.isAutoDeniedPermission(
					(AppCompatActivity) getActivity(),
					deniedPermissions[i]);
			isAutoDeniedPermissions[i] = isAutoDenied;
			hasAtleastOneAutoDeniedPermission |= isAutoDenied;
		}

		if (mListener != null) {
			if (requestCode == OPTIONAL_PERMISSION_REQUEST_CODE) {
				mListener.onOptionalPermissionDenied(deniedPermissions, isAutoDeniedPermissions);
			} else if (requestCode == REQUIRED_PERMISSION_REQUEST_CODE) {
				mListener.onRequiredPermissionDenied(deniedPermissions, isAutoDeniedPermissions);
			}
		}

		// Show the snackbar with a 'settings' button the user can tap to go to their app's setting screen and turn
		// the permissions on from
		if (hasAtleastOneAutoDeniedPermission
				&& requestCode == REQUIRED_PERMISSION_REQUEST_CODE
				&& shouldShowPermissionDeniedSnackbar()) {
			showPermissionDeniedSnackbar();
		}
	}

	/**
	 * Internal method that handles the success case of required permissions being granted and/or optional
	 * permissions getting a result
	 */
	@VisibleForTesting
	/*package*/ void onSuccess() {
		if (mListener != null) {
			mListener.onSuccess();
		}

		if (getDestinationActivityClass() != null || mLaunchIntent != null) {
			// Launch the destination activity! Onwards!
			launchDestinationIntent();
		}
	}

	/**
	 * Launches the destination intent/activity specified through the builder
	 */
	@VisibleForTesting
	/*package*/ void launchDestinationIntent() {
		// Launch the destination intent/activity! Onwards!
		final Intent destinationIntent;
		if (mLaunchIntent != null) {
			destinationIntent = mLaunchIntent;
		} else {
			destinationIntent = new Intent(getActivity(), getDestinationActivityClass());
			if (getDestinationActivityBundle() != null) {
				destinationIntent.putExtras(getDestinationActivityBundle());
			}
		}
		final int requestCode = getDestinationActivityRequestCode();
		@AnimRes final int enterAnim = getEnterAnimation();
		@AnimRes final int exitAnim = getExitAnimation();
		final boolean finishCallerActivity = mPermissionsInfoBundle
				.getBoolean(DESTINATION_FINISH_ACTIVITY_UPON_RESULT_EXTRA);
		final Bundle launchOptionsBundle = getDestinationActivityLaunchOptionsBundle();

		launchIntentWithParameters(destinationIntent,
				(AppCompatActivity) getActivity(),
				launchOptionsBundle,
				getTargetFragment(),
				enterAnim,
				exitAnim,
				requestCode,
				finishCallerActivity
		);
	}

	/**
	 * Override the transition, safely
	 *
	 * @param activity
	 * 		the Activity
	 * @param enterAnim
	 * 		the enter activity transition
	 * @param exitAnim
	 * 		the exit activity transition
	 */
	private static void overrideDefaultTransition(final AppCompatActivity activity,
	                                              @AnimRes final int enterAnim,
	                                              @AnimRes final int exitAnim) {
		if (activity != null) {
			activity.overridePendingTransition(enterAnim, exitAnim);
		}
	}

	/**
	 * Can be null if the user did not not specify launching a destination activity after permissions flow
	 *
	 * @return the destination activity class, or null if not specified
	 */
	@Nullable
	Class getDestinationActivityClass() {
		return (Class) mPermissionsInfoBundle.getSerializable(DESTINATION_ACTIVITY_CLASS_EXTRA);
	}

	@VisibleForTesting
	/*package*/ int getDestinationActivityRequestCode() {
		return mPermissionsInfoBundle.getInt(DESTINATION_START_ACTIVITY_WITH_REQUEST_CODE);
	}

	@Nullable
	Bundle getDestinationActivityBundle() {
		return mPermissionsInfoBundle.getBundle(DESTINATION_ACTIVITY_BUNDLE_EXTRA);
	}

	@Nullable
	@VisibleForTesting
	/*package*/ Bundle getDestinationActivityLaunchOptionsBundle() {
		return mPermissionsInfoBundle.getBundle(DESTINATION_ACTIVITY_LAUNCH_OPTIONS_EXTRA);
	}

	@AnimRes
	@VisibleForTesting
	/*package*/ int getEnterAnimation() {
		return mPermissionsInfoBundle.getInt(DESTINATION_ACTIVITY_ENTER_TRANSITION_EXTRA);
	}

	@AnimRes
	@VisibleForTesting
	/*package*/ int getExitAnimation() {
		return mPermissionsInfoBundle.getInt(DESTINATION_ACTIVITY_EXIT_TRANSITION_EXTRA);
	}

	private boolean shouldShowPermissionDeniedSnackbar() {
		return mPermissionsInfoBundle.getBoolean(SHOULD_SHOW_UI_UPON_FAILURE_EXTRA, true);
	}

	private void showPermissionDeniedSnackbar() {
		PermissMeUtils.showPermissionDeniedSnackbar(getActivity(), getCustomAutoDeniedMessage());
	}

	@Nullable
	private String getCustomAutoDeniedMessage() {
		return mPermissionsInfoBundle.getString(CUSTOM_AUTO_DENIED_MESSAGE_EXTRA);
	}

	private void cleanUp() {
		// Don't hold a reference to the listener anymore
		mListener = null;
	}

	private void setListener(final PermissionListener listener) {
		mListener = listener;
	}
}
