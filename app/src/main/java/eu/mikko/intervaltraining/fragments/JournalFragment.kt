package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.JournalAdapter
import eu.mikko.intervaltraining.viewmodel.JournalViewModel
import kotlinx.android.synthetic.main.fragment_journal.*
import pub.devrel.easypermissions.EasyPermissions
import android.Manifest
import eu.mikko.intervaltraining.other.Constants
import pub.devrel.easypermissions.AppSettingsDialog
import timber.log.Timber

@AndroidEntryPoint
class JournalFragment : Fragment(R.layout.fragment_journal), EasyPermissions.PermissionCallbacks {

    private val viewModel: JournalViewModel by viewModels()

    private lateinit var journalAdapter: JournalAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestReadStoragePermission()

        setupRecyclerView()

        viewModel.allRuns.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isEmpty()) {
                    ivRunEmpty.visibility = View.VISIBLE
                    tvEmptyTitle.visibility = View.VISIBLE
                    tvEmptySubtitle.visibility = View.VISIBLE
                } else {
                    ivRunEmpty.visibility = View.INVISIBLE
                    tvEmptyTitle.visibility = View.INVISIBLE
                    tvEmptySubtitle.visibility = View.INVISIBLE
                }
                journalAdapter.submitList(it)
            }
        }
    }

    private fun setupRecyclerView() = recycler_view.apply {
        journalAdapter = JournalAdapter()
        adapter = journalAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestReadStoragePermission() {
        if(!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.read_storage_permission),
                Constants.REQUEST_CODE_STORAGE_READ,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Timber.d("Permissions granted!")
        journalAdapter.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestReadStoragePermission()
        }
    }
}