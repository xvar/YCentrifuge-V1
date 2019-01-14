package allgoritm.com.centrifuge.ui.main

import allgoritm.com.centrifuge.BaseViewModel
import allgoritm.com.centrifuge.data.CentrifugeCredentialsService
import allgoritm.com.centrifuge.data.UiEvent
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val service: CentrifugeCredentialsService
) : BaseViewModel(), Consumer<UiEvent> {

    private val strProcessor = BehaviorProcessor.create<String>()

    fun observe() : Flowable<String> {
        return strProcessor
    }

    override fun accept(e: UiEvent) {
        when (e) {
            is UiEvent.GetCredentials -> {
                addDisposable(service.getCentrifugeCredentials()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                            c, exc ->
                        if (exc != null) {
                            strProcessor.onNext(exc.toString())
                        } else {
                            strProcessor.onNext(c.toString())
                        }
                    })
            }
        }
    }

}
