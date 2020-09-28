package de.sladkin.corona

import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import com.google.firebase.database.FirebaseDatabase
import de.sladkin.corona.repository.BleRepository
import de.sladkin.corona.repository.DatabaseRepository
import de.sladkin.corona.repository.LocationRepository
import de.sladkin.corona.repository.SharedPrefsUtil
import de.sladkin.corona.viewmodel.MainViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    viewModel { MainViewModel(get(), get(), get()) }

    single { get<Context>().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    single { get<Context>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }

    single { BleRepository(get()) }
    single { DatabaseRepository(FirebaseDatabase.getInstance(), get()) }
    single { SharedPrefsUtil(get()) }
    single { LocationRepository(get()) }
}