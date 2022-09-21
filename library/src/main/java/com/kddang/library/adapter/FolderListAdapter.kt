package com.kddang.library.adapter

import android.content.Context
import android.widget.ImageView
import com.kddang.library.abslistview.EasyLVAdapter
import com.kddang.library.abslistview.EasyLVHolder
import com.kddang.library.ISNav
import com.kddang.library.R
import com.kddang.library.bean.Folder
import com.kddang.library.common.OnFolderChangeListener
import com.kddang.library.config.ISListConfig


/**
 * @author yuyh.
 * @date 2016/8/5.
 */
class FolderListAdapter(
    private val context: Context,
    folderList: MutableList<Folder>,
    config: ISListConfig
) :
    EasyLVAdapter<Folder>(context, folderList, R.layout.item_img_sel_folder) {
    private val folderList: MutableList<Folder>?
    private val config: ISListConfig
    private var selected = 0
    private var listener: OnFolderChangeListener? = null

    override fun convert(holder: EasyLVHolder, position: Int, folder: Folder) {
        if (position == 0) {
            holder.setText(R.id.tvFolderName, "所有图片")
                .setText(R.id.tvImageNum, "共" + totalImageSize + "张")
            val ivFolder: ImageView = holder.getView(R.id.ivFolder)
            if (folderList!!.size > 0) {
                ISNav.instance.displayImage(context, folder.cover?.path, ivFolder)
            }
        } else {
            holder.setText(R.id.tvFolderName, folder.name)
                .setText(R.id.tvImageNum, "共" + folder.images.size.toString() + "张")
            val ivFolder: ImageView = holder.getView(R.id.ivFolder)
            if (folderList!!.size > 0) {
                ISNav.instance.displayImage(context, folder.cover?.path, ivFolder)
            }
        }
        holder.setVisible(R.id.viewLine, position != getCount() - 1)
        if (selected == position) {
            holder.setVisible(R.id.indicator, true)
        } else {
            holder.setVisible(R.id.indicator, false)
        }
        holder.getConvertView().setOnClickListener { selectIndex = position }
    }

    fun setData(folders: List<Folder>?) {
        folderList!!.clear()
        if (folders != null && folders.size > 0) {
            folderList.addAll(folders)
        }
        notifyDataSetChanged()
    }

    private val totalImageSize: Int
        private get() {
            var result = 0
            if (folderList != null && folderList.size > 0) {
                for (folder in folderList) {
                    result += folder.images.size
                }
            }
            return result
        }
    var selectIndex: Int
        get() = selected
        set(position) {
            if (selected == position) return
            listener?.apply {
                onChange(position, folderList!![position])
            }
            selected = position
            notifyDataSetChanged()
        }

    fun setOnFloderChangeListener(listener: OnFolderChangeListener?) {
        this.listener = listener
    }

    init {
        this.folderList = folderList
        this.config = config
    }
}