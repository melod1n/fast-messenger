package com.meloda.fast.api

enum class VKApiKeys(val value: String) {
    READ_MESSAGE("_read_message"),
    RESTORE_MESSAGE("_restore_message"),
    NEW_MESSAGE("_new_message"),
    EDIT_MESSAGE("_edit_message"),
    DELETE_MESSAGE("_delete_message"),

    UPDATE_MESSAGE("_update_message"),
    UPDATE_CONVERSATION("_update_conversation"),
    UPDATE_USER("_update_user"),
    UPDATE_GROUP("_update_group")
}