package com.waddah.pharmacypdf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.waddah.pharmacypdf.databinding.ItemCustomerBinding

class CustomerAdapter(private val customers: List<Customer>) :
    RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCustomerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]
        holder.binding.tvName.text = customer.name
        holder.binding.tvMedicine.text = "الدواء: ${customer.medicine}"
        holder.binding.tvPrice.text = "السعر: ${customer.price} ريال"
    }

    override fun getItemCount() = customers.size
}
