package allgoritm.com.centrifuge.ui.main

import allgoritm.com.centrifuge.BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import allgoritm.com.centrifuge.R
import allgoritm.com.centrifuge.data.UiEvent
import allgoritm.com.centrifuge.di.ViewModelFactory
import android.widget.Button
import android.widget.TextView
import javax.inject.Inject

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

    lateinit var message : TextView
    lateinit var button: Button
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        message = view.findViewById(R.id.message)
        button = view.findViewById(R.id.button)

        button.setOnClickListener { viewModel.accept(UiEvent.GetCredentials()) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = vmFactory.getForActivity(activity!!)

        addDisposable(
            viewModel.observe().subscribe {
                message.text = it
            }
        )

    }

}
