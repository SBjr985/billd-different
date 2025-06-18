package com.beardedbiz.billddifferent.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.beardedbiz.billddifferent.R

class CategorySpinnerAdapter(
    context: Context,
    private val categories: List<String>
) : ArrayAdapter<String>(context, R.layout.item_category_spinner, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createIconView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createIconView(position, convertView, parent)
    }

    private fun createIconView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.item_category_spinner, parent, false)

        val icon = view.findViewById<ImageView>(R.id.icon)
        val text = view.findViewById<TextView>(R.id.text)

        val category = categories[position]
        text.text = category
        icon.setImageResource(getCategoryIconResId(category))

        return view
    }
}
