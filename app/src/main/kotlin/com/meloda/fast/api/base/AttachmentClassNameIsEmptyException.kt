package com.meloda.fast.api.base

import com.meloda.fast.api.model.attachments.VkAttachment
import okio.IOException

class AttachmentClassNameIsEmptyException(attachment: VkAttachment) :
    IOException(
        "attachment ${attachment.javaClass.name} does not have declared field \"className\""
    )
