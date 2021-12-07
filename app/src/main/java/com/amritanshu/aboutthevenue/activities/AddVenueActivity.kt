package com.amritanshu.aboutthevenue.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amritanshu.aboutthevenue.R
import com.amritanshu.aboutthevenue.database.DatabaseHandler
import com.amritanshu.aboutthevenue.models.AboutTheVenueModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_venue.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddVenueActivity : AppCompatActivity(), View.OnClickListener {
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mVenueDetails : AboutTheVenueModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_venue)

        setSupportActionBar(toolbarAddVenue)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarAddVenue.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_VENUE_DETAILS)) {
            mVenueDetails = intent.getParcelableExtra(MainActivity.EXTRA_VENUE_DETAILS)
            //mVenueDetails = intent.getSerializableExtra(MainActivity.EXTRA_VENUE_DETAILS) as AboutTheVenueModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()// Here the calender instance what we have created before will give us the current date which is formatted in the format in function

        if (mVenueDetails != null) {
            supportActionBar?.title = "Edit Venue"

            etTitle.setText(mVenueDetails!!.title)
            etDescription.setText(mVenueDetails!!.description)
            etDate.setText(mVenueDetails!!.date)
            etLocation.setText(mVenueDetails!!.location)
            mLatitude = mVenueDetails!!.latitude
            mLongitude = mVenueDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mVenueDetails!!.image)

            ivPlaceImage.setImageURI(saveImageToInternalStorage)

            btnSave.text = "UPDATE"
        }

        etDate.setOnClickListener(this)
        tvAddImage.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        etLocation.setOnClickListener(this)
    }

    override fun onClick(v: View?)
    {
        when(v!!.id)
        {
            R.id.etDate ->{
                DatePickerDialog(this,dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tvAddImage ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from Camera")
                pictureDialog.setItems(pictureDialogItems){
                    _, which->
                    when(which)
                    {
                        0-> choosePhotoFromGallery()
                        1-> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btnSave->{
                when{
                    etTitle.text.isNullOrEmpty()->{
                        Toast.makeText(this, "Please enter Title", Toast.LENGTH_LONG).show()
                    }
                    etDescription.text.isNullOrEmpty()->{
                        Toast.makeText(this, "Please enter Description", Toast.LENGTH_LONG).show()
                    }
                    //etLocation.text.isNullOrEmpty()->{
                    //    Toast.makeText(this, "Please enter a location", Toast.LENGTH_LONG).show()
                    //}
                    saveImageToInternalStorage == null ->{
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
                    }
                    else->{
                        val venue = AboutTheVenueModel(
                            if(mVenueDetails==null) 0 else mVenueDetails!!.id,
                            etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            etDescription.text.toString(),
                            etDate.text.toString(),
                            etLocation.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        if(mVenueDetails==null)
                        {
                            val addVenue = dbHandler.addVenue(venue)
                            if(addVenue>0)
                            {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                        else
                        {
                            val updateVenue = dbHandler.updateVenue(venue)
                            if(updateVenue>0)
                            {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun choosePhotoFromGallery()
    {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report:MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted())
                {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest> , token: PermissionToken)
            {
                showRationalDialogForPermission()
            }
        }).check()
    }

    private fun takePhotoFromCamera()
    {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report:MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted())
                {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest> , token: PermissionToken)
            {
                showRationalDialogForPermission()
            }
        }).check()

    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this).setMessage("It looks you have turned off the permission required for this feature. It can be enabled under the Applications Settings")
            .setPositiveButton("GO TO SETTINGS"){
                _, _->
                try
                {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                catch (e:ActivityNotFoundException)
                {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){
                dialog, _ -> dialog.dismiss()
            }.show()
    }

    private fun updateDateInView()
    {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        etDate.setText(sdf.format(cal.time).toString())
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK)
        {
            if(requestCode == GALLERY && data!=null)
            {
                val contentURI = data.data
                try {
                    val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                    ivPlaceImage.setImageBitmap(selectedImageBitmap)
                }catch (e:IOException)
                {
                    e.printStackTrace()
                    Toast.makeText(this@AddVenueActivity, "Failed!", Toast.LENGTH_LONG).show()
                }
            }
            else if(requestCode == CAMERA && data!=null)
            {
                val thumbnail:Bitmap = data.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                ivPlaceImage.setImageBitmap(thumbnail)
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream:OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e:IOException)
        {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object
    {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "AboutTheVenueImages"
    }

}