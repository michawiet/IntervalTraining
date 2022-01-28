package eu.mikko.intervaltraining.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.mikko.intervaltraining.repositories.RunRepository
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    runRepository: RunRepository
) : ViewModel() {
    val allRuns = runRepository.getAllRunsSortedByDateDesc()
}