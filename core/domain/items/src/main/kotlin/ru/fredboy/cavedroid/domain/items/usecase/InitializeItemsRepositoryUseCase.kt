package ru.fredboy.cavedroid.domain.items.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import javax.inject.Inject

@Reusable
class InitializeItemsRepositoryUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
) {

    operator fun invoke() {
        itemsRepository.initialize()
    }

}
