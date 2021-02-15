package com.example.covidtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val tag = "trackmain"
    lateinit var stateAdapter: StateAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stateLv.apply {
            addHeaderView(LayoutInflater.from(this@MainActivity).inflate(R.layout.item_header,stateLv,false))
        }
        fetchData()

    }
    private fun fetchData(){
        CoroutineScope(Main).launch {
            val response = withContext(IO) {Client.api.execute()}
            if(response.isSuccessful){
                val data = withContext(IO) {Gson().fromJson(response.body?.string(),Response::class.java)}
                bindCombinedData(data.statewise[0])
                bindStateWiseDate(data.statewise.subList(1,data.statewise.size))
            }
        }
    }

    private fun bindStateWiseDate(subList: List<StatewiseItem>) {
        stateAdapter = StateAdapter(subList)
        stateLv.adapter = stateAdapter
    }

    private fun bindCombinedData(data:StatewiseItem) {
        val lastUpdatedTime = data.lastupdatedtime
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        lastUpdatedTv.text = "Last Updated\n ${getTimeAgo(simpleDateFormat.parse(lastUpdatedTime))}"
        confirmedTv.text = data.confirmed
        deceasedTv.text = data.deaths
        activeTv.text = data.active
        recoveredTv.text = data.recovered
    }

    }
    fun getTimeAgo(past: Date): String {
        val now = Date()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)

        return when {
            seconds < 60 -> {
                "Few seconds ago"
            }
            minutes < 60 -> {
                "$minutes minutes ago"
            }
            hours < 24 -> {
                "$hours hour ${minutes % 60} min ago"
            }
            else -> {
                SimpleDateFormat("dd/MM/yy, hh:mm a").format(past).toString()
            }
        }
}