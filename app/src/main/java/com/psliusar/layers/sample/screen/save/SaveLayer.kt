package com.psliusar.layers.sample.screen.save

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.TextView
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.R
import java.io.Serializable

class SaveLayer : Layer(R.layout.screen_save) {

    private var stringList: ArrayList<String>? by savedState()

    private var testInt: Int? by savedState()
    private var testLong: Long? by savedState()
    private var testFloat: Float? by savedState()
    private var testDouble: Double? by savedState()
    private var testBoolean: Boolean? by savedState()
    private var testString: String? by savedState()
    private var testCharSequence: CharSequence? by savedState()
    private var testSerializable: TestSerializable? by savedState()
    private var testParcelable: TestParcelable? by savedState()
    private var testSparseArray: SparseArray<TestParcelable>? by savedState()
    private var testSerializableArray: Array<TestSerializable>? by savedState()
    private var testEmptyArray: Array<TestSerializable>? by savedState()
    private var testParcelableArray: Array<TestParcelable>? by savedState()
    private var testParcelableArrayList: ArrayList<TestParcelable>? by savedState()

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        getView<TextView>(R.id.save_string_list).text = stringList?.joinToString()

        Log.i(
            "Saved properties",
            String.format(
                "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                testInt,
                testLong,
                testFloat,
                testDouble,
                testBoolean,
                testString,
                testCharSequence,
                testSerializable,
                testParcelable,
                testSparseArray,
                testSerializableArray,
                testEmptyArray,
                testParcelableArray,
                testParcelableArrayList
            )
        )
    }

    fun setParameters(vararg items: String) {
        stringList = ArrayList(items.toList())
        testInt = 42
        testLong = 142L
        testFloat = 4.2F
        testDouble = 14.2
        testBoolean = true
        testString = "Dash Force"
        testCharSequence = "Sequence"
        testSerializable = TestSerializable("Serializable")
        testParcelable = TestParcelable("Parcelable")
        testSparseArray = SparseArray<TestParcelable>().apply {
            put(42, TestParcelable("1, 2, 3, 4"))
        }
        testSerializableArray = arrayOf(TestSerializable("Serializable in Array"))
        testEmptyArray = arrayOf()
        testParcelableArray = arrayOf(TestParcelable("Parcelable in Array"))
        testParcelableArrayList = ArrayList<TestParcelable>().apply {
            add(TestParcelable("Parcelable in ArrayList"))
        }
    }

    private class TestSerializable(private val name: String) : Serializable {
        override fun toString(): String = "TestSerializable(name='$name')"
    }

    private class TestParcelable(private val name: String) : Parcelable {

        constructor(parcel: Parcel) : this(parcel.readString()!!)

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
        }

        override fun describeContents(): Int = 0

        override fun toString(): String = "TestParcelable(name='$name')"

        companion object CREATOR : Parcelable.Creator<TestParcelable> {
            override fun createFromParcel(parcel: Parcel): TestParcelable = TestParcelable(parcel)
            override fun newArray(size: Int): Array<TestParcelable?> = arrayOfNulls(size)
        }
    }
}
