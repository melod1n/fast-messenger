package com.meloda.fast.screens.messages

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.model.attachments.VkFile
import com.meloda.fast.api.model.attachments.VkPhoto
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.databinding.ItemUploadedAttachmentFileBinding
import com.meloda.fast.databinding.ItemUploadedAttachmentPhotoBinding
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.extensions.TypeTransformations

class AttachmentsAdapter(
    context: Context,
    preAddedValues: List<VkAttachment>,
    private var onRemoveClickedListener: ((position: Int) -> Unit)? = null,
) : BaseAdapter<VkAttachment, AttachmentsAdapter.Holder>(
    context, comparator, preAddedValues
) {

    private companion object {

        private const val TypePhoto = 1
        private const val TypeVideo = 2
        private const val TypeAudio = 3
        private const val TypeFile = 4

        private val comparator = object : DiffUtil.ItemCallback<VkAttachment>() {
            override fun areItemsTheSame(oldItem: VkAttachment, newItem: VkAttachment): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: VkAttachment, newItem: VkAttachment): Boolean {
                return false
            }
        }
    }

    open inner class Holder(v: View) : BaseHolder(v)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is VkPhoto -> TypePhoto
            is VkFile -> TypeFile
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return when (viewType) {
            TypePhoto -> PhotoViewHolder(
                ItemUploadedAttachmentPhotoBinding.inflate(inflater, parent, false)
            )
            TypeFile -> FileViewHolder(
                ItemUploadedAttachmentFileBinding.inflate(inflater, parent, false)
            )
            else -> Holder(View(context))
        }
    }

    inner class PhotoViewHolder(
        private val binding: ItemUploadedAttachmentPhotoBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {
            val photo = getItem(position) as VkPhoto

            binding.image.loadWithGlide(
                url = photo.getSizeOrSmaller(VkPhoto.SIZE_TYPE_807)?.url,
                crossFade = true,
                transformations = listOf(TypeTransformations.RoundedCornerCrop(12))
            )

            binding.close.setOnClickListener {
                onRemoveClickedListener?.invoke(position)
            }
        }
    }


    inner class FileViewHolder(
        private val binding: ItemUploadedAttachmentFileBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {
            val file = getItem(position) as VkFile

            binding.image.setImageDrawable(ColorDrawable(Color.GRAY))

            file.preview?.photo?.sizes?.get(0)?.let { size ->
                binding.image.loadWithGlide(
                    url = size.src,
                    crossFade = true,
                    transformations = listOf(
                        TypeTransformations.RoundedCornerCrop(12),
                        TypeTransformations.CenterCrop
                    ),
                )
            }

            binding.close.setOnClickListener {
                onRemoveClickedListener?.invoke(position)
            }
        }

    }

}