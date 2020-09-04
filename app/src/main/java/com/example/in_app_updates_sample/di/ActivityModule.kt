package com.example.in_app_updates_sample.di

import com.example.in_app_updates_sample.MainActivity
import com.example.in_app_updates_sample.MyRouteActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun myRouteActivity():MyRouteActivity
}
