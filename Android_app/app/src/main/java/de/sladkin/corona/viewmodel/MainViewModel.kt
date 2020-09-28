package de.sladkin.corona.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.sladkin.corona.CoronaApp
import de.sladkin.corona.repository.BleRepository
import de.sladkin.corona.repository.DatabaseRepository
import de.sladkin.corona.repository.LocationRepository
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MainViewModel(
    private val bleRepository: BleRepository,
    private val databaseRepository: DatabaseRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    companion object {
        const val LOCATION_UPDATE_COOLDOWN = 300000L // 5 minutes
    }

    sealed class ViewState {
        data class StatusUpdate(
            val isInRisk: Boolean,
            val advertiserStatus: String,
            val devicesFoundNearby: Int
        ) : ViewState()

        object Error : ViewState()
    }

    private val result = MutableLiveData<ViewState>()
    val liveData: LiveData<ViewState>
        get() = result

    private val disposables = CompositeDisposable()
    private var lastLocationUpdateTimestamp = 0L

    fun startObserving() {
        disposables.add(
            bleRepository.startAdvertiser()
                .subscribe({
                    result.value =
                        (result.value as? ViewState.StatusUpdate)?.copy(advertiserStatus = it)
                            ?: ViewState.StatusUpdate(false, it, 0)
                }, {
                    result.value = (result.value as? ViewState.StatusUpdate)?.copy(
                        advertiserStatus = it.message ?: ""
                    )
                        ?: ViewState.StatusUpdate(false, it.message ?: "", 0)
                })
        )

        disposables.add(
            bleRepository.startScan()
                .subscribe({
                    checkDistancingViolation(it)
                }, {
                    result.value = ViewState.Error
                })
        )
    }

    private fun checkDistancingViolation(numberOfDevices: Int) {
        if (numberOfDevices >= CoronaApp.MAXIMUM_ALLOWED_PEOPLE_NEARBY) {
            //high risk
            result.value =
                (result.value as? ViewState.StatusUpdate)?.copy(
                    isInRisk = true,
                    devicesFoundNearby = numberOfDevices
                )
                    ?: ViewState.StatusUpdate(true, "", numberOfDevices)
            if (System.currentTimeMillis() - lastLocationUpdateTimestamp > LOCATION_UPDATE_COOLDOWN) {
                locationRepository.geLocation()?.apply {
                    databaseRepository.writeLocation(this)
                    lastLocationUpdateTimestamp = System.currentTimeMillis()
                }
            }
        } else {
            result.value =
                (result.value as? ViewState.StatusUpdate)?.copy(
                    isInRisk = false,
                    devicesFoundNearby = numberOfDevices
                )
                    ?: ViewState.StatusUpdate(false, "", numberOfDevices)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleRepository.stopAdvertiser()
        disposables.dispose()
    }
}