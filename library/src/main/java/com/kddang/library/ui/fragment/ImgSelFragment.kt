package com.kddang.library.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.transition.Fade
import android.transition.Scene
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.kddang.library.R
import com.kddang.library.adapter.FolderListAdapter
import com.kddang.library.adapter.ImageListAdapter
import com.kddang.library.adapter.PreviewAdapter
import com.kddang.library.bean.Folder
import com.kddang.library.bean.Image
import com.kddang.library.config.ISListConfig
import com.kddang.library.widget.CustomViewPager
import com.kddang.library.common.Callback
import com.kddang.library.common.Constant
import com.kddang.library.common.OnFolderChangeListener
import com.kddang.library.common.OnItemClickListener
import com.kddang.library.ui.ISListActivity
import com.kddang.library.utils.DisplayUtils
import com.kddang.library.utils.FileUtils
import com.kddang.library.utils.LogUtils
import java.io.File


class ImgSelFragment : Fragment(), View.OnClickListener, ViewPager.OnPageChangeListener {
    private lateinit var rvImageList: RecyclerView
    private lateinit var btnAlbumSelected: Button
    private lateinit var rlBottom: View
    private lateinit var viewPager: CustomViewPager
    private lateinit var config: ISListConfig
    private var callback: Callback? = null
    private val folderList: MutableList<Folder> = ArrayList<Folder>()
    private val imageList: MutableList<Image> = ArrayList<Image>()
    private var folderPopupWindow: ListPopupWindow? = null
    private lateinit var imageListAdapter: ImageListAdapter
    private lateinit var folderListAdapter: FolderListAdapter
    private lateinit var previewAdapter: PreviewAdapter
    private var hasFolderGened = false
    private var tempFile: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_img_sel, container, false)
        rvImageList = view.findViewById(R.id.rvImageList)
        btnAlbumSelected = view.findViewById(R.id.btnAlbumSelected)
        btnAlbumSelected.setOnClickListener(this)
        rlBottom = view.findViewById(R.id.rlBottom)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.offscreenPageLimit = 1
        viewPager.addOnPageChangeListener(this)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        config = (activity as ISListActivity).getConfig()
        callback = activity as ISListActivity
        if (config == null) {
            Log.e("ImgSelFragment", "config 参数不能为空")
            return
        }
        btnAlbumSelected.setText(config.allImagesText)
        rvImageList.layoutManager = GridLayoutManager(rvImageList.context, 3)
        rvImageList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            var spacing: Int = DisplayUtils.dip2px(rvImageList.context, 6f)
            var halfSpacing = spacing shr 1
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.left = halfSpacing
                outRect.right = halfSpacing
                outRect.top = halfSpacing
                outRect.bottom = halfSpacing
            }
        })
        if (config.needCamera) imageList.add(Image())
        imageListAdapter = ImageListAdapter(requireActivity(), imageList, config)
        imageListAdapter.setShowCamera(config.needCamera)
        imageListAdapter.setMutiSelect(config.multiSelect)
        rvImageList.setAdapter(imageListAdapter)
        imageListAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onCheckedClick(position: Int, image: Image): Int {
                return checkedImage(position, image)
            }

            override fun onImageClick(position: Int, image: Image) {
                if (config.needCamera && position == 0) {
                    showCameraAction()
                } else {
                    if (config.multiSelect) {
                        TransitionManager.go(Scene(viewPager), Fade().setDuration(200))
                        viewPager.setAdapter(PreviewAdapter(requireActivity(), imageList, config).also {
                            previewAdapter = it
                        })
                        previewAdapter.setListener(object : OnItemClickListener {
                            override fun onCheckedClick(position: Int, image: Image): Int {
                                return checkedImage(position, image)
                            }

                            override fun onImageClick(position: Int, image: Image) {
                                hidePreview()
                            }
                        })
                        if (config.needCamera) {
                            callback?.apply {
                                onPreviewChanged(position, imageList.size - 1, true)
                            }
                        } else {
                            callback?.apply {
                                onPreviewChanged(position + 1, imageList.size, true)
                            }
                        }
                        viewPager.setCurrentItem(if (config.needCamera) position - 1 else position)
                        viewPager.setVisibility(View.VISIBLE)
                    } else {
                        callback?.apply {
                            onSingleImageSelected(image.path)
                        }
                    }
                }
            }
        })
        folderListAdapter = FolderListAdapter(requireActivity(), folderList, config)
        requireActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback)
    }

    private fun checkedImage(position: Int, image: Image?): Int {
        if (image != null) {
            if (Constant.imageList.contains(image.path)) {
                Constant.imageList.remove(image.path)
                callback?.apply {
                    onImageUnselected(image.path)
                }
            } else {
                if (config.maxNum <= Constant.imageList.size) {
                    Toast.makeText(
                        getActivity(),
                        java.lang.String.format(getString(R.string.maxnum), config.maxNum),
                        Toast.LENGTH_SHORT
                    ).show()
                    return 0
                }
                Constant.imageList.add(image.path)
                callback?.apply {
                    onImageSelected(image.path)
                }
            }
            return 1
        }
        return 0
    }

    private val mLoaderCallback: LoaderManager.LoaderCallbacks<Cursor> =
        object : LoaderManager.LoaderCallbacks<Cursor> {
            private val IMAGE_PROJECTION = arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media._ID
            )

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                if (id == LOADER_ALL) {
                    return CursorLoader(
                        requireActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        null, null, MediaStore.Images.Media.DATE_ADDED + " DESC"
                    )
                } else if (id == LOADER_CATEGORY) {
                    return CursorLoader(
                        requireActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " not like '%.gif%'",
                        null,
                        MediaStore.Images.Media.DATE_ADDED + " DESC"
                    )
                }
                return Loader<Cursor>(requireActivity())
            }

            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
                if (data != null) {
                    val count = data.count
                    if (count > 0) {
                        val tempImageList: MutableList<Image> = ArrayList<Image>()
                        data.moveToFirst()
                        do {
                            val path =
                                data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]))
                            val name =
                                data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]))
                            val image = Image(path, name)
                            tempImageList.add(image)
                            if (!hasFolderGened) {
                                val imageFile = File(path)
                                val folderFile = imageFile.parentFile
                                if (folderFile == null || !imageFile.exists() || imageFile.length() < 10) {
                                    continue
                                }
                                var parent: Folder? = null
                                for (folder in folderList) {
                                    if (TextUtils.equals(folder.path, folderFile.absolutePath)) {
                                        parent = folder
                                    }
                                }
                                if (parent != null) {
                                    parent.images.add(image)
                                } else {
                                    parent = Folder()
                                    parent.name = folderFile.name
                                    parent.path = folderFile.absolutePath
                                    parent.cover = image
                                    val imageList: MutableList<Image> = ArrayList<Image>()
                                    imageList.add(image)
                                    parent.images = imageList
                                    folderList.add(parent)
                                }
                            }
                        } while (data.moveToNext())
                        imageList.clear()
                        if (config.needCamera) imageList.add(Image())
                        imageList.addAll(tempImageList)
                        imageListAdapter.notifyDataSetChanged()
                        folderListAdapter.notifyDataSetChanged()
                        hasFolderGened = true
                    }
                }
            }

            override fun onLoaderReset(loader: Loader<Cursor>) {}

        }

    private fun createPopupFolderList(width: Int, height: Int) {
        folderPopupWindow = ListPopupWindow(requireActivity())
        folderPopupWindow?.apply {
            animationStyle = R.style.PopupAnimBottom
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setAdapter(folderListAdapter)
            setContentWidth(width)
            setWidth(width)
            setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            anchorView = rlBottom
            isModal = true
        }

        folderListAdapter.setOnFloderChangeListener(object : OnFolderChangeListener {
            override fun onChange(position: Int, folder: Folder) {
                folderPopupWindow?.dismiss()
                if (position == 0) {
                    requireActivity().supportLoaderManager
                        .restartLoader(LOADER_ALL, null, mLoaderCallback)
                    btnAlbumSelected.text = config.allImagesText
                } else {
                    imageList.clear()
                    if (config.needCamera) imageList.add(Image())
                    imageList.addAll(folder.images)
                    imageListAdapter.notifyDataSetChanged()
                    btnAlbumSelected.setText(folder.name)
                }
            }
        })
        folderPopupWindow?.setOnDismissListener {
            setBackgroundAlpha(1.0f)
        }
    }

    private fun setBackgroundAlpha(bgAlpha: Float) {
        val lp: WindowManager.LayoutParams = requireActivity().window.attributes
        lp.alpha = bgAlpha
        requireActivity().window.attributes = lp
    }

    override fun onClick(v: View) {
        val wm: WindowManager = requireActivity().windowManager
        val size = wm.defaultDisplay.width / 3 * 2
        if (v.id == btnAlbumSelected!!.id) {
            if (folderPopupWindow == null) {
                createPopupFolderList(size, size)
            }
            folderPopupWindow?.apply {
                if (isShowing) {
                    dismiss()
                } else {
                    show()
                    listView?.let{
                        folderPopupWindow?.apply {
                            listView?.divider = ColorDrawable(ContextCompat.getColor(requireActivity(), R.color.bottom_bg))
                        }
                    }
                    var index: Int = folderListAdapter.selectIndex
                    index = if (index == 0) index else index - 1
                    listView?.setSelection(index)
                    listView?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                listView?.viewTreeObserver?.removeGlobalOnLayoutListener(this)
                            } else {
                                listView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                            }
                            val h: Int = (listView?.measuredHeight) ?: 0
                            if (h > size) {
                                height = size
                                show()
                            }
                        }
                    })
                    setBackgroundAlpha(0.6f)
                }
            }
        }
    }

    private fun showCameraAction() {
        if (config.maxNum <= Constant.imageList.size) {
            Toast.makeText(
                requireActivity(),
                java.lang.String.format(getString(R.string.maxnum), config.maxNum),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            return
        }
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            tempFile = File(
                FileUtils.createRootPath(requireActivity())
                    .toString() + "/" + System.currentTimeMillis() + ".jpg"
            )
            LogUtils.e(tempFile!!.absolutePath)
            FileUtils.createFile(tempFile!!)
            val uri: Uri = FileProvider.getUriForFile(
                requireActivity(),
                FileUtils.getApplicationId(requireActivity()).toString() + ".image_provider",
                tempFile!!
            )
            val resInfoList: List<ResolveInfo> = requireActivity().getPackageManager()
                .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                requireActivity().grantUriPermission(
                    packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri) //Uri.fromFile(tempFile)
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        } else {
            Toast.makeText(
                getActivity(),
                getString(R.string.open_camera_failure),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                tempFile?.let { path ->
                    callback?.apply {
                        onCameraShot(path)
                    }
                }
            } else {
                if (tempFile != null && tempFile!!.exists()) {
                    tempFile!!.delete()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (grantResults.size >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCameraAction()
            } else {
                Toast.makeText(
                    getActivity(),
                    getString(R.string.permission_camera_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        if (config.needCamera) {
            callback?.apply {
                onPreviewChanged(position + 1, imageList.size - 1, true)
            }
        } else {
            callback?.apply {
                onPreviewChanged(position + 1, imageList.size, true)
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}
    fun hidePreview(): Boolean {
        return if (viewPager.visibility === View.VISIBLE) {
            TransitionManager.go(Scene(viewPager), Fade().setDuration(200))
            viewPager.setVisibility(View.GONE)
            callback?.apply {
                onPreviewChanged(0, 0, false)
            }
            imageListAdapter.notifyDataSetChanged()
            true
        } else {
            false
        }
    }

    companion object {
        private const val LOADER_ALL = 0
        private const val LOADER_CATEGORY = 1
        private const val REQUEST_CAMERA = 5
        private const val CAMERA_REQUEST_CODE = 1
        fun instance(): ImgSelFragment {
            val fragment = ImgSelFragment()
            val bundle = Bundle()
            fragment.setArguments(bundle)
            return fragment
        }
    }
}