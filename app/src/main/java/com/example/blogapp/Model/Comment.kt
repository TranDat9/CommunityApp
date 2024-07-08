package com.example.blogapp.Model

import android.os.Parcel
import android.os.Parcelable

data  class Comment(

    val comment: String?="null",
    val date: String? ="null",
    val profileImage: String? ="null",
    val username: String? ="null",
    var userId :String="null",
    var commentId :String="null",
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"null",
        parcel.readString()?:"null",
        parcel.readString()?:"null",
        parcel.readString()?:"null",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comment)
        parcel.writeString(date)
        parcel.writeString(profileImage)
        parcel.writeString(username)
        parcel.writeString(commentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }

}
