package com.litegral.gamecorner.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.litegral.gamecorner.R
import com.litegral.gamecorner.models.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BookingSuccessFragment : Fragment() {

    private lateinit var psImageView: ImageView
    private lateinit var psNameText: TextView
    private lateinit var psSubtitleText: TextView
    private lateinit var durationText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var depositItemText: TextView
    private lateinit var viewAllStationsText: TextView
    private lateinit var homeButton: Button

    private var gameStation: GameStation? = null
    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null
    private var selectedDepositItem: DepositItem? = null

    companion object {
        private const val ARG_GAME_STATION = "game_station"
        private const val ARG_SELECTED_DATE = "selected_date"
        private const val ARG_SELECTED_TIME = "selected_time"
        private const val ARG_DEPOSIT_ITEM = "deposit_item"

        fun newInstance(
            gameStation: GameStation,
            selectedDate: LocalDate,
            selectedTime: LocalTime,
            depositItem: DepositItem
        ): BookingSuccessFragment {
            val fragment = BookingSuccessFragment()
            val args = Bundle()
            args.putSerializable(ARG_GAME_STATION, gameStation)
            args.putString(ARG_SELECTED_DATE, selectedDate.toString())
            args.putString(ARG_SELECTED_TIME, selectedTime.toString())
            args.putSerializable(ARG_DEPOSIT_ITEM, depositItem)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            gameStation = it.getSerializable(ARG_GAME_STATION) as? GameStation
            selectedDate = LocalDate.parse(it.getString(ARG_SELECTED_DATE))
            selectedTime = LocalTime.parse(it.getString(ARG_SELECTED_TIME))
            @Suppress("DEPRECATION")
            selectedDepositItem = it.getSerializable(ARG_DEPOSIT_ITEM) as? DepositItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_booking_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        populateBookingDetails()
        loadGameStationImage()
    }

    private fun initializeViews(view: View) {
        psImageView = view.findViewById(R.id.ps_image)
        psNameText = view.findViewById(R.id.ps_name_text)
        psSubtitleText = view.findViewById(R.id.ps_subtitle_text)
        durationText = view.findViewById(R.id.duration_text)
        dateText = view.findViewById(R.id.date_text)
        timeText = view.findViewById(R.id.time_text)
        depositItemText = view.findViewById(R.id.deposit_item_text)
        viewAllStationsText = view.findViewById(R.id.view_all_stations_text)
        homeButton = view.findViewById(R.id.home_button)
    }

    private fun setupClickListeners() {
        homeButton.setOnClickListener {
            // Navigate back to main screen - clear all fragments from back stack
            for (i in 0 until parentFragmentManager.backStackEntryCount) {
                parentFragmentManager.popBackStack()
            }
        }

        viewAllStationsText.setOnClickListener {
            // Navigate back to main screen - clear all fragments from back stack
            for (i in 0 until parentFragmentManager.backStackEntryCount) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun populateBookingDetails() {
        gameStation?.let { station ->
            psNameText.text = "${station.name} - ${station.id}"
            psSubtitleText.text = station.subName
        }

        // Set duration to 1 hour as per design
        durationText.text = "1 hr"

        selectedDate?.let { date ->
            val formatter = DateTimeFormatter.ofPattern("MMMM, dd yyyy")
            dateText.text = date.format(formatter)
        }

        selectedTime?.let { time ->
            val formatter = DateTimeFormatter.ofPattern("HH.mm")
            val endTime = time.plusHours(1)
            timeText.text = "${time.format(formatter)} - ${endTime.format(formatter)} WIB"
        }

        selectedDepositItem?.let { item ->
            depositItemText.text = item.displayName
        }
    }

    private fun loadGameStationImage() {
        gameStation?.let { station ->
            if (station.thumbnailUrl.isNotEmpty()) {
                // Use Glide to load the image with rounded corners
                Glide.with(this)
                    .load(station.thumbnailUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.ic_playstation)
                            .error(R.drawable.ic_playstation)
                            .transform(
                                CenterCrop(),
                                RoundedCorners(24) // 8dp radius converted to pixels (8 * 3 = 24)
                            )
                    )
                    .into(psImageView)
            } else {
                // Show placeholder if no image URL
                psImageView.setImageResource(R.drawable.ic_playstation)
            }
        }
    }
} 