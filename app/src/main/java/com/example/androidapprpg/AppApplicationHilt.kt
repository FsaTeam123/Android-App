package com.example.androidapprpg

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp

class AppApplicationHilt : Application() {

    //Todos os apps que usam o Hilt precisam conter uma classe Application anotada com @HiltAndroidApp.
    //O @HiltAndroidApp aciona a geração de código do Hilt, incluindo uma classe base para seu aplicativo que serve como contêiner de dependências no nível do app.
    //Esse componente Hilt gerado é anexado ao ciclo de vida do objeto Application e fornece dependências a ele.
    // Além disso, ele é o componente pai do app, o que significa que outros componentes podem acessar as dependências fornecidas.

}