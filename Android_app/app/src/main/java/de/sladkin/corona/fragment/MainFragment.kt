package de.sladkin.corona.fragment

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import de.sladkin.corona.R
import de.sladkin.corona.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.main_fragment.status_image
import kotlinx.android.synthetic.main.main_fragment.title_tv
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.inject


class MainFragment : Fragment(), KoinComponent {

    companion object {
        const val REQUEST_BLUETOOTH = 1234
        const val PERMISSION_REQUEST_COARSE_LOCATION = 2345
    }

    private val viewModel: MainViewModel by viewModel()
    private val bluetoothManager: BluetoothManager by inject()
    private val bluetoothAdapter = bluetoothManager.adapter
    private val observer = Observer<MainViewModel.ViewState> { render(it) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.liveData.observe(viewLifecycleOwner, observer)

        if (checkBluetoothPermission()) {
            if (checkLocationPermissions()) {
                viewModel.startObserving()
            }
        }
    }

    private fun render(state: MainViewModel.ViewState) {
        when (state) {
            is MainViewModel.ViewState.StatusUpdate -> {
                if (state.isInRisk) {
                    status_image.setImageResource(R.drawable.cry_banana)
                    title_tv.text = context?.getString(R.string.high_risk)
                } else {
                    status_image.setImageResource(R.drawable.smile_banana)
                    title_tv.text = context?.getString(R.string.thank_you)
                }
            }
            is MainViewModel.ViewState.Error -> {
                status_image.setImageResource(R.drawable.cry_banana)
                title_tv.text = context?.getString(R.string.error)
            }
        }
    }

    private fun checkBluetoothPermission(): Boolean {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH)
            return false
        }
        return true
    }

    private fun checkLocationPermissions(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), PERMISSION_REQUEST_COARSE_LOCATION
            )
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BLUETOOTH && resultCode == Activity.RESULT_OK)
            if (checkLocationPermissions()) {
                viewModel.startObserving()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION && (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
        ) {
            viewModel.startObserving()
        }
    }
}