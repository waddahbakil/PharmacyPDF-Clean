package com.waddah.pharmacypdf

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.waddah.pharmacypdf.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val customers = mutableListOf<Customer>()
    private lateinit var adapter: CustomerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // بيانات تجريبية
        customers.add(Customer(1, "أحمد محمد", "بنادول", 15.5))
        customers.add(Customer(2, "سارة علي", "فيتامين C", 22.0))

        adapter = CustomerAdapter(customers)
        binding.rvCustomers.layoutManager = LinearLayoutManager(this)
        binding.rvCustomers.adapter = adapter

        binding.btnAddCustomer.setOnClickListener {
            val newId = customers.size + 1
            customers.add(Customer(newId, "عميل $newId", "دواء جديد", 10.0))
            adapter.notifyItemInserted(customers.size - 1)
            Toast.makeText(this, "تمت الإضافة", Toast.LENGTH_SHORT).show()
        }

        binding.btnPrintPdf.setOnClickListener {
            Toast.makeText(this, "زر الطباعة شغال", Toast.LENGTH_SHORT).show()
            // بنضيف كود PDF بعد ما يشتغل البناء
        }
    }
}
