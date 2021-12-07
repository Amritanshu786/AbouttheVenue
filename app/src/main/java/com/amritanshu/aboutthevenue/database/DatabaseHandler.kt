package com.amritanshu.aboutthevenue.database

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.amritanshu.aboutthevenue.models.AboutTheVenueModel

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "AboutTheVenueDatabase"
        private const val TABLE_VENUE = "AboutTheVenueTable"

        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_VENUE_TABLE = ("CREATE TABLE " + TABLE_VENUE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(CREATE_VENUE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_VENUE")
        onCreate(db)
    }

    fun addVenue(aboutTheVenue: AboutTheVenueModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, aboutTheVenue.title)
        contentValues.put(KEY_IMAGE, aboutTheVenue.image)
        contentValues.put(
            KEY_DESCRIPTION,
            aboutTheVenue.description
        )
        contentValues.put(KEY_DATE, aboutTheVenue.date)
        contentValues.put(KEY_LOCATION, aboutTheVenue.location)
        contentValues.put(KEY_LATITUDE, aboutTheVenue.latitude)
        contentValues.put(KEY_LONGITUDE, aboutTheVenue.longitude)

        val result = db.insert(TABLE_VENUE, null, contentValues)

        db.close()
        return result
    }

    fun updateVenue(venue: AboutTheVenueModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, venue.title) // HappyPlaceModelClass TITLE
        contentValues.put(KEY_IMAGE, venue.image) // HappyPlaceModelClass IMAGE
        contentValues.put(
            KEY_DESCRIPTION,
            venue.description
        ) // HappyPlaceModelClass DESCRIPTION
        contentValues.put(KEY_DATE, venue.date) // HappyPlaceModelClass DATE
        contentValues.put(KEY_LOCATION, venue.location) // HappyPlaceModelClass LOCATION
        contentValues.put(KEY_LATITUDE, venue.latitude) // HappyPlaceModelClass LATITUDE
        contentValues.put(KEY_LONGITUDE, venue.longitude) // HappyPlaceModelClass LONGITUDE

        // Updating Row
        val success =
            db.update(TABLE_VENUE, contentValues, KEY_ID + "=" + venue.id, null)
        //2nd argument is String containing nullColumnHack

        db.close() // Closing database connection
        return success
    }

    @SuppressLint("Range")
    fun getVenueList():ArrayList<AboutTheVenueModel>{
        val venueList = ArrayList<AboutTheVenueModel>()
        val selectQuery = "SELECT * FROM $TABLE_VENUE"
        val db = this.readableDatabase
        try {
            val cursor : Cursor = db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst())
            {
                do{
                    val venue = AboutTheVenueModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    venueList.add(venue)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e:SQLiteException)
        {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return venueList
    }

    fun deleteVenue(venue: AboutTheVenueModel):Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_VENUE, KEY_ID + "=" + venue.id, null)
        db.close()
        return success
    }

}
