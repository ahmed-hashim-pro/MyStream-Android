package com.medoapps.www.onlinequran.core

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.medoapps.www.onlinequran.base.TestApplication

class QuranTestRunner : AndroidJUnitRunner() {

  override fun newApplication(
    cl: ClassLoader?,
    className: String?,
    context: Context?
  ): Application {
    return super.newApplication(cl, TestApplication::class.qualifiedName, context)
  }
}
