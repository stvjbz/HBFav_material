package me.rei_m.hbfavmaterial.models

import me.rei_m.hbfavmaterial.entities.EntryEntity
import me.rei_m.hbfavmaterial.enums.EntryType
import me.rei_m.hbfavmaterial.events.EventBusHolder
import me.rei_m.hbfavmaterial.events.network.HotEntryLoadedEvent
import me.rei_m.hbfavmaterial.events.network.LoadedEventStatus
import me.rei_m.hbfavmaterial.repositories.EntryRepository
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ホットエントリー情報を取得するModel.
 */
@Singleton
class HotEntryModel @Inject constructor(private val entryRepository: EntryRepository) {

    var isBusy = false
        private set

    val entryList = ArrayList<EntryEntity>()

    var entryType = EntryType.ALL
        private set

    fun fetch(entryType: EntryType) {

        if (isBusy) {
            return
        }

        isBusy = true

        val observer = object : Observer<List<EntryEntity>> {
            override fun onNext(t: List<EntryEntity>?) {
                t ?: return
                entryList.clear()
                entryList.addAll(t)
                this@HotEntryModel.entryType = entryType
            }

            override fun onCompleted() {
                isBusy = false
                EventBusHolder.EVENT_BUS.post(HotEntryLoadedEvent(LoadedEventStatus.OK))
            }

            override fun onError(e: Throwable?) {
                isBusy = false
                EventBusHolder.EVENT_BUS.post(HotEntryLoadedEvent(LoadedEventStatus.ERROR))
            }
        }

        entryRepository.findByEntryTypeForHot(entryType)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }
}
