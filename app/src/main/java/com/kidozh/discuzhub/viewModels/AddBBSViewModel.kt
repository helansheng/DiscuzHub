package com.kidozh.discuzhub.viewModels

import android.app.Application
import androidx.annotation.CheckResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kidozh.discuzhub.R
import com.kidozh.discuzhub.entities.Discuz
import com.kidozh.discuzhub.entities.ErrorMessage
import com.kidozh.discuzhub.results.AddCheckResult
import com.kidozh.discuzhub.services.DiscuzApiService
import com.kidozh.discuzhub.utilities.NetworkUtils
import com.kidozh.discuzhub.utilities.URLUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddBBSViewModel(application: Application) : AndroidViewModel(application) {
    @JvmField
    var currentURLLiveData: MutableLiveData<String> = MutableLiveData("")

    @JvmField
    var errorTextLiveData: MutableLiveData<String> = MutableLiveData("")
    var errorMessageMutableLiveData: MutableLiveData<ErrorMessage?> = MutableLiveData(null)

    @JvmField
    var isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    @JvmField
    var autoVerifyURLLiveData: MutableLiveData<Boolean> = MutableLiveData(true)

    @JvmField
    var verifiedBBS = MutableLiveData<Discuz?>(null)
    fun verifyURL() {
        isLoadingLiveData.postValue(true)
        val base_url = currentURLLiveData.value
        if(base_url == null){
            return
        }
        URLUtils.setBaseUrl(base_url)
        val client = NetworkUtils.getPreferredClient(getApplication())
        val retrofit = NetworkUtils.getRetrofitInstance(base_url,client)
        val service = retrofit.create(DiscuzApiService::class.java)
        val checkCall = service.checkResult
        checkCall.enqueue(object : Callback<AddCheckResult> {
            override fun onResponse(call: Call<AddCheckResult>, response: Response<AddCheckResult>) {
                if (response.isSuccessful && response.body() != null) {
                    val checkResult = response.body() as AddCheckResult
                    if(!checkResult.siteName.equals("")){
                        verifiedBBS.postValue(checkResult.toBBSInformation(base_url))
                    }
                    else{
                        errorMessageMutableLiveData.postValue(ErrorMessage(getApplication<Application>().getString(R.string.check_result_incorrect_key),
                                getApplication<Application>().getString(R.string.check_result_incorrect_description))
                        )
                    }

                }
                else{
                    errorMessageMutableLiveData.postValue(ErrorMessage(response.code().toString(),
                            getApplication<Application>().getString(R.string.discuz_network_unsuccessful, response.message())))
                }

                isLoadingLiveData.postValue(false)
            }

            override fun onFailure(call: Call<AddCheckResult>, t: Throwable) {
                errorMessageMutableLiveData.postValue(ErrorMessage(
                        getApplication<Application>().getString(R.string.discuz_network_failure_template),
                        if (t.localizedMessage == null) t.toString() else t.localizedMessage
                ))
                isLoadingLiveData.postValue(false)
            }

        })


    }

    companion object {
        private val TAG = AddBBSViewModel::class.java.simpleName
    }

}