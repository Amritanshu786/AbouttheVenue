package com.amritanshu.aboutthevenue.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amritanshu.aboutthevenue.R
import com.amritanshu.aboutthevenue.activities.AddVenueActivity
import com.amritanshu.aboutthevenue.activities.MainActivity
import com.amritanshu.aboutthevenue.database.DatabaseHandler
import com.amritanshu.aboutthevenue.models.AboutTheVenueModel
import kotlinx.android.synthetic.main.item_venue.view.*

open class VenueAdapter(
    private val context: Context,
    private var list:ArrayList<AboutTheVenueModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_venue,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder)
        {
            holder.itemView.ivVenueImage.setImageURI(Uri.parse(model.image))
            holder.itemView.tvTitle.text = model.title
            holder.itemView.tvDescription.text = model.description
        }

        holder.itemView.setOnClickListener{
            if(onClickListener!=null)
            {
                onClickListener!!.onClick(position, model)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    // A function to edit the added Venue detail and pass the existing details through intent.
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddVenueActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_VENUE_DETAILS, list[position])
        activity.startActivityForResult(
            intent,
            requestCode
        ) // Activity is started with requestCode

        notifyItemChanged(position) // Notify any registered observers that the item at position has changed.
    }

    fun removeAt(adapterPosition: Int) {
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteVenue(list[adapterPosition])
        if(isDeleted>0)
        {
            list.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener)
    {
        this.onClickListener = onClickListener
    }


    interface OnClickListener
    {
        fun onClick(position: Int, model: AboutTheVenueModel)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}