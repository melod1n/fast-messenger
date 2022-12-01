package com.meloda.fast.screens.messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.shape.ShapeAppearanceModel
import com.meloda.fast.R
import com.meloda.fast.api.model.attachments.*
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.databinding.ItemUploadedAttachmentAudioBinding
import com.meloda.fast.databinding.ItemUploadedAttachmentFileBinding
import com.meloda.fast.databinding.ItemUploadedAttachmentPhotoBinding
import com.meloda.fast.databinding.ItemUploadedAttachmentVideoBinding
import com.meloda.fast.ext.ImageLoader.clear
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.dpToPx
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.ext.visible

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

    private val colorPrimaryVariant = ContextCompat.getColor(context, R.color.colorPrimaryVariant)

    open inner class Holder(v: View) : BaseHolder(v)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is VkPhoto -> TypePhoto
            is VkVideo -> TypeVideo
            is VkAudio -> TypeAudio
            is VkFile -> TypeFile
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return when (viewType) {
            TypePhoto -> PhotoViewHolder(
                ItemUploadedAttachmentPhotoBinding.inflate(inflater, parent, false)
            )
            TypeVideo -> VideoViewHolder(
                ItemUploadedAttachmentVideoBinding.inflate(inflater, parent, false)
            )
            TypeAudio -> AudioViewHolder(
                ItemUploadedAttachmentAudioBinding.inflate(inflater, parent, false)
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

        init {
            binding.image.shapeAppearanceModel =
                binding.image.shapeAppearanceModel.withCornerSize(18.dpToPx().toFloat())
        }

        override fun bind(position: Int) {
            val photo = getItem(position) as VkPhoto

            binding.progressBar.visible()

            binding.image.loadWithGlide(
                url = photo.getSizeOrSmaller(VkPhoto.SIZE_TYPE_807)?.url,
                crossFade = true,
                placeholderColor = colorPrimaryVariant,
                onLoadedAction = { binding.progressBar.gone() },
                onFailedAction = { binding.progressBar.gone() }
            )

            binding.close.setOnClickListener {
                onRemoveClickedListener?.invoke(position)
            }
        }
    }

    inner class VideoViewHolder(
        private val binding: ItemUploadedAttachmentVideoBinding
    ) : Holder(binding.root) {
        init {
            val cornerSizedShapeAppearanceModel = ShapeAppearanceModel().withCornerSize(
                18.dpToPx().toFloat()
            )

            binding.image.shapeAppearanceModel = cornerSizedShapeAppearanceModel
            binding.coloredBackground.shapeAppearanceModel = cornerSizedShapeAppearanceModel
        }

        override fun bind(position: Int) {
            val video = getItem(position) as VkVideo

            binding.title.text = video.title

            val previewSrc = video.imageForWidthAtLeast(300)
            binding.image.toggleVisibility(previewSrc != null)
            binding.coloredBackground.toggleVisibility(previewSrc == null)
            binding.videoIcon.toggleVisibility(previewSrc == null)

            if (previewSrc != null) {
                binding.progressBar.visible()

                binding.image.loadWithGlide(
                    url = previewSrc.url,
                    crossFade = true,
                    placeholderColor = colorPrimaryVariant,
                    onLoadedAction = { binding.progressBar.gone() },
                    onFailedAction = { showPlaceholder() }
                )
            } else {
                binding.progressBar.gone()
                binding.image.clear()
            }

            binding.close.setOnClickListener {
                onRemoveClickedListener?.invoke(position)
            }
        }

        private fun showPlaceholder() {
            binding.coloredBackground.visible()
            binding.videoIcon.visible()
            binding.image.clear()
            binding.image.gone()
            binding.progressBar.gone()
        }
    }

    inner class AudioViewHolder(
        private val binding: ItemUploadedAttachmentAudioBinding
    ) : Holder(binding.root) {
        init {
            binding.coloredBackground.shapeAppearanceModel =
                binding.coloredBackground.shapeAppearanceModel.withCornerSize(18.dpToPx().toFloat())
        }

        override fun bind(position: Int) {
            val audio = getItem(position) as VkAudio

            binding.title.text = audio.title

            binding.close.setOnClickListener {
                onRemoveClickedListener?.invoke(position)
            }
        }
    }

    inner class FileViewHolder(
        private val binding: ItemUploadedAttachmentFileBinding
    ) : Holder(binding.root) {

        init {
            val cornerSizedShapeAppearanceModel = ShapeAppearanceModel().withCornerSize(
                18.dpToPx().toFloat()
            )

            binding.image.shapeAppearanceModel = cornerSizedShapeAppearanceModel
            binding.coloredBackground.shapeAppearanceModel = cornerSizedShapeAppearanceModel
        }

        override fun bind(position: Int) {
            val file = getItem(position) as VkFile

            binding.title.text = file.title

            val previewSrc = file.preview?.photo?.sizes?.get(0)
            binding.image.toggleVisibility(previewSrc != null)
            binding.coloredBackground.toggleVisibility(previewSrc == null)
            binding.fileIcon.toggleVisibility(previewSrc == null)

            if (previewSrc != null) {
                binding.progressBar.visible()

                binding.image.loadWithGlide(
                    url = previewSrc.src,
                    crossFade = true,
                    placeholderColor = colorPrimaryVariant,
                    onLoadedAction = { binding.progressBar.gone() },
                    onFailedAction = { showPlaceholder() }
                )
            } else {
                binding.progressBar.gone()
                binding.image.clear()
            }

            binding.close.setOnClickListener {
                onRemoveClickedListener?.invoke(position)
            }
        }

        private fun showPlaceholder() {
            binding.coloredBackground.visible()
            binding.fileIcon.visible()
            binding.image.clear()
            binding.image.gone()
            binding.progressBar.gone()
        }
    }
}