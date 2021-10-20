package dd.wan.ddwanmediaplayer.fragment

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import dd.wan.ddwanmediaplayer.MyApplication
import dd.wan.ddwanmediaplayer.R
import dd.wan.ddwanmediaplayer.`interface`.DataTransmission
import dd.wan.ddwanmediaplayer.activities.PlayActivity
import dd.wan.ddwanmediaplayer.adapter.RecyclerAdapter
import dd.wan.ddwanmediaplayer.config.Constants
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import dd.wan.ddwanmediaplayer.model.top.Song
import dd.wan.ddwanmediaplayer.service.MyService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_offline.view.*

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
        view.list_Podcast.layoutManager = LinearLayoutManager(context)
        view.list_Podcast.setHasFixedSize(true)
        adapter = RecyclerAdapter(MyApplication.list)
        adapter.setCallback {
            val podcast = MyApplication.list[it]
            val bundle1 = Bundle()

            dataTrans.ChangeData(check1 = true,
                online1 = false,
                activity1 = false,
                currentTime1 = 0,
                position1 = it,
                Song())

            bundle1.putString("Uri", podcast.uri)
            bundle1.putInt("action", MyApplication.ACTION_PLAY_SONG)

            val intent11 = Intent(context, MyService::class.java)
            intent11.putExtras(bundle1)
            context?.startService(intent11)!!

            val intent = Intent(context, PlayActivity::class.java)
            intent.putExtras(bundle1)
            val options = ActivityOptions.makeCustomAnimation(context,
                R.anim.right_to_left,
                R.anim.right_to_left_out)
            startActivity(intent,options.toBundle())
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

}