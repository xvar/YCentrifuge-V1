package allgoritm.com.centrifuge.di

import allgoritm.com.centrifuge.ui.main.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeMainFragment(): MainFragment

}
