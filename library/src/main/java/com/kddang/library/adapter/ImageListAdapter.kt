package com.kddang.library.adapter

import android.content.Context
import android.widget.ImageView
import com.kddang.library.recycleview.EasyRVAdapter
import com.kddang.library.recycleview.EasyRVHolder
import com.kddang.library.ISNav
import com.kddang.library.R
import com.kddang.library.bean.Image
import com.kddang.library.common.Constant
import com.kddang.library.common.OnItemClickListener
import com.kddang.library.config.ISListConfig


/**
 * @author yuyh.
 * @date 2016/8/5.
 */
class ImageListAdapter(private val context: Context, list: MutableList<Image>, config: ISListConfig) :
    EasyRVAdapter<Image>(context, list, R.layout.item_img_sel, R.layout.item_img_sel_take_photo) {
    private var showCamera = false
    private var mutiSelect = false
    private val config: ISListConfig
    private var listener: OnItemClickListener? = null

    override fun onBindData(viewHolder: EasyRVHolder, position: Int, item: Image) {
        if (position == 0 && showCamera) {
            val iv: ImageView = viewHolder.getView(R.id.ivTakePhoto)
            iv.setImageResource(R.drawable.ic_take_photo)
            iv.setOnClickListener {
                listener?.onImageClick(position, item)
            }
            return
        }
        if (mutiSelect) {
            val ivPhotoCheaked = viewHolder.getView<ImageView>(R.id.ivPhotoCheaked)
            ivPhotoCheaked.setOnClickListener {
                listener?.apply {
                    val ret: Int = onCheckedClick(position, item)
                    if (ret == 1) { // 局部刷新
                        if (Constant.imageList.contains(item.path)) {
                            viewHolder.setImageResource(R.id.ivPhotoCheaked, R.drawable.ic_checked)
                        } else {
                            viewHolder.setImageResource(R.id.ivPhotoCheaked, R.drawable.ic_uncheck)
                        }
                    }
                }
            }
        }
        viewHolder.setOnItemViewClickListener {
            listener?.onImageClick(position, item)
        }
        val iv: ImageView = viewHolder.getView(R.id.ivImage)
        ISNav.instance.displayImage(context, item?.path, iv)
        if (mutiSelect) {
            viewHolder.setVisible(R.id.ivPhotoCheaked, true)
            if (Constant.imageList.contains(item?.path)) {
                viewHolder.setImageResource(R.id.ivPhotoCheaked, R.drawable.ic_checked)
            } else {
                viewHolder.setImageResource(R.id.ivPhotoCheaked, R.drawable.ic_uncheck)
            }
        } else {
            viewHolder.setVisible(R.id.ivPhotoCheaked, false)
        }
    }

    fun setShowCamera(showCamera: Boolean) {
        this.showCamera = showCamera
    }

    fun setMutiSelect(mutiSelect: Boolean) {
        this.mutiSelect = mutiSelect
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && showCamera) {
            1
        } else 0
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    init {
        this.config = config
    }
}