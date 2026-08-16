package uk.na2quiz.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Relance automatiquement le kiosque au démarrage de la tablette,
 * pour qu'une coupure de courant ne « libère » jamais l'appareil.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val i = Intent(context, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}
