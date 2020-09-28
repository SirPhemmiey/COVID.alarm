package de.sladkin.corona.repository

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.ParcelUuid
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class BleRepository(bluetoothManager: BluetoothManager) {

    companion object {
        const val SERVICE_DATA_KEYWORD = "CoronaAlarm"
        const val DEVICE_SAVE_INTERVAL = 20000L
    }

    private val serviceUUID = ParcelUuid(UUID.fromString("1706BBC0-88AB-4B8D-877E-2237916EE929"))

    private val bluetoothAdapter = bluetoothManager.adapter
    private val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
    private var advertiseCallback: AdvertiseCallback? = null
    private var scanCallback: ScanCallback? = null
    private var listResultsWithTimestamp = ArrayList<Pair<String, Long>>()

    fun startAdvertiser(): Single<String> {
        return Single.create { emitter ->
            if (advertiser == null) {
                emitter.onError(Throwable("Advertiser is null"))
            }
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(false)
                .setTimeout(0)
                .build()
            val data = AdvertiseData.Builder()
                .addServiceData(
                    serviceUUID,
                    SERVICE_DATA_KEYWORD.toByteArray(Charset.forName("UTF-8"))
                )
                .build()

            advertiseCallback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    emitter.onSuccess("Advertiser started")
                }

                override fun onStartFailure(errorCode: Int) {
                    emitter.onError(Throwable("Advertiser init failed $errorCode"))
                }
            }
            advertiser.startAdvertising(settings, data, advertiseCallback)
        }
    }

    fun stopAdvertiser(): String {
        if (advertiser == null) return "Advertiser is null"
        advertiser.stopAdvertising(advertiseCallback)
        return "Advertiser stopped"
    }

    fun startScan(): Observable<Int> {
        val observable = Observable.create<Int> { emitter ->
            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    checkListResult()
                    result?.scanRecord?.serviceData?.values?.map {
                        String(
                            it,
                            Charset.forName("UTF-8")
                        )
                    }?.let {
                        if (it.contains(SERVICE_DATA_KEYWORD) && result.rssi > -70) {
                            addDevice(result.device.toString())
                        }
                    }
                    emitter.onNext(listResultsWithTimestamp.size)
                    Log.i("onx", "scan result $result")
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    emitter.onError(Throwable("Scan failed $errorCode"))
                    Log.i("onx", "scan result $errorCode")
                }
            }
            emitter.onNext(listResultsWithTimestamp.size)
            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        }
        return observable.doOnDispose { stopScan() }
    }

    private fun checkListResult() {
        val listToRemove = ArrayList<Pair<String, Long>>()
        listResultsWithTimestamp.forEach {
            if ((System.currentTimeMillis() - it.second) > DEVICE_SAVE_INTERVAL)
                listToRemove.add(it)
        }
        listResultsWithTimestamp.removeAll(listToRemove)
    }

    private fun addDevice(device: String) {
        listResultsWithTimestamp.remove(listResultsWithTimestamp.find { it.first == device })
        listResultsWithTimestamp.add(Pair(device, System.currentTimeMillis()))
    }

    private fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

}