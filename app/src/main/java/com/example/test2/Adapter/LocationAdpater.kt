package com.example.test2.Adapter


import androidx.recyclerview.widget.RecyclerView
import com.example.test2.R
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import com.example.test2.Dao.WeatherLocationDatabase
import com.example.test2.Dao.WeatherLocationTable
import com.example.test2.Model.ModelLocation
import com.example.test2.Touch.ItemTouchHelperListener


class LocationAdpater(private val context: Context, var items: MutableList<ModelLocation>) :
    RecyclerView.Adapter<LocationAdpater.ViewHolder>(), ItemTouchHelperListener {
    val WeatherLocationDB = Room.databaseBuilder(
        context,
        WeatherLocationDatabase::class.java,
        "Weatherlocation"
    )
        .allowMainThreadQueries()
        .build()
    lateinit var DBlist: MutableList<WeatherLocationTable>


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdpater.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_location, parent, false)
        return ViewHolder(itemView)
    }

    // 전달받은 위치의 아이템 연결
    override fun onBindViewHolder(holder: LocationAdpater.ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }
    interface OnItemClickListener{
        fun onItemClick(v:View, pos : Int)

    }
    private var listener : OnItemClickListener? = null
    fun setOnItemClickListener(listener : OnItemClickListener) {
        this.listener = listener
    }
    override fun onRightClick(position: Int, viewHolder: RecyclerView.ViewHolder?) {
        DBlist = WeatherLocationDB.WeatherLocationInterface().getAll()
        if(position>=1){
       items.removeAt(position)
            WeatherLocationDB.WeatherLocationInterface().delete(DBlist[position-1])
            notifyItemRemoved(position)
        }
    }
    // 아이템 갯수 리턴
    override fun getItemCount() = items.count()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun setItem(item: ModelLocation) {
            val loaddress = itemView.findViewById<TextView>(R.id.loaddress)
            val imgWeather = itemView.findViewById<ImageView>(R.id.imgWeather)  // 날씨 이미지
            val tvHumidity = itemView.findViewById<TextView>(R.id.tvHumidity)   // 습도
            val tvTemp = itemView.findViewById<TextView>(R.id.tvTemp)           // 온도
            imgWeather.setImageResource(getRainImage(item.rainType, item.sky, item.fcstTime))
            loaddress.text = item.address
            tvHumidity.text = item.humidity + "%"
            tvTemp.text = item.temp + "°"

            val pos = adapterPosition
            if(pos!= RecyclerView.NO_POSITION)
            {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView,pos)
                }
            }

        }
    }

    fun getRainImage(rainType: String, sky: String, factTime: String): Int {
        return when (rainType) {
            "0" -> getWeatherImage(sky, factTime)
            "1" -> R.drawable.rainy
            "2" -> R.drawable.hail
            "3" -> R.drawable.snowy
            "4" -> R.drawable.brash
            else -> getWeatherImage(sky, factTime)
        }
    }

    fun getWeatherImage(sky: String, factTime: String): Int {
        // 하늘 상태
        return when (sky) {
            "1" -> getWeatherImage2(factTime)                      // 맑음
            "3" -> R.drawable.cloudy                     // 구름 많음
            "4" -> R.drawable.blur2                // 흐림
            else -> R.drawable.ic_launcher_foreground   // 오류
        }
    }

    fun getWeatherImage2(factTime: String): Int {
//아침 저녁
        return if (factTime.toInt() < 600) return R.drawable.after
        else if (factTime.toInt() < 1800) return R.drawable.sun2
        else return R.drawable.after

    }


    override fun onItemSwipe(position: Int) {
        TODO("Not yet implemented")
    }


    override fun onItemMove(from_position: Int, to_position: Int): Boolean = false

}