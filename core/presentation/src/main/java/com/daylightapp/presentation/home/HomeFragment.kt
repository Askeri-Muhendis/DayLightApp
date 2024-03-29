package com.daylightapp.presentation.home


import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.daylightapp.domain.entity.city.LocationEntity
import com.daylightapp.presentation.R
import com.daylightapp.presentation.common.Constants.FEEDBACK_KEY
import com.daylightapp.presentation.common.loadImage
import com.daylightapp.presentation.common.observeIfNotNull
import com.daylightapp.presentation.databinding.FragmentHomeBinding
import com.daylightapp.presentation.home.adapter.FiveDayWeatherAdapter
import com.daylightapp.presentation.home.adapter.SliderAdapter
import com.daylightapp.presentation.home.model.FeedBackModel
import com.daylightapp.presentation.home.model.LanguageModel
import com.daylightapp.presentation.home.model.NewFeature
import com.daylightapp.presentation.home.model.SliderModel
import com.daylightapp.presentation.utility.SharedManager
import com.daylightapp.presentation.utility.viewBindingInflater
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBindingInflater(FragmentHomeBinding::bind)
    private val viewModel by viewModels<HomeViewModel>()
    private val fiveDayAdapter = FiveDayWeatherAdapter()
    private val sliderAdapter = SliderAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        observe()
        listener()
        viewModel.homeSliderRemoteConfig()
        viewModel.newFeatureRemoteConfig()
        viewModel.activeIsQuoteService()
        viewModel.languageRemoteConfig()
        viewModel.feedbackRemoteConfig()
    }

    private fun initAdapter(){
        binding.homeFiveDayWeatherRv.adapter = fiveDayAdapter
        binding.homeSliderRv.adapter = sliderAdapter
    }


    private fun listener(){
        binding.apply {
            homeToLoginBtn.setOnClickListener {toLoginFragment()}
            sliderAdapter.sliderClickListener(::openWithIntent)
        }
    }

    private fun openWithIntent(intentUrl: String){
        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(intentUrl)
        )
        startActivity(urlIntent)
    }
    private fun toLoginFragment(){
        val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun observe() {
        observeIfNotNull(viewModel.homeUiState,::setHomeUiState)
        observeIfNotNull(viewModel.quoteUiState,::setQuoteUiState)
        observeIfNotNull(viewModel.fiveDayWeather,::setFiveDayWeatherUiState)
        observeIfNotNull(viewModel.locationLatLon,::navigateHomeToFiveDay)
        observeIfNotNull(viewModel.homeSlider,::setSlider)
        observeIfNotNull(viewModel.newFeature,::newFeature)
        observeIfNotNull(viewModel.activeIsQuoteService,::setQuoteService)
        observeIfNotNull(viewModel.languageRemoteConfig,::languageDialog)
        observeIfNotNull(viewModel.feedBackBottomVisibility,::toFeedBackBottomSheet)
    }

    private fun toFeedBackBottomSheet(feedbackModel : FeedBackModel){
        val shared = SharedManager(requireContext())
        if (feedbackModel.feedbackVisibility && shared.getSharedPreference(FEEDBACK_KEY,"empty") != feedbackModel.feedbackShared){
            toBottomSheet(feedbackModel)
        }
    }

    private fun toBottomSheet(feedbackModel: FeedBackModel){
        val action = HomeFragmentDirections.actionHomeFragmentToFeedBackBottomFragment(feedbackModel.feedbackCollectionId,feedbackModel.feedbackShared,feedbackModel.feedbackTitle)
        findNavController().navigate(action)
    }

    private fun setQuoteService(active : Boolean){
        if (active){
            binding.homeQuoteGroup.visibility = View.VISIBLE
            viewModel.getQuote()
        }else{
            binding.homeQuoteGroup.visibility = View.GONE
        }
    }
    private fun languageDialog(languageModel: LanguageModel){
        if (languageModel.languageVisibility){
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle(languageModel.languageTitle)
                .setMessage(languageModel.languageDescription)
                .setPositiveButton("Tamam") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }
    }
    private fun newFeature(newFeature : NewFeature){
        if (newFeature.featureVisibility){
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle(newFeature.featureTitle)
                .setMessage(newFeature.featureMessage)
                .setPositiveButton("Keşfet") { dialog, _ ->

                    openWithIntent(newFeature.positiveUrl)
                    dialog.dismiss()
                }
                .setNegativeButton("Daha sonra") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }
    }
    private fun setSlider(listSlider : List<SliderModel>){
        sliderAdapter.updateRecyclerList(listSlider)
    }

    private fun setHomeUiState(currentWeather : CurrentWeatherUiState){
        binding.apply {
            homeSunriseTv.text = currentWeather.currentWeatherEntity?.sunrise
            homeSunsetTv.text = currentWeather.currentWeatherEntity?.sunset
            homeWindTv.text = currentWeather.currentWeatherEntity?.windSpeed
            homeCurrentCelciusTv.text = currentWeather.currentWeatherEntity?.currentCelcius
            homeCurrentDate.text = currentWeather.currentWeatherEntity?.currentDate
            currentWeather.currentWeatherEntity?.imageIconId?.let {
                homeCurrentIconIv.loadImage(it)
            }
            homeCurrentDescriptionTv.text = currentWeather.currentWeatherEntity?.description
        }
    }
    private fun setQuoteUiState(quote : QuoteUiState){
        binding.apply {
            homeQuoteTv.text = quote.quoteEntity?.quote
            homeQuoteAuthorTv.text = quote.quoteEntity?.author
        }
    }
    private fun setFiveDayWeatherUiState(fiveDayWeather : FiveDayWeatherUiState){
        fiveDayWeather.fiveDayWeather?.let {
            fiveDayAdapter.updateRecyclerList(it)
        }
    }

    private fun navigateHomeToFiveDay(entity : LocationEntity){
        binding.homeFiveDayWeatherBtn.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToFiveDayWeatherListFragment(
                entity.lat,
                entity.lon
            )
            it.findNavController().navigate(action)
        }
    }
    companion object{
        const val FIRST_SEASON = "first_season"
        const val SECOND_SEASON = "second_season"
    }
}