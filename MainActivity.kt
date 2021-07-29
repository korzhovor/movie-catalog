package com.example.moviecatalog

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.moviecatalog.R.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.IOException


//Список дел:

//Рефакторинг всего приложения
//Описание в 4 строки для фильмов, название которых занимает 1 строку (желательно)
//Предустановленный фон для фильмов без постера (опционально)

var displayMetrics = 0

var likes: SharedPreferences? = null

fun likesCheck(id: Int): Boolean? {
    return likes?.contains(id.toString())
}

fun likesAdd(id: Int) {
    val editor = likes?.edit()
    editor?.putBoolean(id.toString(), true)
    editor?.apply()
}

fun likesRemove(id: Int) {
    val editor = likes?.edit()
    editor?.remove(id.toString())
    editor?.apply()
}

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        displayMetrics = resources.displayMetrics.densityDpi
        val swipe = findViewById<SwipeRefreshLayout>(id.swipe_refresh_layout)
        swipe.setColorSchemeResources(color.secondary, color.white)
        likes = getSharedPreferences("TABLE", Context.MODE_PRIVATE)
        var query = ""
        val recyclerView = findViewById<RecyclerView>(id.recycler_view_main)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val editText = findViewById<EditText>(id.search_bar)
        editText.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                true
            } else {
                if (editText.text.isNotEmpty()) {
                    query = editText.text.toString()
                    doRequest(editText.text.toString())
                } else {
                    hideKeyboard(editText)
                }
                false
            }
        }
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefresh.setOnRefreshListener {
            if (editText.text.isNotEmpty()) {
                query = editText.text.toString()
                doRequest(editText.text.toString())
                swipeRefresh.isRefreshing = false
            } else {
                doRequest(query)
                swipeRefresh.isRefreshing = false
            }
        }
    }

    fun setVisibility(search: Boolean, layoutV: Boolean, mainSearch: Boolean, image: Int, imageV: Boolean, text: String, textV: Boolean) {
        val progressBar = findViewById<ProgressBar>(id.search_progress_bar)
        val mainProgressBar = findViewById<ProgressBar>(id.main_progress_bar)
        val layout: LinearLayout = findViewById(id.invisible_layout)
        val imageView: ImageView = findViewById(id.extra_image)
        val textView: TextView = findViewById(id.extra_text)

        val uiHandler = Handler(Looper.getMainLooper())
        uiHandler.post(Runnable {
            Picasso.with(layout.context).load(image).into(imageView)
            textView.text = text
            if (search)
                progressBar.visibility = ProgressBar.VISIBLE
            else
                progressBar.visibility = ProgressBar.INVISIBLE
            if (mainSearch)
                mainProgressBar.visibility = ProgressBar.VISIBLE
            else
                mainProgressBar.visibility = ProgressBar.GONE
            if (layoutV)
                layout.visibility = LinearLayout.VISIBLE
            else
                layout.visibility = LinearLayout.GONE
            if (imageV)
                imageView.visibility = ImageView.VISIBLE
            else
                imageView.visibility = ImageView.GONE
            if (textV)
                textView.visibility = TextView.VISIBLE
            else
                textView.visibility = TextView.GONE
        })
    }

    fun hideKeyboard(editText: EditText) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun doRequest(query: String) {
        val layout = findViewById<RecyclerView>(id.recycler_view_main)
        val editText = findViewById<EditText>(id.search_bar)
        val link = "https://api.themoviedb.org/3/search/movie?api_key=59cb3ffb58efdcce00495e7b29ea70e2&language=ru-RU&query=$query&page=1&include_adult=false"
        val request = Request.Builder().url(link).build()
        val client = OkHttpClient()
        val gson = GsonBuilder().create()
        if (layout.isEmpty())
            setVisibility(false, true, true, drawable.big_search, false, "", false)
        else
            setVisibility(true, false, false, drawable.big_search, false, "", false)
        hideKeyboard(editText)
        editText.setText("")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (layout.isEmpty())
                    setVisibility(false, true, false, drawable.alert_triangle, true, "Нам не удалось обработать ваш запрос. Попробуйте еще раз", true)
                else {
                    setVisibility(false, false, false, drawable.alert_triangle, false, "Нам не удалось обработать ваш запрос. Попробуйте еще раз", false)
                    Snackbar.make(layout, "Проверьте ваше соединение с интернетом и попробуйте еще раз", Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val movieList = gson.fromJson(body, MovieList::class.java)
                if (movieList.results.isEmpty()) {
                    setVisibility(false, true, false, drawable.big_search, true, "По запросу \"$query\" ничего не найдено", true)
                } else
                    runOnUiThread {
                        layout.adapter = MainAdapter(movieList)
                        setVisibility(false, false, false, drawable.big_search, false, "По запросу ничего не найдено", false)
                    }
            }
        })
    }

    class MovieList(val page: Int, val results: List<MovieModel>, val total_pages: Int, total_results: Int)

    class MovieModel(
            val title: String,
            val overview: String,
            val release_date: String,
            val poster_path: String,
            val id: Int
    )
}

