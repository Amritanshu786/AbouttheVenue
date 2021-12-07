package com.amritanshu.aboutthevenue.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amritanshu.aboutthevenue.R
import com.amritanshu.aboutthevenue.models.AboutTheVenueModel
import kotlinx.android.synthetic.main.activity_venue_details.*

class VenueDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_details)

        var venueDetailModel : AboutTheVenueModel? = null

        if(intent.hasExtra(MainActivity.EXTRA_VENUE_DETAILS))
        {
            //venueDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_VENUE_DETAILS) as AboutTheVenueModel
            venueDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_VENUE_DETAILS)
        }

        if(venueDetailModel!=null)
        {
            setSupportActionBar(toolbarVenueDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = venueDetailModel.title

            toolbarVenueDetail.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        ivPlaceImage.setImageURI(Uri.parse(venueDetailModel!!.image))
        tvDescription.text = venueDetailModel.description
        tvLocation.text = venueDetailModel.location
    }
}