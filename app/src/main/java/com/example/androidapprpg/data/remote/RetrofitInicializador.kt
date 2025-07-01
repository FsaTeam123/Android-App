//package com.example.androidapprpg.data.remote

//import com.example.androidapprpg.data.remote.services.AuthService
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory

//class RetrofitInicializador {

// private val retrofit = Retrofit.Builder() //deixamos como private, pois não queremos acessar o retrofit diretamente e sim o nosso service
//       .baseUrl("http://15.228.149.190:8085") //Comunicação HTTP trafega dados no formato de texto sem criptografia, liberamos comunicação HTTP com android:usesCleartextTraffic="true"
//      .addConverterFactory(GsonConverterFactory.create()) //Conversor para leitura do JSON
//      .build()

//  //Precisamos acessar o nosso service como instancia e podemos usar a instancia do retrofit
//  val authService : AuthService = retrofit.create(AuthService::class.java) //acredito que falta inicializar esse serviço

//}