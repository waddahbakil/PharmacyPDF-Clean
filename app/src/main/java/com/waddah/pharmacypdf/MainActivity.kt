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
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.BaseDirection
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.waddah.pharmacypdf.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val customers = mutableListOf<Customer>()
    private lateinit var adapter: CustomerAdapter
    private val STORAGE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customers.add(Customer(1, "أحمد محمد", "بنادول", 15.5))
        customers.add(Customer(2, "سارة علي", "فيتامين C", 22.0))

        adapter = CustomerAdapter(customers)
        binding.rvCustomers.layoutManager = LinearLayoutManager(this)
        binding.rvCustomers.adapter = adapter

        binding.btnAddCustomer.setOnClickListener {
            val newId = customers.size + 1
            customers.add(Customer(newId, "عميل $newId", "دواء جديد", 10.0))
            adapter.notifyItemInserted(customers.size - 1)
            binding.rvCustomers.scrollToPosition(customers.size - 1)
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
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(path, "PharmacyReport_$timeStamp.pdf")
            val writer = PdfWriter(file)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            // 1. تحميل الخط العربي
            var font: PdfFont? = null
            try {
                val fontData = assets.open("fonts/Arial.ttf").readBytes()
                val fontProgram = FontProgramFactory.createFont(fontData)
                font = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H)
                document.setFont(font)
            } catch (e: Exception) {
                Toast.makeText(this, "ملف Arial.ttf غير موجود", Toast.LENGTH_LONG).show()
            }

            // 2. تفعيل الكتابة من اليمين لليسار للصفحة كلها
            document.setBaseDirection(BaseDirection.RIGHT_TO_LEFT)

            // دالة مساعدة للنص
            fun textP(text: String): Paragraph {
                val p = Paragraph(text).setTextAlignment(TextAlignment.CENTER)
                if (font!= null) p.setFont(font)
                return p
            }

            document.add(textP("تقرير مبيعات الصيدلية").setBold().setFontSize(20f))
            document.add(textP("التاريخ: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}"))
            document.add(Paragraph(" "))

            // 3. الجدول لازم نحدد 3 أعمدة ونخليه RTL
            val table = Table(UnitValue.createPercentArray(floatArrayOf(20f, 40f))).useAllAvailableWidth()
            table.setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
            table.setTextAlignment(TextAlignment.RIGHT)

            // ترتيب الأعمدة: اسم العميل - الدواء - السعر عشان RTL
            table.addHeaderCell(Cell().add(textP("اسم العميل").setBold()))
            table.addHeaderCell(Cell().add(textP("الدواء").setBold()))
            table.addHeaderCell(Cell().add(textP("السعر").setBold()))

            var total = 0.0
            for (customer in customers) {
                table.addCell(Cell().add(textP(customer.name)))
                table.addCell(Cell().add(textP(customer.medicine)))
                table.addCell(Cell().add(textP("${customer.price}")))
                total += customer.price
            }
            document.add(table)

            document.add(Paragraph(" "))
            document.add(textP("الإجمالي: $total ريال").setBold().setFontSize(16f).setTextAlignment(TextAlignment.LEFT))

            document.close()

            Toast.makeText(this, "تم حفظ PDF في Downloads", Toast.LENGTH_LONG).show()

            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
