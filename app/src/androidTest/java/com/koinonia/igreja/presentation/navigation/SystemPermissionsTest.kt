package com.koinonia.igreja.presentation.navigation

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemPermissionsTest {

    @Test
    fun androidManifest_declaresInternetPermission() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        val requestedPermissions = packageInfo.requestedPermissions ?: emptyArray()
        assertTrue(requestedPermissions.contains("android.permission.INTERNET"))
    }

    @Test
    fun androidManifest_runtimePermissionsCheck_noDangerousPermissionsDeclared() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        val requestedPermissions = packageInfo.requestedPermissions ?: emptyArray()
        
        // Verifica que permissões de risco (Câmera, Localização, Contatos) não estão no AndroidManifest.xml
        assertFalse(requestedPermissions.contains("android.permission.CAMERA"))
        assertFalse(requestedPermissions.contains("android.permission.ACCESS_FINE_LOCATION"))
        assertFalse(requestedPermissions.contains("android.permission.READ_CONTACTS"))
    }
}
