package de.sladkin.corona

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.sladkin.corona.fragment.MainFragment

class MainActivity : AppCompatActivity() {

    enum class NavDirection {
        MAIN;
    }

    private val bluetoothManager: BluetoothManager? by lazy { getSystemService(Activity.BLUETOOTH_SERVICE) as BluetoothManager }
    val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager?.adapter }

    override fun onCreate(savedInstanceState: Bundle?) {
//        setTheme(R.style.splashScreenTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        navigateTo(NavDirection.MAIN)
    }

    fun navigateTo(direction: NavDirection) {
        val fragment = when (direction) {
            NavDirection.MAIN -> MainFragment()
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .addToBackStack(fragment.tag)
            .commit()
    }
}