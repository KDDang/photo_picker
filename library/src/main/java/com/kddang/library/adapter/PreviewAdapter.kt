package com.kddang.library.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.kddang.library.ISNav
import com.kddang.library.R
import com.kddang.library.bean.Image
import com.kddang.library.common.Constant
import com.kddang.library.common.OnItemClickListener
import com.kddang.library.config.ISListConfig


/**
 * @author yuyh.
 * @date 2016/9/28.
 */
class PreviewAdapter(private val activity: Activity, images: List<Image>, config: ISListConfig) : PagerAdapter() {
    private val images: List<Image>
    private val config: ISListConfig
    private var listener: OnItemClickListener? = null

    override fun getCount(): Int {
        return if (config.needCamera) images.size - 1 else images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View {
        val root = View.inflate(activity, R.layout.item_pager_img_sel, null)
        val photoView = root.findViewById<View>(R.id.ivImage) as ImageView
        val ivChecked = root.findViewById<View>(R.id.ivPhotoCheaked) as ImageView
        if (config.multiSelect) {
            ivChecked.visibility = View.VISIBLE
            val image: Image = images[if (config.needCamera) position + 1 else position]
            if (Constant.imageList.contains(image.path)) {
                ivChecked.setImageResource(R.drawable.ic_checked)
            } else {
                ivChecked.setImageResource(R.drawable.ic_uncheck)
            }
            ivChecked.setOnClickListener {
                if (listener != null) {
                    val ret: Int = listener!!.onCheckedClick(position, image)
                    if (ret == 1) { // 局部刷新
                        if (Constant.imageList.contains(image.path)) {
                            ivChecked.setImageResource(R.drawable.ic_checked)
                        } else {
                            ivChecked.setImageResource(R.drawable.ic_uncheck)
                        }
                    }
                }
            }
            photoView.setOnClickListener {
                if (listener != null) {
                    listener?.onImageClick(position, images[position])
                }
            }
        } else {
            ivChecked.visibility = View.GONE
        }
        container.addView(
            root, ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        displayImage(photoView, images[if (config.needCamera) position + 1 else position].path)
        return root
    }

    private fun displayImage(photoView: ImageView, path: String) {
        ISNav.instance.displayImage(activity, path, photoView)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    fun setListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    init {
        this.images = images
        this.config = config
    }
}