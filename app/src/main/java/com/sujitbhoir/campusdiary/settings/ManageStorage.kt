package com.sujitbhoir.campusdiary.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityManageStorageBinding
import java.io.File
import java.text.DecimalFormat


class ManageStorage : AppCompatActivity() {

    private lateinit var binding : ActivityManageStorageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //back
        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }
        fun dirSize(dir: File): Long {

            if (dir.exists()) {
                var result: Long = 0
                val fileList = dir.listFiles()
                for (i in fileList!!.indices) {
                    if (fileList[i].isDirectory) {
                        result += dirSize(fileList[i])
                    } else {
                        result += fileList[i].length()
                    }
                }
                return result
            }
            return 0
        }

        fun getStringSize(size: Long): String {
            if (size <= 0)
                return "0MB"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
        }

        fun filesize(f : File) : String
        {
           return getStringSize(dirSize(f))
        }

        fun deletefolder(dir : File)
        {
            if (dir.isDirectory) {
                val children: Array<String> = dir.list()
                for (i in children.indices) {
                    File(dir, children[i]).delete()
                }
            }        }



        fun load()
        {


            //create path
            File(this.filesDir.absolutePath+ File.separator + "PostsMedia").let {
                if (!  it.exists())
                {
                    it.mkdir()
                }
            }
            File(this.filesDir.absolutePath+ File.separator + "ProductsMedia").let {
                if (!  it.exists())
                {
                    it.mkdir()
                }
            }
            File(this.filesDir.absolutePath+ File.separator + "CommunityPictures").let {
                if (!  it.exists())
                {
                    it.mkdir()
                }
            }
            File(this.filesDir.absolutePath+ File.separator + "ProfilePictures").let {
                if (!  it.exists())
                {
                    it.mkdir()
                }
            }

            val posm =  File(this.filesDir.absolutePath+ File.separator + "PostsMedia")
            val prom =  File(this.filesDir.absolutePath+ File.separator + "ProductsMedia")
            val comp =  File(this.filesDir.absolutePath+ File.separator + "CommunityPictures")
            val prop =  File(this.filesDir.absolutePath+ File.separator + "ProfilePictures")



            var d = "Posts Media : ${filesize(posm)}"
            d  += "\nProducts Media : ${filesize(prom)}"
            d  += "\nCommunity Media : ${filesize(comp)}"
            d  += "\nUsers Media : ${filesize(prop)}"
            d  += "\nTotal size : ${getStringSize(dirSize(posm) + dirSize(prom) +dirSize(comp) +dirSize(prop))}"

            binding.textView12.text = d.toString()

            binding.button.setOnClickListener {
                deletefolder(posm)
                deletefolder(prom)
                deletefolder(comp)
                deletefolder(prop)
                load()
            }

        }
        load()







    }
}