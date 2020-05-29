package com.adman.shadman.naqashishoo.ui.main_activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.adman.shadman.naqashishoo.R
import com.adman.shadman.naqashishoo.adapter.StyleRecyclerViewAdapter
import com.adman.shadman.naqashishoo.databinding.MainBinding
import com.adman.shadman.naqashishoo.ui.image_editore.EditImageActivity
import com.adman.shadman.naqashishoo.ui.style_transform.camera_fragment.CameraFragment
import com.adman.shadman.naqashishoo.ui.style_transform.details_transform.StyleTransformDetailsBottomSheet
import com.adman.shadman.naqashishoo.utils.ImageUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import java.io.*
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


private const val REQUEST_CODE_PERMISSIONS = 10
private const val READ_STORAGE_PERMISSION_CODE = 20
private const val GALLERY_REQUEST_CODE = 30
private const val WRITE_EXTERNAL_STORAGE = 40
private val REQUIRED_PERMISSIONS =
    arrayOf(Manifest.permission.CAMERA)

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CameraFragment.OnCaptureFinished,
    OnListFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var cameraFragment: CameraFragment
    private lateinit var viewModel: MLExecutionViewModel
    private var selectedImageFromGallery: File? = null
    private var lensFacing = CameraCharacteristics.LENS_FACING_FRONT
    private lateinit var binding: MainBinding
    private var lastSavedFile = ""
    private var selectedStyle: String = ""
    private var isRunningModel = false
    private lateinit var styleTransferModelExecutor: StyleTransferModelExecutor
    private val inferenceThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val mainScope = MainScope()
    private var useGPU = false
    private var styleTransformDetails: String = ""

    fun start(context: Context) {
        val starter = Intent(context, MainActivity::class.java)
        starter.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(starter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)

        makeStatusBarColorTransparent(this)

        setupStylesRecyclerView()

        setupCameraControls()

        initDrawer()

        viewModel = ViewModelProvider(this)
            .get(MLExecutionViewModel::class.java)

        viewModel.styledBitmap.observe(
            this,
            Observer { resultImage ->
                if (resultImage != null) {
                    updateUIWithResults(resultImage)
                }
            }
        )

        binding.activityMain.saveImage.setOnClickListener {
            saveImageSetup()
        }

        mainScope.async(inferenceThread) {
            styleTransferModelExecutor = StyleTransferModelExecutor(this@MainActivity, useGPU)
            Log.d(TAG, "Executor created")
        }

        binding.activityMain.transformDetails.setOnClickListener {
            StyleTransformDetailsBottomSheet.newInstance(styleTransformDetails)
                .show(supportFragmentManager, "")
        }

        binding.activityMain.infoImageButton.setOnClickListener {
            EditImageActivity().start(this)
        }


        lastSavedFile = getLastTakenPicture()
        setImageView(binding.activityMain.resultImageview, lastSavedFile)

        setContentView(binding.root)
    }

    private fun initDrawer(){
        setSupportActionBar(binding.activityMain.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.activityMain.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_sort_black_24dp)
        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun saveImageSetup() { // بررسی دسترسی به حافظه
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
            saveImage()
        }else{
            ActivityCompat.requestPermissions(
                this,
               arrayOf( Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun makeStatusBarColorTransparent(activity: Activity) { // ترنسپرنت کردن استه توس بار
        activity.window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
    }

    private fun setupStylesRecyclerView() { // نمایش لیست تصاویر تبدیل شونده
        listener = this
        val styles = ArrayList<String>()
        styles.addAll(assets.list("thumbnails")!!)
        with(binding.activityMain.stylesRecyclerview) {
            layoutManager =
                LinearLayoutManager(MainActivity(), LinearLayoutManager.HORIZONTAL, false)
            adapter = StyleRecyclerViewAdapter(styles, context, listener)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }


    private fun getLastTakenPicture(): String { // دریافت اخرین تصویر گرفته شده یا بارگذاری شده
        val directory = baseContext.filesDir // externalMediaDirs.first()
        val files =
            directory.listFiles()?.filter { file -> file.absolutePath.endsWith(".jpg") }?.sorted()
        if (files == null || files.isEmpty()) {
            Log.d(TAG, "there is no previous saved file")
            return ""
        }

        val file = files.last()
        Log.d(TAG, "lastsavedfile: " + file.absolutePath)
        return file.absolutePath
    }


    private fun addCameraFragment() { // جاگذاری فرگمنت دوربین
        cameraFragment = CameraFragment.newInstance()
        cameraFragment.setFacingCamera(lensFacing)
        supportFragmentManager.popBackStack()
        supportFragmentManager.beginTransaction()
            .replace(R.id.view_finder, cameraFragment)
            .commit()
        binding.activityMain.layoutCamera.visibility = View.VISIBLE
    }

    private fun setupCameraControls() {
        // تعیین دکمه گرفتن تصویر با دوربین
        binding.activityMain.captureButton.setOnClickListener {
            it.clearAnimation()
            cameraFragment.takePicture()
        }

        // چرخش دوربین
        binding.activityMain.toggleButton.setOnClickListener {
            lensFacing = if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                CameraCharacteristics.LENS_FACING_FRONT
            } else {
                CameraCharacteristics.LENS_FACING_BACK
            }
            cameraFragment.setFacingCamera(lensFacing)
            addCameraFragment()
        }
    }

    /*
    جاگذاری تصویر کاربر با استفاده از دریافت ادرس مسیر تصویر
     */
    private fun setImageView(imageView: ImageView, imagePath: String) {
        Glide.with(baseContext)
            .asBitmap()
            .load(imagePath)
            .override(512, 512)
            .apply(RequestOptions().transform(CropTop()))
            .into(imageView)
    }

    /*
    جاگذاری تصویر با استفاده از تبدیل تصویر به بیتمپ
     */
    private fun setImageView(imageView: ImageView, image: Bitmap) {
        Glide.with(baseContext)
            .load(image)
            .override(512, 512)
            .fitCenter()
            .into(imageView)
    }


    override fun onCaptureFinished(file: File) {
        val msg = "Photo capture succeeded: ${file.absolutePath}"
        Log.d(TAG, msg)

        lastSavedFile = file.absolutePath
        setImageView(binding.activityMain.resultImageview, lastSavedFile)
        supportFragmentManager.beginTransaction()
            .remove(cameraFragment)
            .commit()
        binding.activityMain.layoutCamera.visibility = View.GONE

    }

    /*
    کار این تابع تبدیل تصویر گرفته شده یا بارگذاری شده به یک تصویر مربعی است.
    از انجایی که مدل ها برای تبدیل تصویر به نقاشی روی این بخش کار میکنند.
     */
    class CropTop : BitmapTransformation() {
        override fun transform(
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int
        ): Bitmap {
            return if (toTransform.width == outWidth && toTransform.height == outHeight) {
                toTransform
            } else ImageUtils.scaleBitmapAndKeepRatio(toTransform, outWidth, outHeight)
        }

        override fun equals(other: Any?): Boolean {
            return other is CropTop
        }

        override fun hashCode(): Int {
            return ID.hashCode()
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(ID_BYTES)
        }

        companion object {
            private const val ID = "org.tensorflow.lite.examples.styletransfer.CropTop"
            private val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))
        }
    }

    // دریافت تگ مدلی که کاربر آن را لمس کرده
    override fun onListFragmentInteraction(item: String) {
        selectedStyle = item
        if (selectedStyle == "style1.jpg") {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
                addCameraFragment()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }
        } else if (selectedStyle == "style0.jpg") {
            showFileChooser()
        } else
            startRunningModel()
    }

    // شروع فرآیند تبدیل تصویر
    private fun startRunningModel() {
        if (!isRunningModel && lastSavedFile.isNotEmpty() && selectedStyle.isNotEmpty()) {
            binding.activityMain.resultImageview.visibility = View.INVISIBLE
            binding.activityMain.progressBar.visibility = View.VISIBLE
            viewModel.onApplyStyle(
                baseContext, lastSavedFile, selectedStyle, styleTransferModelExecutor,
                inferenceThread
            )
        } else {
            Toast.makeText(this, "Previous Model still running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIWithResults(modelExecutionResult: ModelExecutionResult) {
        binding.activityMain.progressBar.visibility = View.INVISIBLE
        binding.activityMain.resultImageview.visibility = View.VISIBLE
        setImageView(binding.activityMain.resultImageview, modelExecutionResult.styledImage)
        styleTransformDetails = modelExecutionResult.executionLog
        binding.activityMain.saveImage.show()
        binding.activityMain.transformDetails.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
                    addCameraFragment()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.camera_permission_rejected),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            READ_STORAGE_PERMISSION_CODE -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(intent, "انتخاب تصویر"),
                        GALLERY_REQUEST_CODE
                    )
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.read_storage_permission_rejected),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            WRITE_EXTERNAL_STORAGE->{
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                    saveImage()
                }else{
                    Toast.makeText(
                        this,
                        getString(R.string.read_storage_permission_rejected),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                getImageFile(data?.data!!)
                if (selectedImageFromGallery != null) {
                    setImageView(binding.activityMain.resultImageview, selectedImageFromGallery!!.absolutePath)
                    lastSavedFile = selectedImageFromGallery!!.absolutePath
                }
            }
        }
    }


    // انتخاب تصویر از گالری
    private fun showFileChooser() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_CODE
            )
        } else {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "انتخاب تصویر"),
                GALLERY_REQUEST_CODE
            )
        }

    }


    /*
  تبدیل تصویر انتخاب شده از گالری به بیت مپ
   */
    private fun getImageFile(data: Uri) {
        val options = BitmapFactory.Options()
        val REQUIRED_SIZE = 100
        var scale = 2
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
            options.outHeight / scale / 2 >= REQUIRED_SIZE
        ) {
            scale *= 2
        }
        options.inSampleSize = scale
        val imageUri = data
        var imageStream: InputStream? = null
        try {
            imageStream = contentResolver.openInputStream(imageUri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val selectedImage = BitmapFactory.decodeStream(imageStream, null, options)
        persistImage(selectedImage!!)
    }

    /*
    تغییر سایز و تغییر نام تصویر انتخاب شده
     */
    private fun persistImage(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val filesDir = filesDir
        selectedImageFromGallery = File(filesDir, "image" + timeStamp + ".jpg")
        val os: OutputStream
        try {
            os = FileOutputStream(selectedImageFromGallery)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
        }
    }

    override fun onBackPressed() {

        val backStackEntryCount = supportFragmentManager.backStackEntryCount

        if (backStackEntryCount == 0) {
            supportFragmentManager.beginTransaction()
                .remove(cameraFragment)
                .commit()
            binding.activityMain.layoutCamera.visibility=View.GONE
        }
        else
            super.onBackPressed()

    }


    private fun getDownloadDirPath(): String { // دریافت مسیر پوشه دانلود
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            var storageDir: String = ""
            try {
                val sdPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .absolutePath
                val file = File(sdPath, "")
                if (!file.exists()) {
                    file.mkdirs();
                    storageDir = file.absolutePath
                } else if (file.exists()) {
                    storageDir = file.absolutePath
                }
            } catch (e: Exception) {

            }
            Log.e("Mounted Path ", storageDir)
            return storageDir
        } else {
            val storageDir = filesDir.absolutePath
            Log.e("Not Mounted Path ", storageDir)
            return storageDir
        }
    }


    /*
    ذخیره تصویر
     */
    fun saveImage() {
        var success =false
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val fname = "image$n.png"
        val image = File(getDownloadDirPath(), fname)
        binding.activityMain.resultImageview.setDrawingCacheEnabled(true)
        val bitmap: Bitmap = binding.activityMain.resultImageview.getDrawingCache()
        // Encode the file as a PNG image.
        val outStream: FileOutputStream
        try {
            outStream = FileOutputStream(image)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            /* 100 to keep full quality of the image */outStream.flush()
            outStream.close()
            success = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (success) {
            Toast.makeText(applicationContext, R.string.image_saved_successfuly, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, R.string.error_in_saving_image, Toast.LENGTH_LONG)
                .show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(image)
            scanIntent.data = contentUri
            sendBroadcast(scanIntent)
        } else {
            sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://mnt/sdcard/" + Environment.getExternalStorageDirectory())
                )
            )
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}
