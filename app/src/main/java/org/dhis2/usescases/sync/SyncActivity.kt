package org.dhis2.usescases.sync

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import org.dhis2.Bindings.Bindings
import org.dhis2.Bindings.drawableFrom
import org.dhis2.Bindings.userComponent
import org.dhis2.R
import org.dhis2.databinding.ActivitySynchronizationBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.OnDialogClickListener
import org.dhis2.utils.extension.navigateTo
import org.dhis2.utils.extension.share
import javax.inject.Inject

class SyncActivity : ActivityGlobalAbstract(), SyncView {

    val binding: ActivitySynchronizationBinding by lazy {
        ActivitySynchronizationBinding.inflate(
            layoutInflater
        )
    }

    @Inject
    lateinit var presenter: SyncPresenter

    @Inject
    lateinit var animations: SyncAnimations

    override fun onCreate(savedInstanceState: Bundle?) {
        userComponent()?.plus(SyncModule(this))?.inject(this) ?: finish()
        super.onCreate(savedInstanceState)
        binding.presenter = presenter
        presenter.sync()
    }

    override fun onResume() {
        super.onResume()
        presenter.observeSyncProcess().observe(
            this,
            Observer<List<WorkInfo>> { workInfoList: List<WorkInfo> ->
                presenter.handleSyncInfo(workInfoList)
            }
        )
    }

    override fun setMetadataSyncStarted() {
        Bindings.setDrawableEnd(
            binding.metadataText,
            AppCompatResources.getDrawable(
                this,
                R.drawable.animator_sync
            )
        )
    }

    override fun setMetadataSyncSucceed() {
        binding.metadataText.text = getString(R.string.configuration_ready)
        Bindings.setDrawableEnd(
            binding.metadataText,
            AppCompatResources.getDrawable(
                this,
                R.drawable.animator_done
            )
        )
        presenter.onMetadataSyncSuccess()
    }

    override fun showMetadataFailedMessage(message: String?) {
        showInfoDialog(
            getString(R.string.something_wrong),
            getString(R.string.metada_first_sync_error),
            getString(R.string.go_back),
            getString(R.string.share),
            object : OnDialogClickListener {
                override fun onPositiveClick() {
                    share(message!!)
                }

                override fun onNegativeClick() {
                    presenter.onLogout()
                }
            }
        )
    }

    override fun setDataSyncStarted() {
        binding.eventsText.apply {
            text = getString(R.string.syncing_data)
            Bindings.setDrawableEnd(this, drawableFrom(R.drawable.animator_sync))
            alpha = 1.0f
        }
    }

    override fun setDataSyncSucceed() {
        binding.eventsText.apply {
            text = getString(R.string.data_ready)
            Bindings.setDrawableEnd(this, drawableFrom(R.drawable.animator_done))
        }
        presenter.onDataSyncSuccess()
    }

    override fun onStart() {
        super.onStart()
        animations.startLottieAnimation(binding.lottieView)
    }

    override fun onStop() {
        binding.lottieView.cancelAnimation()
        presenter.onDetach()
        super.onStop()
    }

    override fun setTheme(themeId: Int) {
        animations.startThemeAnimation(this, { super.setTheme(themeId) }) { colorValue ->
            binding.logo.setBackgroundColor(colorValue)
        }
    }

    override fun setFlag(flagName: String?) {
        binding.logoFlag.setImageResource(
            resources.getIdentifier(flagName, "drawable", packageName)
        )
        animations.startFlagAnimation { value: Float? ->
            binding.apply {
                logoFlag.alpha = value!!
                dhisLogo.alpha = 0f
            }
        }
    }

    override fun goToMain() {
        navigateTo<MainActivity>(true, flagsToApply = Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    override fun goToLogin() {
        navigateTo<LoginActivity>(true, flagsToApply = Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}