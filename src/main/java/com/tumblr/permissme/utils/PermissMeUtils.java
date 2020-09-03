package com.tumblr.permissme.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;

import com.google.android.material.snackbar.Snackbar;
import com.tumblr.permissme.PermissMeConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Utils to help in common runtime permission related tasks introduced in AndroidM
 * @see <a href="http://developer.android.com/intl/ko/training/permissions/requesting.html">Runtime Permissions Ref
 * Doc</a>}
 */
public final class PermissMeUtils {

	private PermissMeUtils() {

	}

	/**
	 * Check that all given permissions have been granted by verifying that each entry in the
	 * given array is of the value {@link PermissionChecker#PERMISSION_GRANTED}.
	 *
	 * @param grantResults
	 * 		the results to check whether they are granted
	 * @return whether the permissions have been granted
	 *
	 * @see Activity#onRequestPermissionsResult(int, String[], int[])
	 */
	public static boolean verifyPermissions(@NonNull final int[] grantResults) {
		// At least one result must be checked.
		if (grantResults.length < 1) {
			return false;
		}

		// Verify that each required permission has been granted, otherwise return false.
		for (int result : grantResults) {
			if (result != PermissionChecker.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Given a list of permissions, get all the permissions that are denied by the user.
	 *
	 * @param context
	 * 		Caller context
	 * @param permissions
	 * 		List of permissions to check
	 * @return The denied permission, if there is one, else empty array
	 */
	@NonNull
	public static String[] getDeniedPermissions(@NonNull final Context context, @Nullable final String... permissions) {
		if (permissions == null || permissions.length <= 0) {
			return new String[0];
		}

		final List<String> deniedPermissions = new ArrayList<>();
		for (int i = 0; i < permissions.length; i++) {
			final String permission = permissions[i];
			if (!PermissMeUtils.permissionIsInvalidOrHasPermission(context, permission)) {
				deniedPermissions.add(permission);
			}
		}

		final String[] p = new String[deniedPermissions.size()];
		return deniedPermissions.toArray(p);
	}

	/**
	 * Check if we already have access granted for this permission
	 *
	 * @param context
	 * 		The caller activity
	 * @param permissions
	 * 		The permissions that needs to be granted
	 * @return Whether we have access to the permission
	 */
	public static boolean needToRequestPermission(@NonNull final Context context, @NonNull final String... permissions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			for (int i = 0; i < permissions.length; i++) {
				final String permission = permissions[i];
				if (!PermissMeUtils.permissionIsInvalidOrHasPermission(context, permission)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the permission is invalid (null) or if it's not, if the package has the parameter permission already
	 * granted
	 *
	 * @param context
	 * 		the context
	 * @param permission
	 * 		permission to check
	 * @return {@code true} if the permission is null or empty or the package has the permission already,
	 * {@code false} if the permission is ungranted for the package
	 */
	@VisibleForTesting
	/*package*/ static boolean permissionIsInvalidOrHasPermission(@NonNull final Context context,
	                                                              final String permission) {
		return permission == null
				|| permission.isEmpty()
				|| PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
	}

	/**
	 * Shows the snackbar which is used in the scenario where user auto-denied permissions before. It can also be
	 * used to display the permissions snackbar if user is trying to interact with a feature which requires
	 * permissions the user hasn't granted
	 *
	 * @param activity
	 * 		the activity with the view to attach the snackbar to
	 * @param customErrorMessage
	 * 		an optional custom error message. this method uses the default one if the custom one
	 * 		is null
	 */
	public static void showPermissionDeniedSnackbar(@NonNull final Activity activity,
	                                                @Nullable final String customErrorMessage) {
		final View topView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);

		final String defaultErrorMessage =
				activity.getResources().getString(PermissMeConfig.getInstance().getDefaultPermissionDeniedMsg());
		final String errorMessage = customErrorMessage != null ? customErrorMessage : defaultErrorMessage;

		final String ctaString =
				activity.getResources().getString(PermissMeConfig.getInstance().getDefaultCtaButtonMsg());

		if (topView != null) {
			showSnackBar(topView,
					errorMessage,
					activity.getResources().getColor(PermissMeConfig.getInstance().getSnackBarBgColorRes()),
					ctaString,
					createSettingsClickListener(activity));
		}
	}

	/**
	 * Common camera permission check.
	 *
	 * @param context
	 * 		caller context
	 * 	@return whether the package has camera permissions granted
	 */
	public static boolean hasCameraPermission(final Context context) {
		return !PermissMeUtils.needToRequestPermission(context, Manifest.permission.CAMERA);
	}

	/**
	 * Common READ/WRITE Storage Permission Check.
	 *
	 * @param context
	 * 		caller context
	 * @return whether the package has read+write storage permissions granted
	 */
	public static boolean hasReadWriteStoragePermission(final Context context) {
		final String[] storagePermissions = {
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
		};
		return !PermissMeUtils.needToRequestPermission(context, storagePermissions);
	}

	/**
	 * Checks if the permission is auto-denied, i.e. the user tapped "Never ask again" in the dialog before, thus, the
	 * permission cannot be queried for
	 *
	 * @param activity
	 * 		the activity
	 * @param permission
	 * 		the permission being queried
	 * @return whether the permission was auto-denied by the system for the package
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public static boolean isAutoDeniedPermission(final AppCompatActivity activity, @NonNull final String permission) {
		return !activity.shouldShowRequestPermissionRationale(permission);
	}

	/**
	 * The onClickListener that takes you to the app's system settings screen
	 *
	 * @param activity
	 * 		the caller activity used to start the settings intent
	 * @return the {@link View.OnClickListener}
	 */
	public static View.OnClickListener createSettingsClickListener(final Activity activity) {
		return new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.setData(Uri.parse("package:" + activity.getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				activity.startActivity(intent);
			}
		};
	}

	/**
	 * Displays a SnackBar message.
	 *
	 * @param view
	 * 		the view to find a parent from.
	 * @param msg
	 * 		the message to display.
	 * @param backgroundColor
	 * 		the background color.
	 * @param ctaString
	 * 		the call to action string
	 * @param ctaClickListener
	 * 		the call to action click listener.
	 */
	public static void showSnackBar(final View view,
	                                final String msg,
	                                final int backgroundColor,
	                                final String ctaString,
	                                final View.OnClickListener ctaClickListener) {
		final Snackbar snack = Snackbar.make(view, msg,
				ctaString != null ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT);

		final ViewGroup group = (ViewGroup) snack.getView();
		group.setBackgroundColor(backgroundColor);

		final int textColor =
				view.getContext().getResources().getColor(PermissMeConfig.getInstance().getTextColorRes());
		if (ctaString != null && ctaClickListener != null) {
			snack.setActionTextColor(textColor);
			snack.setAction(ctaString, ctaClickListener);
		}

		snack.show();
	}

	/**
	 * Helper method to post a {@link Runnable} to the main thread
	 *
	 * @param runnable
	 * 		runnable to execute on the main thread
	 */
	public static void runOnUiThread(final Runnable runnable) {
		new Handler(Looper.getMainLooper()).post(runnable);
	}
}
