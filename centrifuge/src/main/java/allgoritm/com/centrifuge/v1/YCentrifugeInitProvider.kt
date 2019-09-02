package allgoritm.com.centrifuge.v1

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri


internal class YCentrifugeInitProvider : ContentProvider() {

    /** Should match the [YCentrifugeInitProvider] authority if $androidId is empty.  */
    private val emptyApplicationProvideAuthority = "allgoritm.com.centrifuge.v1.ycentrifugeinitprovider"

    override fun attachInfo(context: Context, info: ProviderInfo) {
        // super.attachInfo calls onCreate. Fail as early as possible.
        checkContentProviderAuthority(info)
        super.attachInfo(context, info)
    }

    override fun onCreate(): Boolean {
        val ctx = context!!
        Initializer.init(ctx)
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }
    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }

    private fun checkContentProviderAuthority(info: ProviderInfo) {
        if (emptyApplicationProvideAuthority == info.authority) {
            throw IllegalStateException(
                "Incorrect provider authority in manifest. Most likely due to a missing " + "applicationId variable in application's build.gradle."
            )
        }
    }

}