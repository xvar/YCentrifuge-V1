package allgoritm.com.centrifuge.data

const val CREDENTIALS = 1
sealed class UiEvent(val id: Int) {
    class GetCredentials: UiEvent(CREDENTIALS)
}