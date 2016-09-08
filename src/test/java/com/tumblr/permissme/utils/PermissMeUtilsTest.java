package com.tumblr.permissme.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.PermissionChecker;
import com.tumblr.permissme.utils.PermissMeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;

/**
 * Tests for testing functionality of {@link PermissMeUtils} methods
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PermissionChecker.class)
@SuppressWarnings("ConstantConditions")
public class PermissMeUtilsTest {

	private final Context mockContext;

	public PermissMeUtilsTest() {
		mockContext = Mockito.mock(Context.class);
	}

	@Before
	public void init() {
		PowerMockito.mockStatic(PermissionChecker.class);
	}

	@Test
	public void testVerifyPermissions_whenEmptyListOfPermissions_returnFalse() {
		// Case: Empty result list
		final int[] results = new int[] {};
		final boolean actualResult = PermissMeUtils.verifyPermissions(results);
		assertFalse(actualResult);
	}

	@Test
	public void testVerifyPermissions_whenJustGrantedPermissions_returnTrue() {
		// Case: Results with just granted
		final int[] results = new int[] {
				PackageManager.PERMISSION_GRANTED,
				PackageManager.PERMISSION_GRANTED
		};
		final boolean actualResult = PermissMeUtils.verifyPermissions(results);
		assertTrue(actualResult);
	}

	@Test
	public void testVerifyPermissions_whenJustDeniedPermissions_returnFalse() {
		// Case: Results with just denied
		final int[] results = new int[] { PackageManager.PERMISSION_DENIED };
		final boolean actualResult = PermissMeUtils.verifyPermissions(results);
		assertFalse(actualResult);
	}

	@Test
	public void testVerifyPermissions_whenMixOfGrantedAndDeniedPermissions_returnFalse() {
		// Case: Results with mixed granted and denied permissions
		final int[] results = {
				PackageManager.PERMISSION_GRANTED,
				PackageManager.PERMISSION_GRANTED,
				PackageManager.PERMISSION_DENIED,
				PackageManager.PERMISSION_GRANTED,
				PackageManager.PERMISSION_DENIED
		};
		final boolean actualResult = PermissMeUtils.verifyPermissions(results);
		assertFalse(actualResult);
	}

	@Test
	public void testGetDeniedPermissions_whenNullStringValues_returnEmptyArray() {
		// Case 1: Null permission string values
		final String[] deniedPermissions = PermissMeUtils.getDeniedPermissions(
				mockContext,
				null, null, null
		);
		assertNotNull(deniedPermissions);
		assertTrue(deniedPermissions.length == 0);
	}

	@Test
	public void testGetDeniedPermissions_whenNullVarArgValue_returnEmptyArray() {
		// Case 2: Null varargs String[]
		final String[] deniedPermissions = PermissMeUtils.getDeniedPermissions(
				mockContext,
				(String[]) null
		);
		assertNotNull(deniedPermissions);
		assertTrue(deniedPermissions.length == 0);
	}

	@Test
	public void testGetDeniedPermissions_whenOnePermissionsUngranted_returnThatOnePermission() {
		// Case 1: Ungranted permissions
		BDDMockito.given(PermissionChecker.checkSelfPermission(any(Context.class), anyString())).willReturn
				(PackageManager.PERMISSION_DENIED);

		final String[] deniedPermissions = PermissMeUtils.getDeniedPermissions(
				mockContext,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
		);
		assertNotNull(deniedPermissions);
		assertTrue(deniedPermissions.length > 0);
		assertTrue(deniedPermissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE));
	}

	@Test
	public void testGetDeniedPermissions_whenAllPermissionsGranted_returnEmptyArray() {
		// Case 2: Granted permissions
		BDDMockito.given(PermissionChecker.checkSelfPermission(any(Context.class), anyString()))
				.willReturn(PackageManager.PERMISSION_GRANTED);

		final String[] deniedPermissions = PermissMeUtils.getDeniedPermissions(
				mockContext,
				Manifest.permission.INTERNET
		);
		assertNotNull(deniedPermissions);
		assertTrue(deniedPermissions.length == 0);
	}

	@Test
	public void testGetDeniedPermissions_whenMixPermissionsGrantedAndUngranted_returnUngrantedPermissions() {
		// Case 3: 2 permissions, 1 ungranted, 1 granted
		BDDMockito.given(
				PermissionChecker.checkSelfPermission(
						any(Context.class),
						eq(Manifest.permission.WRITE_EXTERNAL_STORAGE)
				)
		).willReturn(PackageManager.PERMISSION_DENIED);

		BDDMockito.given(
				PermissionChecker.checkSelfPermission(any(Context.class), eq(Manifest.permission.READ_SMS))
		).willReturn(PackageManager.PERMISSION_GRANTED);

		final String[] deniedPermissions = PermissMeUtils.getDeniedPermissions(
				mockContext,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_SMS
		);
		assertNotNull(deniedPermissions);
		assertTrue(deniedPermissions.length == 1);
		assertTrue(deniedPermissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE));
	}

	@Test
	public void testGetDeniedPermissions_whenAllPermissionsUngranted_returnAllPermissions() {
		// Case 4: 2 permissions, 2 ungranted
		BDDMockito.given(
				PermissionChecker.checkSelfPermission(any(Context.class), anyString())
		).willReturn(PackageManager.PERMISSION_DENIED);

		String[] deniedPermissions = PermissMeUtils.getDeniedPermissions(
				mockContext,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_SMS
		);
		assertNotNull(deniedPermissions);
		assertTrue(deniedPermissions.length == 2);
		assertTrue(deniedPermissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE));
		assertTrue(deniedPermissions[1].equals(Manifest.permission.READ_SMS));
	}

	@Test
	public void testPermissionIsInvalidOrHasPermission_whenInvalidNullPermission_returnTrue() {
		// Case 1: null permission
		boolean result = PermissMeUtils.permissionIsInvalidOrHasPermission(mockContext, null);
		assertTrue(result);
	}
		@Test
		public void testPermissionIsInvalidOrHasPermission_whenInvalidEmptyPermission_returnTrue() {
			// Case 2: empty permission string
			final boolean result = PermissMeUtils.permissionIsInvalidOrHasPermission(mockContext, "");
			assertTrue(result);
		}
	@Test
	public void testPermissionIsInvalidOrHasPermission_whenPermissionValidAndUngranted_returnFalse() {
		final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
		// Case 3: Ungranted permission
		BDDMockito.given(
				PermissionChecker.checkSelfPermission(any(Context.class), eq(permission))
		).willReturn(PackageManager.PERMISSION_DENIED);
		final boolean result = PermissMeUtils.permissionIsInvalidOrHasPermission(mockContext, permission);
		assertFalse(result);
	}
	@Test
	public void testPermissionIsInvalidOrHasPermission_whenPermissionValidAndGranted_returnTrue() {
		final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
		// Case 4: Granted permission
		BDDMockito.given(
				PermissionChecker.checkSelfPermission(any(Context.class), eq(permission))
		).willReturn(PackageManager.PERMISSION_GRANTED);
		final boolean result = PermissMeUtils.permissionIsInvalidOrHasPermission(mockContext, permission);
		assertTrue(result);
	}

	@Test
	public void testNeedToRequestPermission_whenUserDeviceBelowMarshmallow_returnFalse() throws Exception {
		final int minBuildLevelWithRPApplicable = Build.VERSION_CODES.M;
		final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

		for (int apiLevel = 0; apiLevel < minBuildLevelWithRPApplicable; apiLevel++) {
			setEnvBuildVersion(apiLevel);
			assertFalse(PermissMeUtils.needToRequestPermission(mockContext, permission));
		}
	}

	@Test
	public void testNeedToRequestPermission_whenDeviceMarshmallowAndUngrantedPermissions_returnTrue() throws Exception {
		setEnvBuildVersion(Build.VERSION_CODES.M);

		final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

		// Case 1: Ungranted permission
		BDDMockito.given(
				PermissionChecker.checkSelfPermission(any(Context.class), eq(permission))
		).willReturn(PackageManager.PERMISSION_DENIED);

		assertTrue(PermissMeUtils.needToRequestPermission(mockContext, permission));
	}

	@Test
	public void testNeedToRequestPermission_whenDeviceMarshmallowAndGrantedPermissions_returnFalse() throws Exception {
		setEnvBuildVersion(Build.VERSION_CODES.M);
		final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

		// Case 2: Granted permission
		BDDMockito.given(
				PermissionChecker.checkSelfPermission(any(Context.class), eq(permission))
		).willReturn(PackageManager.PERMISSION_GRANTED);
		assertFalse(PermissMeUtils.needToRequestPermission(mockContext, permission));
	}

	private void setEnvBuildVersion(final int version) throws Exception {
		final Field sdkIntField = Build.VERSION.class.getDeclaredField("SDK_INT");
		final Field modifiersField = Field.class.getDeclaredField("modifiers");

		sdkIntField.setAccessible(true);
		modifiersField.setAccessible(true);
		modifiersField.setInt(sdkIntField, sdkIntField.getModifiers() & ~Modifier.FINAL);

		sdkIntField.set(null, version);
		modifiersField.setInt(sdkIntField, sdkIntField.getModifiers() | Modifier.FINAL);
		sdkIntField.setAccessible(false);
	}
}
