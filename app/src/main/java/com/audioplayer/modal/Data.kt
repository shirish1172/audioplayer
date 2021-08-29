package com.audioplayer.modal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class AudioData(val title:String,val path:String) : Parcelable