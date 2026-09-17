package info.hyperreal.journal.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class InsightsData(
    val totalIngestions: Int = 0,
    val uniqueSubstances: Int = 0,
    val topSubstance: String? = null,
    val last30DaysCounts: List<DayCount> = emptyList(),
    val substanceBreakdown: List<SubstanceCount> = emptyList(),
    val averageDaysBetween: Float? = null,
    val last7DaysCount: Int = 0
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

    val insights: StateFlow<InsightsData> = combine(
        ingestionRepository.getAllIngestions(),
        substanceRepository.getAllSubstances()
    ) { ingestions, substances ->
        val total = ingestions.size
        val unique = ingestions.distinctBy { it.substanceId }.size

        val counts = ingestions.groupingBy { it.substanceId }.eachCount()
        val topId = counts.maxByOrNull { it.value }?.key
        val topName = substances.find { it.id == topId }?.name ?: topId

        // Last 30 days breakdown
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - TimeUnit.DAYS.toMillis(30)
        val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
        
        val recentIngestions = ingestions.filter { it.timestamp >= thirtyDaysAgo }
        val last7DaysCount = ingestions.count { it.timestamp >= sevenDaysAgo }
        
        val cal = Calendar.getInstance()
        val last30DaysCounts = (0 until 30).map { daysAgo ->
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)
            
            val count = recentIngestions.count { it.timestamp in dayStart until dayEnd }
            val label = "${cal.get(Calendar.DAY_OF_MONTH)}.${cal.get(Calendar.MONTH) + 1}"
            DayCount(label, count)
        }.reversed()

        // Substance breakdown
        val substanceBreakdown = counts.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { (id, count) ->
                val name = substances.find { it.id == id }?.name ?: id
                SubstanceCount(name, count)
            }

        // Average days between uses
        val averageDaysBetween = if (ingestions.size >= 2) {
            val sorted = ingestions.sortedBy { it.timestamp }
            val gaps = sorted.zipWithNext { a, b ->
                TimeUnit.MILLISECONDS.toDays(b.timestamp - a.timestamp).toFloat()
            }.filter { it > 0 }
            if (gaps.isNotEmpty()) gaps.average().toFloat() else null
        } else null

        InsightsData(total, unique, topName, last30DaysCounts, substanceBreakdown, averageDaysBetween, last7DaysCount)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsData()
    )
}
