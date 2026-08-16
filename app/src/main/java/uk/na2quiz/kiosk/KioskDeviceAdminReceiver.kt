package uk.na2quiz.kiosk

import android.app.admin.DeviceAdminReceiver

/**
 * Récepteur d'administration d'appareil.
 * Sa présence + le provisioning Device Owner (voir provisioning/) permettent
 * d'activer le Lock Task Mode de façon silencieuse et non contournable.
 */
class KioskDeviceAdminReceiver : DeviceAdminReceiver()
