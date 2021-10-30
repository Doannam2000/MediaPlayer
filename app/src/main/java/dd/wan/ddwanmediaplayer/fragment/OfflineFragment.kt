package dd.wan.ddwanmediaplayer.fragment

import android.Manifest
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dd.wan.ddwanmediaplayer.BuildConfig
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataTransmission
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.MyService
import kotlinx.android.synthetic.main.fragment_offline.*
import kotlinx.android.synthetic.main.fragment_offline.view.*
import kotlin.system.exitProcess

class OfflineFragment : Fragment() {

    var listP = ArrayList<Podcast>()
    lateinit var adapter: RecyclerAdapter
    val handle = Handler()
    val run = Runnable {
        val text = searchView.text
        MyApplication.list.clear()
        for (item in listP) {
            if (item.title.uppercase().contains(text.toString().uppercase())) {
                MyApplication.list.add(item)
            }
        }
        adapter.notifyDataSetChanged()
    }

    lateinit var dataTrans: DataTransmission

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataTrans = context as DataTransmission
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_offline, container, false)
        checkPermission(requireContext())
        view.list_Podcast.layoutManager = LinearLayoutManager(context)
        view.list_Podcast.setHasFixedSize(true)
        adapter = RecyclerAdapter(MyApplication.list)
        adapter.setCallback {
            dataTrans.ChangeData(check1 = true,
                online1 = false,
                activity1 = true,
                currentTime1 = 0,
                position1 = it,
                isFavorite1 = 0,
                Song())
        }
        view.list_Podcast.adapter = adapter

        listP.addAll(MyApplication.list)
        view.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(p0: Editable?) {
                handle.removeCallbacks(run)
                handle.postDelayed(run, 1000)
            }
        })

        return view
    }

    override fun onPause() {
        super.onPause()
        handle.removeCallbacks(run)
    }

    private fun checkPermission(context: Context){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts(
                "package",
                BuildConfig.APPLICATION_ID, null
            )
            intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            Toast.makeText(context,"Hãy cấp quyền cho phép ứng dụng truy cập bộ nhớ !!!",Toast.LENGTH_LONG).show()
            startActivity(intent)
            exitProcess(0)
        }
    }
}