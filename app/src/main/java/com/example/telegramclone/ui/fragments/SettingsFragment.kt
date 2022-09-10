package com.example.telegramclone.ui.fragments

import android.os.Bundle
import android.view.*
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.telegramclone.R
import com.example.telegramclone.activities.RegisterActivity
import com.example.telegramclone.databinding.FragmentSettingsBinding
import com.example.telegramclone.utilits.*

class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            val path = REF_STORAGE_ROOT.child(FOLDER_PROFILE_IMAGE)
                .child(CURRENT_UID)

            uriContent?.let { it ->
                putImageToStorage(it, path) {
                    getUrlFromStorage(path) { url ->
                        putUrlToDatabase(url) {
                            binding.settingsUserPhotoImageView.downloadAndSetImage(url)
                            showToast(getString(R.string.toast_data_update))
                            USER.photoUrl = url
                            APP_ACTIVITY.appDrawer.updateHeader()
                        }
                    }
                }
            }
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
        initFields()
    }

    private fun initFields() {
        binding.settingsBio.text = USER.bio
        binding.settingsFullName.text = USER.full_name
        binding.settingsPhoneNumber.text = USER.phone
        binding.settingsStatus.text = USER.state
        binding.settingsUsername.text = USER.username
        binding.settingsBntChangeUsername.setOnClickListener { replaceFragment(ChangeUsernameFragment()) }
        binding.settingsBtnChangeBio.setOnClickListener { replaceFragment(ChangeBioFragment()) }
        binding.settingsChangePhoto.setOnClickListener { changePhotoUser() }
        binding.settingsUserPhotoImageView.downloadAndSetImage(USER.photoUrl)
    }

    private fun changePhotoUser() {
        startCrop()
    }

    private fun startCrop() {
        cropImage.launch(
            options() {
                setGuidelines(CropImageView.Guidelines.ON)
                setAspectRatio(1, 1)
                setRequestedSize(600, 600)
                setCropShape(CropImageView.CropShape.OVAL)
            }

        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.settings_action_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings_menu_exit -> {
                AppStates.updateState(AppStates.OFFLINE)
                AUTH.signOut()
                APP_ACTIVITY.replaceActivity(RegisterActivity())
            }
            R.id.settings_menu_change_name -> replaceFragment(ChangeNameFragment())
        }
        return true
    }
}
