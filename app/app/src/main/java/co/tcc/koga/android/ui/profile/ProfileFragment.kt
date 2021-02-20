package co.tcc.koga.android.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.ProfileFragmentBinding
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.utils.Avatar
import java.io.File
import javax.inject.Inject


class ProfileFragment : Fragment(R.layout.profile_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: ProfileFragmentBinding
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }
    private val readCode: Int = 1000
    private val gallery: Int = 1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProfileFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.apply {
            loadUserAvatar()
            toolbarProfile.inflateMenu(R.menu.settings_menu)
            toolbarProfile.setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
                true
            }
            toolbarProfile.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textViewRole.text = viewModel.getRole()
        binding.textViewCompany.text = "Koga Transportes"
        binding.buttonChangePhoto.setOnClickListener {
            showGallery()
        }

        viewModel.isLoadingUpload.observe(viewLifecycleOwner) {
            loadUserAvatar()
        }

        viewModel.formErrors.observe(viewLifecycleOwner, { errors ->
            clearError()
            for ((key, value) in errors) {
                if (key === "name") {
                    binding.textInputName.error = value
                }

                if (key === "phone") {
                    binding.textInputPhone.error = value
                }

                if (key === "email") {
                    binding.textInputEmail.error = value
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_settings -> {
                findNavController().navigate(
                    R.id.action_profileFragment_to_settingsFragment
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun showGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED && requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), readCode
                )
            } else {
                openGallery()
            }
        } else openGallery()

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            gallery -> {
                if (data !== null && resultCode == Activity.RESULT_OK) {
                    val bytes = readBytes(data.data as Uri)
                    val outputDir = requireContext().cacheDir
                    val outputFile = File.createTempFile("avatar", ".jpeg", outputDir)
                    outputFile.writeBytes(bytes as ByteArray)
                    viewModel.uploadPhoto(outputFile)
                }
            }
        }
    }

   private fun readBytes(uri: Uri): ByteArray? =
        requireContext().contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, gallery)
    }


    private fun loadUserAvatar() {
        val avatar = viewModel.getAvatar()
        Avatar.loadImage(
            requireContext(),
            binding.imageViewProfilePhoto,
            avatar,
            R.drawable.ic_round_person
        )
    }

    private fun clearError() {
        binding.apply {
            textInputName.error = null
            textInputEmail.error = null
            textInputPhone.error = null

        }
    }

}