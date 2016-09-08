package com.tumblr.permissme;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import com.tumblr.permissme.utils.PermissMeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link PermissMe} class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PermissMeUtils.class, PermissMe.class})
public class PermissMeTests {

	private PermissMe mPermissMe;

	@Before
	public void init() {
		mPermissMe = new PermissMe();
		PowerMockito.mockStatic(PermissMeUtils.class);
	}

	@Test
	public void testOnPermissionDenied_whenRequiredPermissionsNotAutoDenied_callRequiredPermissionListener() {
		final String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE};
		PowerMockito.when(PermissMeUtils.getDeniedPermissions(any(Activity.class), any(String[].class))).thenReturn(permissions);
		PowerMockito.when(PermissMeUtils.isAutoDeniedPermission(any(AppCompatActivity.class), any(String.class))).thenReturn(false);

		mPermissMe.mListener = mock(TestPermissionListener.class);
		mPermissMe.onPermissionDenied(PermissMe.REQUIRED_PERMISSION_REQUEST_CODE, permissions);

		ArgumentCaptor<String[]> permissionArgumentCaptor = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<boolean[]> autoDeniedArgumentCaptor = ArgumentCaptor.forClass(boolean[].class);
		verify(mPermissMe.mListener, times(1)).onRequiredPermissionDenied(permissionArgumentCaptor.capture(),
				autoDeniedArgumentCaptor.capture());
		verify(mPermissMe.mListener, never()).onOptionalPermissionDenied(any(String[].class),
				any(boolean[].class));
		verify(mPermissMe.mListener, never()).onSuccess();

		PowerMockito.verifyStatic(never());
		PermissMeUtils.showPermissionDeniedSnackbar(any(Activity.class), anyString());

		assertTrue(permissionArgumentCaptor.getValue().length == 1);
		assertTrue(autoDeniedArgumentCaptor.getValue().length == 1);
		assertArrayEquals(permissions, permissionArgumentCaptor.getValue());
		assertFalse(autoDeniedArgumentCaptor.getValue()[0]);
	}

	@Test
	public void testOnPermissionDenied_whenRequiredPermissionsAutoDenied_callRequiredPermissionListenerShowFailureUI() {
		final String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE};
		PowerMockito.when(PermissMeUtils.getDeniedPermissions(any(Activity.class), any(String[].class))).thenReturn
				(permissions);
		PowerMockito.when(PermissMeUtils.isAutoDeniedPermission(any(AppCompatActivity.class), any(String.class)))
				.thenReturn(true);

		// Return true to show permissionDeniedSnackbar
		mPermissMe.mPermissionsInfoBundle = mock(Bundle.class);
		when(mPermissMe.mPermissionsInfoBundle.getBoolean(eq(PermissMe.SHOULD_SHOW_UI_UPON_FAILURE_EXTRA), eq(true)))
				.thenReturn(true);

		// Create mock listener to check that correct callbacks are being called
		mPermissMe.mListener = mock(TestPermissionListener.class);

		// <<< EXECUTE CALL TO METHOD >>>
		mPermissMe.onPermissionDenied(PermissMe.REQUIRED_PERMISSION_REQUEST_CODE, permissions);

		ArgumentCaptor<String[]> permissionArgumentCaptor = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<boolean[]> autoDeniedArgumentCaptor = ArgumentCaptor.forClass(boolean[].class);

		// Test callback methods in listener
		verify(mPermissMe.mListener, times(1)).onRequiredPermissionDenied(permissionArgumentCaptor.capture(),
				autoDeniedArgumentCaptor.capture());
		verify(mPermissMe.mListener, never()).onOptionalPermissionDenied(any(String[].class),
				any(boolean[].class));
		verify(mPermissMe.mListener, never()).onSuccess();

		// Test values of the callbacks
		assertTrue(permissionArgumentCaptor.getValue().length == 1);
		assertTrue(autoDeniedArgumentCaptor.getValue().length == 1);
		assertArrayEquals(permissions, permissionArgumentCaptor.getValue());
		assertTrue(autoDeniedArgumentCaptor.getValue()[0]);

		// Test that we do show the permission denied snackbar
		PowerMockito.verifyStatic(times(1));
		PermissMeUtils.showPermissionDeniedSnackbar(any(Activity.class), anyString());
	}

	@Test
	public void
	testOnPermissionDenied_whenOptionalPermissionsNotAutoDenied_callOptionalPermissionDeniedListener() {
		final String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE};
		PowerMockito.when(PermissMeUtils.getDeniedPermissions(any(Activity.class), any(String[].class))).thenReturn
				(permissions);
		PowerMockito.when(PermissMeUtils.isAutoDeniedPermission(any(AppCompatActivity.class), any(String.class)))
				.thenReturn(false);

		// Create mock listener to check that correct callbacks are being called
		mPermissMe.mListener = mock(TestPermissionListener.class);

		// <<< EXECUTE CALL TO METHOD >>>
		mPermissMe.onPermissionDenied(PermissMe.OPTIONAL_PERMISSION_REQUEST_CODE, permissions);

		ArgumentCaptor<String[]> permissionArgumentCaptor = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<boolean[]> autoDeniedArgumentCaptor = ArgumentCaptor.forClass(boolean[].class);

		// Test callback methods in listener
		verify(mPermissMe.mListener, times(1)).onOptionalPermissionDenied(permissionArgumentCaptor.capture(),
				autoDeniedArgumentCaptor.capture());
		verify(mPermissMe.mListener, never()).onRequiredPermissionDenied(any(String[].class),
				any(boolean[].class));
		verify(mPermissMe.mListener, never()).onSuccess();

		// Test values of the callbacks
		assertTrue(permissionArgumentCaptor.getValue().length == 1);
		assertTrue(autoDeniedArgumentCaptor.getValue().length == 1);
		assertArrayEquals(permissions, permissionArgumentCaptor.getValue());
		assertFalse(autoDeniedArgumentCaptor.getValue()[0]);

		// Test that we do show the permission denied snackbar
		PowerMockito.verifyStatic(never());
		PermissMeUtils.showPermissionDeniedSnackbar(any(Activity.class), anyString());
	}

	@Test
	public void testOnPermissionDenied_whenOptionalPermissionsAutoDenied_callOptionalPermissionDeniedListenerShowNoFailUi() {
		final String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE};
		PowerMockito.when(PermissMeUtils.getDeniedPermissions(any(Activity.class), any(String[].class))).thenReturn
				(permissions);
		PowerMockito.when(PermissMeUtils.isAutoDeniedPermission(any(AppCompatActivity.class), any(String.class)))
				.thenReturn(true);

		// Create mock listener to check that correct callbacks are being called
		mPermissMe.mListener = mock(TestPermissionListener.class);

		// <<< EXECUTE CALL TO METHOD >>>
		mPermissMe.onPermissionDenied(PermissMe.OPTIONAL_PERMISSION_REQUEST_CODE, permissions);

		ArgumentCaptor<String[]> permissionArgumentCaptor = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<boolean[]> autoDeniedArgumentCaptor = ArgumentCaptor.forClass(boolean[].class);

		// Test callback methods in listener
		verify(mPermissMe.mListener, times(1)).onOptionalPermissionDenied(permissionArgumentCaptor.capture(),
				autoDeniedArgumentCaptor.capture());
		verify(mPermissMe.mListener, never()).onRequiredPermissionDenied(any(String[].class),
				any(boolean[].class));
		verify(mPermissMe.mListener, never()).onSuccess();

		// Test values of the callbacks
		assertTrue(permissionArgumentCaptor.getValue().length == 1);
		assertTrue(autoDeniedArgumentCaptor.getValue().length == 1);
		assertArrayEquals(permissions, permissionArgumentCaptor.getValue());
		assertTrue(autoDeniedArgumentCaptor.getValue()[0]);

		// Test that we do show the permission denied snackbar
		PowerMockito.verifyStatic(never());
		PermissMeUtils.showPermissionDeniedSnackbar(any(Activity.class), anyString());
	}

	@Test
	public void
	testOnPermissionDenied_withOneAutoDeniedRequiredPermissionOneNotAutoDenied_callRequiredPermissionDeniedWithDifferentAutoDeniedResults() {
		final String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_SMS};

		PowerMockito.when(PermissMeUtils.getDeniedPermissions(any(Context.class), Matchers.<String>anyVararg())).thenReturn
				(permissions);
		PowerMockito.when(PermissMeUtils.isAutoDeniedPermission(any(AppCompatActivity.class), eq(Manifest.permission.WRITE_EXTERNAL_STORAGE)))
				.thenReturn(false);
		PowerMockito.when(PermissMeUtils.isAutoDeniedPermission(any(AppCompatActivity.class), eq(Manifest.permission.READ_SMS)))
				.thenReturn(true);

		// Return true to show permissionDeniedSnackbar
		mPermissMe.mPermissionsInfoBundle = mock(Bundle.class);
		when(mPermissMe.mPermissionsInfoBundle.getBoolean(eq(PermissMe.SHOULD_SHOW_UI_UPON_FAILURE_EXTRA), eq(true)))
				.thenReturn(true);

		// Create mock listener to check that correct callbacks are being called
		mPermissMe.mListener = mock(TestPermissionListener.class);

		// <<< EXECUTE CALL TO METHOD >>>
		mPermissMe.onPermissionDenied(PermissMe.REQUIRED_PERMISSION_REQUEST_CODE, permissions);

		ArgumentCaptor<String[]> permissionArgumentCaptor = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<boolean[]> autoDeniedArgumentCaptor = ArgumentCaptor.forClass(boolean[].class);

		// Test callback methods in listener
		verify(mPermissMe.mListener, times(1)).onRequiredPermissionDenied(permissionArgumentCaptor.capture(),
				autoDeniedArgumentCaptor.capture());
		verify(mPermissMe.mListener, never()).onOptionalPermissionDenied(any(String[].class),
				any(boolean[].class));
		verify(mPermissMe.mListener, never()).onSuccess();

		// Test values of the callbacks
		assertTrue(permissionArgumentCaptor.getValue().length == 2);
		assertTrue(autoDeniedArgumentCaptor.getValue().length == 2);
		assertArrayEquals(permissions, permissionArgumentCaptor.getValue());
		assertFalse(autoDeniedArgumentCaptor.getValue()[0]);
		assertTrue(autoDeniedArgumentCaptor.getValue()[1]);

		// Test that we do show the permission denied snackbar
		PowerMockito.verifyStatic(times(1));
		PermissMeUtils.showPermissionDeniedSnackbar(any(Activity.class), anyString());
	}

	@Test
	public void testOnPermissionDenied_listenerNullRequiredPermission_noListenerCallBackAndShowFailureUi() {
		final String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE };

		PowerMockito.when(PermissMeUtils.getDeniedPermissions(any(Activity.class), any(String[].class))).thenReturn
				(permissions);
		PowerMockito.when(PermissMeUtils.isAutoDeniedPermission(any(AppCompatActivity.class), anyString()))
				.thenReturn(true);

		// Return true to show permissionDeniedSnackbar
		mPermissMe.mPermissionsInfoBundle = mock(Bundle.class);
		when(mPermissMe.mPermissionsInfoBundle.getBoolean(eq(PermissMe.SHOULD_SHOW_UI_UPON_FAILURE_EXTRA), eq(true)))
				.thenReturn(true);

		// <<< EXECUTE CALL TO METHOD >>>
		mPermissMe.onPermissionDenied(PermissMe.REQUIRED_PERMISSION_REQUEST_CODE, permissions);

		// Test that we do show the permission denied snackbar
		PowerMockito.verifyStatic(times(1));
		PermissMeUtils.showPermissionDeniedSnackbar(any(Activity.class), anyString());
	}

	@Test
	public void testOnPermissionDenied_listenerNullOptionalPermission_noListenerCallBackAndDontShowFailureUi() {
		final String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE };

		PowerMockito.when(PermissMeUtils.getDeniedPermissions(any(Activity.class), any(String[].class))).thenReturn
				(permissions);
		PowerMockito.when(PermissMeUtils.isAutoDeniedPermission(any(AppCompatActivity.class), anyString()))
				.thenReturn(true);

		// Return true to show permissionDeniedSnackbar
		mPermissMe.mPermissionsInfoBundle = mock(Bundle.class);
		when(mPermissMe.mPermissionsInfoBundle.getBoolean(eq(PermissMe.SHOULD_SHOW_UI_UPON_FAILURE_EXTRA), eq(true)))
				.thenReturn(true);

		// <<< EXECUTE CALL TO METHOD >>>
		mPermissMe.onPermissionDenied(PermissMe.OPTIONAL_PERMISSION_REQUEST_CODE, permissions);

		// Test that we do show the permission denied snackbar
		PowerMockito.verifyStatic(never());
		PermissMeUtils.showPermissionDeniedSnackbar(any(Activity.class), anyString());
	}

	@Test
	public void testOnSuccess_listenerHasValueAndNoIntentToLaunch_callOnSuccessDontLaunchIntent() {
		// Create mock listener to check that correct callbacks are being called
		mPermissMe.mListener = mock(TestPermissionListener.class);

		// Fix a mock permission bundle
		mPermissMe.mPermissionsInfoBundle = mock(Bundle.class);

		final PermissMe spyPermissMe = spy(mPermissMe);
		spyPermissMe.onSuccess();

		verify(spyPermissMe.mListener, times(1)).onSuccess();
		verify(spyPermissMe, never()).launchDestinationIntent();
	}

	@Test
	public void testOnSuccess_listenerNullAndHasLaunchIntent_callLaunchDestinationIntent() {

		// Fix a mock permission bundle
		mPermissMe.mPermissionsInfoBundle = mock(Bundle.class);
		// Mock launch intent
		mPermissMe.mLaunchIntent = mock(Intent.class);

		final PermissMe spyPermissMe = spy(mPermissMe);

		doNothing().when(spyPermissMe).launchDestinationIntent();

		// <<< EXECUTE CALL TO TEST METHOD >>>
		spyPermissMe.onSuccess();

		verify(spyPermissMe, times(1)).launchDestinationIntent();
	}

	@Test
	public void testOnSuccess_listenerNullAndHasDestinationIntent_callLaunchDestinationIntent() {

		// Fix a mock permission bundle
		mPermissMe.mPermissionsInfoBundle = mock(Bundle.class);

		when(mPermissMe.mPermissionsInfoBundle.getSerializable(eq(PermissMe.DESTINATION_ACTIVITY_CLASS_EXTRA)))
				.thenReturn(mock(Activity.class).getClass());

		final PermissMe spyPermissMe = spy(mPermissMe);

		doNothing().when(spyPermissMe).launchDestinationIntent();

		// <<< EXECUTE CALL TO TEST METHOD >>>
		spyPermissMe.onSuccess();

		verify(spyPermissMe, times(1)).launchDestinationIntent();
	}

	@Test
	public void testLaunchDestinationIntent_whenLaunchIntentNotNullNoExtraParameters_launchIntentWithCorrectParameters() throws Exception {
		// Fix a mock permission bundle
		mPermissMe.mPermissionsInfoBundle = mock(Bundle.class);
		mPermissMe.mLaunchIntent = mock(Intent.class);

		// Mock some responses
		when(mPermissMe.mPermissionsInfoBundle.getBoolean(eq(PermissMe.DESTINATION_FINISH_ACTIVITY_UPON_RESULT_EXTRA)
		)).thenReturn(true);
		final PermissMe spyPermissMe = spy(mPermissMe);
		when(spyPermissMe.getDestinationActivityRequestCode()).thenReturn(1);
		when(spyPermissMe.getEnterAnimation()).thenReturn(1);
		when(spyPermissMe.getExitAnimation()).thenReturn(1);
		when(spyPermissMe.getDestinationActivityLaunchOptionsBundle()).thenReturn(mPermissMe.mPermissionsInfoBundle);

		ArgumentCaptor<Intent> intentArgCap = ArgumentCaptor.forClass(Intent.class);
		ArgumentCaptor<Bundle> bundleArgCap= ArgumentCaptor.forClass(Bundle.class);
		ArgumentCaptor<Integer> animEnterArgCap = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> animExitArgCap = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> requestCodeArgCap = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Boolean> finishCallerActivityArgCap = ArgumentCaptor.forClass(Boolean.class);

		PowerMockito.mockStatic(PermissMe.class);
		PowerMockito.doNothing().when(PermissMe.class, "launchIntentWithParameters",
				intentArgCap.capture(),
				any(AppCompatActivity.class),
				bundleArgCap.capture(),
				any(Fragment.class),
				animEnterArgCap.capture(),
				animExitArgCap.capture(),
				requestCodeArgCap.capture(),
				finishCallerActivityArgCap.capture());

		// << EXECUTE CALL TO METHOD >>
		spyPermissMe.launchDestinationIntent();

		// Make sure all the parameters are correctly set in the launch intent
		assertTrue(mPermissMe.mLaunchIntent == intentArgCap.getValue());
		assertEquals(1, (int)requestCodeArgCap.getValue());
		assertEquals(1, (int)animEnterArgCap.getValue());
		assertEquals(1, (int)animExitArgCap.getValue());
		assertEquals(true, finishCallerActivityArgCap.getValue());
		assertTrue(mPermissMe.mPermissionsInfoBundle == bundleArgCap.getValue());
	}

	private class TestPermissionListener implements PermissMe.PermissionListener {

		@Override
		public void onSuccess() {
			// no-op
		}

		@Override
		public void onRequiredPermissionDenied(String[] deniedPermissions, boolean[] isAutoDenied) {
			// no-op
		}

		@Override
		public void onOptionalPermissionDenied(String[] deniedPermissions, boolean[] isAutoDenied) {
			// no-op
		}
	}

}
