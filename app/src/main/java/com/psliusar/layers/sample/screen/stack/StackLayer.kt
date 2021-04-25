package com.psliusar.layers.sample.screen.stack

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.psliusar.layers.Layer
import com.psliusar.layers.sample.MainActivity
import com.psliusar.layers.sample.R
import java.io.Serializable

class StackLayer : Layer(R.layout.screen_stack) {

    private var title: CharSequence? by savedState()
    private var level: Int by savedState()

    private var testInt: Int by savedState()
    private var testLong: Long by savedState()
    private var testFloat: Float by savedState()
    private var testDouble: Double by savedState()
    private var testBoolean: Boolean by savedState()
    private var testString: String by savedState()
    private var testCharSequence: CharSequence by savedState()
    private var testSerializable: Serializable by savedState()
    private var testParcelable: Parcelable by savedState()

    private lateinit var viewModel: StackViewModel

    private val nextLayerTitle: CharSequence
        get() = view!!.findViewById<TextView>(R.id.stack_next_title).text

    private val isNextOpaque: Boolean
        get() = view!!.findViewById<CheckBox>(R.id.stack_next_opaque).isChecked

    private lateinit var mainActivity: MainActivity

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        mainActivity = getParent()
        viewModel = getViewModel()
    }

    override fun onBindView(savedState: Bundle?, view: View) {
        super.onBindView(savedState, view)
        view.findViewById<View>(R.id.stack_add).setOnClickListener {
            mainActivity.addToStack(
                nextLayerTitle,
                level + 1,
                isNextOpaque
            )
        }

        view.findViewById<View>(R.id.stack_replace).setOnClickListener {
            mainActivity.replaceInStack(
                nextLayerTitle,
                level + 1,
                isNextOpaque
            )
        }

        title?.let {
            view.findViewById<TextView>(R.id.stack_level).text = viewModel.getStackLevelText(it, level)
        }

        Log.i(
            "Saved properties",
            String.format(
                "%s, %s, %s, %s, %s, %s, %s, %s, %s",
                testInt,
                testLong,
                testFloat,
                testDouble,
                testBoolean,
                testString,
                testCharSequence,
                testSerializable,
                testParcelable
            )
        )
    }

    fun setParameters(title: CharSequence, level: Int) {
        this.title = title
        this.level = level

        testInt = 42
        testLong = 142L
        testFloat = 4.2F
        testDouble = 14.2
        testBoolean = true
        testString = "Dash Force"
        testCharSequence = "Sequence"
        testSerializable = TestSerializable("Serializable")
        testParcelable = TestParcelable("Parcelable")
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
            override fun createFromParcel(parcel: Parcel): TestParcelable {
                return TestParcelable(parcel)
            }

            override fun newArray(size: Int): Array<TestParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }
}
