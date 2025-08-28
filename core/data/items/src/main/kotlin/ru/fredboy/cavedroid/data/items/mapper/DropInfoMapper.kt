package ru.fredboy.cavedroid.data.items.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.items.model.DropAmountDto
import ru.fredboy.cavedroid.data.items.model.DropInfoDto
import ru.fredboy.cavedroid.data.items.repository.ItemsRepositoryImpl
import ru.fredboy.cavedroid.domain.items.model.drop.DropAmount
import ru.fredboy.cavedroid.domain.items.model.drop.DropInfo
import javax.inject.Inject

@Reusable
class DropInfoMapper @Inject constructor() {

    fun mapDropInfo(dropInfoDto: DropInfoDto): DropInfo? {
        val key = dropInfoDto.key.takeUnless { it == ItemsRepositoryImpl.FALLBACK_ITEM_KEY }
            ?: return null
        val requiresTool = dropInfoDto.requiresTool

        val amount = when (dropInfoDto.amount) {
            is DropAmountDto.ExactAmount -> DropAmount.ExactAmount(
                amount = dropInfoDto.amount.amount,
            )

            is DropAmountDto.RandomChance -> DropAmount.RandomChance(
                chance = dropInfoDto.amount.chance,
                amount = dropInfoDto.amount.amount,
            )

            is DropAmountDto.RandomRange -> DropAmount.RandomRange(
                range = dropInfoDto.amount.min..dropInfoDto.amount.max,
                chance = dropInfoDto.amount.chance,
            )
        }

        return DropInfo(
            itemKey = key,
            requiresTool = requiresTool,
            amount = amount,
        )
    }
}
