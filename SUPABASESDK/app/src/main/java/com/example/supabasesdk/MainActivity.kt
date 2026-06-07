package com.example.supabasesdk

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.supabasesdk.adapter.AlumnoAdapter
import com.example.supabasesdk.api.SupabaseManager
import com.example.supabasesdk.model.Alumno
import com.example.supabasesdk.model.Materia
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val actvMaterias = findViewById<AutoCompleteTextView>(R.id.actvListaMaterias)
        val actvListaNiveles = findViewById<AutoCompleteTextView>(R.id.actvListaNiveles)
        val lvAlumnos = findViewById<ListView>(R.id.lvAlumnos)

        actvListaNiveles.setOnItemClickListener { _, _, position, _ ->
            actvMaterias.setText("")
            val lstMaterias = ArrayList<String>()
            lifecycleScope.launch {
                try {
                    val listaMaterias = ArrayList(
                        SupabaseManager.client
                            .from("materias")
                            .select {
                                filter {
                                    eq("nivel", position + 1)
                                }
                                order("nombre", Order.ASCENDING)
                            }
                            .decodeList<Materia>()
                    )

                    for (materia in listaMaterias) {
                        lstMaterias.add(materia.nombre ?: "")
                    }

                    Log.d(TAG, "Materias cargadas: ${lstMaterias.size}")

                } catch (e: RestException) {
                    Log.e(TAG, "Error RestException: ${e.error} - ${e.description}")
                    lstMaterias.clear()
                    Toast.makeText(
                        this@MainActivity,
                        "Error al cargar materias: ${e.description}",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error general: ${e.message}", e)
                    lstMaterias.clear()
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        lstMaterias
                    )
                    actvMaterias.setAdapter(adapter)
                }
            }
        }

        actvMaterias.setOnItemClickListener { _, _, _, _ ->
            var lstAlumnos = ArrayList<Alumno>()
            lifecycleScope.launch {
                try {
                    lstAlumnos = ArrayList(
                        SupabaseManager.client
                            .from("alumnos")
                            .select {
                                order("nombres", Order.ASCENDING)
                            }
                            .decodeList<Alumno>()
                    )

                    Log.d(TAG, "Alumnos cargados: ${lstAlumnos.size}")

                } catch (e: RestException) {
                    Log.e(TAG, "Error RestException: ${e.error} - ${e.description}")
                    lstAlumnos.clear()
                    Toast.makeText(
                        this@MainActivity,
                        "Error al cargar alumnos: ${e.description}",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error general: ${e.message}", e)
                    lstAlumnos.clear()
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    val adapter = AlumnoAdapter(this@MainActivity, lstAlumnos)
                    lvAlumnos.adapter = adapter
                }
            }
        }
    }
}
