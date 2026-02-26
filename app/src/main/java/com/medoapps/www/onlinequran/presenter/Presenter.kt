package com.medoapps.www.onlinequran.presenter

interface Presenter<T : Any> {
  fun bind(what: T)
  fun unbind(what: T)
}
