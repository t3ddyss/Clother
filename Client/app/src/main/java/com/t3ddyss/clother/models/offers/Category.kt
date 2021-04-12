package com.t3ddyss.clother.models.offers

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.utilities.DEFAULT_STRING_VALUE

@Entity(tableName = "category",
        foreignKeys = [ForeignKey(
                entity = Category::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("parent_id"))])
data class Category(@PrimaryKey val id: Int,
                    @SerializedName("parent_id")
                    @ColumnInfo(name="parent_id", index = true)
                    val parentId: Int?,
                    val title: String,
                    @SerializedName("last_level")
                    @ColumnInfo(name = "last_level")
                    val isLastLevel: Boolean) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString() ?: DEFAULT_STRING_VALUE,
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeValue(parentId)
        parcel.writeString(title)
        parcel.writeByte(if (isLastLevel) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}