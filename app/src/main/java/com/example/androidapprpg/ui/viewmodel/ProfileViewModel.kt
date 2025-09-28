package com.example.androidapprpg.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.ProfileDataModel.ProfileDataModelRequest
import com.example.androidapprpg.data.model.ProfileDataModel.ProfileDataModelResponse
import com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel.SexoDataModel
import com.example.androidapprpg.data.repository.ProfileRepository
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    // ---- LiveData ----
    private val _getProfile = MutableLiveData<Result<ProfileDataModelResponse>>()
    val profileResult: LiveData<Result<ProfileDataModelResponse>> = _getProfile

    private val _updateProfile = MutableLiveData<Result<ProfileDataModelResponse>>()
    val updateResult: LiveData<Result<ProfileDataModelResponse>> = _updateProfile

    private val _getSexos = MutableLiveData<Result<List<SexoDataModel>>>()
    val sexosResult: LiveData<Result<List<SexoDataModel>>> = _getSexos

    private val _photoBytes = MutableLiveData<Result<ByteArray>>()
    val photoBytesResult: LiveData<Result<ByteArray>> = _photoBytes

    private val _uploadPhoto = MutableLiveData<Result<Unit>>()
    val uploadPhotoResult: LiveData<Result<Unit>> = _uploadPhoto

    // ---- Cache em memória (sobrevive a rotação enquanto o processo vive) ----
    private var cachedProfile: ProfileDataModelResponse? = null
    private var cachedSexos: List<SexoDataModel>? = null
    private var cachedPhoto: ByteArray? = null


    fun getProfile(id: Int, forceRefresh: Boolean = false) = viewModelScope.launch {
        if (!forceRefresh) cachedProfile?.let {
            _getProfile.value = Result.Success(it); return@launch
        }
        _getProfile.value = Result.Loading
        try {
            val resp = repository.getProfile(id)
            val body = resp.body()
            if (resp.isSuccessful && body != null) {
                cachedProfile = body
                _getProfile.value = Result.Success(body)
            } else {
                _getProfile.value = Result.Error("Erro ${resp.code()}: ${resp.message()}")
            }
        } catch (e: Exception) {
            _getProfile.value = Result.Error("Falha na rede: ${e.message}")
        }
    }


    fun loadSexos(forceRefresh: Boolean = false) = viewModelScope.launch {
        if (!forceRefresh) cachedSexos?.let {
            _getSexos.value = Result.Success(it); return@launch
        }
        _getSexos.value = Result.Loading
        try {
            val resp = repository.getSexos()
            if (resp.isSuccessful) {
                val list = resp.body().orEmpty()
                cachedSexos = list
                _getSexos.value = Result.Success(list)
            } else {
                _getSexos.value = Result.Error("Erro ${resp.code()}: ${resp.message()}")
            }
        } catch (e: Exception) {
            _getSexos.value = Result.Error("Falha na rede: ${e.message}")
        }
    }


    fun getProfilePhoto(id: Int, forceRefresh: Boolean = false) = viewModelScope.launch {
        if (!forceRefresh) cachedPhoto?.let {
            _photoBytes.value = Result.Success(it); return@launch
        }
        _photoBytes.value = Result.Loading
        try {
            val resp = repository.getProfilePhoto(id)
            if (resp.isSuccessful && resp.body() != null) {
                val bytes = withContext(Dispatchers.IO) { resp.body()!!.bytes() }
                cachedPhoto = bytes
                _photoBytes.value = Result.Success(bytes)
            } else {
                _photoBytes.value = Result.Error("Erro ${resp.code()}: ${resp.message()}")
            }
        } catch (e: Exception) {
            _photoBytes.value = Result.Error("Falha na rede: ${e.message}")
        }
    }


    fun updateProfile(id: Int, bodyReq: ProfileDataModelRequest) = viewModelScope.launch {
        _updateProfile.value = Result.Loading
        try {
            val resp = repository.updateProfile(id, bodyReq)
            val body = resp.body()
            if (resp.isSuccessful && body != null) {
                cachedProfile = body
                _updateProfile.value = Result.Success(body)
            } else {
                _updateProfile.value = Result.Error("Erro ${resp.code()}: ${resp.message()}")
            }
        } catch (e: Exception) {
            _updateProfile.value = Result.Error("Falha na rede: ${e.message}")
        }
    }

    /** Upload da foto com atualização otimista do cache/preview */
    fun uploadPhoto(id: Int, uri: Uri, cr: ContentResolver) = viewModelScope.launch {
        _uploadPhoto.value = Result.Loading
        try {
            // Atualização otimista no UI
            val localBytes = withContext(Dispatchers.IO) {
                cr.openInputStream(uri)!!.use { it.readBytes() }
            }
            cachedPhoto = localBytes
            _photoBytes.value = Result.Success(localBytes)

            // Envia ao servidor
            val part = withContext(Dispatchers.IO) { uri.toMultipartPart(cr, "file") }
            val resp = repository.addProfilePhoto(id, part)
            _uploadPhoto.value =
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("Upload falhou: ${resp.code()} ${resp.message()}")
        } catch (e: Exception) {
            _uploadPhoto.value = Result.Error("Falha na rede: ${e.message}")
        }
    }

    /** Invalida tudo (ex.: logout) */
    fun invalidateCache() {
        cachedProfile = null
        cachedSexos = null
        cachedPhoto = null
    }

    // ---- helper Uri -> Multipart ----
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
}



