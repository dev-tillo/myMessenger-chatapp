package com.example.mymessenger.keraksiz.utils

import android.app.Application
import lv.chi.photopicker.ChiliPhotoPicker

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ChiliPhotoPicker.init(
            loader = GlideImageLoader(),
            authority = "lv.chi.sample.fileprovider"
        )
    }
}