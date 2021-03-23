package de.rki.coronawarnapp.ui.eventregistration.organizer.create

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import de.rki.coronawarnapp.ui.durationpicker.toReadableDuration
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.category.TraceLocationUIType
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.util.Locale
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class TraceLocationCreateViewModel @AssistedInject constructor(
    @Assisted private val category: TraceLocationCategory
) : CWAViewModel() {

    private val mutableUiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState>
        get() = mutableUiState

    var description: String? by UpdateDelegate()
    var address: String? by UpdateDelegate()
    var checkInLength: Duration? by UpdateDelegate()
    var begin: LocalDateTime? by UpdateDelegate()
    var end: LocalDateTime? by UpdateDelegate()

    init {
        checkInLength = when (category.uiType) {
            TraceLocationUIType.LOCATION -> {
                Duration.standardHours(2)
            }
            TraceLocationUIType.EVENT -> {
                Duration.ZERO
            }
        }
    }

    fun send() {
        // TODO: This will be implemented in another PR
    }

    private fun updateState() {
        mutableUiState.value = UIState(
            begin = begin,
            end = end,
            checkInLength = checkInLength,
            title = category.title,
            isDateVisible = category.uiType == TraceLocationUIType.EVENT,
            isSendEnable = when (category.uiType) {
                TraceLocationUIType.LOCATION -> {
                    description?.trim()?.length in 1..100 &&
                        address?.trim()?.length in 0..100 &&
                        (checkInLength ?: Duration.ZERO) > Duration.ZERO
                }
                TraceLocationUIType.EVENT -> {
                    description?.trim()?.length in 1..100 &&
                        address?.trim()?.length in 0..100 &&
                        begin != null &&
                        end != null &&
                        end?.isAfter(begin) == true
                }
            }
        )
    }

    data class UIState(
        private val begin: LocalDateTime? = null,
        private val end: LocalDateTime? = null,
        private val checkInLength: Duration? = null,
        @StringRes val title: Int,
        val isDateVisible: Boolean,
        val isSendEnable: Boolean
    ) {
        fun getBegin(locale: Locale) = getFormattedTime(begin, locale)

        fun getEnd(locale: Locale) = getFormattedTime(end, locale)

        fun getCheckInLength(resources: Resources): String? {
            return checkInLength?.toReadableDuration(
                suffix = resources.getString(R.string.tracelocation_organizer_duration_suffix)
            )
        }

        private fun getFormattedTime(value: LocalDateTime?, locale: Locale) =
            value?.toString("E, ${locale.shortDatePattern()}   HH:mm", locale)
    }

    private class UpdateDelegate<T> : ReadWriteProperty<TraceLocationCreateViewModel?, T?> {
        var value: T? = null

        override fun setValue(
            thisRef: TraceLocationCreateViewModel?,
            property: KProperty<*>,
            value: T?
        ) {
            if (value != null) {
                this.value = value
            }
            thisRef?.updateState()
        }

        override fun getValue(thisRef: TraceLocationCreateViewModel?, property: KProperty<*>): T? {
            return this.value
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TraceLocationCreateViewModel> {
        fun create(category: TraceLocationCategory): TraceLocationCreateViewModel
    }
}
