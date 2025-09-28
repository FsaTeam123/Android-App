package com.example.androidapprpg.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.androidapprpg.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _photoResult = MutableLiveData<Result<ByteArray>>()
    val photoResult: LiveData<Result<ByteArray>> = _photoResult

    private var cachedPhoto: ByteArray? = null

    fun getProfilePhoto(id: Int, forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            cachedPhoto?.let { _photoResult.value = Result.Success(it); return }
        }
        _photoResult.postValue(Result.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = repository.getProfilePhoto(id)
                if (resp.isSuccessful) {
                    val bytes = resp.body()?.bytes()
                    withContext(Dispatchers.Main) {
                        if (bytes != null && bytes.isNotEmpty()) {
                            cachedPhoto = bytes
                            _photoResult.value = Result.Success(bytes)
                        } else {
                            _photoResult.value = Result.Error("Foto não encontrada.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _photoResult.value = Result.Error("Erro ${resp.code()}: ${resp.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _photoResult.value = Result.Error("Falha na rede: ${e.message}")
                }
            }
        }
    }

    /** Use quando souber a foto nova (ex.: baixou os bytes) */
    fun updatePhotoCache(bytes: ByteArray?) {
        if (bytes == null || bytes.isEmpty()) return
        cachedPhoto = bytes
        _photoResult.value = Result.Success(bytes)
    }

    /** Use quando só quiser invalidar e forçar novo GET */
    fun invalidatePhotoCache() { cachedPhoto = null }
}


    private fun Uri.toMultipartPart(
        cr: ContentResolver,
        fieldName: String = "file"
    ): MultipartBody.Part {
        val mime = cr.getType(this) ?: "image/jpeg"
        val ext = mime.substringAfter('/', "jpg")
        val name = "foto_${System.currentTimeMillis()}.$ext"
        val bytes = cr.openInputStream(this)!!.use { it.readBytes() }
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, name, body)
    }


