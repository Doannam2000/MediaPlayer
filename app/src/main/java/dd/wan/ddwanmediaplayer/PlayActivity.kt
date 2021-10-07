package dd.wan.ddwanmediaplayer

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.SeekBar
import dd.wan.ddwanmediaplayer.model.Podcast
import kotlinx.android.synthetic.main.activity_play.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayActivity : AppCompatActivity() {

    private var type = 0
    private var position = 0
    private var list = ArrayList<Podcast>()
    var mediaPlayer = MediaPlayer()
    var sdf = SimpleDateFormat("mm:ss")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        position = intent.extras?.getInt("position") as Int
        list = loadSong()
        previous.setOnClickListener {
            finish()
        }
        imageView.animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)

        playSong()

        btnPlay.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                btnPlay.setImageResource(R.drawable.ic_outline_play_arrow_24)
            } else {
                mediaPlayer.start()
                btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
            }
        }
        btnBack.setOnClickListener {
            if (type == 2) {
                list.shuffle()
            } else {
                position--
                if (position < 0) {
                    position = list.size - 1
                }
            }
            playSong()
        }
        btnNext.setOnClickListener {
            if (type == 2) {
                list.shuffle()
            } else {
                position++
                if (position == list.size) {
                    position = 0
                }
            }
            playSong()
        }
        btnBackward.setOnClickListener {
            if (mediaPlayer.currentPosition > 15000) {
                seekBar.progress = mediaPlayer.currentPosition - 10000
                mediaPlayer.seekTo(mediaPlayer.currentPosition - 10000)
            }
        }
        btnForward.setOnClickListener {
            if (mediaPlayer.currentPosition + 10000 < mediaPlayer.duration) {
                seekBar.progress = mediaPlayer.currentPosition + 10000
                mediaPlayer.seekTo(mediaPlayer.currentPosition + 10000)
            }
        }
        setting.setOnClickListener { it ->
            var pop = PopupMenu(this, it)
            pop.menuInflater.inflate(R.menu.menu, pop.menu)
            pop.show()
            pop.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.repeatAll -> type = 0
                    R.id.repeat -> type = 1
                    R.id.shuffle -> type = 2
                    R.id.noRepeat -> type = 3
                }
                false
            }
        }
        updateTime()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.progress?.let { mediaPlayer.seekTo(it) }
            }

        })
    }

    @SuppressLint("Range")
    fun loadSong(): ArrayList<Podcast> {
        var list = ArrayList<Podcast>()
        var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var rs = this.contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM_ID
            ),
            MediaStore.Audio.Media.IS_MUSIC + " != 0",
            null,
            null
        )
        if (rs != null) {
            while (rs!!.moveToNext()) {
                var uri = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DATA))
                var title = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.TITLE))
                var artist = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                var media = MediaMetadataRetriever()
                media.setDataSource(uri)
                var bitmap: ByteArray? = media.embeddedPicture
                if (bitmap == null) {
                    bitmap = byteArrayOf()
                }
                list.add(Podcast(uri, title, artist, bitmap))
            }
        }
        return list
    }

    fun playSong() {
        if(list[position].image.isNotEmpty())
        {
            var image = list[position].image
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image!!.size))
        }
        name1.text = list.get(position).title
        name.text = list.get(position).title + "\n\n" + list.get(position).artist
        if (mediaPlayer.isPlaying) {
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(list.get(position).uri)
        mediaPlayer.prepare()
        mediaPlayer.start()
        time2.text = sdf.format(mediaPlayer.duration)
        seekBar.max = mediaPlayer.duration
        btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
    }

    fun updateTime() {
        var handle = Handler()
        handle.postDelayed(object : Runnable {
            override fun run() {
                time1.text = sdf.format(mediaPlayer.currentPosition)
                seekBar.progress = mediaPlayer.currentPosition
                mediaPlayer.setOnCompletionListener {
                    when (type) {
                        0 -> {
                            position++
                            if (position == list.size) {
                                position = 0
                            }
                            playSong()
                        }
                        1 -> {
                            playSong()
                        }
                        2 -> {
                            list.shuffle()
                            playSong()
                        }
                        3 -> {
                            position++
                            if (position == list.size)
                                mediaPlayer.stop()
                            else
                                playSong()
                        }
                    }
                }
                handle.postDelayed(this, 500)
            }
        }, 100)
    }
}