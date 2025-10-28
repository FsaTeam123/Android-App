package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class ClasseDataModel(
    @SerializedName("idClasse")          val idClasse: Int? = null,
    @SerializedName("nome")              val nome: String? = null,
    @SerializedName("descricao")         val descricao: String? = null,
    @SerializedName("pvInit")            val pvInit: Int? = null,
    @SerializedName("pvNivel")           val pvNivel: Int? = null,
    @SerializedName("atributoPV")        val atributoPV: AtributosDataModel? = null,
    @SerializedName("pmInit")            val pmInit: Int? = null,
    @SerializedName("pmNivel")           val pmNivel: Int? = null,
    @SerializedName("ativo")             val ativo: Int? = null,
    @SerializedName("imagemBase64")      val imagemBase64: String? = null,
    @SerializedName("imagemContentType") val imagemContentType: String? = null,
    @SerializedName("imagemFilename")    val imagemFilename: String? = null,
    @SerializedName("proeficiencias")    val proeficiencias: List<ProeficienciasDataModel> = emptyList(),
    @SerializedName("pericias")          val pericias: List<PericiasDataModel> = emptyList()
)
