package com.example.beacon.utils

object ProfileInformation {
    private var name: String = "Anon"
    private var email: String = ""


    fun setName(newName: String){
        name = newName;
    }
    fun getName(): String{
        return name
    }

    fun setEmail(newEmail: String){
        email = newEmail;
    }
    fun getEmail(): String{
        return email
    }

}