package allgoritm.com.centrifuge.ui.main

import allgoritm.com.centrifuge.BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import allgoritm.com.centrifuge.R
import allgoritm.com.centrifuge.di.ViewModelFactory
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = vmFactory.getForActivity(activity!!)

        viewModel.observe()

    }

}
