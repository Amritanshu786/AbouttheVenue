package com.amritanshu.aboutthevenue.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amritanshu.aboutthevenue.R
import com.amritanshu.aboutthevenue.adapters.VenueAdapter
import com.amritanshu.aboutthevenue.database.DatabaseHandler
import com.amritanshu.aboutthevenue.models.AboutTheVenueModel
import com.amritanshu.aboutthevenue.utils.SwipeToDeleteCallback
import com.amritanshu.aboutthevenue.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addVenue.setOnClickListener{
            val intent = Intent(this, AddVenueActivity::class.java)
            startActivityForResult(intent, ADD_VENUE_ACTIVITY_REQUEST_CODE)
        }

        getVenueListFromLocalDB()
    }

    private fun setupVenueRecyclerView(venueList : ArrayList<AboutTheVenueModel>)
    {
        rvVenueList.layoutManager = LinearLayoutManager(this)
        rvVenueList.setHasFixedSize(true)

        val venueAdapter = VenueAdapter(this, venueList)
        rvVenueList.adapter = venueAdapter

        venueAdapter.setOnClickListener(object : VenueAdapter.OnClickListener {
            override fun onClick(position: Int, model: AboutTheVenueModel) {
                val intent = Intent(this@MainActivity, VenueDetailsActivity::class.java)
                intent.putExtra(EXTRA_VENUE_DETAILS, model)
                startActivity(intent)
            }
        })
        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvVenueList.adapter as VenueAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_VENUE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rvVenueList)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvVenueList.adapter as VenueAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getVenueListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rvVenueList)
    }

    private fun getVenueListFromLocalDB()
    {
        val dbHandler = DatabaseHandler(this)
        val getVenueList : ArrayList<AboutTheVenueModel> = dbHandler.getVenueList()

        if(getVenueList.size > 0)
        {
            rvVenueList.visibility = View.VISIBLE
            tvNoRecordsAvailable.visibility = View.GONE
            setupVenueRecyclerView(getVenueList)
        }
        else
        {
            rvVenueList.visibility = View.GONE
            tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== ADD_VENUE_ACTIVITY_REQUEST_CODE && resultCode== Activity.RESULT_OK)
        {
            getVenueListFromLocalDB()
        }
    }
    companion object{
        private const val ADD_VENUE_ACTIVITY_REQUEST_CODE = 1
        const val EXTRA_VENUE_DETAILS = "extra_venue_details"
    }
}