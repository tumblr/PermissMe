package com.tumblr.permissme;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

/**
 * Configuration singleton class that will setup your custom configuration of {@link PermissMe} permission denied
 * messages and colors for the {@link android.support.design.widget.Snackbar}
 */
public final class PermissMeConfig {

	private static final PermissMeConfig INSTANCE = new PermissMeConfig();

	@StringRes
	private int mDefaultPermissionDeniedMsgRes;

	@StringRes
	private int mDefaultCtaButtonStringRes;

	@ColorRes
	private int mTextColorRes;

	@ColorRes
	private int mSnackBarBgColorRes;

	public static PermissMeConfig getInstance() {
		return INSTANCE;
	}

	private PermissMeConfig() {
		mDefaultPermissionDeniedMsgRes = R.string.default_permission_denied_msg;
		mDefaultCtaButtonStringRes = R.string.default_permission_denied_cta_text;
		mTextColorRes =  R.color.white;
		mSnackBarBgColorRes = R.color.red_toast_background_color;
	}

	/**
	 * Config method to setup the strings and color resources pertaining to {@link PermissMe}
	 *
	 * @param defaultPermissionDeniedMessage
	 * 		The res id for the message that will show on the auto-denied permission snackbar; will use default if this
	 * 		is 0
	 * @param defaultCtaButtonString
	 * 		The res id for the cta string that will show on the auto-denied permission snackbar; will use default if
	 * 		this is 0
	 * @param textColorRes
	 * 		The resource id of the color that will be the cta color on the auto-denied permission snackbar; will use
	 * 		default if this is 0
	 * @param snackbarBgColorRes
	 * 		The resource id of the color that will be the bg color on the auto-denied permission snackbar; will use
	 * 		default if this is 0
	 */
	public static void config(@StringRes final int defaultPermissionDeniedMessage,
	                          @StringRes final int defaultCtaButtonString,
	                          @ColorRes final int textColorRes,
	                          @ColorRes final int snackbarBgColorRes) {

		if (defaultPermissionDeniedMessage != 0) {
			getInstance().setDefaultPermissionDeniedMessage(defaultPermissionDeniedMessage);
		}

		if (defaultCtaButtonString != 0) {
			getInstance().setDefaultCtaButtonString(defaultCtaButtonString);
		}

		if (textColorRes != 0) {
			getInstance().setTextColorRes(textColorRes);
		}

		if (snackbarBgColorRes != 0) {
			getInstance().setSnackBarBgColorRes(snackbarBgColorRes);
		}
	}

	/**
	 * The message that will show when a permission is auto-denied through the user checking "do not ask again" on
	 * the permission dialog
	 *
	 * @return the string res id for the message
	 */
	@StringRes
	public int getDefaultPermissionDeniedMsg() {
		return mDefaultPermissionDeniedMsgRes;
	}

	/**
	 * The string on the cta button of the {@link android.support.design.widget.Snackbar} that will take the user to
	 * the app settings to toggle their permissions
	 * @return the string res id for the cta button message
	 */
	@StringRes
	public int getDefaultCtaButtonMsg() {
		return mDefaultCtaButtonStringRes;
	}

	/**
	 * The resource id of the color that will be set as the background color of the permission auto-denied
	 * {@link android.support.design.widget.Snackbar}
	 * @return the color resource id
	 */
	@ColorRes
	public int getSnackBarBgColorRes() {
		return mSnackBarBgColorRes;
	}

	/**
	 * The resource id of the color that will be set as the CTA button color on the permission denied
	 * {@link android.support.design.widget.Snackbar}
	 *
	 * @return the color resource id
	 */
	@ColorRes
	public int getTextColorRes() {
		return mTextColorRes;
	}

	private void setDefaultPermissionDeniedMessage(@StringRes final int strResId) {
		mDefaultPermissionDeniedMsgRes = strResId;
	}

	private void setDefaultCtaButtonString(@StringRes final int ctaResId) {
		mDefaultCtaButtonStringRes = ctaResId;
	}

	private void setSnackBarBgColorRes(@ColorRes final int snackBarBgColorRes) {
		mSnackBarBgColorRes = snackBarBgColorRes;
	}

	private void setTextColorRes(@ColorRes final int textColorRes) {
		mTextColorRes = textColorRes;
	}
}
