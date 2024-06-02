package com.example.chatapp.model_classes

import java.io.Serializable

class Users : Serializable {
    private var uid: String = ""
    private var username: String = ""
    private var profile: String = ""
    private var cover: String = ""
    private var status: String = ""
    private var search: String = ""
    private var facebook: String = ""
    private var instagram: String = ""
    private var website: String = ""
    private var unreadMessageCount: Int = 0

    constructor()
    constructor(
        uId: String,
        username: String,
        profile: String,
        cover: String,
        status: String,
        search: String,
        facebook: String,
        instagram: String,
        website: String,
        unreadMessageCount: Int
    ) {
        this.uid = uId
        this.username = username
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
        this.unreadMessageCount = unreadMessageCount
    }

    fun getUID(): String? {
        return uid
    }

    fun setUID(uId: String) {
        this.uid = uId
    }

    fun getUsername(): String? {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun getProfile(): String? {
        return profile
    }

    fun setProfile(profile: String) {
        this.profile = profile
    }

    fun getCover(): String? {
        return cover
    }

    fun setCover(cover: String) {
        this.cover = cover
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String) {
        this.status = status
    }

    fun getSearch(): String? {
        return search
    }

    fun setSearch(search: String) {
        this.search = search
    }

    fun getFacebook(): String? {
        return facebook
    }

    fun setFacebook(facebook: String) {
        this.facebook = facebook
    }

    fun getInstagram(): String? {
        return instagram
    }

    fun setInstagram(instagram: String) {
        this.instagram = instagram
    }

    fun getWebsite(): String? {
        return website
    }

    fun setWebsite(website: String) {
        this.website = website
    }

    fun getUnreadMessageCount(): Int {
        return unreadMessageCount
    }

    fun setUnreadMessageCount(unreadMessageCount: Int) {
        this.unreadMessageCount = unreadMessageCount
    }

}