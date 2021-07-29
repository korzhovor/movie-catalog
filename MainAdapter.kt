package com.example.moviecatalog

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class MainAdapter(val movieList: MainActivity.MovieList): RecyclerView.Adapter<RecyclerViewHolder>() {

    override fun getItemCount(): Int {
        return movieList.results.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.movie_card, parent, false)
        return RecyclerViewHolder(cellForRow)
    }

    fun makeSnack(view: RecyclerViewHolder){
        Snackbar.make(view.title, view.title.text, Snackbar.LENGTH_LONG).show()
    }

    fun parseDate(text: String): String {
        var result = ""
        if (text[8] != '0')
            result += text[8]
        result += text[9]
        result += " "
        result += if (text[5] != '0')
            parseMonth((text[6].toInt() - '0'.toInt()) + 10)
        else
            parseMonth(text[6].toInt() - '0'.toInt())
        result += " "
        result += text[0]
        result += text[1]
        result += text[2]
        result += text[3]
        return result
    }

    fun parseMonth(month: Int): String {
        when(month) {
            1 -> return "Января"
            2 -> return "Февраля"
            3 -> return "Марта"
            4 -> return "Апреля"
            5 -> return "Мая"
            6 -> return "Июня"
            7 -> return "Июля"
            8 -> return "Августа"
            9 -> return "Сентября"
            10 -> return "Октября"
            11 -> return "Ноября"
            12 -> return "Декабря"
        }
        return "Ошибка"
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val movie = movieList.results[position]
        holder.layout.setOnClickListener{ makeSnack(holder) }
        holder.title.text = movie.title
        holder.overview.text = movie.overview
        if (!movie.release_date.isNullOrEmpty())
            holder.releaseDate.text = parseDate(movie.release_date)
        if (likesCheck(movie.id) == true)
            Picasso.with(holder.itemView.context).load(R.drawable.heart_filled).into(holder.like)
        else
            Picasso.with(holder.itemView.context).load(R.drawable.heart).into(holder.like)
        holder.like.setOnClickListener{
            if (likesCheck(movie.id) == true) {
                Picasso.with(holder.itemView.context).load(R.drawable.heart).into(holder.like)
                likesRemove(movie.id)
            } else {
                Picasso.with(holder.itemView.context).load(R.drawable.heart_filled).into(holder.like)
                likesAdd(movie.id)
            }
        }
        val w: Int
//        if (displayMetrics < 500)
        Picasso.with(holder.itemView.context).load("https://image.tmdb.org/t/p/w342" + movie.poster_path).into(holder.poster)
    }
}

class RecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val layout: ConstraintLayout = view.findViewById(R.id.movie_card)
    val title: TextView = view.findViewById(R.id.title)
    val overview: TextView = view.findViewById(R.id.overview)
    val releaseDate: TextView = view.findViewById(R.id.release_date)
    val poster: ImageView = view.findViewById(R.id.poster)
    val like: ImageButton = view.findViewById(R.id.like)
}