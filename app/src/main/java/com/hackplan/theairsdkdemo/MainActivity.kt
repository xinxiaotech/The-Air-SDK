package com.hackplan.theairsdkdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hackplan.theairsdk.UpdateChecker

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val updateChecker = UpdateChecker(this)
    updateChecker.start(false)
  }
}
