package com.adman.shadman.naqashishoo.ui.image_editore

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.adman.shadman.naqashishoo.R
import com.adman.shadman.naqashishoo.ui.image_editore.EmojiBSFragment.EmojiListener
import com.adman.shadman.naqashishoo.ui.image_editore.StickerBSFragment.StickerListener
import com.adman.shadman.naqashishoo.ui.image_editore.TextEditorDialogFragment.TextEditor
import com.adman.shadman.naqashishoo.ui.image_editore.base.BaseActivity
import com.adman.shadman.naqashishoo.ui.image_editore.filters.FilterListener
import com.adman.shadman.naqashishoo.ui.image_editore.filters.FilterViewAdapter
import com.adman.shadman.naqashishoo.ui.image_editore.tools.EditingToolsAdapter
import com.adman.shadman.naqashishoo.ui.image_editore.tools.EditingToolsAdapter.OnItemSelected
import com.adman.shadman.naqashishoo.ui.image_editore.tools.ToolType
import com.adman.shadman.naqashishoo.ui.main_activity.MainActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.PhotoFilter
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import kotlinx.android.synthetic.main.activity_image_editor.*
import java.io.File
import java.io.IOException


class EditImageActivity : AppCompatActivity(), OnPhotoEditorListener, View.OnClickListener,
    PropertiesBSFragment.Properties, EmojiListener,
    StickerListener, OnItemSelected, FilterListener {
    var mPhotoEditor: PhotoEditor? = null
    private var mPhotoEditorView: PhotoEditorView? = null
    private var mPropertiesBSFragment: PropertiesBSFragment? = null
    private var mEmojiBSFragment: EmojiBSFragment? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mTxtCurrentTool: TextView? = null
    private var mRvTools: RecyclerView? = null
    private var mRvFilters: RecyclerView? = null
    private var mFloatingActionButtonClose:FloatingActionButton?=null
    private var mEditingToolsAdapter :EditingToolsAdapter?=null
    private val mFilterViewAdapter = FilterViewAdapter(this)
    private var mRootView: ConstraintLayout? = null
    private val mConstraintSet = ConstraintSet()
    private var mIsFilterVisible = false
    private var mProgressDialog: ProgressDialog? = null

    @VisibleForTesting
    var mSaveImageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_image_editor)
        initViews()
        handleIntentImage(mPhotoEditorView!!.source)
        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mStickerBSFragment!!.setStickerListener(this)
        mEmojiBSFragment!!.setEmojiListener(this)
        mPropertiesBSFragment!!.setPropertiesChangeListener(this)
        val llmTools =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools!!.layoutManager = llmTools
        mRvTools!!.adapter = mEditingToolsAdapter
        val llmFilters =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvFilters!!.layoutManager = llmFilters
        mRvFilters!!.adapter = mFilterViewAdapter


        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");
        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true) // set flag to make text scalable when pinch
            //.setDefaultTextTypeface(mTextRobotoTf)
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk
        mPhotoEditor!!.setOnPhotoEditorListener(this)

        //Set Image Dynamically
        // mPhotoEditorView.getSource().setImageResource(R.drawable.color_palette);
    }

    fun start(context: Context) {
        val starter = Intent(context, EditImageActivity::class.java)
        starter.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(starter)
    }

    private fun handleIntentImage(source: ImageView) {
        val intent = intent
        if (intent != null) {
            val intentType = intent.type
            if (intentType != null && intentType.startsWith("image/")) {
                val imageUri: Uri? = intent.data
                if (imageUri != null) {
                    source.setImageURI(imageUri)
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    private fun initViews() {
        val imgUndo: ImageView
        val imgRedo: ImageView
        val imgCamera: ImageView
        val imgGallery: ImageView
        val imgSave: ImageView
        val imgClose: ImageView
        val imgShare: ImageView
        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRvFilters = findViewById(R.id.rvFilterView)
        mRootView = findViewById(R.id.rootView)
        mFloatingActionButtonClose=findViewById(R.id.back)
        mFloatingActionButtonClose!!.setOnClickListener(this)
        imgUndo = findViewById(R.id.imgUndo)
        imgUndo.setOnClickListener(this)
        imgRedo = findViewById(R.id.imgRedo)
        imgRedo.setOnClickListener(this)
        imgCamera = findViewById(R.id.imgCamera)
        imgCamera.setOnClickListener(this)
        imgGallery = findViewById(R.id.imgGallery)
        imgGallery.setOnClickListener(this)
        imgSave = findViewById(R.id.imgSave)
        imgSave.setOnClickListener(this)
        imgClose = findViewById(R.id.imgClose)
        imgClose.setOnClickListener(this)
        imgShare = findViewById(R.id.imgShare)
        imgShare.setOnClickListener(this)
        mEditingToolsAdapter = EditingToolsAdapter(this,this)
    }

    override fun onEditTextChangeListener(
        rootView: View?,
        text: String?,
        colorCode: Int
    ) {
        val textEditorDialogFragment =
            TextEditorDialogFragment.show(this, text!!, colorCode)
        textEditorDialogFragment.setOnTextEditorListener(object : TextEditor {
            override fun onDone(inputText: String?, colorCode: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
                mPhotoEditor!!.editText(rootView!!, inputText, styleBuilder)
                mTxtCurrentTool!!.setText(R.string.label_text)
            }
        })
    }

    override fun onAddViewListener(
        viewType: ViewType,
        numberOfAddedViews: Int
    ) {
        Log.d(
            TAG,
            "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onRemoveViewListener(
        viewType: ViewType,
        numberOfAddedViews: Int
    ) {
        Log.d(
            TAG,
            "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onStartViewChangeListener(viewType: ViewType) {
        Log.d(
            TAG,
            "onStartViewChangeListener() called with: viewType = [$viewType]"
        )
    }

    override fun onStopViewChangeListener(viewType: ViewType) {
        Log.d(
            TAG,
            "onStopViewChangeListener() called with: viewType = [$viewType]"
        )
    }

    override fun onClick(view: View) {
        when (view.getId()) {
            R.id.imgUndo -> mPhotoEditor!!.undo()
            R.id.imgRedo -> mPhotoEditor!!.redo()
            R.id.imgSave -> saveImage()
            R.id.imgClose -> onBackPressed()
            R.id.back->onBackPressed()
            R.id.imgShare -> shareImage()
            R.id.imgCamera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
            R.id.imgGallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.select_picture)),
                    PICK_REQUEST
                )
            }
        }
    }

    private fun shareImage() {
        if (mSaveImageUri == null) {
            Toast.makeText(this,getString(R.string.msg_save_image_to_share),Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(mSaveImageUri!!))
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)))
    }

    private fun buildFileProviderUri(uri: Uri): Uri {
        return FileProvider.getUriForFile(
            this,
            FILE_PROVIDER_AUTHORITY,
            File(uri.getPath())
        )
    }

    @SuppressLint("MissingPermission")
    private fun saveImage() {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showLoading(getString(R.string.saving_image))
            val sdPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .absolutePath + File.separator + System.currentTimeMillis().toString() + ".png"
            val file = File(
                sdPath,""
            )
            try {
                file.createNewFile()
                val saveSettings = SaveSettings.Builder()
                    .setClearViewsEnabled(true)
                    .setTransparencyEnabled(true)
                    .build()
                mPhotoEditor!!.saveAsFile(
                    file.getAbsolutePath(),
                    saveSettings,
                    object : OnSaveListener {
                        override fun onSuccess(imagePath: String) {
                            hideLoading()
                            showSnackbar(getString(R.string.image_saved_succesfuly))
                            mSaveImageUri = Uri.fromFile(File(imagePath))
                            mPhotoEditorView!!.source.setImageURI(mSaveImageUri)
                        }

                        override fun onFailure(exception: Exception) {
                            hideLoading()
                            showSnackbar(getString(R.string.image_faild_to_save_please_try_agian))
                        }
                    })
            } catch (e: IOException) {
                e.printStackTrace()
                hideLoading()
                showSnackbar(e.message!!)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    mPhotoEditor!!.clearAllViews()
                    val photo = data!!.extras!!["data"] as Bitmap?
                    mPhotoEditorView!!.source.setImageBitmap(photo)
                }
                PICK_REQUEST -> try {
                    mPhotoEditor!!.clearAllViews()
                    val uri: Uri? = data!!.data
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    mPhotoEditorView!!.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor!!.brushColor = colorCode
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor!!.setOpacity(opacity)
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor!!.brushSize = brushSize.toFloat()
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor!!.addEmoji(emojiUnicode)
        mTxtCurrentTool!!.setText(R.string.label_emoji)
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor!!.addImage(bitmap)
        mTxtCurrentTool!!.setText(R.string.label_sticker)
    }


    private fun showSaveDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.msg_save_image))
        builder.setPositiveButton(getString(R.string.save),
            { dialog, which -> saveImage() })
        builder.setNegativeButton(getString(R.string.cancel),
            { dialog, which -> dialog.dismiss() })
        builder.setNeutralButton(getString(R.string.discard),
            { dialog, which -> finish() })
        builder.create().show()
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        mPhotoEditor!!.setFilterEffect(photoFilter)
    }

    override fun onToolSelected(toolType: ToolType?) {
        when (toolType) {
            ToolType.BRUSH -> {
                mPhotoEditor!!.setBrushDrawingMode(true)
                mTxtCurrentTool!!.setText(R.string.label_brush)
                mPropertiesBSFragment!!.show(
                    supportFragmentManager,
                    mPropertiesBSFragment!!.tag
                )
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment =
                    TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object : TextEditor {
                    override fun onDone(
                        inputText: String?,
                        colorCode: Int
                    ) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        mPhotoEditor!!.addText(inputText, styleBuilder)
                        mTxtCurrentTool!!.setText(R.string.label_text)
                    }
                })
            }
            ToolType.ERASER -> {
                mPhotoEditor!!.brushEraser()
                mTxtCurrentTool!!.setText(R.string.label_eraser_mode)
            }
            ToolType.FILTER -> {
                mTxtCurrentTool!!.setText(R.string.label_filter)
                showFilter(true)
            }
            ToolType.EMOJI -> mEmojiBSFragment!!.show(
                supportFragmentManager,
                mEmojiBSFragment!!.tag
            )
            ToolType.STICKER -> mStickerBSFragment!!.show(
                supportFragmentManager,
                mStickerBSFragment!!.tag
            )
        }
    }

    fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(mRootView)
        if (isVisible) {
            mConstraintSet.clear(mRvFilters!!.id, ConstraintSet.START)
            mConstraintSet.connect(
                mRvFilters!!.id, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                mRvFilters!!.id, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                mRvFilters!!.id, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(mRvFilters!!.id, ConstraintSet.END)
        }
        val changeBounds = ChangeBounds()
        changeBounds.setDuration(350)
        changeBounds.setInterpolator(AnticipateOvershootInterpolator(1.0f))
        TransitionManager.beginDelayedTransition(mRootView!!, changeBounds)
        mConstraintSet.applyTo(mRootView)
    }

    override fun onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false)
            mTxtCurrentTool!!.setText(R.string.app_name)
        } else if (!mPhotoEditor!!.isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private val TAG = EditImageActivity::class.java.simpleName
        const val FILE_PROVIDER_AUTHORITY = "com.burhanrashid52.photoeditor.fileprovider"
        private const val CAMERA_REQUEST = 52
        private const val PICK_REQUEST = 53
    }

    private fun showLoading(message: String) {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage(message)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
    }

    private fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    fun showSnackbar(message: String) {
        Snackbar.make(this.rootView, message, Snackbar.LENGTH_SHORT).show()
    }


    fun requestPermission(permission: String): Boolean {
        val isGranted =
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission),
                BaseActivity.READ_WRITE_STORAGE
            )
        }
        return isGranted
    }
}
