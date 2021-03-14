package com.coures.renaud.sportnath

class InfoArduino (infosBrutes : String){

    // DEB;35.35;0.03;FIN
    val vitesse : Float
    val distance : Float

    // Constructeur
        init {
            vitesse = infosBrutes.split(";")[1].toFloat()
            distance = infosBrutes.split(";")[2].toFloat()

        }


}

