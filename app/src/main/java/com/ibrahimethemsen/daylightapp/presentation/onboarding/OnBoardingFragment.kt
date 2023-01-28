package com.ibrahimethemsen.daylightapp.presentation.onboarding

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.ibrahimethemsen.daylightapp.R
import com.ibrahimethemsen.daylightapp.common.Constants.HOME_FRAGMENT
import com.ibrahimethemsen.daylightapp.common.isVisibility
import com.ibrahimethemsen.daylightapp.common.nullVisibility
import com.ibrahimethemsen.daylightapp.data.dto.city.City
import com.ibrahimethemsen.daylightapp.databinding.FragmentOnBoardingBinding
import com.ibrahimethemsen.daylightapp.presentation.onboarding.adapter.CityRecyclerViewAdapter
import com.ibrahimethemsen.daylightapp.utility.viewBindingInflater
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingFragment : Fragment(R.layout.fragment_on_boarding) {

    private val binding by viewBindingInflater(FragmentOnBoardingBinding::bind)
    private val viewModel by viewModels<OnBoardingViewModel>()
    private val cityAdapter = CityRecyclerViewAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getStartDestination()
        viewModel.getList()
        binding.onboardingSearchCityRv.adapter = cityAdapter
        binding.onboardingSearchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterCityList(newText).also {
                    cityAdapter.updateListCity(it)
                }
                return true
            }
        })
        observer()
        cityAdapter.onCityClickListener(::cityWriteDataStore)
    }

    private fun cityWriteDataStore(city: City) {
        if (!city.latitude.isNullOrEmpty() && !city.longitude.isNullOrEmpty() && !city.name.isNullOrEmpty()) {
            viewModel.writeDataStoreCity(city.latitude, city.longitude, city.name).also {
                val action = OnBoardingFragmentDirections.actionOnBoardingFragmentToHomeFragment()
                requireView().findNavController().navigate(action)
            }
        }
    }

    private fun observer() {
        viewModel.cityList.observe(viewLifecycleOwner) { onBoardingUi ->
            onBoardingUi.data?.let {
                cityAdapter.updateListCity(it)
            }
            binding.onboardingErrorTv nullVisibility onBoardingUi.error
            binding.onboardingProgress isVisibility onBoardingUi.loading
        }
        viewModel.navStartDestination.observe(viewLifecycleOwner) { destination ->
            if (destination == HOME_FRAGMENT) {
                val action = OnBoardingFragmentDirections.actionOnBoardingFragmentToHomeFragment()
                requireView().findNavController().navigate(action)
            }
        }

    }
}