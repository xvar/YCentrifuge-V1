package allgoritm.com.centrifuge.ui.main

import allgoritm.com.centrifuge.BaseFragment
import allgoritm.com.centrifuge.R
import allgoritm.com.centrifuge.data.UiEvent
import allgoritm.com.centrifuge.di.ViewModelFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.main_fragment.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MainFragment : BaseFragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var vmFactory: ViewModelFactory<MainViewModel>
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    private lateinit var adapter: LogAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LogAdapter(activity!!, ArrayList())
        logRv.adapter = adapter
        logRv.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        connect.setOnClickListener {
            adapter.logs.clear()
            adapter.notifyDataSetChanged()
            viewModel.accept(UiEvent.CredentialsAndConnect())
        }
        history.setOnClickListener { viewModel.accept(UiEvent.History()) }
        subscribe.setOnClickListener { viewModel.accept(UiEvent.Subscribe()) }
        unsubscribe.setOnClickListener { viewModel.accept(UiEvent.Unsubscribe()) }
        disconnect.setOnClickListener { viewModel.accept(UiEvent.Disconnect()) }
        presence.setOnClickListener { viewModel.accept(UiEvent.Presence()) }
        publish.setOnClickListener { viewModel.accept(UiEvent.Publish(data = UUID.randomUUID().toString())) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = vmFactory.getForActivity(activity!!)

        addDisposable("ui",
            viewModel.observe().subscribe {
                Log.d("client_fr", "$it")
                adapter.logs.clear()
                adapter.logs.addAll(it)
                adapter.notifyDataSetChanged()
                logRv.scrollToPosition(it.size - 1)
            }
        )

    }

}
