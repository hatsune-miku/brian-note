package com.eggtartc.briannote.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PreferenceBind(val key: String)
