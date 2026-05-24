package com.yodhan18.ydn18optimizer.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.yodhan18.ydn18optimizer.R

class HomeFragment : Fragment() {

    data class VideoItem(val label: String, val thumbUrl: String, val videoUrl: String)

    private val videos = listOf(
        VideoItem(
            "Highlight 1",
            "https://img.youtube.com/vi/g_4264sNTok/0.jpg",
            "https://www.youtube.com/watch?v=g_4264sNTok"
        ),
        VideoItem(
            "Highlight 2",
            "https://img.youtube.com/vi/Rr5FFM1YqFk/0.jpg",
            "https://www.youtube.com/watch?v=Rr5FFM1YqFk"
        ),
        VideoItem(
            "Highlight 3",
            "https://img.youtube.com/vi/sW83XF_EgBo/0.jpg",
            "https://www.youtube.com/watch?v=sW83XF_EgBo"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Social buttons
        view.findViewById<android.widget.Button>(R.id.btnYoutube).setOnClickListener {
            openUrl("https://youtube.com/@yodhan18?si=usffmrSpD0jqEic2")
        }
        view.findViewById<android.widget.Button>(R.id.btnInstagram).setOnClickListener {
            openUrl("https://www.instagram.com/yodhan_18?igsh=MXI5bDkzdG5pdDU3MQ==")
        }
        view.findViewById<android.widget.Button>(R.id.btnDiscord).setOnClickListener {
            openUrl("https://discord.gg/2bTF2uVUDK")
        }
        view.findViewById<android.widget.Button>(R.id.btnTelegram).setOnClickListener {
            openUrl("https://t.me/+Ul6MswW09js0NmY1")
        }

        // HUD code copy
        val tvHudCode = view.findViewById<TextView>(R.id.tvHudCode)
        tvHudCode.setOnClickListener {
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("HUD Code", "#FFHUDT6O3jmR/HslPo7eO")
            cm.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "HUD Code copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        // Video thumbnails
        setupVideo(view.findViewById(R.id.video1), videos[0])
        setupVideo(view.findViewById(R.id.video2), videos[1])
        setupVideo(view.findViewById(R.id.video3), videos[2])
    }

    private fun setupVideo(videoView: View, item: VideoItem) {
        val ivThumb = videoView.findViewById<ImageView>(R.id.ivThumbnail)
        val tvLabel = videoView.findViewById<TextView>(R.id.tvVideoLabel)
        tvLabel.text = item.label

        Glide.with(this)
            .load(item.thumbUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(ivThumb)

        videoView.setOnClickListener { openUrl(item.videoUrl) }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
