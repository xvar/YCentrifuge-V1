package allgoritm.com.centrifuge.data

import io.reactivex.Single
import javax.inject.Inject

class CentrifugeCredentialsService @Inject constructor(private val reqMan: RequestManager) {

    fun getCentrifugeCredentials() : Single<CentrifugeCredentials> {
        return Single.fromCallable { reqMan.get<CentrifugeCredentials>("c/credentials") }
    }

}