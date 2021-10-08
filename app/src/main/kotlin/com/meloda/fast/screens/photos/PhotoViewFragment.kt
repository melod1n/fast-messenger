package com.meloda.fast.screens.photos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import com.meloda.fast.base.BaseViewModelFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoViewFragment : BaseViewModelFragment<PhotoViewViewModel>() {

    override val viewModel: PhotoViewViewModel by viewModels()

//    private val photosList: MutableList<VkPhoto> = mutableListOf()

    private var photoLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoLink = requireArguments().getString("photoLink")

//        val list: List<*>? = Gson().fromJson(
//            requireArguments().getString("photosList"),
//            List::class.java
//        )
//
//        list?.forEach { if (it is VkPhoto) photosList.add(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ImageView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoLink?.let { viewModel.loadImageFromUrl(it, requireView() as ImageView) }
    }

}