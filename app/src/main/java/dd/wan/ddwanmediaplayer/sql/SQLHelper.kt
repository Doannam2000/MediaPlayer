package dd.wan.ddwanmediaplayer.sql

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import dd.wan.ddwanmediaplayer.model.FavoriteSong
import dd.wan.ddwanmediaplayer.model.offline.Podcast
import java.lang.Exception

class SQLHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        private const val DB_NAME = "FavoriteMusic"
        private const val DB_VERSION = 1
        private const val TB_FAVORITE = "tbl_favoriteMusic"
        private const val ID = "id"
        private const val NAME = "name"
        private const val ARTIST = "artist"
        private const val IMAGE = "image"
        private const val DURATION = "duration"
        private const val GENER = "gener"
        private const val ONLINE = "isOnline"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        var query =
            "Create table $TB_FAVORITE($ID TEXT,$NAME TEXT,$ARTIST TEXT,$IMAGE TEXT,$DURATION TEXT,$GENER TEXT,$ONLINE TEXT)"
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TB_FAVORITE")
        onCreate(db)
    }

    fun insertDB(favoriteSong: FavoriteSong): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, favoriteSong.song.uri)
        contentValues.put(NAME, favoriteSong.song.title)
        contentValues.put(ARTIST, favoriteSong.song.artist)
        contentValues.put(IMAGE, favoriteSong.thumbnail)
        contentValues.put(DURATION, favoriteSong.song.duration)
        contentValues.put(GENER, favoriteSong.song.gener)
        Log.d("hihichecksong",favoriteSong.isOnline.toString())
        contentValues.put(ONLINE, favoriteSong.isOnline.toString())
        val success = db.insert(TB_FAVORITE, null, contentValues)
        db.close()
        return success
    }

    fun deleteDB(id: String): Int {
        val db = this.writableDatabase
        var success = db.delete(TB_FAVORITE, "$ID = ?", arrayOf(id))
        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getAll():ArrayList<FavoriteSong>{
        var list = ArrayList<FavoriteSong>()
        val query = "SELECT * FROM $TB_FAVORITE"
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(query, null)
        } catch (e: Exception) {
            db.execSQL(query)
            return list
        }
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(ID))
                val name = cursor.getString(cursor.getColumnIndex(NAME))
                val artist = cursor.getString(cursor.getColumnIndex(ARTIST))
                val image = cursor.getString(cursor.getColumnIndex(IMAGE))
                val duration = cursor.getString(cursor.getColumnIndex(DURATION)).toInt()
                val gener = cursor.getString(cursor.getColumnIndex(GENER))
                val online = cursor.getString(cursor.getColumnIndex(ONLINE)).toBoolean()
                val favoriteSong = FavoriteSong(Podcast(id,name,artist, byteArrayOf(),duration,gener),image,online)
                Log.d("hihichecksong",favoriteSong.thumbnail)
                Log.d("hihichecksong",favoriteSong.isOnline.toString())
                Log.d("hihichecksong",favoriteSong.song.duration.toString())
                list.add(favoriteSong)
            } while (cursor.moveToNext())
        }
        return list
    }


}