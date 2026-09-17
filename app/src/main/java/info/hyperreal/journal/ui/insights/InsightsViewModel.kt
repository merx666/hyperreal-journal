package info.hyperreal.journal.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class InsightsTimeRange(val label: String, val days: Int?) {
    DAYS_7("7 dni", 7),
    DAYS_30("30 dni", 30),
    DAYS_90("90 dni", 90),
    ALL("Wszystko", null)
}

data class InsightsData(
    val totalIngestions: Int = 0,
    val uniqueSubstances: Int = 0,
    val topSubstance: String? = null,
    val last30DaysCounts: List<DayCount> = emptyList(),
    val substanceBreakdown: List<SubstanceCount> = emptyList(),
    val averageDaysBetween: Float? = null,
    val periodIngestionsCount: Int = 0,
    val selectedTimeRange: InsightsTimeRange = InsightsTimeRange.DAYS_30
)

data class DayCount(
    val dayLabel: String,
    val count: Int
)

data class SubstanceCount(
    val name: String,
    val count: Int
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val ingestionRepository: IngestionRepository,
    private val substanceRepository: SubstanceRepository
) : ViewModel() {

    private val _timeRange = MutableStateFlow(InsightsTimeRange.DAYS_30)
    val timeRange: StateFlow<InsightsTimeRange> = _timeRange.asStateFlow()

    fun setTimeRange(range: InsightsTimeRange) {
        _timeRange.value = range
    }

    val insights: StateFlow<InsightsData> = combine(
        ingestionRepository.getAllIngestions(),
        substanceRepository.getAllSubstances(),
        _timeRange
    ) { ingestions, substances, range ->
        val total = ingestions.size
        val unique = ingestions.distinctBy { it.substanceId }.size

        val now = System.currentTimeMillis()
        val rangeCutoff = if (range.days != null) {
            now - TimeUnit.DAYS.toMillis(range.days.toLong())
        } else {
            0L
        }

        val filteredIngestions = ingestions.filter { it.timestamp >= rangeCutoff }
        val periodCount = filteredIngestions.size

        val counts = filteredIngestions.groupingBy { it.substanceId }.eachCount()
        val topId = counts.maxByOrNull { it.value }?.key
        val topName = substances.find { it.id == topId }?.name ?: topId

        val numDaysToShow = range.days ?: 30
        val cal = Calendar.getInstance()
        val chartDays = (0 until numDaysToShow.coerceAtMost(30)).map { daysAgo ->
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)

            val count = filteredIngestions.count { it.timestamp in dayStart until dayEnd }
            val label = "${cal.get(Calendar.DAY_OF_MONTH)}.${cal.get(Calendar.MONTH) + 1}"
            DayCount(label, count)
        }.reversed()

        val substanceBreakdown = counts.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { (id, count) ->
                val name = substances.find { it.id == id }?.name ?: id
                SubstanceCount(name, count)
            }

        val averageDaysBetween = if (filteredIngestions.size >= 2) {
            val sorted = filteredIngestions.sortedBy { it.timestamp }
            val gaps = sorted.zipWithNext { a, b ->
                TimeUnit.MILLISECONDS.toDays(b.timestamp - a.timestamp).toFloat()
            }.filter { it > 0 }
            if (gaps.isNotEmpty()) gaps.average().toFloat() else null
        } else null

        InsightsData(
            totalIngestions = total,
            uniqueSubstances = unique,
            topSubstance = topName,
            last30DaysCounts = chartDays,
            substanceBreakdown = substanceBreakdown,
            averageDaysBetween = averageDaysBetween,
            periodIngestionsCount = periodCount,
            selectedTimeRange = range
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsData()
    )
}
