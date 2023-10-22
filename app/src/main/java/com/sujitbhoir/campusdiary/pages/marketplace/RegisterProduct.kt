package com.sujitbhoir.campusdiary.pages.marketplace

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.ui.FilePicker
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.sujitbhoir.campusdiary.R
import com.sujitbhoir.campusdiary.databinding.ActivityRegisterProductBinding
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.FirebaseFirestoreHandler
import com.sujitbhoir.campusdiary.datahandlers.MarketplaceManager
import com.sujitbhoir.campusdiary.helperclass.DataHandler
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class RegisterProduct : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterProductBinding
    private lateinit var filePaths : ArrayList<String>
    private lateinit var data : UserData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data = DataHandler.getUserData(baseContext)!!

        binding.toolbar1.setNavigationIcon(R.drawable.arrow_back_24px)
        binding.toolbar1.setNavigationOnClickListener {
            finish()
        }

        //tags
        val taglist = resources.getStringArray(R.array.MarketplaceCategory)
        AddChipsInView(taglist, binding.chipGroupTags)

        //condition
        val commAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.ProductCondition))
        binding.dpCondition.setAdapter(commAdapter)

        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    // Use the uri to load the image
                    filePaths = it.data?.getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST)!!
                    binding.placeholderimage.visibility = View.GONE

                    val list = mutableListOf<CarouselItem>()
                    list.reverse()

                    for (file in filePaths)
                    {
                        list.add(
                            CarouselItem(
                                imageUrl = file
                            )
                        )
                    }

                    binding.carousel.registerLifecycle(lifecycle)
                    binding.carousel.setData(list)
                }
            }

        binding.btnAdd.setOnClickListener{

            launcher.launch(
                FilePicker.Builder(this)
                    .pickMediaBuild(
                        PickMediaConfig(
                        mPickMediaType = PickMediaType.ImageOnly,
                        allowMultiple = true,
                    )
                    )
            )

        }

        binding.btnDiscard.setOnClickListener{
            finish()
        }

        binding.btnUpdate.setOnClickListener{
            binding.btnUpdate.isClickable = false
            register()
        }

    }

    fun register()
    {
        val checkedChipIds = binding.chipGroupTags.checkedChipIds

        val tags = ArrayList<String>()

        for (chip in checkedChipIds)
        {
            tags.add(findViewById<Chip>(chip).text.toString())
        }


        if (validate())
        {

            MarketplaceManager(this).registerProduct(
                name = binding.tvName.text.toString(),
                campus = data.campus,
                details = binding.tvDetails.text.toString(),
                condition = binding.dpCondition.text.toString(),
                contactWhatsapp = binding.tvContact.text.toString(),
                images = filePaths,
                tags = tags.toList(),
                price = binding.tvPrize.text.toString(),
                sellerId = data.id,
                sellerName = data.name,
                sellerPic = data.profilePicId
            )
            {
                    Toast.makeText(this, "Product Added Successfully", Toast.LENGTH_LONG).show()
                    finish()

            }
        }
        else
        {
            binding.btnUpdate.isClickable = true
        }
    }

    fun validate() : Boolean
    {
        binding.tvName.error = null
        binding.tvContact.error = null
        binding.tvPrize.error = null
        binding.tvDetails.error = null
        binding.dpCondition.error = null

    if (binding.tvName.text!!.isBlank())
    {
        binding.tvName.error = "please enter product name"
        return false
    }
        if (binding.tvContact.text!!.isBlank())
        {
            binding.tvContact.error = "please enter your contact"
            return false
        }
        if (binding.tvPrize.text!!.isBlank())
        {
            binding.tvPrize.error = "please enter product prize"
            return false
        }
        if (binding.tvDetails.text!!.isBlank())
        {
            binding.tvDetails.error = "please enter product details"
            return false
        }
        if (binding.tvName.text!!.isBlank())
        {
            binding.tvName.error = "please select product condition"
            return false
        }

        if (binding.chipGroupTags.checkedChipIds.isEmpty())
        {
            Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show()
            return false
        }
        if (filePaths.isEmpty())
        {
            Toast.makeText(this, "Please select at least one product image", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun AddChipsInView(chipslist : Array<String>, view : ChipGroup, style : Int = com.google.android.material.R.style.Widget_Material3_Chip_Filter)
    {
        for (chipname in chipslist)
        {
            val chip = Chip(this )
            chip.setChipDrawable(ChipDrawable.createFromAttributes(this, null, 0, style))
            chip.text = chipname
            view.addView(chip)
        }
    }
}