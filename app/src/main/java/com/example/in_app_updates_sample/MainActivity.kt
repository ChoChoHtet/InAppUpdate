package com.example.in_app_updates_sample

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executor
import javax.inject.Inject

class MainActivity : AppCompatActivity(),InstallStateUpdatedListener {

    companion object {
        val TAG : String = MainActivity::class.java.simpleName
        const val REQUEST_UPDATE_CODE = 1
    }

    lateinit var installStateUpdatedListener: InstallStateUpdatedListener

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    @Inject
    lateinit var playServiceExecutor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)
        btnShow.setOnClickListener {
            startActivity(Intent(this,MyRouteActivity::class.java))
        }

        updateChecker()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == REQUEST_UPDATE_CODE) {
            if (resultCode != RESULT_OK) {
                // If the update is cancelled or fails, you can request to start the update again.
                Log.e(TAG, "Update flow failed! Result code: $resultCode")
            }
        }
    }

    override fun onResume() {

        super.onResume()

//        appUpdateManager.appUpdateInfo.addOnSuccessListener(playServiceExecutor, OnSuccessListener { appUpdateInfo ->
//            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
//                // If the update is downloaded but not installed,
//                // notify the user to complete the update.
//                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED)
//                    updaterDownloadCompleted()
//            } else {
//                // for AppUpdateType.IMMEDIATE only
//                // already executing updater
//                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
////                    appUpdateManager.startUpdateFlowForResult(
////                        appUpdateInfo,
////                        AppUpdateType.IMMEDIATE,
////                        this,
////                        REQUEST_UPDATE_CODE
////                    )
//                    customDialog()
//                    Toast.makeText(this,"updateAvailability = " + appUpdateInfo.updateAvailability(),Toast.LENGTH_LONG).show()
//                }
//                Toast.makeText(this,"updateAvailability = " + appUpdateInfo.updateAvailability(),Toast.LENGTH_LONG).show()
//            }
//        })
    }
    private fun customDialog(){
       AlertDialog.Builder(this)
            .setTitle("New DMS Update is available")
            .setMessage("New Version")
            .setPositiveButton("Update",DialogInterface.OnClickListener { dialog, which ->
                //https://play.google.com/store/apps/details?id=io.haulio.thailand.dms.prod&hl=en
                dialog.dismiss()
                Toast.makeText(this,"Downloading.....",Toast.LENGTH_LONG).show()
                val intent=Intent(Intent.ACTION_VIEW)
                    .apply {
                        setPackage("com.android.vending")
                        data= Uri.parse("https://play.google.com/store/apps/details?id=io.haulio.thailand.dms.prod&hl=en")
                    }
                startActivity(intent)
            })
            .setNegativeButton("Cancel",DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .show()


    }
    private fun updateChecker() {
        //appUpdateManager = AppUpdateManagerFactory.create(this)
        installStateUpdatedListener = InstallStateUpdatedListener { installState ->
            when (installState.installStatus()) {
                InstallStatus.DOWNLOADING ->{
                    val bytesDownloaded = installState.bytesDownloaded()
                    val totalBytesToDownload = installState.totalBytesToDownload()
                }
                InstallStatus.DOWNLOADED -> {
                    Log.d(TAG, "Downloaded")
                    updaterDownloadCompleted()
                }
                InstallStatus.INSTALLED -> {
                    Log.d(TAG, "Installed")
                    appUpdateManager.unregisterListener(installStateUpdatedListener)
                }
                else -> {
                    Log.d(TAG, "installStatus = " + installState.installStatus())
                }
            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener(playServiceExecutor, OnSuccessListener { appUpdateInfo ->
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    Log.d(TAG,"Update Status = " + appUpdateInfo.updateAvailability())
                    //appUpdateInfo.updatePriority()
                    appUpdateInfo.updatePriority()
                    customDialog()
//                    val updateTypes = arrayOf(AppUpdateType.FLEXIBLE, AppUpdateType.IMMEDIATE)
//                    run loop@{
//                        updateTypes.forEach { type ->
//                            if (appUpdateInfo.isUpdateTypeAllowed(type)) {
//                                Toast.makeText(this,"Update Status = " + appUpdateInfo.updateAvailability(),Toast.LENGTH_LONG).show()
//                                customDialog()
//                                //appUpdateManager.startUpdateFlowForResult(appUpdateInfo, type, this, REQUEST_UPDATE_CODE)
//                                return@loop
//                            }
//                        }
//                    }
                }
                else -> {
                    Log.d(TAG, "updateAvailability = " + appUpdateInfo.updateAvailability())
                    Toast.makeText(this,"updateAvailability = " + appUpdateInfo.updateAvailability(),Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun updaterDownloadCompleted() {

        Snackbar.make(
            findViewById(R.id.activity_main_layout),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            show()
        }
        appUpdateManager.completeUpdate()
    }

    override fun onStateUpdate(state: InstallState?) {
        TODO("Not yet implemented")
    }
}
