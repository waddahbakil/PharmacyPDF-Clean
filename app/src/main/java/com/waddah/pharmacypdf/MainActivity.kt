package com.waddah.pharmacypdf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.waddah.pharmacypdf.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val customers = mutableListOf<Customer>()
    private lateinit var adapter: CustomerAdapter
    private val STORAGE_PERMISSION_CODE = 100

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
        }

        binding.btnPrintPdf.setOnClickListener {
            if (checkPermission()) {
                createPdf()
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createPdf()
            } else {
                Toast.makeText(this, "يجب السماح بالصلاحية لطباعة PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createPdf() {
        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
            val file = File(path, "PharmacyReport.pdf")
            val writer = PdfWriter(file)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            document.add(Paragraph("تقرير مبيعات الصيدلية").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(20f))
            document.add(Paragraph(" "))

            val table = Table(3)
            table.addCell("اسم العميل")
            table.addCell("الدواء")
            table.addCell("السعر")

            var total = 0.0
            for (customer in customers) {
                table.addCell(customer.name)
                table.addCell(customer.medicine)
                table.addCell("${customer.price}")
                total += customer.price
            }
            document.add(table)
            document.add(Paragraph(" "))
            document.add(Paragraph("الإجمالي: $total ريال").setBold())

            document.close()
            Toast.makeText(this, "تم حفظ PDF في Downloads", Toast.LENGTH_LONG).show()

            // فتح الملف بعد الحفظ
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
